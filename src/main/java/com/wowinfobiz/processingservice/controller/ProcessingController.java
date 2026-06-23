package com.wowinfobiz.processingservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.time.Duration;

@RestController
@RequestMapping("/api/v1/processing")
public class ProcessingController {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessingController.class);

    private final ObjectMapper objectMapper;
    @Nullable
    private final KafkaTemplate<Object, Object> kafkaTemplate;
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
    private final List<SseEmitter> liveEmitters = new CopyOnWriteArrayList<>();
    private final AtomicLong kafkaConsumedCount = new AtomicLong(0);
    private final AtomicLong thresholdPublishAttemptCount = new AtomicLong(0);
    private final AtomicLong thresholdPublishSuccessCount = new AtomicLong(0);
    private final AtomicLong thresholdPublishFailureCount = new AtomicLong(0);
    private final AtomicLong manualConsumerInstanceCounter = new AtomicLong(0);
    private volatile Instant lastKafkaMessageAt;
    private volatile Instant lastThresholdPublishAt;
    private volatile Instant lastThresholdPublishSuccessAt;
    private volatile Instant lastThresholdPublishFailureAt;
    private volatile String lastThresholdPublishError;
    private volatile Map<String, Object> lastThresholdPayloadSummary = Map.of();
    private final Object manualConsumerLock = new Object();
    @Nullable
    private volatile KafkaConsumer<String, byte[]> manualPollingConsumer;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.topics.ingestion}")
    private String ingestionTopic;

    @Value("${spring.kafka.consumer.group-id:processing-service-group}")
    private String consumerGroupId;

    @Value("${app.kafka.topics.threshold}")
    private String thresholdTopic;

    @Value("${app.kafka.listener.enabled:false}")
    private boolean kafkaListenerEnabled;

    @Value("${app.kafka.manual-polling.enabled:true}")
    private boolean manualPollingEnabled;

    @Value("${app.kafka.manual-polling.poll-timeout-ms:3000}")
    private long manualPollingTimeoutMs;

    @Value("${app.kafka.manual-polling.max-records:200}")
    private int manualPollingMaxRecords;

    @Value("${app.kafka.manual-polling.startup-replay-enabled:true}")
    private boolean manualPollingStartupReplayEnabled;

    @Value("${app.kafka.manual-polling.startup-replay-records:500}")
    private int manualPollingStartupReplayRecords;

    private volatile List<String> assignedIngestionPartitions = List.of();
    private final AtomicBoolean startupReplayApplied = new AtomicBoolean(false);

    public ProcessingController(@Nullable KafkaTemplate<Object, Object> kafkaTemplate,
                                Accelerometer accelerometer,
                                CrackMeter crackMeter,
                                InclinoMeter inclinoMeter,
                                LoadCellSensor loadCellSensor,
                                PiezoMeter piezoMeter,
                                StrainGuage strainGuage,
                                TempratorSensor tempratorSensor,
                                TiltMeter tiltMeter,
                                VibrationSensor vibrationSensor,
                                ProcessedReadingStoreService processedReadingStoreService) {
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
        this.kafkaTemplate = kafkaTemplate;
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
    }

    @PostConstruct
    public void logKafkaRuntimeConfig() {
        LOG.info("Processing Kafka config => bootstrapServers={}, ingestionTopic={}, consumerGroupId={}, thresholdTopic={}, listenerEnabled={}, manualPollingEnabled={}",
                bootstrapServers, ingestionTopic, consumerGroupId, thresholdTopic, kafkaListenerEnabled, manualPollingEnabled);
    }

    @PostMapping("/process")
    public ResponseEntity<?> processSensor(@RequestBody Map<String, Object> payload) {
        SensorRawDataRequest<?> request = toRequest(payload);
        ProcessDataResponse<?> response = processByDataType(request);
        persistAndPublishParallel(request, response, copyPayload(payload));
        return ResponseEntity.ok(response);
    }

    @GetMapping({"/readings"})
    public ResponseEntity<?> getAllProcessedReadings(@RequestParam(name = "sensorId", required = false) UUID sensorId,
                                                     @RequestParam(name = "from", required = false) Instant from,
                                                     @RequestParam(name = "to", required = false) Instant to) {
        if (from != null && to != null && from.isAfter(to)) {
            return ResponseEntity.badRequest().body(new ProcessDataResponse<>("Invalid range: 'from' must be before 'to'", false, Map.of()));
        }

        List<Map<String, Object>> records = processedReadingStoreService.findReadings(sensorId, from, to);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("count", records.size());
        body.put("sensorId", sensorId);
        body.put("from", from);
        body.put("to", to);
        body.put("records", records);

        return ResponseEntity.ok(new ProcessDataResponse<>("Processed readings fetched successfully", true, body));
    }

    @GetMapping(value = "/readings/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamLiveIngestionReadings() {
        SseEmitter emitter = new SseEmitter(0L);
        liveEmitters.add(emitter);

        emitter.onCompletion(() -> liveEmitters.remove(emitter));
        emitter.onTimeout(() -> liveEmitters.remove(emitter));
        emitter.onError(ex -> liveEmitters.remove(emitter));

        try {
            Map<String, Object> connectedEvent = Map.of("type", "connected", "message", "Live stream connected", "data", emitter.toString());
            emitter.send(SseEmitter.event().name("connected").data(connectedEvent));
            // Also send unnamed event so clients using EventSource.onmessage receive it.
            emitter.send(SseEmitter.event().data(connectedEvent));

            List<Map<String, Object>> existing = processedReadingStoreService.findReadings(null, null, null);
            Map<String, Object> snapshotEvent = new LinkedHashMap<>();
            snapshotEvent.put("type", "snapshot");
            snapshotEvent.put("count", existing.size());
            snapshotEvent.put("records", existing);
            emitter.send(SseEmitter.event().name("snapshot").data(snapshotEvent));
            emitter.send(SseEmitter.event().data(snapshotEvent));
        } catch (IOException ex) {
            liveEmitters.remove(emitter);
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

    @GetMapping("/kafka/status")
    public ResponseEntity<?> kafkaStatus() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("bootstrapServers", bootstrapServers);
        body.put("ingestionTopic", ingestionTopic);
        body.put("consumerGroupId", consumerGroupId);
        body.put("listenerEnabled", kafkaListenerEnabled);
        body.put("manualPollingEnabled", manualPollingEnabled);
        body.put("assignedIngestionPartitions", assignedIngestionPartitions);
        body.put("consumedCount", kafkaConsumedCount.get());
        body.put("lastKafkaMessageAt", lastKafkaMessageAt);
        return ResponseEntity.ok(new ProcessDataResponse<>("Kafka listener status fetched", true, body));
    }

    @GetMapping("/kafka/threshold-publish-status")
    public ResponseEntity<?> thresholdPublishStatus() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("bootstrapServers", bootstrapServers);
        body.put("thresholdTopic", thresholdTopic);
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

    @GetMapping("/kafka/probe")
    public ResponseEntity<?> kafkaProbe(@RequestParam(name = "timeoutMs", defaultValue = "3000") long timeoutMs,
                                        @RequestParam(name = "maxRecords", defaultValue = "5") int maxRecords) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "processing-probe-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

        List<Map<String, Object>> samples = new ArrayList<>();
        List<Map<String, Object>> partitions = new ArrayList<>();
        int totalPolled = 0;
        int pollAttempts = 0;
        Map<String, Object> partitionOffsets = new LinkedHashMap<>();

        try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props)) {
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(ingestionTopic, Duration.ofMillis(1000));
            for (PartitionInfo info : partitionInfos) {
                Map<String, Object> partition = new LinkedHashMap<>();
                partition.put("topic", info.topic());
                partition.put("partition", info.partition());
                partition.put("leader", info.leader() == null ? null : info.leader().idString());
                partitions.add(partition);
            }
            Set<TopicPartition> assigned = partitionInfos.stream()
                    .map(info -> new TopicPartition(info.topic(), info.partition()))
                    .collect(java.util.stream.Collectors.toSet());
            consumer.assign(new ArrayList<>(assigned));

            if (!assigned.isEmpty()) {
                Map<TopicPartition, Long> beginning = consumer.beginningOffsets(assigned);
                Map<TopicPartition, Long> end = consumer.endOffsets(assigned);
                for (TopicPartition tp : assigned) {
                    Map<String, Object> off = new LinkedHashMap<>();
                    off.put("beginningOffset", beginning.get(tp));
                    off.put("endOffset", end.get(tp));
                    partitionOffsets.put(tp.topic() + "-" + tp.partition(), off);
                }
                consumer.seekToBeginning(assigned);
            }

            long deadline = System.currentTimeMillis() + timeoutMs;
            while (System.currentTimeMillis() < deadline && samples.size() < maxRecords) {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(500));
                pollAttempts++;
                totalPolled += records.count();

                for (ConsumerRecord<String, byte[]> record : records) {
                    if (samples.size() >= maxRecords) {
                        break;
                    }
                    Map<String, Object> sample = new LinkedHashMap<>();
                    sample.put("topic", record.topic());
                    sample.put("partition", record.partition());
                    sample.put("offset", record.offset());
                    sample.put("key", record.key());
                    sample.put("value", record.value() == null ? null : new String(record.value(), StandardCharsets.UTF_8));
                    samples.add(sample);
                }
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("bootstrapServers", bootstrapServers);
        body.put("ingestionTopic", ingestionTopic);
        body.put("timeoutMs", timeoutMs);
        body.put("pollAttempts", pollAttempts);
        body.put("totalPolled", totalPolled);
        body.put("partitions", partitions);
        body.put("partitionOffsets", partitionOffsets);
        body.put("samples", samples);
        return ResponseEntity.ok(new ProcessDataResponse<>("Kafka probe completed", true, body));
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

    @KafkaListener(
            id = "ingestion-topic-listener",
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${app.kafka.topics.ingestion}",
            properties = {"auto.offset.reset=earliest"},
            containerFactory = "kafkaListenerContainerFactory",
            autoStartup = "${app.kafka.listener.enabled:false}"
    )
    public void consumeIngestionData(byte[] payloadBytes) {
        String payloadJson = payloadBytes == null ? "" : new String(payloadBytes, StandardCharsets.UTF_8);
        processKafkaPayload(payloadJson);
    }

    @Scheduled(
            initialDelayString = "${app.kafka.manual-polling.initial-delay-ms:5000}",
            fixedDelayString = "${app.kafka.manual-polling.fixed-delay-ms:3000}"
    )
    public void consumeIngestionDataByPolling() {
        if (!manualPollingEnabled) {
            return;
        }

        int processed = 0;
        try {
            KafkaConsumer<String, byte[]> consumer = getOrCreateManualPollingConsumer();
            ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(manualPollingTimeoutMs));
            if (records.isEmpty()) {
                return;
            }

            for (ConsumerRecord<String, byte[]> record : records) {
                String payloadJson = record.value() == null ? "" : new String(record.value(), StandardCharsets.UTF_8);
                processKafkaPayload(payloadJson);
                processed++;
            }
            LOG.info("Manual Kafka polling processed {} records from topic {}", processed, ingestionTopic);
        } catch (Exception ex) {
            LOG.warn("Manual Kafka polling failed for topic {} using bootstrapServers={}", ingestionTopic, bootstrapServers, ex);
            resetManualPollingConsumer();
        }
    }

    private KafkaConsumer<String, byte[]> getOrCreateManualPollingConsumer() {
        synchronized (manualConsumerLock) {
            if (manualPollingConsumer != null) {
                return manualPollingConsumer;
            }

            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, manualPollingMaxRecords);
            long consumerInstance = manualConsumerInstanceCounter.incrementAndGet();
            String manualConsumerClientId = "processing-manual-poller-" + consumerInstance;
            props.put(ConsumerConfig.CLIENT_ID_CONFIG, manualConsumerClientId);
            props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
            props.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 60000);

            KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props);
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(ingestionTopic, Duration.ofMillis(2000));
            if (partitionInfos == null || partitionInfos.isEmpty()) {
                consumer.close(Duration.ofSeconds(1));
                assignedIngestionPartitions = List.of();
                throw new IllegalStateException("No partitions found for ingestion topic " + ingestionTopic);
            }

            List<TopicPartition> topicPartitions = partitionInfos.stream()
                    .map(info -> new TopicPartition(info.topic(), info.partition()))
                    .toList();
            consumer.assign(topicPartitions);
            assignedIngestionPartitions = topicPartitions.stream()
                    .map(tp -> tp.topic() + "-" + tp.partition())
                    .toList();

            if (manualPollingStartupReplayEnabled && startupReplayApplied.compareAndSet(false, true)) {
                Map<TopicPartition, Long> endOffsets = consumer.endOffsets(Set.copyOf(topicPartitions));
                long replayCount = Math.max(1, manualPollingStartupReplayRecords);
                for (TopicPartition tp : topicPartitions) {
                    long end = endOffsets.getOrDefault(tp, 0L);
                    long start = Math.max(0L, end - replayCount);
                    consumer.seek(tp, start);
                }
                LOG.info("Manual Kafka startup replay enabled for {} partitions, replay records per partition={}",
                        topicPartitions.size(), replayCount);
            } else {
                consumer.seekToEnd(topicPartitions);
                LOG.info("Manual Kafka startup replay disabled. Starting from latest offsets for partitions={}",
                        assignedIngestionPartitions);
            }

            manualPollingConsumer = consumer;
            LOG.info("Manual Kafka poller started. bootstrapServers={}, topic={}, groupId={}, clientId={}, partitions={}",
                    bootstrapServers, ingestionTopic, consumerGroupId, manualConsumerClientId, assignedIngestionPartitions);
            return consumer;
        }
    }

    private void resetManualPollingConsumer() {
        synchronized (manualConsumerLock) {
            if (manualPollingConsumer == null) {
                return;
            }
            try {
                manualPollingConsumer.close(Duration.ofSeconds(2));
            } catch (Exception ignored) {
                // no-op
            }
            manualPollingConsumer = null;
            assignedIngestionPartitions = List.of();
        }
    }

    @PreDestroy
    public void shutdownManualPollingConsumer() {
        resetManualPollingConsumer();
    }

    private void processKafkaPayload(String payloadJson) {
        try {
            LOG.info("Received ingestion payload from Kafka: {}", payloadJson);
            kafkaConsumedCount.incrementAndGet();
            lastKafkaMessageAt = Instant.now();
            Map<String, Object> payload = objectMapper.readValue(payloadJson, new TypeReference<>() {
            });
            SensorRawDataRequest<?> request = toRequest(payload);
            ProcessDataResponse<?> response = processByDataType(request);
            persistAndPublishParallel(request, response, copyPayload(payload));
        } catch (Exception ex) {
            LOG.error("Failed to process ingestion payload: {}", payloadJson, ex);
        }
    }

    private void persistAndPublishParallel(SensorRawDataRequest<?> request,
                                           ProcessDataResponse<?> response,
                                           Map<String, Object> rawPayload) {
        CompletableFuture<Void> saveFuture = CompletableFuture.runAsync(() -> {
            processedReadingStoreService.save(request, response, rawPayload);
            LOG.info("Stored processed reading {} in date-wise CSV", request.getReadingId());
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
        if (liveEmitters.isEmpty()) {
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

        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : liveEmitters) {
            try {
                emitter.send(SseEmitter.event().name("reading").data(event));
                // Also emit unnamed event for generic onmessage listeners.
                emitter.send(SseEmitter.event().data(event));
            } catch (IOException ex) {
                deadEmitters.add(emitter);
            }
        }
        liveEmitters.removeAll(deadEmitters);
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
            case "tiltmeter", "tilt" -> tiltMeter.processSensorData(request);
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
        if (kafkaTemplate == null) {
            LOG.warn("KafkaTemplate bean not found. Skipping publish for reading {}", request.getReadingId());
            return;
        }
        String key = request.getSensorId() == null ? null : request.getSensorId().toString();
        Map<String, Object> thresholdPayload = buildThresholdPayload(request, response);
        thresholdPublishAttemptCount.incrementAndGet();
        lastThresholdPublishAt = Instant.now();
        lastThresholdPayloadSummary = summarizeThresholdPayload(thresholdPayload);
        kafkaTemplate.send(thresholdTopic, key, thresholdPayload).whenComplete((result, ex) -> {
            if (ex != null) {
                thresholdPublishFailureCount.incrementAndGet();
                lastThresholdPublishFailureAt = Instant.now();
                lastThresholdPublishError = ex.getMessage();
                LOG.error("Failed to publish processed reading {} to topic {}", request.getReadingId(), thresholdTopic, ex);
            } else {
                thresholdPublishSuccessCount.incrementAndGet();
                lastThresholdPublishSuccessAt = Instant.now();
                lastThresholdPublishError = null;
                LOG.info("Published processed reading {} to topic {}", request.getReadingId(), thresholdTopic);
            }
        });
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
}
