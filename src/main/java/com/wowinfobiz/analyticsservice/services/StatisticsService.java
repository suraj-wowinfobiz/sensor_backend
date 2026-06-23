package com.wowinfobiz.analyticsservice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wowinfobiz.analyticsservice.entities.AnalyticsEventEntity;
import com.wowinfobiz.analyticsservice.repositories.AnalyticsEventRepository;
import com.wowinfobiz.analyticsservice.repositories.AuditLogRepository;
import com.wowinfobiz.analyticsservice.repositories.ReportRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class StatisticsService {
    private final AnalyticsEventRepository analyticsEventRepository;
    private final AuditLogRepository auditLogRepository;
    private final ReportRepository reportRepository;
    private final ObjectMapper objectMapper;

    public StatisticsService(AnalyticsEventRepository analyticsEventRepository,
                             AuditLogRepository auditLogRepository,
                             ReportRepository reportRepository,
                             ObjectMapper objectMapper) {
        this.analyticsEventRepository = analyticsEventRepository;
        this.auditLogRepository = auditLogRepository;
        this.reportRepository = reportRepository;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> overview() {
        long totalSensors = analyticsEventRepository.countDistinctSensorId();
        long totalDevices = analyticsEventRepository.findAll().stream()
                .map(this::payload)
                .map(data -> data.get("deviceId"))
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .distinct()
                .count();
        long totalUsers = auditLogRepository.findAll().stream()
                .map(log -> log.getUserId())
                .filter(Objects::nonNull)
                .distinct()
                .count();
        long totalOrganizations = analyticsEventRepository.findAll().stream()
                .map(this::payload)
                .map(data -> data.get("organizationId") == null ? data.get("orgId") : data.get("organizationId"))
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .distinct()
                .count();

        Map<String, Object> response = base();
        response.put("totalSensors", totalSensors);
        response.put("totalDevices", totalDevices);
        response.put("totalUsers", totalUsers);
        response.put("totalOrganizations", totalOrganizations);
        return response;
    }

    public Map<String, Object> sensors() {
        long totalSensors = analyticsEventRepository.countDistinctSensorId();
        long online = analyticsEventRepository.findAll().stream()
                .filter(event -> event.getReceivedAt() != null && event.getReceivedAt().isAfter(Instant.now().minus(24, ChronoUnit.HOURS)))
                .map(AnalyticsEventEntity::getSensorId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        long alertingSensors = analyticsEventRepository.findByAlertCountGreaterThanOrderByReceivedAtDesc(0L).stream()
                .map(AnalyticsEventEntity::getSensorId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        double avgHealthScore = totalSensors == 0 ? 100.0 : Math.max(0.0, 100.0 - ((alertingSensors * 100.0) / totalSensors));

        Map<String, Object> response = base();
        response.put("online", online);
        response.put("offline", Math.max(totalSensors - online, 0));
        response.put("avgHealthScore", Math.round(avgHealthScore * 10.0) / 10.0);
        return response;
    }

    public Map<String, Object> devices() {
        long totalDevices = analyticsEventRepository.findAll().stream()
                .map(this::payload)
                .map(data -> data.get("deviceId"))
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .distinct()
                .count();

        long disconnected = auditLogRepository.findAll().stream()
                .filter(log -> "DEVICE".equalsIgnoreCase(log.getResourceType()))
                .filter(log -> "DISCONNECT".equalsIgnoreCase(log.getAction()) || "OFFLINE".equalsIgnoreCase(log.getAction()))
                .count();

        long connected = Math.max(totalDevices - disconnected, 0);
        double avgUptime = totalDevices == 0 ? 100.0 : (connected * 100.0) / totalDevices;

        Map<String, Object> response = base();
        response.put("connected", connected);
        response.put("disconnected", disconnected);
        response.put("avgUptime", Math.round(avgUptime * 10.0) / 10.0);
        return response;
    }

    public Map<String, Object> alerts() {
        Instant now = Instant.now();
        long today = analyticsEventRepository.countByAlertCountGreaterThanAndReceivedAtAfter(0L, now.minus(1, ChronoUnit.DAYS));
        long weekly = analyticsEventRepository.countByAlertCountGreaterThanAndReceivedAtAfter(0L, now.minus(7, ChronoUnit.DAYS));
        long critical = analyticsEventRepository.findByAlertCountGreaterThanOrderByReceivedAtDesc(2L).size();

        Map<String, Object> response = base();
        response.put("today", today);
        response.put("weekly", weekly);
        response.put("critical", critical);
        return response;
    }

    public Map<String, Object> users() {
        long active = auditLogRepository.countByTimestampAfter(Instant.now().minus(30, ChronoUnit.DAYS));
        long distinct = auditLogRepository.findAll().stream()
                .map(log -> log.getUserId())
                .filter(Objects::nonNull)
                .distinct()
                .count();

        Map<String, Object> response = base();
        response.put("active", Math.min(active, distinct));
        response.put("inactive", Math.max(distinct - Math.min(active, distinct), 0));
        return response;
    }

    public Map<String, Object> organizations() {
        long active = analyticsEventRepository.findAll().stream()
                .map(this::payload)
                .map(data -> data.get("organizationId") == null ? data.get("orgId") : data.get("organizationId"))
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .distinct()
                .count();

        long withAlerts = analyticsEventRepository.findByAlertCountGreaterThanOrderByReceivedAtDesc(0L).stream()
                .map(this::payload)
                .map(data -> data.get("organizationId") == null ? data.get("orgId") : data.get("organizationId"))
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .distinct()
                .count();

        Map<String, Object> response = base();
        response.put("active", active);
        response.put("withAlerts", withAlerts);
        return response;
    }

    public Map<String, Object> readings() {
        long dailyReadings = analyticsEventRepository.countByReceivedAtAfter(Instant.now().minus(1, ChronoUnit.DAYS));
        long totalReadings = analyticsEventRepository.count();
        long totalSensors = analyticsEventRepository.countDistinctSensorId();
        long totalAlerts = analyticsEventRepository.countByAlertCountGreaterThan(0L);

        Map<String, Object> response = base();
        response.put("dailyReadings", dailyReadings);
        response.put("avgPerSensor", totalSensors == 0 ? 0.0 : Math.round((totalReadings / (double) totalSensors) * 100.0) / 100.0);
        response.put("dropRate", totalReadings == 0 ? 0.0 : Math.round((totalAlerts * 1.0 / totalReadings) * 10000.0) / 10000.0);
        return response;
    }

    public Map<String, Object> custom(String metric, String range) {
        Instant now = Instant.now();
        long hours = parseRangeToHours(range);
        List<Double> series = new ArrayList<>();

        for (int i = 4; i >= 0; i--) {
            Instant start = now.minus((i + 1L) * hours / 5L, ChronoUnit.HOURS);
            Instant end = now.minus(i * hours / 5L, ChronoUnit.HOURS);
            long value;
            if ("alerts".equalsIgnoreCase(metric)) {
                value = analyticsEventRepository.findByAlertCountGreaterThanOrderByReceivedAtDesc(0L).stream()
                        .filter(event -> event.getReceivedAt() != null)
                        .filter(event -> event.getReceivedAt().isAfter(start) && event.getReceivedAt().isBefore(end))
                        .count();
            } else if ("reports".equalsIgnoreCase(metric)) {
                value = reportRepository.countByCreatedAtAfter(start) - reportRepository.countByCreatedAtAfter(end);
            } else {
                value = analyticsEventRepository.findAll().stream()
                        .filter(event -> event.getReceivedAt() != null)
                        .filter(event -> event.getReceivedAt().isAfter(start) && event.getReceivedAt().isBefore(end))
                        .count();
            }
            series.add((double) value);
        }

        Map<String, Object> response = base();
        response.put("metric", metric);
        response.put("range", range);
        response.put("series", series);
        return response;
    }

    private long parseRangeToHours(String range) {
        if (range == null || range.isBlank()) {
            return 24L;
        }
        String normalized = range.trim().toLowerCase();
        try {
            if (normalized.endsWith("d")) {
                return Long.parseLong(normalized.substring(0, normalized.length() - 1)) * 24L;
            }
            if (normalized.endsWith("h")) {
                return Long.parseLong(normalized.substring(0, normalized.length() - 1));
            }
            return Long.parseLong(normalized);
        } catch (NumberFormatException ex) {
            return 24L;
        }
    }

    private Map<String, Object> payload(AnalyticsEventEntity event) {
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
