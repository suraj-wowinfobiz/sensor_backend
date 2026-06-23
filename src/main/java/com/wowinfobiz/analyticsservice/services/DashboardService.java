package com.wowinfobiz.analyticsservice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wowinfobiz.analyticsservice.entities.AnalyticsEventEntity;
import com.wowinfobiz.analyticsservice.entities.AuditLogEntity;
import com.wowinfobiz.analyticsservice.repositories.AnalyticsEventRepository;
import com.wowinfobiz.analyticsservice.repositories.AuditLogRepository;
import com.wowinfobiz.analyticsservice.repositories.ReportRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class DashboardService {
    private final AnalyticsEventRepository analyticsEventRepository;
    private final AuditLogRepository auditLogRepository;
    private final ReportRepository reportRepository;
    private final ObjectMapper objectMapper;

    public DashboardService(AnalyticsEventRepository analyticsEventRepository,
                            AuditLogRepository auditLogRepository,
                            ReportRepository reportRepository,
                            ObjectMapper objectMapper) {
        this.analyticsEventRepository = analyticsEventRepository;
        this.auditLogRepository = auditLogRepository;
        this.reportRepository = reportRepository;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> getStats() {
        Instant now = Instant.now();
        long activeSensors = analyticsEventRepository.countDistinctSensorId();
        long onlineDevices = extractDistinctDevices(analyticsEventRepository.findAll()).size();
        long openAlerts = analyticsEventRepository.countByAlertCountGreaterThanAndReceivedAtAfter(0L, now.minus(24, ChronoUnit.HOURS));

        Map<String, Object> response = base();
        response.put("activeSensors", activeSensors);
        response.put("onlineDevices", onlineDevices);
        response.put("openAlerts", openAlerts);
        return response;
    }

    public Map<String, Object> getOverview() {
        long totalEvents = analyticsEventRepository.count();
        long alerts = analyticsEventRepository.countByAlertCountGreaterThan(0L);
        double healthyPercent = totalEvents == 0 ? 100.0 : Math.max(0.0, 100.0 - ((alerts * 100.0) / totalEvents));

        Map<String, Object> response = base();
        response.put("summary", "Construction monitoring dashboard overview from persisted records");
        response.put("healthySensorsPercent", Math.round(healthyPercent * 10.0) / 10.0);
        return response;
    }

    public Map<String, Object> getRecentAlerts() {
        List<Map<String, Object>> items = analyticsEventRepository
                .findByAlertCountGreaterThanOrderByReceivedAtDesc(0L, PageRequest.of(0, 20))
                .stream()
                .map(this::toAlertItem)
                .toList();

        Map<String, Object> response = base();
        response.put("items", items);
        return response;
    }

    public Map<String, Object> getSensorStatus() {
        long totalSensors = analyticsEventRepository.countDistinctSensorId();
        long online = analyticsEventRepository.findAll().stream()
                .filter(event -> event.getReceivedAt() != null && event.getReceivedAt().isAfter(Instant.now().minus(24, ChronoUnit.HOURS)))
                .map(AnalyticsEventEntity::getSensorId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        long maintenance = analyticsEventRepository.searchByPayload("maintenance", PageRequest.of(0, 500)).size();

        Map<String, Object> response = base();
        response.put("online", online);
        response.put("offline", Math.max(totalSensors - online, 0));
        response.put("maintenance", maintenance);
        return response;
    }

    public Map<String, Object> getDeviceStatus() {
        long connected = extractDistinctDevices(analyticsEventRepository.findAll()).size();
        long disconnected = auditLogRepository.findAll().stream()
                .filter(log -> "DEVICE".equalsIgnoreCase(log.getResourceType()))
                .filter(log -> "DISCONNECT".equalsIgnoreCase(log.getAction()) || "OFFLINE".equalsIgnoreCase(log.getAction()))
                .count();

        Map<String, Object> response = base();
        response.put("connected", connected);
        response.put("disconnected", disconnected);
        return response;
    }

    public Map<String, Object> getSystemHealth() {
        long eventsLastHour = analyticsEventRepository.countByReceivedAtAfter(Instant.now().minus(1, ChronoUnit.HOURS));
        long alertsLastHour = analyticsEventRepository.countByAlertCountGreaterThanAndReceivedAtAfter(0L, Instant.now().minus(1, ChronoUnit.HOURS));

        Map<String, Object> response = base();
        response.put("status", alertsLastHour > eventsLastHour / 2 ? "DEGRADED" : "HEALTHY");
        response.put("eventsLastHour", eventsLastHour);
        response.put("alertsLastHour", alertsLastHour);
        response.put("reportsToday", reportRepository.countByCreatedAtAfter(Instant.now().minus(1, ChronoUnit.DAYS)));
        return response;
    }

    public Map<String, Object> getTiltReadingsChart() {
        List<Double> series = analyticsEventRepository.findAllByOrderByReceivedAtDesc(PageRequest.of(0, 50)).stream()
                .map(this::eventPayload)
                .map(payload -> payload.get("value"))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(Number::doubleValue)
                .toList();

        Map<String, Object> response = base();
        response.put("series", series);
        response.put("unit", "degrees");
        return response;
    }

    public Map<String, Object> getSensorDistributionChart() {
        long total = analyticsEventRepository.countDistinctSensorId();
        long withAlerts = analyticsEventRepository.findByAlertCountGreaterThanOrderByReceivedAtDesc(0L).stream()
                .map(AnalyticsEventEntity::getSensorId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        Map<String, Object> response = base();
        Map<String, Object> distribution = new LinkedHashMap<>();
        distribution.put("normal", Math.max(total - withAlerts, 0));
        distribution.put("alerting", withAlerts);
        response.put("distribution", distribution);
        return response;
    }

    public Map<String, Object> getAlertsTrendChart() {
        List<Long> dailyCounts = new ArrayList<>();
        Instant now = Instant.now();
        for (int i = 6; i >= 0; i--) {
            Instant start = now.minus(i + 1L, ChronoUnit.DAYS);
            Instant end = now.minus(i, ChronoUnit.DAYS);
            long count = analyticsEventRepository.findByAlertCountGreaterThanOrderByReceivedAtDesc(0L).stream()
                    .filter(event -> event.getReceivedAt() != null)
                    .filter(event -> event.getReceivedAt().isAfter(start) && event.getReceivedAt().isBefore(end))
                    .count();
            dailyCounts.add(count);
        }

        Map<String, Object> response = base();
        response.put("dailyCounts", dailyCounts);
        return response;
    }

    public Map<String, Object> getDeviceUptimeChart() {
        List<Map<String, Object>> devices = extractDistinctDevices(analyticsEventRepository.findAll()).stream()
                .limit(20)
                .map(deviceId -> {
                    long total = analyticsEventRepository.searchByPayload(deviceId, PageRequest.of(0, 1000)).size();
                    long alerted = analyticsEventRepository.searchByPayload(deviceId, PageRequest.of(0, 1000)).stream()
                            .filter(event -> event.getAlertCount() != null && event.getAlertCount() > 0)
                            .count();
                    double uptime = total == 0 ? 100.0 : Math.max(0.0, 100.0 - ((alerted * 100.0) / total));

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("deviceId", deviceId);
                    item.put("uptime", Math.round(uptime * 10.0) / 10.0);
                    return item;
                })
                .toList();

        Map<String, Object> response = base();
        response.put("devices", devices);
        return response;
    }

    public Map<String, Object> getActivityFeed() {
        List<Map<String, Object>> events = auditLogRepository.findAllByOrderByTimestampDesc().stream()
                .limit(20)
                .map(this::auditToActivity)
                .toList();

        Map<String, Object> response = base();
        response.put("events", events);
        return response;
    }

    public Map<String, Object> getQuickStats() {
        Instant now = Instant.now();
        long todayAlerts = analyticsEventRepository.countByAlertCountGreaterThanAndReceivedAtAfter(0L, now.minus(1, ChronoUnit.DAYS));
        long criticalAlerts = analyticsEventRepository.findByAlertCountGreaterThanOrderByReceivedAtDesc(2L).size();
        long actionsToday = auditLogRepository.countByTimestampAfter(now.minus(1, ChronoUnit.DAYS));

        Map<String, Object> response = base();
        response.put("todayAlerts", todayAlerts);
        response.put("criticalAlerts", criticalAlerts);
        response.put("activityToday", actionsToday);
        return response;
    }

    private Map<String, Object> toAlertItem(AnalyticsEventEntity event) {
        Map<String, Object> payload = eventPayload(event);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("alertId", event.getId());
        item.put("severity", payload.getOrDefault("severity", (event.getAlertCount() != null && event.getAlertCount() > 2) ? "HIGH" : "MEDIUM"));
        item.put("message", payload.getOrDefault("message", "Alert generated from persisted analytics event"));
        item.put("sensorId", event.getSensorId());
        item.put("time", event.getReceivedAt());
        return item;
    }

    private Map<String, Object> auditToActivity(AuditLogEntity log) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("type", log.getAction());
        item.put("message", log.getDescription());
        item.put("time", log.getTimestamp());
        return item;
    }

    private List<String> extractDistinctDevices(List<AnalyticsEventEntity> events) {
        return events.stream()
                .map(this::eventPayload)
                .map(payload -> payload.get("deviceId"))
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    private Map<String, Object> eventPayload(AnalyticsEventEntity event) {
        try {
            return objectMapper.readValue(event.getPayloadJson(), new TypeReference<>() {
            });
        } catch (Exception ex) {
            return Map.of();
        }
    }

    private Map<String, Object> base() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("generatedAt", Instant.now());
        return response;
    }
}
