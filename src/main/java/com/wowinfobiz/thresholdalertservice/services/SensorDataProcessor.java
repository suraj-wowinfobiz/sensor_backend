package com.wowinfobiz.thresholdalertservice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wowinfobiz.analyticsservice.services.observer.AnalyticsEventSubject;
import com.wowinfobiz.devicemanagmentservice.dto.SensorParameterDTO;
import com.wowinfobiz.devicemanagmentservice.models.SensorParameterDocument;
import com.wowinfobiz.devicemanagmentservice.models.SensorParameters;
import com.wowinfobiz.devicemanagmentservice.repository.SensorParameterDocumentRepository;
import com.wowinfobiz.devicemanagmentservice.repository.SensorParametersRepository;
import com.wowinfobiz.thresholdalertservice.dto.SensorDataDTO;
import com.wowinfobiz.thresholdalertservice.models.AlertEntity;
import com.wowinfobiz.thresholdalertservice.models.ThresholdValueEntity;
import com.wowinfobiz.thresholdalertservice.repository.AlertRepository;
import com.wowinfobiz.thresholdalertservice.repository.ThresholdValueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SensorDataProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(SensorDataProcessor.class);

    @Autowired
    private ThresholdValueRepository thresholdValueRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private SensorParameterDocumentRepository sensorParameterDocumentRepository;

    @Autowired
    private SensorParametersRepository sensorParametersRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnalyticsEventSubject analyticsEventSubject;

    private final AtomicLong processedCount = new AtomicLong(0);
    private volatile Instant lastProcessedAt;
    private volatile Map<String, Object> lastPayloadSummary = Map.of();

    public void processRawPayload(byte[] payloadBytes) {
        try {
            String message = payloadBytes == null ? "{}" : new String(payloadBytes, StandardCharsets.UTF_8);
            Map<String, Object> payload = objectMapper.readValue(message, new TypeReference<>() {
            });
            processLivePayload(payload);
        } catch (Exception ex) {
            LOG.error("Failed to process raw threshold payload", ex);
        }
    }

    public Map<String, Object> ingestionStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("mode", "direct-websocket-sse");
        status.put("processedCount", processedCount.get());
        status.put("lastProcessedAt", lastProcessedAt);
        status.put("lastPayloadSummary", lastPayloadSummary);
        return status;
    }

    public Map<String, Object> processLivePayload(Map<String, Object> payload) {
        Map<String, Object> safePayload = payload == null ? Map.of() : payload;
        processedCount.incrementAndGet();
        lastProcessedAt = Instant.now();
        lastPayloadSummary = summarizePayload(safePayload);
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
                UUID sensorParameterId = resolveSensorParameterId(parameterName, payload.get("sensorParameterId"));
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
        if (sensorData.getSensorParameterId() == null) {
            return null;
        }
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
        analyticsEventSubject.publish(analyticsEvent);
        LOG.info("Published analytics event for sensor {}", analyticsEvent.get("sensorId"));
    }

    private void publishNotification(AlertEntity alert) {
        Map<String, Object> notification = new LinkedHashMap<>();
        notification.put("userId", alert.getSensorId() == null ? "system" : alert.getSensorId().toString());
        notification.put("title", alert.getAlertLevel() + " threshold alert");
        notification.put("message", alert.getMessage());
        notification.put("type", alert.getAlertLevel());
        LOG.info("Threshold notification created for alert {}: {}", alert.getAlertId(), notification);
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

    private UUID resolveSensorParameterId(String parameterName, Object fallbackValue) {
        String normalizedParameter = normalizeName(parameterName);
        if (!normalizedParameter.isBlank()) {
            Optional<UUID> fromDocument = sensorParameterDocumentRepository.findAll().stream()
                    .map(this::readSensorParameterDocument)
                    .filter(Objects::nonNull)
                    .filter(parameter -> namesMatch(parameter.getName(), parameterName, normalizedParameter))
                    .map(SensorParameterDTO::getSensorParameterId)
                    .filter(Objects::nonNull)
                    .findFirst();
            if (fromDocument.isPresent()) {
                return fromDocument.get();
            }

            try {
                Optional<UUID> fromEntity = sensorParametersRepository.findAll().stream()
                        .filter(parameter -> namesMatch(parameter.getName(), parameterName, normalizedParameter))
                        .map(SensorParameters::getSensorParameterId)
                        .filter(Objects::nonNull)
                        .findFirst();
                if (fromEntity.isPresent()) {
                    return fromEntity.get();
                }
            } catch (Exception ex) {
                LOG.debug("Legacy sensor_parameter lookup skipped for {}", parameterName, ex);
            }
        }
        return parseUuid(fallbackValue, false);
    }

    private SensorParameterDTO readSensorParameterDocument(SensorParameterDocument document) {
        if (document == null || document.getData() == null || document.getData().isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(document.getData(), SensorParameterDTO.class);
        } catch (Exception ex) {
            LOG.debug("Unable to read sensor parameter document {}", document.getId(), ex);
            return null;
        }
    }

    private boolean namesMatch(String candidate, String parameterName, String normalizedParameter) {
        String normalizedCandidate = normalizeName(candidate);
        if (normalizedCandidate.isBlank()) {
            return false;
        }
        if (normalizedCandidate.equals(normalizedParameter)) {
            return true;
        }
        String rawParameter = parameterName == null ? "" : parameterName.trim();
        int dotIndex = rawParameter.lastIndexOf('.');
        String leaf = dotIndex >= 0 ? rawParameter.substring(dotIndex + 1) : rawParameter;
        return normalizedCandidate.equals(normalizeName(leaf));
    }

    private String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
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
