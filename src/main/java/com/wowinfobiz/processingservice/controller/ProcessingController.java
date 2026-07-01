package com.wowinfobiz.processingservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wowinfobiz.analyticsservice.services.observer.AnalyticsEventSubject;
import com.wowinfobiz.authenticationservice.security.LiveEndpointAccessService;
import com.wowinfobiz.devicemanagmentservice.servicesImp.SensorEndpointSupport;
import com.wowinfobiz.processingservice.dto.ProcessDataResponse;
import com.wowinfobiz.processingservice.dto.SensorRawDataRequest;
import com.wowinfobiz.processingservice.services.ProcessedReadingStoreService;
import com.wowinfobiz.processingservice.services.sensors.Accelerometer;
import com.wowinfobiz.processingservice.services.sensors.CrackMeter;
import com.wowinfobiz.processingservice.services.sensors.InclinoMeter;
import com.wowinfobiz.processingservice.services.sensors.LoadCellSensor;
import com.wowinfobiz.processingservice.services.sensors.PiezoMeter;
import com.wowinfobiz.processingservice.services.sensors.StrainGuage;
import com.wowinfobiz.processingservice.services.sensors.TempratorSensor;
import com.wowinfobiz.processingservice.services.sensors.TiltMeter;
import com.wowinfobiz.processingservice.services.sensors.VibrationSensor;
import com.wowinfobiz.thresholdalertservice.services.SensorDataProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/v1/processing")
public class ProcessingController {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessingController.class);
    private static final int LIVE_SNAPSHOT_LIMIT = 25;

    private final ObjectMapper objectMapper;
    private final Accelerometer accelerometer;
    private final CrackMeter crackMeter;
    private final InclinoMeter inclinoMeter;
    private final LoadCellSensor loadCellSensor;
    private final PiezoMeter piezoMeter;
    private final StrainGuage strainGuage;
    private final TempratorSensor tempratorSensor;
    private final TiltMeter tiltMeter;
    private final VibrationSensor vibrationSensor;
    private final ProcessedReadingStoreService processedReadingStoreService;
    private final AnalyticsEventSubject analyticsEventSubject;
    private final SensorEndpointSupport sensorEndpointSupport;
    private final LiveEndpointAccessService liveEndpointAccessService;
    private final SensorDataProcessor thresholdProcessor;
    private final List<LiveReadingSubscriber> liveSubscribers = new CopyOnWriteArrayList<>();
    private final AtomicLong thresholdPublishAttemptCount = new AtomicLong(0);
    private final AtomicLong thresholdPublishSuccessCount = new AtomicLong(0);
    private final AtomicLong thresholdPublishFailureCount = new AtomicLong(0);
    private volatile Instant lastThresholdPublishAt;
    private volatile Instant lastThresholdPublishSuccessAt;
    private volatile Instant lastThresholdPublishFailureAt;
    private volatile String lastThresholdPublishError;
    private volatile Map<String, Object> lastThresholdPayloadSummary = Map.of();

    public ProcessingController(Accelerometer accelerometer,
                                CrackMeter crackMeter,
                                InclinoMeter inclinoMeter,
                                LoadCellSensor loadCellSensor,
                                PiezoMeter piezoMeter,
                                StrainGuage strainGuage,
                                TempratorSensor tempratorSensor,
                                TiltMeter tiltMeter,
                                VibrationSensor vibrationSensor,
                                ProcessedReadingStoreService processedReadingStoreService,
                                AnalyticsEventSubject analyticsEventSubject,
                                SensorEndpointSupport sensorEndpointSupport,
                                LiveEndpointAccessService liveEndpointAccessService,
                                SensorDataProcessor thresholdProcessor) {
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
        this.accelerometer = accelerometer;
        this.crackMeter = crackMeter;
        this.inclinoMeter = inclinoMeter;
        this.loadCellSensor = loadCellSensor;
        this.piezoMeter = piezoMeter;
        this.strainGuage = strainGuage;
        this.tempratorSensor = tempratorSensor;
        this.tiltMeter = tiltMeter;
        this.vibrationSensor = vibrationSensor;
        this.processedReadingStoreService = processedReadingStoreService;
        this.analyticsEventSubject = analyticsEventSubject;
        this.sensorEndpointSupport = sensorEndpointSupport;
        this.liveEndpointAccessService = liveEndpointAccessService;
        this.thresholdProcessor = thresholdProcessor;
    }

    @PostMapping("/process")
    public ResponseEntity<?> processSensor(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(processPayload(payload));
    }

    public ProcessDataResponse<?> processPayload(Map<String, Object> payload) {
        SensorRawDataRequest<?> request = toRequest(payload);
        ProcessDataResponse<?> response = processByDataType(request);
        persistAndPublishParallel(request, response, copyPayload(payload));
        return response;
    }

    @GetMapping({"/readings"})
    public ResponseEntity<?> getAllProcessedReadings(@RequestParam(name = "sensorId", required = false) UUID sensorId,
                                                     @RequestParam(name = "from", required = false) Instant from,
                                                     @RequestParam(name = "to", required = false) Instant to,
                                                     @RequestParam(name = "limit", required = false) Integer limit) {
        if (from != null && to != null && from.isAfter(to)) {
            return ResponseEntity.badRequest().body(new ProcessDataResponse<>("Invalid range: 'from' must be before 'to'", false, Map.of()));
        }

        List<Map<String, Object>> records = processedReadingStoreService.findReadings(sensorId, from, to);
        int safeLimit = limit == null ? 0 : Math.max(limit, 0);
        if (safeLimit > 0 && records.size() > safeLimit) {
            records = records.subList(0, safeLimit);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("count", records.size());
        body.put("sensorId", sensorId);
        body.put("from", from);
        body.put("to", to);
        body.put("limit", safeLimit > 0 ? safeLimit : null);
        body.put("records", records);

        return ResponseEntity.ok(new ProcessDataResponse<>("Processed readings fetched successfully", true, body));
    }

    @GetMapping(value = "/readings/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamLiveIngestionReadings(@RequestParam String userId) {
        liveEndpointAccessService.requireUser(userId);
        return subscribeLiveReadings(null);
    }

    @GetMapping(value = "/readings/live/{endpointKey}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamLiveSensorReadings(@PathVariable String endpointKey,
                                               @RequestParam String userId) {
        SensorEndpointSupport.ResolvedSensorEndpoint resolved = sensorEndpointSupport.resolveByEndpointKey(endpointKey)
                .orElseThrow(() -> new IllegalArgumentException("Sensor endpoint not found: " + endpointKey));
        liveEndpointAccessService.requireUserForSensor(
                userId,
                resolved.sensorId(),
                resolved.device().getSiteId(),
                resolved.device().getZoneId()
        );
        return subscribeLiveReadings(resolved.sensorId());
    }

    @GetMapping(value = "/readings/live/{endpointKey}/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamLiveSensorReadingsWithUserPath(@PathVariable String endpointKey,
                                                           @PathVariable String userId) {
        SensorEndpointSupport.ResolvedSensorEndpoint resolved = sensorEndpointSupport.resolveByEndpointKey(endpointKey)
                .orElseThrow(() -> new IllegalArgumentException("Sensor endpoint not found: " + endpointKey));
        liveEndpointAccessService.requireUserForSensor(
                userId,
                resolved.sensorId(),
                resolved.device().getSiteId(),
                resolved.device().getZoneId()
        );
        return subscribeLiveReadings(resolved.sensorId());
    }

    private SseEmitter subscribeLiveReadings(String sensorIdFilter) {
        SseEmitter emitter = new SseEmitter(0L);
        LiveReadingSubscriber subscriber = new LiveReadingSubscriber(emitter, normalizeSensorId(sensorIdFilter));
        liveSubscribers.add(subscriber);

        emitter.onCompletion(() -> liveSubscribers.remove(subscriber));
        emitter.onTimeout(() -> liveSubscribers.remove(subscriber));
        emitter.onError(ex -> liveSubscribers.remove(subscriber));

        try {
            Map<String, Object> connectedEvent = Map.of("type", "connected", "message", "Live stream connected", "data", emitter.toString());
            emitter.send(SseEmitter.event().name("connected").data(connectedEvent));

            List<Map<String, Object>> existing = processedReadingStoreService.findReadings(null, null, null).stream()
                    .filter(record -> matchesSensor(subscriber.sensorIdFilter(), valueAsString(record.get("sensorId"))))
                    .limit(LIVE_SNAPSHOT_LIMIT)
                    .toList();
            Map<String, Object> snapshotEvent = new LinkedHashMap<>();
            snapshotEvent.put("type", "snapshot");
            snapshotEvent.put("count", existing.size());
            snapshotEvent.put("records", existing);
            emitter.send(SseEmitter.event().name("snapshot").data(snapshotEvent));
        } catch (IOException ex) {
            liveSubscribers.remove(subscriber);
            emitter.completeWithError(ex);
        }

        return emitter;
    }

    @GetMapping("/readings/{readingId}")
    public ResponseEntity<?> getProcessedReadingById(@PathVariable(name = "readingId") UUID readingId) {
        Optional<Map<String, Object>> record = processedReadingStoreService.findReadingById(readingId);
        if (record.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(new ProcessDataResponse<>("Reading not found for id: " + readingId, false, Map.of("readingId", readingId)));
        }
        return ResponseEntity.ok(new ProcessDataResponse<>("Processed reading fetched successfully", true, record.get()));
    }

    @GetMapping("/runtime/status")
    public ResponseEntity<?> runtimeStatus() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mode", "direct-websocket-sse");
        body.put("liveSubscribers", liveSubscribers.size());
        body.put("thresholdPublishAttemptCount", thresholdPublishAttemptCount.get());
        body.put("thresholdPublishSuccessCount", thresholdPublishSuccessCount.get());
        body.put("thresholdPublishFailureCount", thresholdPublishFailureCount.get());
        body.put("lastThresholdPublishAt", lastThresholdPublishAt);
        body.put("lastThresholdPublishSuccessAt", lastThresholdPublishSuccessAt);
        body.put("lastThresholdPublishFailureAt", lastThresholdPublishFailureAt);
        body.put("lastThresholdPublishError", lastThresholdPublishError);
        body.put("lastThresholdPayloadSummary", lastThresholdPayloadSummary);
        return ResponseEntity.ok(new ProcessDataResponse<>("Processing runtime status fetched", true, body));
    }

    @GetMapping("/runtime/threshold-status")
    public ResponseEntity<?> thresholdPublishStatus() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mode", "direct-websocket-sse");
        body.put("publishAttemptCount", thresholdPublishAttemptCount.get());
        body.put("publishSuccessCount", thresholdPublishSuccessCount.get());
        body.put("publishFailureCount", thresholdPublishFailureCount.get());
        body.put("lastThresholdPublishAt", lastThresholdPublishAt);
        body.put("lastThresholdPublishSuccessAt", lastThresholdPublishSuccessAt);
        body.put("lastThresholdPublishFailureAt", lastThresholdPublishFailureAt);
        body.put("lastThresholdPublishError", lastThresholdPublishError);
        body.put("lastThresholdPayloadSummary", lastThresholdPayloadSummary);
        return ResponseEntity.ok(new ProcessDataResponse<>("Threshold publish status fetched", true, body));
    }

    @PostMapping("/recalculate/{readingId}")
    public ResponseEntity<?> recalculateByReadingId(@PathVariable(name = "readingId") UUID readingId) {
        Optional<Map<String, Object>> rawPayloadOptional = processedReadingStoreService.findRawPayloadByReadingId(readingId);
        if (rawPayloadOptional.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(new ProcessDataResponse<>("Reading not found for id: " + readingId, false, Map.of("readingId", readingId)));
        }

        Map<String, Object> rawPayload = rawPayloadOptional.get();
        SensorRawDataRequest<?> request = toRequest(rawPayload);
        request.setReadingId(readingId);

        ProcessDataResponse<?> response = processByDataType(request);
        persistAndPublishParallel(request, response, copyPayload(rawPayload));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/recalculate")
    public ResponseEntity<?> recalculateBatch(@RequestParam(name = "sensorId", required = false) UUID sensorId,
                                              @RequestParam(name = "from", required = false) Instant from,
                                              @RequestParam(name = "to", required = false) Instant to) {
        if (from != null && to != null && from.isAfter(to)) {
            return ResponseEntity.badRequest().body(new ProcessDataResponse<>("Invalid range: 'from' must be before 'to'", false, Map.of()));
        }

        List<Map<String, Object>> rawPayloads = processedReadingStoreService.findRawPayloads(sensorId, from, to);
        int successCount = 0;
        List<Object> failedReadingIds = new ArrayList<>();

        for (Map<String, Object> rawPayload : rawPayloads) {
            try {
                SensorRawDataRequest<?> request = toRequest(rawPayload);
                ProcessDataResponse<?> response = processByDataType(request);
                persistAndPublishParallel(request, response, copyPayload(rawPayload));
                successCount++;
            } catch (Exception ex) {
                Object failedId = firstPresent(rawPayload, "readingId", "id");
                failedReadingIds.add(failedId == null ? "unknown" : failedId);
                LOG.error("Failed to recalculate reading {}", failedId, ex);
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sensorId", sensorId);
        body.put("from", from);
        body.put("to", to);
        body.put("total", rawPayloads.size());
        body.put("successCount", successCount);
        body.put("failedCount", rawPayloads.size() - successCount);
        body.put("failedReadingIds", failedReadingIds);

        return ResponseEntity.ok(new ProcessDataResponse<>("Batch recalculation completed", true, body));
    }

    private void persistAndPublishParallel(SensorRawDataRequest<?> request,
                                           ProcessDataResponse<?> response,
                                           Map<String, Object> rawPayload) {
        CompletableFuture<Void> saveFuture = CompletableFuture.runAsync(() -> {
            processedReadingStoreService.save(request, response, rawPayload);
            LOG.debug("Stored processed reading {} in date-wise CSV", request.getReadingId());
        });

        CompletableFuture<Void> publishFuture = CompletableFuture.runAsync(() -> publishToThreshold(request, response));

        Throwable saveError = waitForFuture(saveFuture);
        Throwable publishError = waitForFuture(publishFuture);

        if (publishError != null) {
            LOG.error("Threshold publish failed for reading {}", request.getReadingId(), publishError);
        }
        if (saveError != null) {
            throw new IllegalStateException("Failed to store processed reading " + request.getReadingId(), saveError);
        }

        broadcastLiveUpdate(request, response, rawPayload);
        if (publishError != null) {
            publishAnalyticsLiveEvent(request, response);
        }
    }

    private Throwable waitForFuture(CompletableFuture<Void> future) {
        try {
            future.join();
            return null;
        } catch (CompletionException ex) {
            return ex.getCause() == null ? ex : ex.getCause();
        } catch (Exception ex) {
            return ex;
        }
    }

    private void broadcastLiveUpdate(SensorRawDataRequest<?> request,
                                     ProcessDataResponse<?> response,
                                     Map<String, Object> rawPayload) {
        if (liveSubscribers.isEmpty()) {
            return;
        }

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("readingId", request.getReadingId());
        event.put("sensorId", request.getSensorId());
        event.put("dataType", request.getDataType());
        event.put("timestamp", request.getTimestamp());
        event.put("rawPayload", rawPayload);
        event.put("processedPayload", response.getBody());
        event.put("processedStatus", response.isStatus());

        List<LiveReadingSubscriber> deadSubscribers = new ArrayList<>();
        for (LiveReadingSubscriber subscriber : liveSubscribers) {
            if (!matchesSensor(subscriber.sensorIdFilter(), request.getSensorId() == null ? null : request.getSensorId().toString())) {
                continue;
            }
            try {
                subscriber.emitter().send(SseEmitter.event().name("reading").data(event));
            } catch (IOException ex) {
                deadSubscribers.add(subscriber);
            }
        }
        liveSubscribers.removeAll(deadSubscribers);
    }

    private ProcessDataResponse<?> processByDataType(SensorRawDataRequest<?> request) {
        String dataType = normalizeDataType(request.getDataType());
        System.out.println("Data Type Received: "+dataType);
        return switch (dataType) {
            case "accelerometer", "accelometer" -> processAccelerometerWithDerivedMetrics(request);
            case "crackmeter" -> crackMeter.processSensorData(request);
            case "inclinometer", "inclino", "inclinometer_sensor", "inclinometer-sensor" -> inclinoMeter.processSensorData(request);
            case "loadcell", "loadcellsensor" -> loadCellSensor.processSensorData(request);
            case "piezometer", "piezo" -> piezoMeter.processSensorData(request);
            case "strainguage", "straingauge", "strain" -> strainGuage.processSensorData(request);
            case "temprator", "temperature", "tempratorsensor" -> tempratorSensor.processSensorData(request);
            case "tiltmeter", "tiltmoter", "tilt" -> tiltMeter.processSensorData(request);
            case "vibration", "vibrationsensor" -> vibrationSensor.processSensorData(request);
            default -> new ProcessDataResponse<>("Unsupported dataType: " + request.getDataType(), false, Map.of(
                    "sensorId", request.getSensorId(),
                    "readingId", request.getReadingId(),
                    "dataType", request.getDataType()
            ));
        };
    }

    private ProcessDataResponse<?> processAccelerometerWithDerivedMetrics(SensorRawDataRequest<?> request) {
        ProcessDataResponse<?> accelerometerResponse = accelerometer.processSensorData(request);
        ProcessDataResponse<?> tiltResponse = tiltMeter.processSensorData(request);
        ProcessDataResponse<?> vibrationResponse = vibrationSensor.processSensorData(request);
        ProcessDataResponse<?> inclinoResponse = inclinoMeter.processSensorData(request);

        Map<String, Object> merged = new LinkedHashMap<>();
        merged.putAll(safeBody(accelerometerResponse));
        merged.put("tilt", safeBody(tiltResponse));
        merged.put("vibration", safeBody(vibrationResponse));
        merged.put("inclinometer", safeBody(inclinoResponse));

        boolean success = accelerometerResponse.isStatus()
                && tiltResponse.isStatus()
                && vibrationResponse.isStatus()
                && inclinoResponse.isStatus();

        String message = success ? "Processed successfully" : "Processed with one or more calculation errors";
        return new ProcessDataResponse<>(message, success, merged);
    }

    private Map<String, Object> safeBody(ProcessDataResponse<?> response) {
        return response == null || response.getBody() == null ? Map.of() : response.getBody();
    }


    private void publishToThreshold(SensorRawDataRequest<?> request, ProcessDataResponse<?> response) {
        Map<String, Object> thresholdPayload = buildThresholdPayload(request, response);
        thresholdPublishAttemptCount.incrementAndGet();
        lastThresholdPublishAt = Instant.now();
        lastThresholdPayloadSummary = summarizeThresholdPayload(thresholdPayload);
        try {
            thresholdProcessor.processLivePayload(thresholdPayload);
            thresholdPublishSuccessCount.incrementAndGet();
            lastThresholdPublishSuccessAt = Instant.now();
            lastThresholdPublishError = null;
        } catch (Exception ex) {
            thresholdPublishFailureCount.incrementAndGet();
            lastThresholdPublishFailureAt = Instant.now();
            lastThresholdPublishError = ex.getMessage();
            LOG.error("Failed to process threshold checks for reading {}", request.getReadingId(), ex);
        }
    }

    private Map<String, Object> summarizeThresholdPayload(Map<String, Object> payload) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("sensorId", payload.get("sensorId"));
        summary.put("readingId", payload.get("readingId"));
        summary.put("dataType", payload.get("dataType"));
        summary.put("timestamp", payload.get("timestamp"));
        Map<String, Object> calculated = payload.get("calculatedValues") instanceof Map<?, ?> map
                ? objectMapper.convertValue(map, new TypeReference<>() {
        })
                : Map.of();
        summary.put("calculatedValuesCount", calculated.size());
        return summary;
    }

    private Map<String, Object> buildThresholdPayload(SensorRawDataRequest<?> request, ProcessDataResponse<?> response) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sensorId", request.getSensorId());
        payload.put("readingId", request.getReadingId());
        payload.put("timestamp", request.getTimestamp());
        payload.put("dataType", request.getDataType());
        payload.put("sensorParameterId", firstPresent(request.getParameters() == null ? Map.of() : request.getParameters(),
                "sensorParameterId", "sensor_parameter_id", "parameterId", "parameter_id"));
        payload.put("processedStatus", response.isStatus());
        payload.put("message", response.getMessage());
        payload.put("processedBody", safeBody(response));
        payload.put("calculatedValues", extractNumericValues(safeBody(response)));
        return payload;
    }

    private void publishAnalyticsLiveEvent(SensorRawDataRequest<?> request,
                                           ProcessDataResponse<?> response) {
        try {
            Map<String, Object> thresholdPayload = buildThresholdPayload(request, response);
            Map<String, Object> calculatedValues = thresholdPayload.get("calculatedValues") instanceof Map<?, ?> map
                    ? objectMapper.convertValue(map, new TypeReference<>() {
            })
                    : collectNumericValues(safeBody(response));

            List<Map<String, Object>> evaluations = new ArrayList<>();
            for (Map.Entry<String, Object> entry : calculatedValues.entrySet()) {
                if (!(entry.getValue() instanceof Number number)) {
                    continue;
                }
                Map<String, Object> evaluation = new LinkedHashMap<>();
                evaluation.put("parameterName", entry.getKey());
                evaluation.put("value", number.doubleValue());
                evaluation.put("alertCreated", false);
                evaluations.add(evaluation);
            }

            Map<String, Object> event = new LinkedHashMap<>();
            event.put("sensorId", request.getSensorId());
            event.put("readingId", request.getReadingId());
            event.put("timestamp", request.getTimestamp() == null ? Instant.now() : request.getTimestamp());
            event.put("dataType", request.getDataType());
            event.put("evaluations", evaluations);
            event.put("alertCount", 0);
            event.put("source", "processing-service-fallback");

            analyticsEventSubject.publish(event);
        } catch (Exception ex) {
            LOG.warn("Failed to publish analytics live event for reading {}", request.getReadingId(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> collectNumericValues(Map<String, Object> source) {
        Map<String, Object> values = new LinkedHashMap<>();
        flattenNumericValues("", source == null ? Map.of() : source, values);
        return values;
    }

    private void flattenNumericValues(String prefix, Object value, Map<String, Object> target) {
        if (value instanceof Number number) {
            target.put(prefix.isEmpty() ? "value" : prefix, number.doubleValue());
            return;
        }
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                String next = prefix.isEmpty() ? key : prefix + "." + key;
                flattenNumericValues(next, entry.getValue(), target);
            }
            return;
        }
        if (value instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                flattenNumericValues(prefix + "[" + i + "]", list.get(i), target);
            }
        }
    }

    private Map<String, Object> extractNumericValues(Map<String, Object> source) {
        Map<String, Object> flattened = new LinkedHashMap<>();
        flattenNumeric("", source, flattened);
        return flattened;
    }

    @SuppressWarnings("unchecked")
    private void flattenNumeric(String prefix, Object value, Map<String, Object> out) {
        if (value instanceof Number number) {
            out.put(prefix, number.doubleValue());
            return;
        }
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                if (!StringUtils.hasText(key)) {
                    continue;
                }
                String nextPrefix = prefix.isEmpty() ? key : prefix + "." + key;
                flattenNumeric(nextPrefix, entry.getValue(), out);
            }
            return;
        }
        if (value instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                String nextPrefix = prefix + "[" + i + "]";
                flattenNumeric(nextPrefix, list.get(i), out);
            }
        }
    }

    private SensorRawDataRequest<?> toRequest(Map<String, Object> payload) {
        SensorRawDataRequest<Object> request = new SensorRawDataRequest<>();

        request.setDataType(stringValue(firstPresent(payload, "dataType", "data_type", "sensorType", "sensor_type", "type"), "generic"));
        request.setReadingId(parseUuid(firstPresent(payload, "readingId", "id"), true));
        request.setSensorId(parseUuid(firstPresent(payload, "sensorId", "sensor_id", "deviceId", "device_id"), true));
        request.setTimestamp(parseTimestamp(firstPresent(payload, "timestamp", "time", "ts")));
        request.setParameters(extractParameters(payload));

        return request;
    }

    private Map<String, Object> extractParameters(Map<String, Object> payload) {
        Object raw = firstPresent(payload, "parameters", "paramters", "values", "data", "payload", "readings");
        if (raw instanceof Map<?, ?> map) {
            return objectMapper.convertValue(map, new TypeReference<>() {
            });
        }

        Map<String, Object> copy = new LinkedHashMap<>(payload);
        copy.remove("sensorId");
        copy.remove("sensor_id");
        copy.remove("deviceId");
        copy.remove("device_id");
        copy.remove("readingId");
        copy.remove("id");
        copy.remove("dataType");
        copy.remove("data_type");
        copy.remove("sensorType");
        copy.remove("sensor_type");
        copy.remove("type");
        copy.remove("timestamp");
        copy.remove("time");
        copy.remove("ts");
        return copy;
    }

    private Object firstPresent(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            if (payload.containsKey(key)) {
                return payload.get(key);
            }
        }
        return null;
    }

    private UUID parseUuid(Object value, boolean fallbackRandom) {
        if (value == null) {
            return fallbackRandom ? UUID.randomUUID() : null;
        }
        String raw = String.valueOf(value).trim();
        if (!StringUtils.hasText(raw)) {
            return fallbackRandom ? UUID.randomUUID() : null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            return UUID.nameUUIDFromBytes(raw.getBytes(StandardCharsets.UTF_8));
        }
    }

    private Instant parseTimestamp(Object value) {
        if (value == null) {
            return Instant.now();
        }
        if (value instanceof Number n) {
            long epoch = n.longValue();
            return epoch > 1_000_000_000_000L ? Instant.ofEpochMilli(epoch) : Instant.ofEpochSecond(epoch);
        }
        String raw = String.valueOf(value).trim();
        if (!StringUtils.hasText(raw)) {
            return Instant.now();
        }
        try {
            if (raw.matches("^\\d+$")) {
                long epoch = Long.parseLong(raw);
                return epoch > 1_000_000_000_000L ? Instant.ofEpochMilli(epoch) : Instant.ofEpochSecond(epoch);
            }
            return Instant.parse(raw);
        } catch (Exception ignored) {
            return Instant.now();
        }
    }

    private String normalizeDataType(String dataType) {
        if (dataType == null) {
            return "";
        }
        return dataType.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);
    }

    private String stringValue(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String s = String.valueOf(value).trim();
        return StringUtils.hasText(s) ? s : defaultValue;
    }

    private Map<String, Object> copyPayload(Map<String, Object> payload) {
        return payload == null ? Map.of() : new LinkedHashMap<>(payload);
    }

    private boolean matchesSensor(String expectedSensorId, String actualSensorId) {
        return expectedSensorId == null
                || expectedSensorId.isBlank()
                || expectedSensorId.equals(normalizeSensorId(actualSensorId));
    }

    private String normalizeSensorId(String sensorId) {
        return sensorId == null ? null : sensorId.trim();
    }

    private String valueAsString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private record LiveReadingSubscriber(SseEmitter emitter, String sensorIdFilter) {
    }
}
