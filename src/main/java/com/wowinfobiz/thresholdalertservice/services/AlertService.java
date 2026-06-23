package com.wowinfobiz.thresholdalertservice.services;

import com.wowinfobiz.thresholdalertservice.dto.AlertCreateRequest;
import com.wowinfobiz.thresholdalertservice.dto.AlertUpdateRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface AlertService {
    ResponseEntity<?> getAlerts(String status, String alertLevel);
    ResponseEntity<?> getAlertById(UUID id);
    ResponseEntity<?> createAlert(AlertCreateRequest alert);
    ResponseEntity<?> updateAlert(UUID id, AlertUpdateRequest alert);
    ResponseEntity<?> deleteAlert(UUID id);
    ResponseEntity<?> resolveAlert(UUID id);
    ResponseEntity<?> acknowledgeAlert(UUID id);
    ResponseEntity<?> escalateAlert(UUID id);
    ResponseEntity<?> assignAlert(UUID id, String assignee);
    ResponseEntity<?> getActiveAlerts();
    ResponseEntity<?> getResolvedAlerts();
    ResponseEntity<?> getAlertHistory();
    ResponseEntity<?> getAlertStats();
    ResponseEntity<?> getAlertSummary();
    ResponseEntity<?> bulkResolveAlerts(List<UUID> alertIds);
}
