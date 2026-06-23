package com.wowinfobiz.analyticsservice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wowinfobiz.analyticsservice.entities.AnalyticsEventEntity;
import com.wowinfobiz.analyticsservice.repositories.AnalyticsEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsEventStore {
    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsEventStore.class);

    private final AnalyticsEventRepository analyticsEventRepository;
    private final ObjectMapper objectMapper;

    public AnalyticsEventStore(AnalyticsEventRepository analyticsEventRepository, ObjectMapper objectMapper) {
        this.analyticsEventRepository = analyticsEventRepository;
        this.objectMapper = objectMapper;
    }

    public void addEvent(Map<String, Object> event) {
        Map<String, Object> normalized = normalizeEvent(event);
        AnalyticsEventEntity entity = new AnalyticsEventEntity();
        entity.setSensorId(extractSensorId(normalized));
        entity.setAlertCount(extractAlertCount(normalized));
        entity.setPayloadJson(toJson(normalized));
        entity.setReceivedAt(extractEventTime(normalized));
        analyticsEventRepository.save(entity);
    }

    public List<Map<String, Object>> getAllEvents() {
        return analyticsEventRepository.findAll().stream()
                .sorted((left, right) -> right.getReceivedAt().compareTo(left.getReceivedAt()))
                .map(this::toMap)
                .toList();
    }

    public List<Map<String, Object>> getRecentEvents(int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        Pageable pageable = PageRequest.of(0, limit);
        return analyticsEventRepository.findAllByOrderByReceivedAtDesc(pageable)
                .stream()
                .map(this::toMap)
                .toList();
    }

    public List<Map<String, Object>> getAlertEvents() {
        Pageable pageable = PageRequest.of(0, 500);
        return analyticsEventRepository.findByAlertCountGreaterThanOrderByReceivedAtDesc(0L, pageable)
                .stream()
                .map(this::toMap)
                .toList();
    }

    public long count() {
        return analyticsEventRepository.count();
    }

    public Map<String, Object> getDashboard() {
        Map<String, Object> response = baseResponse();
        response.put("totalEvents", count());
        response.put("alertEvents", getAlertEvents().size());
        response.put("activeSensors", uniqueSensorCount());
        return response;
    }

    public Map<String, Object> getOverview() {
        Map<String, Object> response = baseResponse();
        response.put("message", "Analytics overview generated from persisted events");
        response.put("totalEvents", count());
        response.put("eventsLast24Hours", analyticsEventRepository.countByReceivedAtAfter(Instant.now().minus(24, ChronoUnit.HOURS)));
        return response;
    }

    public Map<String, Object> getSensorTrends(String sensorId, String range) {
        Pageable pageable = PageRequest.of(0, 50);
        List<AnalyticsEventEntity> events = analyticsEventRepository.findRecentBySensorId(sensorId, pageable);

        List<Double> trendPoints = new ArrayList<>();
        for (int i = events.size() - 1; i >= 0; i--) {
            Object value = toMap(events.get(i)).get("value");
            if (value instanceof Number number) {
                trendPoints.add(number.doubleValue());
            }
        }

        Map<String, Object> response = baseResponse();
        response.put("sensorId", sensorId);
        response.put("range", range);
        response.put("points", trendPoints.size());
        response.put("trendPoints", trendPoints);
        return response;
    }

    public Map<String, Object> getSensorPredictions(String sensorId, String horizon) {
        Map<String, Object> trends = getSensorTrends(sensorId, horizon);
        List<Double> trendPoints = (List<Double>) trends.getOrDefault("trendPoints", List.of());

        double baseline = trendPoints.isEmpty() ? 0.0 : trendPoints.get(trendPoints.size() - 1);
        List<Double> predictions = List.of(
                baseline + 0.2,
                baseline + 0.4,
                baseline + 0.5,
                baseline + 0.6
        );

        Map<String, Object> response = baseResponse();
        response.put("sensorId", sensorId);
        response.put("horizon", horizon);
        response.put("predictions", predictions);
        return response;
    }

    public Map<String, Object> compareSensors(List<String> sensorIds) {
        List<Map<String, Object>> comparison = sensorIds.stream().map(sensorId -> {
            Pageable pageable = PageRequest.of(0, 100);
            List<AnalyticsEventEntity> events = analyticsEventRepository.findRecentBySensorId(sensorId, pageable);

            double avgAlerts = events.stream()
                    .map(AnalyticsEventEntity::getAlertCount)
                    .filter(value -> value != null)
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);

            double reliability = events.isEmpty()
                    ? 1.0
                    : events.stream().filter(event -> (event.getAlertCount() == null || event.getAlertCount() == 0L)).count() / (double) events.size();

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("sensorId", sensorId);
            item.put("events", events.size());
            item.put("avgAlertCount", avgAlerts);
            item.put("reliability", Math.round(reliability * 1000.0) / 1000.0);
            return item;
        }).toList();

        Map<String, Object> response = baseResponse();
        response.put("sensorIds", sensorIds);
        response.put("comparison", comparison);
        return response;
    }

    public Map<String, Object> getAnomalies() {
        List<Map<String, Object>> alertEvents = getAlertEvents();
        Map<String, Object> response = baseResponse();
        response.put("count", alertEvents.size());
        response.put("items", alertEvents);
        return response;
    }

    public Map<String, Object> getHealthScore() {
        long total = count();
        long alerts = getAlertEvents().size();
        double score = total == 0 ? 100.0 : Math.max(0.0, 100.0 - ((alerts * 100.0) / total));

        Map<String, Object> response = baseResponse();
        response.put("score", Math.round(score * 10.0) / 10.0);
        return response;
    }

    public Map<String, Object> getDistribution() {
        List<Map<String, Object>> recent = getRecentEvents(500);
        int low = 0;
        int medium = 0;
        int high = 0;

        for (Map<String, Object> event : recent) {
            Object value = event.get("value");
            if (!(value instanceof Number number)) {
                continue;
            }
            double numeric = number.doubleValue();
            if (numeric < 10.0) {
                low++;
            } else if (numeric < 20.0) {
                medium++;
            } else {
                high++;
            }
        }

        Map<String, Object> response = baseResponse();
        response.put("buckets", Map.of("low", low, "medium", medium, "high", high));
        return response;
    }

    public Map<String, Object> getPerformance() {
        long lastHourEvents = analyticsEventRepository.countByReceivedAtAfter(Instant.now().minus(1, ChronoUnit.HOURS));

        Map<String, Object> response = baseResponse();
        response.put("eventsLastHour", lastHourEvents);
        response.put("throughputPerMinute", Math.round((lastHourEvents / 60.0) * 100.0) / 100.0);
        return response;
    }

    public Map<String, Object> getUtilization() {
        long totalEvents = count();
        long alertEvents = getAlertEvents().size();

        Map<String, Object> response = baseResponse();
        response.put("eventLoad", totalEvents);
        response.put("alertRatio", totalEvents == 0 ? 0.0 : Math.round((alertEvents * 10000.0 / totalEvents)) / 100.0);
        return response;
    }

    public Map<String, Object> getDowntime() {
        long alertsLastDay = analyticsEventRepository.countByAlertCountGreaterThanAndReceivedAtAfter(0L, Instant.now().minus(24, ChronoUnit.HOURS));

        Map<String, Object> response = baseResponse();
        response.put("estimatedDowntimeMinutes", alertsLastDay * 2);
        response.put("alertsLast24Hours", alertsLastDay);
        return response;
    }

    public Map<String, Object> getAlertsTrend() {
        long today = analyticsEventRepository.countByAlertCountGreaterThanAndReceivedAtAfter(0L, Instant.now().minus(1, ChronoUnit.DAYS));
        long week = analyticsEventRepository.countByAlertCountGreaterThanAndReceivedAtAfter(0L, Instant.now().minus(7, ChronoUnit.DAYS));

        Map<String, Object> response = baseResponse();
        response.put("last24Hours", today);
        response.put("last7Days", week);
        return response;
    }

    public Map<String, Object> getSensorReliability() {
        List<Map<String, Object>> sensors = analyticsEventRepository.findAll().stream()
                .map(AnalyticsEventEntity::getSensorId)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .limit(20)
                .map(sensorId -> {
                    Pageable pageable = PageRequest.of(0, 100);
                    List<AnalyticsEventEntity> sensorEvents = analyticsEventRepository.findRecentBySensorId(sensorId, pageable);
                    long stable = sensorEvents.stream().filter(event -> event.getAlertCount() == null || event.getAlertCount() == 0L).count();
                    double reliability = sensorEvents.isEmpty() ? 1.0 : stable / (double) sensorEvents.size();
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("sensorId", sensorId);
                    item.put("reliability", Math.round(reliability * 1000.0) / 1000.0);
                    return item;
                })
                .toList();

        Map<String, Object> response = baseResponse();
        response.put("sensors", sensors);
        return response;
    }

    public Map<String, Object> getDeviceUptime() {
        long totalEvents = count();
        long alerts = getAlertEvents().size();
        double uptimePercent = totalEvents == 0 ? 100.0 : Math.max(0.0, 100.0 - ((alerts * 100.0) / totalEvents));

        Map<String, Object> response = baseResponse();
        response.put("devices", List.of(Map.of("deviceId", "derived-from-analytics", "uptimePercent", Math.round(uptimePercent * 10.0) / 10.0)));
        return response;
    }

    public Map<String, Object> runCustomQuery(String query, int limit) {
        int safeLimit = Math.max(limit, 0);
        List<Map<String, Object>> selected;
        if (safeLimit == 0) {
            selected = List.of();
        } else if (query == null || query.isBlank()) {
            selected = getRecentEvents(safeLimit);
        } else {
            Pageable pageable = PageRequest.of(0, safeLimit);
            selected = analyticsEventRepository.searchByPayload(query.trim(), pageable).stream().map(this::toMap).toList();
        }

        Map<String, Object> response = baseResponse();
        response.put("query", query);
        response.put("limit", limit);
        response.put("resultCount", selected.size());
        response.put("results", selected);
        return response;
    }

    public Map<String, Object> getLiveAnalyticsSnapshot(Map<String, Object> latestEvent) {
        Map<String, Object> response = baseResponse();
        response.put("latestEvent", latestEvent == null ? Map.of() : latestEvent);
        response.put("overview", getOverview());
        response.put("dashboard", getDashboard());
        response.put("healthScore", getHealthScore());
        response.put("performance", getPerformance());
        response.put("alertsTrend", getAlertsTrend());
        response.put("utilization", getUtilization());
        response.put("distribution", getDistribution());
        response.put("recentEvents", getRecentEvents(20));
        response.put("alertEvents", getAlertEvents());
        return response;
    }

    private int uniqueSensorCount() {
        return (int) analyticsEventRepository.countDistinctSensorId();
    }

    private Map<String, Object> toMap(AnalyticsEventEntity entity) {
        try {
            Map<String, Object> payload = objectMapper.readValue(entity.getPayloadJson(), new TypeReference<>() {
            });
            payload.putIfAbsent("eventId", entity.getId());
            payload.putIfAbsent("sensorId", entity.getSensorId());
            payload.putIfAbsent("alertCount", entity.getAlertCount());
            payload.putIfAbsent("receivedAt", entity.getReceivedAt());
            return payload;
        } catch (Exception ex) {
            LOG.warn("Failed to parse analytics event payload for id {}", entity.getId(), ex);
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("eventId", entity.getId());
            fallback.put("sensorId", entity.getSensorId());
            fallback.put("alertCount", entity.getAlertCount());
            fallback.put("receivedAt", entity.getReceivedAt());
            fallback.put("payload", entity.getPayloadJson());
            return fallback;
        }
    }

    private String extractSensorId(Map<String, Object> event) {
        Object sensorId = event.get("sensorId");
        return sensorId == null ? null : String.valueOf(sensorId);
    }

    private long extractAlertCount(Map<String, Object> event) {
        Object alertCount = event.get("alertCount");
        if (alertCount instanceof Number number) {
            return number.longValue();
        }
        if (alertCount instanceof String value) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ignored) {
                return 0L;
            }
        }
        return 0L;
    }

    private String toJson(Map<String, Object> event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to serialize analytics event", ex);
        }
    }

    private Instant extractEventTime(Map<String, Object> event) {
        Object timestamp = event.get("timestamp");
        if (timestamp == null) {
            timestamp = event.get("eventTime");
        }
        if (timestamp == null) {
            return Instant.now();
        }

        if (timestamp instanceof Instant instant) {
            return instant;
        }

        if (timestamp instanceof Number number) {
            long epoch = number.longValue();
            return epoch > 100000000000L ? Instant.ofEpochMilli(epoch) : Instant.ofEpochSecond(epoch);
        }

        try {
            return Instant.parse(String.valueOf(timestamp));
        } catch (Exception ignored) {
            return Instant.now();
        }
    }

    private Map<String, Object> baseResponse() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("generatedAt", Instant.now());
        return response;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> normalizeEvent(Map<String, Object> incoming) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        Map<String, Object> source = incoming == null ? Map.of() : incoming;

        normalized.put("sensorId", source.get("sensorId"));
        normalized.put("readingId", source.get("readingId"));
        normalized.put("dataType", source.getOrDefault("dataType", "unknown"));
        normalized.put("timestamp", source.getOrDefault("timestamp", Instant.now().toString()));
        normalized.put("source", source.getOrDefault("source", "threshold-alert-service"));

        Object evaluationsRaw = source.get("evaluations");
        List<Map<String, Object>> evaluations = new ArrayList<>();
        if (evaluationsRaw instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    evaluations.add((Map<String, Object>) map);
                }
            }
        }
        normalized.put("evaluations", evaluations);

        Map<String, Object> primary = pickPrimaryEvaluation(evaluations);
        if (!primary.isEmpty()) {
            normalized.put("sensorParameterId", primary.get("sensorParameterId"));
            normalized.put("parameterName", primary.get("parameterName"));
            normalized.put("value", primary.get("value"));
            normalized.put("alertCreated", primary.get("alertCreated"));
            normalized.put("alertLevel", primary.get("alertLevel"));
            normalized.put("message", primary.get("message"));
            normalized.put("alertId", primary.get("alertId"));
        } else {
            normalized.put("parameterName", "value");
            normalized.put("value", asDouble(source.get("value")));
            normalized.put("alertCreated", false);
        }

        normalized.put("alertCount", extractAlertCount(source));
        return normalized;
    }

    private Map<String, Object> pickPrimaryEvaluation(List<Map<String, Object>> evaluations) {
        if (evaluations == null || evaluations.isEmpty()) {
            return Map.of();
        }
        for (Map<String, Object> eval : evaluations) {
            if (Boolean.TRUE.equals(eval.get("alertCreated")) && eval.get("value") instanceof Number) {
                return eval;
            }
        }
        for (Map<String, Object> eval : evaluations) {
            if (eval.get("value") instanceof Number) {
                return eval;
            }
        }
        return evaluations.get(0);
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
        } catch (Exception ex) {
            return null;
        }
    }
}
