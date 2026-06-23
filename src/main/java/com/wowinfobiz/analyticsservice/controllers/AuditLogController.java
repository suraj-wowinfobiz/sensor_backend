package com.wowinfobiz.analyticsservice.controllers;

import com.wowinfobiz.analyticsservice.services.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {
    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAuditLogs() {
        return ResponseEntity.ok(auditLogService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAuditLogById(@PathVariable String id) {
        return ResponseEntity.ok(auditLogService.getById(id));
    }

    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportAuditLogs() {
        return ResponseEntity.ok(auditLogService.export());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAuditLogStats() {
        return ResponseEntity.ok(auditLogService.stats());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getAuditLogsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(auditLogService.getByUserId(userId));
    }

    @GetMapping("/resource/{resourceType}/{resourceId}")
    public ResponseEntity<List<Map<String, Object>>> getAuditLogsByResource(@PathVariable String resourceType,
                                                                             @PathVariable String resourceId) {
        return ResponseEntity.ok(auditLogService.getByResource(resourceType, resourceId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAuditLog(@PathVariable String id) {
        return ResponseEntity.ok(auditLogService.deleteById(id));
    }
}
