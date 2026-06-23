package com.wowinfobiz.configurationservice.thresholdalert.controller;

import com.wowinfobiz.configurationservice.thresholdalert.dto.AlertCreateRequest;
import com.wowinfobiz.configurationservice.thresholdalert.dto.AlertUpdateRequest;
import com.wowinfobiz.configurationservice.thresholdalert.dto.AssignAlertRequest;
import com.wowinfobiz.configurationservice.thresholdalert.services.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @GetMapping
    public ResponseEntity<?> getAlerts(@RequestParam(required = false) String status,
                                        @RequestParam(required = false) String alertLevel) {
        return alertService.getAlerts(status, alertLevel);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAlertById(@PathVariable UUID id) {
        return alertService.getAlertById(id);
    }

    @PostMapping
    public ResponseEntity<?> createAlert(@RequestBody AlertCreateRequest alert) {
        return alertService.createAlert(alert);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAlert(@PathVariable UUID id, @RequestBody AlertUpdateRequest alert) {
        return alertService.updateAlert(id, alert);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAlert(@PathVariable UUID id) {
        return alertService.deleteAlert(id);
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<?> resolveAlert(@PathVariable UUID id) {
        return alertService.resolveAlert(id);
    }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<?> acknowledgeAlert(@PathVariable UUID id) {
        return alertService.acknowledgeAlert(id);
    }

    @PostMapping("/{id}/escalate")
    public ResponseEntity<?> escalateAlert(@PathVariable UUID id) {
        return alertService.escalateAlert(id);
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assignAlert(@PathVariable UUID id, @RequestBody AssignAlertRequest request) {
        return alertService.assignAlert(id, request.getAssignee());
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveAlerts() {
        return alertService.getActiveAlerts();
    }

    @GetMapping("/resolved")
    public ResponseEntity<?> getResolvedAlerts() {
        return alertService.getResolvedAlerts();
    }

    @GetMapping("/history")
    public ResponseEntity<?> getAlertHistory() {
        return alertService.getAlertHistory();
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getAlertStats() {
        return alertService.getAlertStats();
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getAlertSummary() {
        return alertService.getAlertSummary();
    }

    @PostMapping("/bulk-resolve")
    public ResponseEntity<?> bulkResolveAlerts(@RequestBody java.util.List<UUID> alertIds) {
        return alertService.bulkResolveAlerts(alertIds);
    }
}

