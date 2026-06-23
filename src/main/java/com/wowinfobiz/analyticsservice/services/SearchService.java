package com.wowinfobiz.analyticsservice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wowinfobiz.analyticsservice.entities.AnalyticsEventEntity;
import com.wowinfobiz.analyticsservice.entities.AuditLogEntity;
import com.wowinfobiz.analyticsservice.repositories.AnalyticsEventRepository;
import com.wowinfobiz.analyticsservice.repositories.AuditLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class SearchService {
    private final AnalyticsEventRepository analyticsEventRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public SearchService(AnalyticsEventRepository analyticsEventRepository,
                         AuditLogRepository auditLogRepository,
                         ObjectMapper objectMapper) {
        this.analyticsEventRepository = analyticsEventRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> search(String q, int limit) {
        int safeLimit = Math.max(limit, 1);
        List<Map<String, Object>> sensorResults = searchSensorsInternal(q, safeLimit);
        List<Map<String, Object>> deviceResults = searchDevicesInternal(q, safeLimit);
        List<Map<String, Object>> userResults = searchUsersInternal(q, safeLimit);

        List<Map<String, Object>> results = Stream.of(sensorResults, deviceResults, userResults)
                .flatMap(List::stream)
                .limit(safeLimit)
                .toList();

        Map<String, Object> response = base();
        response.put("query", q);
        response.put("limit", limit);
        response.put("results", results);
        return response;
    }

    public Map<String, Object> searchSensors(String q, int limit) {
        Map<String, Object> response = base();
        response.put("query", q);
        response.put("limit", limit);
        response.put("items", searchSensorsInternal(q, Math.max(limit, 1)));
        return response;
    }

    public Map<String, Object> searchDevices(String q, int limit) {
        Map<String, Object> response = base();
        response.put("query", q);
        response.put("limit", limit);
        response.put("items", searchDevicesInternal(q, Math.max(limit, 1)));
        return response;
    }

    public Map<String, Object> searchUsers(String q, int limit) {
        Map<String, Object> response = base();
        response.put("query", q);
        response.put("limit", limit);
        response.put("items", searchUsersInternal(q, Math.max(limit, 1)));
        return response;
    }

    public Map<String, Object> searchOrganizations(String q, int limit) {
        Map<String, Object> response = base();
        response.put("query", q);
        response.put("limit", limit);
        response.put("items", searchOrganizationsInternal(q, Math.max(limit, 1)));
        return response;
    }

    public Map<String, Object> globalSearch(String q, int limit) {
        int safeLimit = Math.max(limit, 1);
        List<Map<String, Object>> sensors = searchSensorsInternal(q, safeLimit);
        List<Map<String, Object>> devices = searchDevicesInternal(q, safeLimit);
        List<Map<String, Object>> users = searchUsersInternal(q, safeLimit);
        List<Map<String, Object>> organizations = searchOrganizationsInternal(q, safeLimit);

        Map<String, Object> response = base();
        response.put("query", q);
        response.put("limit", limit);
        response.put("summary", Map.of(
                "sensors", sensors.size(),
                "devices", devices.size(),
                "users", users.size(),
                "organizations", organizations.size()
        ));
        response.put("results", Stream.of(sensors, devices, users, organizations)
                .flatMap(List::stream)
                .limit(safeLimit)
                .toList());
        return response;
    }

    private List<Map<String, Object>> searchSensorsInternal(String q, int limit) {
        String query = normalize(q);
        return analyticsEventRepository.findAll().stream()
                .map(AnalyticsEventEntity::getSensorId)
                .filter(Objects::nonNull)
                .distinct()
                .filter(sensorId -> contains(sensorId, query))
                .limit(limit)
                .map(sensorId -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("sensorId", sensorId);
                    item.put("type", "sensor");
                    return item;
                })
                .toList();
    }

    private List<Map<String, Object>> searchDevicesInternal(String q, int limit) {
        String query = normalize(q);
        return analyticsEventRepository.searchByPayload(query.isEmpty() ? "device" : query, PageRequest.of(0, 1000)).stream()
                .map(this::payload)
                .map(event -> event.get("deviceId"))
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .distinct()
                .filter(deviceId -> contains(deviceId, query))
                .limit(limit)
                .map(deviceId -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("deviceId", deviceId);
                    item.put("type", "device");
                    return item;
                })
                .toList();
    }

    private List<Map<String, Object>> searchUsersInternal(String q, int limit) {
        String query = normalize(q);
        return auditLogRepository.findAllByOrderByTimestampDesc().stream()
                .map(AuditLogEntity::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .filter(userId -> contains(userId, query))
                .limit(limit)
                .map(userId -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("userId", userId);
                    item.put("type", "user");
                    return item;
                })
                .toList();
    }

    private List<Map<String, Object>> searchOrganizationsInternal(String q, int limit) {
        String query = normalize(q);
        return analyticsEventRepository.searchByPayload(query.isEmpty() ? "org" : query, PageRequest.of(0, 1000)).stream()
                .map(this::payload)
                .map(event -> event.get("organizationId") == null ? event.get("orgId") : event.get("organizationId"))
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .distinct()
                .filter(orgId -> contains(orgId, query))
                .limit(limit)
                .map(orgId -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("organizationId", orgId);
                    item.put("type", "organization");
                    return item;
                })
                .toList();
    }

    private boolean contains(String value, String query) {
        return query.isEmpty() || normalize(value).contains(query);
    }

    private String normalize(String input) {
        return input == null ? "" : input.toLowerCase(Locale.ROOT).trim();
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
