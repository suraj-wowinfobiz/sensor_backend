package com.wowinfobiz.analyticsservice.kafka;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wowinfobiz.analyticsservice.services.observer.AnalyticsEventSubject;
import jakarta.annotation.PostConstruct;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AnalyticsKafkaConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsKafkaConsumer.class);

    private final AnalyticsEventSubject analyticsEventSubject;
    private final ObjectMapper objectMapper;
    private final AtomicLong kafkaConsumedCount = new AtomicLong(0);
    private volatile Instant lastKafkaMessageAt;
    private volatile String lastSensorId;
    private volatile Integer lastKafkaPartition;
    private volatile Long lastKafkaOffset;
    private final AtomicBoolean pollerRunning = new AtomicBoolean(false);
    private Thread pollerThread;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:analytics-service-group}")
    private String consumerGroupId;

    @Value("${spring.kafka.consumer.auto-offset-reset:latest}")
    private String autoOffsetReset;

    @Value("${app.kafka.topics.analytics}")
    private String analyticsTopic;

    @Value("${app.kafka.analytics.startup-replay-enabled:true}")
    private boolean startupReplayEnabled;

    @Value("${app.kafka.analytics.startup-replay-records:200}")
    private int startupReplayRecords;

    @Value("${app.kafka.analytics.poll-interval-ms:100}")
    private long pollIntervalMs;

    @Value("${app.kafka.analytics.poller-enabled:false}")
    private boolean pollerEnabled;

    private volatile List<String> assignedPartitions = List.of();

    public AnalyticsKafkaConsumer(AnalyticsEventSubject analyticsEventSubject, ObjectMapper objectMapper) {
        this.analyticsEventSubject = analyticsEventSubject;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void initKafkaPoller() {
        if (!pollerEnabled) {
            LOG.info("Analytics Kafka poller is disabled by configuration.");
            return;
        }
        startKafkaPoller();
    }

    @PreDestroy
    public void stopKafkaPoller() {
        pollerRunning.set(false);
        if (pollerThread != null) {
            pollerThread.interrupt();
        }
    }

    public Map<String, Object> kafkaIngestionStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("bootstrapServers", bootstrapServers);
        status.put("analyticsTopic", analyticsTopic);
        status.put("consumerGroupId", consumerGroupId + "-poller");
        status.put("pollerEnabled", pollerEnabled);
        status.put("pollerRunning", pollerRunning.get());
        status.put("assignedPartitions", assignedPartitions);
        status.put("kafkaConsumedCount", kafkaConsumedCount.get());
        status.put("lastKafkaMessageAt", lastKafkaMessageAt);
        status.put("lastSensorId", lastSensorId);
        status.put("lastKafkaPartition", lastKafkaPartition);
        status.put("lastKafkaOffset", lastKafkaOffset);
        return status;
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

                try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props)) {
                    List<TopicPartition> topicPartitions = List.of();
                    while (pollerRunning.get() && topicPartitions.isEmpty()) {
                        List<PartitionInfo> partitionInfos = consumer.partitionsFor(analyticsTopic, Duration.ofMillis(2000));
                        topicPartitions = partitionInfos.stream()
                                .map(info -> new TopicPartition(info.topic(), info.partition()))
                                .toList();

                        if (topicPartitions.isEmpty()) {
                            assignedPartitions = List.of();
                            LOG.warn("No partitions found for analytics topic {}. Retrying in 2 seconds", analyticsTopic);
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

                    if (startupReplayEnabled) {
                        Map<TopicPartition, Long> endOffsets = consumer.endOffsets(Set.copyOf(topicPartitions));
                        long replayCount = Math.max(1, startupReplayRecords);
                        for (TopicPartition tp : topicPartitions) {
                            long end = endOffsets.getOrDefault(tp, 0L);
                            long start = Math.max(0L, end - replayCount);
                            consumer.seek(tp, start);
                        }
                        LOG.info("Analytics Kafka startup replay enabled for {} partitions, replay records per partition={}",
                                topicPartitions.size(), replayCount);
                    } else {
                        consumer.poll(Duration.ofMillis(200));
                    }
                    LOG.info("Analytics Kafka poller started for topic={} partitions={}", analyticsTopic, assignedPartitions);

                    while (pollerRunning.get()) {
                        ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(Math.max(25L, pollIntervalMs)));
                        for (ConsumerRecord<String, byte[]> record : records) {
                            consumeAnalyticsRecord(record);
                        }
                    }
                } catch (Exception ex) {
                    assignedPartitions = List.of();
                    if (!pollerRunning.get()) {
                        break;
                    }
                    LOG.error("Analytics Kafka poller error. Will retry in 3 seconds", ex);
                    sleepSilently(3000L);
                }
            }
            pollerRunning.set(false);
        }, "analytics-kafka-poller");
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

    private void consumeAnalyticsRecord(ConsumerRecord<String, byte[]> record) {
        Map<String, Object> event = toMap(record.value());
        if (event.isEmpty()) {
            return;
        }
        kafkaConsumedCount.incrementAndGet();
        lastKafkaMessageAt = Instant.now();
        lastSensorId = String.valueOf(event.get("sensorId"));
        lastKafkaPartition = record.partition();
        lastKafkaOffset = record.offset();

        analyticsEventSubject.publish(event);
        LOG.info("Observed analytics event for sensor {} (partition={}, offset={})",
                event.get("sensorId"), record.partition(), record.offset());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object payload) {
        if (payload == null) {
            return Map.of();
        }
        if (payload instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        try {
            if (payload instanceof byte[] bytes) {
                return objectMapper.readValue(new String(bytes, StandardCharsets.UTF_8), new TypeReference<>() {
                });
            }
            if (payload instanceof String json) {
                return objectMapper.readValue(json, new TypeReference<>() {
                });
            }
            return objectMapper.convertValue(payload, new TypeReference<>() {
            });
        } catch (Exception ex) {
            LOG.error("Failed to parse analytics payload", ex);
            return Map.of();
        }
    }
}
