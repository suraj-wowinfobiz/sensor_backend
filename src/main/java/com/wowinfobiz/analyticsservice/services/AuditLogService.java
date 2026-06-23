package com.wowinfobiz.analyticsservice.services;

import com.wowinfobiz.analyticsservice.entities.AuditLogEntity;
import com.wowinfobiz.analyticsservice.repositories.AuditLogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public List<Map<String, Object>> getAll() {
        return auditLogRepository.findAll().stream()
                .sorted((left, right) -> right.getTimestamp().compareTo(left.getTimestamp()))
                .map(this::toMap)
                .toList();
    }

    public Map<String, Object> getById(String id) {
        AuditLogEntity log = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Audit log not found: " + id));
        return toMap(log);
    }

    public Map<String, Object> export() {
        List<Map<String, Object>> items = getAll();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("generatedAt", Instant.now());
        response.put("count", items.size());
        response.put("items", items);
        return response;
    }

    public Map<String, Object> stats() {
        List<AuditLogEntity> logs = auditLogRepository.findAll();
        Map<String, Long> byAction = logs.stream()
                .collect(Collectors.groupingBy(AuditLogEntity::getAction, Collectors.counting()));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("total", logs.size());
        response.put("byAction", byAction);
        response.put("generatedAt", Instant.now());
        return response;
    }

    public List<Map<String, Object>> getByUserId(String userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId)
                .stream()
                .map(this::toMap)
                .toList();
    }

    public List<Map<String, Object>> getByResource(String resourceType, String resourceId) {
        return auditLogRepository.findByResourceTypeIgnoreCaseAndResourceIdOrderByTimestampDesc(resourceType, resourceId)
                .stream()
                .map(this::toMap)
                .toList();
    }

    public Map<String, Object> deleteById(String id) {
        AuditLogEntity log = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Audit log not found: " + id));
        auditLogRepository.delete(log);
        return Map.of("deleted", true, "id", id);
    }

    public void createLog(String userId, String resourceType, String resourceId, String action, String description) {
        AuditLogEntity log = new AuditLogEntity();
        log.setId(UUID.randomUUID().toString());
        log.setUserId(userId);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setAction(action);
        log.setDescription(description);
        log.setTimestamp(Instant.now());
        auditLogRepository.save(log);
    }

    private Map<String, Object> toMap(AuditLogEntity log) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", log.getId());
        data.put("userId", log.getUserId());
        data.put("resourceType", log.getResourceType());
        data.put("resourceId", log.getResourceId());
        data.put("action", log.getAction());
        data.put("description", log.getDescription());
        data.put("timestamp", log.getTimestamp());
        return data;
    }
}
