package com.wowinfobiz.ingestionservice.service;

import com.wowinfobiz.ingestionservice.dto.SensorReadingRequest;
import com.wowinfobiz.ingestionservice.dto.SensorReadingResponse;
import com.wowinfobiz.ingestionservice.dto.SensorReadingView;
import com.wowinfobiz.ingestionservice.model.SensorReading;
import com.wowinfobiz.processingservice.controller.ProcessingController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class IngestionService {
    private static final Logger LOG = LoggerFactory.getLogger(IngestionService.class);

    private static final String DEFAULT_SENSOR_ID = "unknown";
    private static final List<String> SENSOR_ID_KEYS = List.of("sensorId", "sensor_id", "deviceId", "device_id", "id");
    private static final List<String> TIMESTAMP_KEYS = List.of("timestamp", "time", "ts", "createdAt", "created_at");
    private static final List<String> PARAMETERS_KEYS = List.of("parameters", "readings", "data", "values", "payload");
    private static final List<String> DATA_TYPE_KEYS = List.of("dataType", "data_type", "sensorType", "sensor_type", "type");

    private final Map<UUID, SensorReading> readingsById = new ConcurrentHashMap<>();
    private final List<ReadingSubscriber> subscribers = new CopyOnWriteArrayList<>();
    private final ObjectProvider<ProcessingController> processingControllerProvider;
    private final boolean directProcessingEnabled;

    public IngestionService(ObjectProvider<ProcessingController> processingControllerProvider,
                            @Value("${app.ingestion.direct-processing.enabled:true}") boolean directProcessingEnabled) {
        this.processingControllerProvider = processingControllerProvider;
        this.directProcessingEnabled = directProcessingEnabled;
    }

    public SensorReadingResponse saveReading(SensorReadingRequest request) {
        UUID readingId = UUID.randomUUID();
        String sensorId = request.getSensorId() == null || request.getSensorId().isBlank()
                ? DEFAULT_SENSOR_ID
                : request.getSensorId();
        Instant timestamp = request.getTimestamp() == null ? Instant.now() : request.getTimestamp();
        Map<String, Object> parameters = request.getParameters() == null ? Collections.emptyMap() : request.getParameters();

        SensorReading reading = new SensorReading(readingId, sensorId, timestamp, parameters);
        readingsById.put(readingId, reading);
        publishUpdate(toView(reading));
        Map<String, Object> processingPayload = buildProcessingPayload(reading, null);
        processDirectly(processingPayload);
        return new SensorReadingResponse("SUCCESS", "Reading stored successfully", readingId);
    }

    public SensorReadingResponse saveRawReading(Map<String, Object> payload) {
        UUID readingId = UUID.randomUUID();
        String sensorId = extractSensorId(payload);
        Instant timestamp = extractTimestamp(payload);
        Map<String, Object> parameters = extractParameters(payload);

        SensorReading reading = new SensorReading(
                readingId,
                sensorId,
                timestamp,
                parameters
        );
        readingsById.put(readingId, reading);
        publishUpdate(toView(reading));
        Map<String, Object> processingPayload = buildProcessingPayload(reading, payload);
        processDirectly(processingPayload);

        return new SensorReadingResponse("SUCCESS", "Reading stored successfully", readingId);
    }

    public List<SensorReadingResponse> saveBatch(List<SensorReadingRequest> requests) {
        return requests.stream().map(this::saveReading).collect(Collectors.toList());
    }

    public List<SensorReadingResponse> saveRawBatch(List<Map<String, Object>> payloads) {
        return payloads.stream().map(this::saveRawReading).collect(Collectors.toList());
    }

    public List<SensorReadingView> getReadings(String sensorId, Instant from, Instant to) {
        return readingsById.values().stream()
                .filter(reading -> sensorId == null || sensorId.equals(reading.getSensorId()))
                .filter(reading -> from == null || !reading.getTimestamp().isBefore(from))
                .filter(reading -> to == null || !reading.getTimestamp().isAfter(to))
                .sorted(Comparator.comparing(SensorReading::getTimestamp))
                .map(this::toView)
                .collect(Collectors.toList());
    }

    public List<SensorReadingView> getAllReadings() {
        return getReadings(null, null, null);
    }

    public Optional<SensorReadingView> getReadingById(UUID readingId) {
        return Optional.ofNullable(readingsById.get(readingId)).map(this::toView);
    }

    public long getReadingCount() {
        return readingsById.size();
    }

    public SseEmitter subscribeAllReadings() {
        return subscribeReadings(null);
    }

    public SseEmitter subscribeReadings(String sensorIdFilter) {
        SseEmitter emitter = new SseEmitter(0L);
        ReadingSubscriber subscriber = new ReadingSubscriber(emitter, normalizeSensorId(sensorIdFilter));
        subscribers.add(subscriber);

        emitter.onCompletion(() -> subscribers.remove(subscriber));
        emitter.onTimeout(() -> subscribers.remove(subscriber));
        emitter.onError(ex -> subscribers.remove(subscriber));

        try {
            emitter.send(SseEmitter.event()
                    .name("snapshot")
                    .data(getReadings(subscriber.sensorIdFilter(), null, null)));
        } catch (IOException ex) {
            subscribers.remove(subscriber);
            emitter.completeWithError(ex);
        }

        return emitter;
    }

    private SensorReadingView toView(SensorReading reading) {
        return new SensorReadingView(
                reading.getReadingId(),
                reading.getSensorId(),
                reading.getTimestamp(),
                reading.getParameters()
        );
    }

    private String extractSensorId(Map<String, Object> payload) {
        Object sensorIdNode = firstPresent(payload, SENSOR_ID_KEYS);
        if (sensorIdNode == null || String.valueOf(sensorIdNode).isBlank()) {
            return DEFAULT_SENSOR_ID;
        }
        return String.valueOf(sensorIdNode);
    }

    private Instant extractTimestamp(Map<String, Object> payload) {
        Object timestampNode = firstPresent(payload, TIMESTAMP_KEYS);
        if (timestampNode == null) {
            return Instant.now();
        }

        if (timestampNode instanceof Number number) {
            return toInstantFromEpoch(number.longValue());
        }

        String value = String.valueOf(timestampNode).trim();
        if (value.isBlank()) {
            return Instant.now();
        }

        if (value.matches("^\\d+$")) {
            try {
                return toInstantFromEpoch(Long.parseLong(value));
            } catch (NumberFormatException ex) {
                return Instant.now();
            }
        }

        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ex) {
            return Instant.now();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractParameters(Map<String, Object> payload) {
        Object parametersNode = firstPresent(payload, PARAMETERS_KEYS);
        if (parametersNode instanceof Map<?, ?> parametersMap) {
            return ((Map<?, ?>) parametersMap).entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> String.valueOf(entry.getKey()),
                            Map.Entry::getValue
                    ));
        }
        Map<String, Object> payloadMap = new HashMap<>(payload);
        SENSOR_ID_KEYS.forEach(payloadMap::remove);
        TIMESTAMP_KEYS.forEach(payloadMap::remove);
        PARAMETERS_KEYS.forEach(payloadMap::remove);
        return payloadMap;
    }

    private Object firstPresent(Map<String, Object> payload, List<String> keys) {
        if (payload == null) {
            return null;
        }
        for (String key : keys) {
            if (payload.containsKey(key)) {
                return payload.get(key);
            }
        }
        return null;
    }

    private Instant toInstantFromEpoch(long epoch) {
        // Heuristic: 13 digits is epoch millis; otherwise treat as epoch seconds.
        if (Math.abs(epoch) >= 1_000_000_000_000L) {
            return Instant.ofEpochMilli(epoch);
        }
        return Instant.ofEpochSecond(epoch);
    }

    private void publishUpdate(SensorReadingView readingView) {
        List<ReadingSubscriber> deadSubscribers = new ArrayList<>();
        for (ReadingSubscriber subscriber : subscribers) {
            if (!matchesSensor(subscriber.sensorIdFilter(), readingView.getSensorId())) {
                continue;
            }
            try {
                subscriber.emitter().send(SseEmitter.event()
                        .name("update")
                        .data(readingView));
            } catch (IOException ex) {
                deadSubscribers.add(subscriber);
            }
        }
        subscribers.removeAll(deadSubscribers);
    }

    private Map<String, Object> buildProcessingPayload(SensorReading reading, Map<String, Object> sourcePayload) {
        String dataType = resolveDataType(sourcePayload, reading.getParameters());
        System.out.println("DATA TYPE SEND: "+dataType);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("readingId", reading.getReadingId());
        payload.put("sensorId", reading.getSensorId());
        payload.put("timestamp", reading.getTimestamp());
        payload.put("parameters", reading.getParameters());
        payload.put("dataType", dataType);
        payload.put("data_type", dataType);
        payload.put("sensorType", dataType);
        return payload;
    }

    private void processDirectly(Map<String, Object> payload) {
        if (!directProcessingEnabled) {
            return;
        }
        ProcessingController processingController = processingControllerProvider.getIfAvailable();
        if (processingController == null) {
            LOG.warn("ProcessingController bean not found. Skipping direct processing.");
            return;
        }
        try {
            processingController.processPayload(payload);
        } catch (Exception ex) {
            LOG.error("Direct processing failed for reading {}", payload.get("readingId"), ex);
        }
    }

    private String resolveDataType(Map<String, Object> sourcePayload, Map<String, Object> parameters) {
        Object payloadDataType = firstPresent(sourcePayload, DATA_TYPE_KEYS);
        if (payloadDataType != null && !String.valueOf(payloadDataType).isBlank()) {
            System.out.println("payload Data type: "+payloadDataType);
            return String.valueOf(payloadDataType).trim();
        }

        Object parameterDataType = firstPresent(parameters, DATA_TYPE_KEYS);
        if (parameterDataType != null && !String.valueOf(parameterDataType).isBlank()) {
            return String.valueOf(parameterDataType).trim();
        }

        String inferred = inferDataTypeFromParameters(parameters);
        if (!inferred.isBlank()) {
            return inferred;
        }

        return "generic";
    }

    private String inferDataTypeFromParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "";
        }

        Set<String> keys = new HashSet<>();
        collectNormalizedKeys(parameters, keys);

        if (hasAny(keys, "tiltx", "tilty", "anglex", "angley", "tilt")) {
            return "tiltmeter";
        }
        if (hasAny(keys, "inclino", "inclinometer", "inclination")) {
            return "inclinometer";
        }
        if (hasAll(keys, "x", "y", "z") || hasAll(keys, "ax", "ay", "az") || hasAny(keys, "acceleration", "accelerometer")) {
            return "accelerometer";
        }
        if (hasAny(keys, "amplitude", "frequency", "hz", "vibration", "rms")) {
            return "vibration";
        }
        if (hasAny(keys, "force", "load", "weight", "newton")) {
            return "loadcell";
        }
        if (hasAny(keys, "microstrain", "strain", "epsilon")) {
            return "straingauge";
        }
        if (hasAny(keys, "crackwidth", "displacement", "distance", "crack")) {
            return "crackmeter";
        }
        if (hasAny(keys, "pressure", "porepressure", "head", "waterlevel", "piezometer")) {
            return "piezometer";
        }
        if (hasAny(keys, "temperature", "temp", "celsius", "fahrenheit")) {
            return "temperature";
        }

        return "";
    }

    private void collectNormalizedKeys(Map<String, Object> map, Set<String> keys) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            keys.add(normalize(entry.getKey()));
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> nestedMap) {
                Map<String, Object> converted = new HashMap<>();
                for (Map.Entry<?, ?> nestedEntry : nestedMap.entrySet()) {
                    converted.put(String.valueOf(nestedEntry.getKey()), nestedEntry.getValue());
                }
                collectNormalizedKeys(converted, keys);
            }
        }
    }

    private boolean hasAny(Set<String> keys, String... candidates) {
        for (String candidate : candidates) {
            String normalizedCandidate = normalize(candidate);
            for (String key : keys) {
                if (key.contains(normalizedCandidate)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasAll(Set<String> keys, String... required) {
        for (String req : required) {
            String normalizedReq = normalize(req);
            boolean present = false;
            for (String key : keys) {
                if (key.equals(normalizedReq)) {
                    present = true;
                    break;
                }
            }
            if (!present) {
                return false;
            }
        }
        return true;
    }

    private String normalize(String value) {
        return value == null ? "" : value.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);
    }

    private boolean matchesSensor(String expectedSensorId, String actualSensorId) {
        return expectedSensorId == null
                || expectedSensorId.isBlank()
                || Objects.equals(expectedSensorId, normalizeSensorId(actualSensorId));
    }

    private String normalizeSensorId(String sensorId) {
        return sensorId == null ? null : sensorId.trim();
    }

    private record ReadingSubscriber(SseEmitter emitter, String sensorIdFilter) {
    }
}
