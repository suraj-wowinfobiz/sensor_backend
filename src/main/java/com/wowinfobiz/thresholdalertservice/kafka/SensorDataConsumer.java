package com.wowinfobiz.thresholdalertservice.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wowinfobiz.thresholdalertservice.dto.SensorDataDTO;
import com.wowinfobiz.thresholdalertservice.models.AlertEntity;
import com.wowinfobiz.thresholdalertservice.models.ThresholdValueEntity;
import com.wowinfobiz.thresholdalertservice.repository.AlertRepository;
import com.wowinfobiz.thresholdalertservice.repository.ThresholdValueRepository;
import jakarta.annotation.PreDestroy;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SensorDataConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(SensorDataConsumer.class);

    @Autowired
    private ThresholdValueRepository thresholdValueRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Nullable
    @Autowired(required = false)
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${app.kafka.topics.analytics}")
    private String analyticsTopic;

    @Value("${app.kafka.topics.notification}")
    private String notificationTopic;

    @Value("${app.kafka.topics.threshold}")
    private String thresholdTopic;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:threshold-alert-group}")
    private String consumerGroupId;

    @Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;

    @Value("${app.kafka.threshold.startup-replay-enabled:false}")
    private boolean startupReplayEnabled;

    @Value("${app.kafka.threshold.startup-replay-records:200}")
    private int startupReplayRecords;

    @Value("${app.kafka.threshold.poll-interval-ms:100}")
    private long pollIntervalMs;

    @Value("${app.kafka.threshold.poller-enabled:false}")
    private boolean pollerEnabled;

    @Value("${app.kafka.threshold.processing-threads:2}")
    private int processingThreads;

    @Value("${app.kafka.threshold.processing-queue-capacity:500}")
    private int processingQueueCapacity;

    @Value("${app.kafka.threshold.max-poll-records:100}")
    private int maxPollRecords;

    @Value("${app.kafka.threshold.backpressure-queue-threshold-percent:80}")
    private int backpressureQueueThresholdPercent;

    private final AtomicLong kafkaConsumedCount = new AtomicLong(0);
    private final AtomicLong processingDroppedCount = new AtomicLong(0);
    private volatile Instant lastKafkaMessageAt;
    private volatile Map<String, Object> lastKafkaPayloadSummary = Map.of();
    private volatile Integer lastKafkaPartition;
    private volatile Long lastKafkaOffset;
    private volatile List<String> assignedPartitions = List.of();
    private final AtomicBoolean pollerRunning = new AtomicBoolean(false);
    private volatile boolean backpressureActive;
    private Thread pollerThread;
    private ThreadPoolExecutor processingExecutor;

    @PostConstruct
    public void initKafkaProcessing() {
        LOG.info("Threshold Kafka config => bootstrapServers={}, thresholdTopic={}, consumerGroupId={}, autoOffsetReset={}",
                bootstrapServers, thresholdTopic, consumerGroupId, autoOffsetReset);
        if (!pollerEnabled) {
            LOG.info("Threshold Kafka poller is disabled by configuration.");
            return;
        }
        initProcessingExecutor();
        startKafkaPoller();
    }

    private void initProcessingExecutor() {
        int workerThreads = Math.max(1, processingThreads);
        int queueCapacity = Math.max(100, processingQueueCapacity);
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(queueCapacity);
        AtomicInteger threadCounter = new AtomicInteger(1);
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("threshold-kafka-processor-" + threadCounter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
        processingExecutor = new ThreadPoolExecutor(
                workerThreads,
                workerThreads,
                60L,
                TimeUnit.SECONDS,
                queue,
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        processingExecutor.allowCoreThreadTimeOut(false);
        LOG.info("Threshold Kafka processing executor initialized with threads={} queueCapacity={}",
                workerThreads, queueCapacity);
    }

    private void startKafkaPoller() {
        if (!pollerRunning.compareAndSet(false, true)) {
            return;
        }
        pollerThread = new Thread(() -> {
            while (pollerRunning.get()) {
                Properties props = new Properties();
                props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId + "-poller");
                props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
                props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
                props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
                props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
                props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
                props.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 15000);
                int recommendedMaxPoll = Math.max(1, processingThreads * 10);
                props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, Math.min(Math.max(1, maxPollRecords), recommendedMaxPoll));

                try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props)) {
                    List<TopicPartition> topicPartitions = List.of();
                    while (pollerRunning.get() && topicPartitions.isEmpty()) {
                        List<PartitionInfo> partitionInfos = consumer.partitionsFor(thresholdTopic, Duration.ofMillis(2000));
                        topicPartitions = partitionInfos.stream()
                                .map(info -> new TopicPartition(info.topic(), info.partition()))
                                .toList();

                        if (topicPartitions.isEmpty()) {
                            assignedPartitions = List.of();
                            LOG.warn("No partitions found for threshold topic {}. Retrying in 2 seconds", thresholdTopic);
                            sleepSilently(2000L);
                        }
                    }
                    if (!pollerRunning.get()) {
                        break;
                    }
                    if (topicPartitions.isEmpty()) {
                        continue;
                    }

                    consumer.assign(topicPartitions);
                    assignedPartitions = topicPartitions.stream()
                            .map(tp -> tp.topic() + "-" + tp.partition())
                            .toList();

                    Map<TopicPartition, Long> endOffsets = consumer.endOffsets(Set.copyOf(topicPartitions));
                    if (startupReplayEnabled) {
                        long replayCount = Math.max(1, startupReplayRecords);
                        for (TopicPartition tp : topicPartitions) {
                            long end = endOffsets.getOrDefault(tp, 0L);
                            long start = Math.max(0L, end - replayCount);
                            consumer.seek(tp, start);
                        }
                        LOG.info("Threshold Kafka startup replay enabled for {} partitions, replay records per partition={}",
                                topicPartitions.size(), replayCount);
                    } else {
                        // Keep existing behavior aligned with AUTO_OFFSET_RESET for new group; poll once to initialize position.
                        consumer.poll(Duration.ofMillis(200));
                    }
                    LOG.info("Threshold Kafka poller started for topic={} partitions={}", thresholdTopic, assignedPartitions);

                    while (pollerRunning.get()) {
                        if (isBackpressureActive()) {
                            sleepSilently(Math.max(50L, pollIntervalMs));
                            continue;
                        }
                        ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(Math.max(25L, pollIntervalMs)));
                        for (ConsumerRecord<String, byte[]> record : records) {
                            submitForProcessing(record);
                        }
                    }
                } catch (Exception ex) {
                    assignedPartitions = List.of();
                    if (!pollerRunning.get()) {
                        break;
                    }
                    LOG.error("Threshold Kafka poller error. Will retry in 3 seconds", ex);
                    sleepSilently(3000L);
                }
            }
            pollerRunning.set(false);
        }, "threshold-kafka-poller");
        pollerThread.setDaemon(true);
        pollerThread.start();
    }

    private void sleepSilently(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    public void stopKafkaPoller() {
        pollerRunning.set(false);
        if (pollerThread != null) {
            pollerThread.interrupt();
        }
        if (processingExecutor != null) {
            processingExecutor.shutdown();
        }
    }

    private void submitForProcessing(ConsumerRecord<String, byte[]> record) {
        if (processingExecutor == null) {
            consumeSensorData(record);
            return;
        }
        try {
            processingExecutor.execute(() -> consumeSensorData(record));
        } catch (RejectedExecutionException rejectedExecutionException) {
            processingDroppedCount.incrementAndGet();
            LOG.warn("Threshold processor queue is full. Dropping message partition={} offset={}",
                    record.partition(), record.offset());
        }
    }

    private boolean isBackpressureActive() {
        if (processingExecutor == null) {
            backpressureActive = false;
            return false;
        }
        int thresholdPercent = Math.max(50, Math.min(95, backpressureQueueThresholdPercent));
        int capacity = processingExecutor.getQueue().size() + processingExecutor.getQueue().remainingCapacity();
        if (capacity <= 0) {
            backpressureActive = false;
            return false;
        }
        int usagePercent = (processingExecutor.getQueue().size() * 100) / capacity;
        backpressureActive = usagePercent >= thresholdPercent;
        if (backpressureActive) {
            LOG.debug("Threshold Kafka backpressure active. queueUsage={}%, threshold={}%", usagePercent, thresholdPercent);
        }
        return backpressureActive;
    }

    private void consumeSensorData(ConsumerRecord<String, byte[]> record) {
        try {
            processRawKafkaPayload(record.value(), record.partition(), record.offset(), true);
        } catch (Exception ex) {
            LOG.error("Failed to consume/process threshold message", ex);
        }
    }

    public void processRawKafkaPayload(byte[] payloadBytes, Integer partition, Long offset, boolean countAsConsumed) {
        try {
            if (countAsConsumed) {
                kafkaConsumedCount.incrementAndGet();
            }
            lastKafkaMessageAt = Instant.now();
            lastKafkaPartition = partition;
            lastKafkaOffset = offset;

            String message = payloadBytes == null ? "{}" : new String(payloadBytes, StandardCharsets.UTF_8);
            Map<String, Object> payload = objectMapper.readValue(message, new TypeReference<>() {
            });
            lastKafkaPayloadSummary = summarizePayload(payload);
            processLivePayload(payload);
        } catch (Exception ex) {
            LOG.error("Failed to process raw threshold payload", ex);
        }
    }

    public Map<String, Object> kafkaIngestionStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("bootstrapServers", bootstrapServers);
        status.put("thresholdTopic", thresholdTopic);
        status.put("consumerGroupId", consumerGroupId);
        status.put("pollerRunning", pollerRunning.get());
        status.put("assignedPartitions", assignedPartitions);
        status.put("kafkaConsumedCount", kafkaConsumedCount.get());
        status.put("processingDroppedCount", processingDroppedCount.get());
        status.put("processingQueueSize", processingExecutor == null ? 0 : processingExecutor.getQueue().size());
        status.put("processingActiveThreads", processingExecutor == null ? 0 : processingExecutor.getActiveCount());
        status.put("processingPoolSize", processingExecutor == null ? 0 : processingExecutor.getPoolSize());
        status.put("backpressureActive", backpressureActive);
        status.put("lastKafkaMessageAt", lastKafkaMessageAt);
        status.put("lastKafkaPartition", lastKafkaPartition);
        status.put("lastKafkaOffset", lastKafkaOffset);
        status.put("lastKafkaPayloadSummary", lastKafkaPayloadSummary);
        return status;
    }

    public Map<String, Object> processLivePayload(Map<String, Object> payload) {
        Map<String, Object> safePayload = payload == null ? Map.of() : payload;
        Map<String, Object> analyticsEvent = evaluatePayload(safePayload);
        publishAnalyticsEvent(analyticsEvent);
        return analyticsEvent;
    }

    private Map<String, Object> evaluatePayload(Map<String, Object> payload) {
        UUID sensorId = parseUuid(payload.get("sensorId"), true);
        UUID readingId = parseUuid(payload.get("readingId"), true);
        String dataType = String.valueOf(payload.getOrDefault("dataType", "unknown"));
        Object timestamp = payload.getOrDefault("timestamp", Instant.now().toString());

        Map<String, Object> calculatedValues = asMap(payload.get("calculatedValues"));
        if (calculatedValues.isEmpty()) {
            calculatedValues = collectNumericValues(asMap(payload.get("processedBody")));
        }

        List<Map<String, Object>> evaluations = new ArrayList<>();

        if (calculatedValues.isEmpty() && payload.containsKey("value")) {
            UUID sensorParameterId = parseUuid(payload.get("sensorParameterId"), true);
            String parameterName = String.valueOf(payload.getOrDefault("parameterName", "value"));
            Double value = asDouble(payload.get("value"));
            if (value != null) {
                evaluations.add(evaluateOne(sensorId, sensorParameterId, parameterName, value));
            }
        } else {
            for (Map.Entry<String, Object> entry : calculatedValues.entrySet()) {
                Double value = asDouble(entry.getValue());
                if (value == null) {
                    continue;
                }
                String parameterName = entry.getKey();
                UUID sensorParameterId = parseUuid(payload.get("sensorParameterId"), false);
                if (sensorParameterId == null) {
                    sensorParameterId = UUID.nameUUIDFromBytes((sensorId + ":" + parameterName).getBytes(StandardCharsets.UTF_8));
                }
                evaluations.add(evaluateOne(sensorId, sensorParameterId, parameterName, value));
            }
        }

        long alertCount = evaluations.stream().filter(e -> Boolean.TRUE.equals(e.get("alertCreated"))).count();

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("sensorId", sensorId);
        event.put("readingId", readingId);
        event.put("timestamp", timestamp);
        event.put("dataType", dataType);
        event.put("evaluations", evaluations);
        event.put("alertCount", alertCount);
        event.put("source", "threshold-alert-service");
        return event;
    }

    private Map<String, Object> evaluateOne(UUID sensorId, UUID sensorParameterId, String parameterName, double value) {
        SensorDataDTO sensorData = new SensorDataDTO();
        sensorData.setSensorId(sensorId);
        sensorData.setSensorParameterId(sensorParameterId);
        sensorData.setParameterName(parameterName);
        sensorData.setValue(value);

        AlertEntity alert = checkThresholdAndCreateAlert(sensorData);
        if (alert != null) {
            publishNotification(alert);
        }

        Map<String, Object> evaluation = new LinkedHashMap<>();
        evaluation.put("sensorParameterId", sensorParameterId);
        evaluation.put("parameterName", parameterName);
        evaluation.put("value", value);
        evaluation.put("alertCreated", alert != null);
        if (alert != null) {
            evaluation.put("alertId", alert.getAlertId());
            evaluation.put("alertLevel", alert.getAlertLevel());
            evaluation.put("message", alert.getMessage());
        }
        return evaluation;
    }

    private AlertEntity checkThresholdAndCreateAlert(SensorDataDTO sensorData) {
        Optional<ThresholdValueEntity> thresholdOpt = thresholdValueRepository
                .findBySensorParameterId(sensorData.getSensorParameterId());

        if (thresholdOpt.isEmpty()) {
            return null;
        }

        ThresholdValueEntity threshold = thresholdOpt.get();
        String alertLevel = null;
        String message = null;

        if (sensorData.getValue() >= threshold.getCriticalLevel()) {
            alertLevel = "CRITICAL";
            message = "CRITICAL: " + sensorData.getParameterName() + " exceeded critical threshold. Value: " + sensorData.getValue();
        } else if (sensorData.getValue() >= threshold.getWarrningLevel()) {
            alertLevel = "WARNING";
            message = "WARNING: " + sensorData.getParameterName() + " exceeded warning threshold. Value: " + sensorData.getValue();
        } else if (sensorData.getValue() < threshold.getMinThresholdValue() || sensorData.getValue() > threshold.getMaxThresholdValue()) {
            alertLevel = "INFO";
            message = "INFO: " + sensorData.getParameterName() + " out of normal range. Value: " + sensorData.getValue();
        }

        if (alertLevel == null) {
            return null;
        }

        AlertEntity alert = new AlertEntity();
        alert.setAlertId(UUID.randomUUID());
        alert.setSensorId(sensorData.getSensorId());
        alert.setSensorParameterId(sensorData.getSensorParameterId());
        alert.setAlertLevel(alertLevel);
        alert.setMessage(message);
        alert.setTriggeredAt(new Date());
        alert.setStatus("ACTIVE");
        return alertRepository.save(alert);
    }

    private void publishAnalyticsEvent(Map<String, Object> analyticsEvent) {
        if (kafkaTemplate == null) {
            LOG.warn("KafkaTemplate bean not found in threshold-alert-service. Skipping analytics publish.");
            return;
        }
        String key = String.valueOf(analyticsEvent.get("sensorId"));
        kafkaTemplate.send(analyticsTopic, key, analyticsEvent).whenComplete((result, ex) -> {
            if (ex != null) {
                LOG.error("Failed to publish analytics event to topic {}", analyticsTopic, ex);
            } else {
                LOG.info("Published analytics event for sensor {} to topic {}", key, analyticsTopic);
            }
        });
    }

    private void publishNotification(AlertEntity alert) {
        if (kafkaTemplate == null) {
            LOG.warn("KafkaTemplate bean not found in threshold-alert-service. Skipping notification publish.");
            return;
        }
        Map<String, Object> notification = new LinkedHashMap<>();
        notification.put("userId", alert.getSensorId() == null ? "system" : alert.getSensorId().toString());
        notification.put("title", alert.getAlertLevel() + " threshold alert");
        notification.put("message", alert.getMessage());
        notification.put("type", alert.getAlertLevel());
        kafkaTemplate.send(notificationTopic, String.valueOf(notification.get("userId")), notification).whenComplete((result, ex) -> {
            if (ex != null) {
                LOG.error("Failed to publish notification for alert {}", alert.getAlertId(), ex);
            } else {
                LOG.info("Published notification for alert {} to topic {}", alert.getAlertId(), notificationTopic);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private Double asDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Map<String, Object> collectNumericValues(Map<String, Object> source) {
        Map<String, Object> values = new LinkedHashMap<>();
        flattenValues("", source, values);
        return values;
    }

    @SuppressWarnings("unchecked")
    private void flattenValues(String prefix, Object value, Map<String, Object> target) {
        if (value instanceof Number number) {
            target.put(prefix, number.doubleValue());
            return;
        }
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                String next = prefix.isEmpty() ? key : prefix + "." + key;
                flattenValues(next, entry.getValue(), target);
            }
            return;
        }
        if (value instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                flattenValues(prefix + "[" + i + "]", list.get(i), target);
            }
        }
    }

    private UUID parseUuid(Object value, boolean fallbackRandom) {
        if (value == null) {
            return fallbackRandom ? UUID.randomUUID() : null;
        }
        String raw = String.valueOf(value).trim();
        if (raw.isEmpty()) {
            return fallbackRandom ? UUID.randomUUID() : null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException ex) {
            return fallbackRandom ? UUID.nameUUIDFromBytes(raw.getBytes(StandardCharsets.UTF_8)) : null;
        }
    }

    private Map<String, Object> summarizePayload(Map<String, Object> payload) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("sensorId", payload == null ? null : payload.get("sensorId"));
        summary.put("readingId", payload == null ? null : payload.get("readingId"));
        summary.put("dataType", payload == null ? null : payload.get("dataType"));
        summary.put("timestamp", payload == null ? null : payload.get("timestamp"));
        Map<String, Object> calculated = payload == null ? Map.of() : asMap(payload.get("calculatedValues"));
        summary.put("calculatedValuesCount", calculated.size());
        return summary;
    }
}
