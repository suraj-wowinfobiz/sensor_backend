package com.wowinfobiz.analyticsservice.controllers;

import com.wowinfobiz.analyticsservice.services.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getReports() {
        return ResponseEntity.ok(reportService.getReports());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReportById(@PathVariable String id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateReport(@RequestBody(required = false) Map<String, Object> payload) {
        return ResponseEntity.ok(reportService.generateReport(payload));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Map<String, Object>> downloadReport(@PathVariable String id) {
        return ResponseEntity.ok(reportService.downloadReport(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReport(@PathVariable String id) {
        return ResponseEntity.ok(reportService.deleteReport(id));
    }

    @GetMapping("/templates")
    public ResponseEntity<List<Map<String, Object>>> getTemplates() {
        return ResponseEntity.ok(reportService.getTemplates());
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<Map<String, Object>> getTemplateById(@PathVariable String id) {
        return ResponseEntity.ok(reportService.getTemplateById(id));
    }

    @PostMapping("/templates")
    public ResponseEntity<Map<String, Object>> createTemplate(@RequestBody(required = false) Map<String, Object> payload) {
        return ResponseEntity.ok(reportService.createTemplate(payload));
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<Map<String, Object>> updateTemplate(@PathVariable String id,
                                                               @RequestBody(required = false) Map<String, Object> payload) {
        return ResponseEntity.ok(reportService.updateTemplate(id, payload));
    }

    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Map<String, Object>> deleteTemplate(@PathVariable String id) {
        return ResponseEntity.ok(reportService.deleteTemplate(id));
    }

    @PostMapping("/schedule")
    public ResponseEntity<Map<String, Object>> scheduleReport(@RequestBody(required = false) Map<String, Object> payload) {
        return ResponseEntity.ok(reportService.scheduleReport(payload));
    }

    @GetMapping("/scheduled")
    public ResponseEntity<List<Map<String, Object>>> getScheduledReports() {
        return ResponseEntity.ok(reportService.getScheduledReports());
    }
}
