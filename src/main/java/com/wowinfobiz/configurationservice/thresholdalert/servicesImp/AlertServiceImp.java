package com.wowinfobiz.configurationservice.thresholdalert.servicesImp;

import com.wowinfobiz.configurationservice.thresholdalert.dto.AlertCreateRequest;
import com.wowinfobiz.configurationservice.thresholdalert.dto.AlertResponse;
import com.wowinfobiz.configurationservice.thresholdalert.dto.AlertUpdateRequest;
import com.wowinfobiz.configurationservice.thresholdalert.models.AlertEntity;
import com.wowinfobiz.configurationservice.thresholdalert.repository.AlertRepository;
import com.wowinfobiz.configurationservice.thresholdalert.services.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlertServiceImp implements AlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Override
    public ResponseEntity<?> getAlerts(String status, String alertLevel) {
        List<AlertEntity> alerts = alertRepository.findAll();
        if (status != null && status.equals("active")) {
            alerts = alerts.stream().filter(a -> a.getResolvedAt() == null).collect(Collectors.toList());
        }
        if (alertLevel != null) {
            alerts = alerts.stream().filter(a -> alertLevel.equalsIgnoreCase(a.getAlertLevel())).collect(Collectors.toList());
        }
        return ResponseEntity.ok(alerts.stream().map(this::toResponse).toList());
    }

    @Override
    public ResponseEntity<?> getAlertById(UUID id) {
        return alertRepository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<?> createAlert(AlertCreateRequest alert) {
        AlertEntity entity = new AlertEntity();
        entity.setAlertId(UUID.randomUUID());
        entity.setSensorId(alert.getSensorId());
        entity.setSensorParameterId(alert.getSensorParameterId());
        entity.setAlertLevel(alert.getAlertLevel());
        entity.setMessage(alert.getMessage());
        entity.setAssignedTo(alert.getAssignedTo());
        entity.setTriggeredAt(new Date());
        entity.setStatus("ACTIVE");
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(alertRepository.save(entity)));
    }

    @Override
    public ResponseEntity<?> updateAlert(UUID id, AlertUpdateRequest alert) {
        return alertRepository.findById(id).map(existing -> {
            if (alert.getMessage() != null) existing.setMessage(alert.getMessage());
            if (alert.getAlertLevel() != null) existing.setAlertLevel(alert.getAlertLevel());
            if (alert.getStatus() != null) existing.setStatus(alert.getStatus());
            if (alert.getAssignedTo() != null) existing.setAssignedTo(alert.getAssignedTo());
            return ResponseEntity.ok(toResponse(alertRepository.save(existing)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<?> deleteAlert(UUID id) {
        if (alertRepository.existsById(id)) {
            alertRepository.deleteById(id);
            return ResponseEntity.ok("Alert deleted");
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<?> resolveAlert(UUID id) {
        return alertRepository.findById(id).map(alert -> {
            alert.setResolvedAt(new Date());
            alert.setStatus("RESOLVED");
            return ResponseEntity.ok(toResponse(alertRepository.save(alert)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<?> acknowledgeAlert(UUID id) {
        return alertRepository.findById(id).map(alert -> {
            alert.setAcknowledgedAt(new Date());
            alert.setStatus("ACKNOWLEDGED");
            return ResponseEntity.ok(toResponse(alertRepository.save(alert)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<?> escalateAlert(UUID id) {
        return alertRepository.findById(id).map(alert -> {
            alert.setAlertLevel("CRITICAL");
            alert.setStatus("ESCALATED");
            return ResponseEntity.ok(toResponse(alertRepository.save(alert)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<?> assignAlert(UUID id, String assignee) {
        return alertRepository.findById(id).map(alert -> {
            alert.setAssignedTo(assignee);
            alert.setStatus("ASSIGNED");
            return ResponseEntity.ok(toResponse(alertRepository.save(alert)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<?> getActiveAlerts() {
        return ResponseEntity.ok(alertRepository.findByResolvedAtIsNull().stream().map(this::toResponse).toList());
    }

    @Override
    public ResponseEntity<?> getResolvedAlerts() {
        return ResponseEntity.ok(alertRepository.findByResolvedAtIsNotNull().stream().map(this::toResponse).toList());
    }

    @Override
    public ResponseEntity<?> getAlertHistory() {
        return ResponseEntity.ok(alertRepository.findAll().stream().map(this::toResponse).toList());
    }

    @Override
    public ResponseEntity<?> getAlertStats() {
        List<AlertEntity> all = alertRepository.findAll();
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", all.size());
        stats.put("active", all.stream().filter(a -> a.getResolvedAt() == null).count());
        stats.put("resolved", all.stream().filter(a -> a.getResolvedAt() != null).count());
        stats.put("critical", all.stream().filter(a -> "CRITICAL".equals(a.getAlertLevel())).count());
        stats.put("warning", all.stream().filter(a -> "WARNING".equals(a.getAlertLevel())).count());
        return ResponseEntity.ok(stats);
    }

    @Override
    public ResponseEntity<?> getAlertSummary() {
        List<AlertEntity> active = alertRepository.findByResolvedAtIsNull();
        Map<String, Object> summary = new HashMap<>();
        summary.put("activeCount", active.size());
        summary.put("criticalCount", active.stream().filter(a -> "CRITICAL".equals(a.getAlertLevel())).count());
        summary.put("recentAlerts", active.stream().limit(10).map(this::toResponse).collect(Collectors.toList()));
        return ResponseEntity.ok(summary);
    }

    @Override
    public ResponseEntity<?> bulkResolveAlerts(List<UUID> alertIds) {
        alertIds.forEach(id -> alertRepository.findById(id).ifPresent(alert -> {
            alert.setResolvedAt(new Date());
            alert.setStatus("RESOLVED");
            alertRepository.save(alert);
        }));
        return ResponseEntity.ok("Alerts resolved");
    }

    private AlertResponse toResponse(AlertEntity entity) {
        AlertResponse response = new AlertResponse();
        response.setAlertId(entity.getAlertId());
        response.setSensorId(entity.getSensorId());
        response.setSensorParameterId(entity.getSensorParameterId());
        response.setAlertLevel(entity.getAlertLevel());
        response.setMessage(entity.getMessage());
        response.setTriggeredAt(entity.getTriggeredAt());
        response.setResolvedAt(entity.getResolvedAt());
        response.setAcknowledgedAt(entity.getAcknowledgedAt());
        response.setAssignedTo(entity.getAssignedTo());
        response.setStatus(entity.getStatus());
        return response;
    }
}

