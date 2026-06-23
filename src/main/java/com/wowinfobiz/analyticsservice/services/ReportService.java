package com.wowinfobiz.analyticsservice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wowinfobiz.analyticsservice.entities.ReportEntity;
import com.wowinfobiz.analyticsservice.entities.ReportScheduleEntity;
import com.wowinfobiz.analyticsservice.entities.ReportTemplateEntity;
import com.wowinfobiz.analyticsservice.repositories.ReportRepository;
import com.wowinfobiz.analyticsservice.repositories.ReportScheduleRepository;
import com.wowinfobiz.analyticsservice.repositories.ReportTemplateRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final ReportTemplateRepository reportTemplateRepository;
    private final ReportScheduleRepository reportScheduleRepository;
    private final ObjectMapper objectMapper;

    public ReportService(ReportRepository reportRepository,
                         ReportTemplateRepository reportTemplateRepository,
                         ReportScheduleRepository reportScheduleRepository,
                         ObjectMapper objectMapper) {
        this.reportRepository = reportRepository;
        this.reportTemplateRepository = reportTemplateRepository;
        this.reportScheduleRepository = reportScheduleRepository;
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> getReports() {
        return reportRepository.findAll().stream().map(this::toReportMap).toList();
    }

    public Map<String, Object> getReportById(String id) {
        ReportEntity report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found: " + id));
        return toReportMap(report);
    }

    public Map<String, Object> generateReport(Map<String, Object> payload) {
        ReportEntity report = new ReportEntity();
        report.setId(UUID.randomUUID().toString());
        report.setStatus("generated");
        report.setCreatedAt(Instant.now());
        report.setUpdatedAt(Instant.now());
        report.setRequestJson(toJson(payload));
        reportRepository.save(report);
        return toReportMap(report);
    }

    public Map<String, Object> downloadReport(String id) {
        ReportEntity report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found: " + id));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("reportId", id);
        response.put("downloadUrl", "/api/v1/reports/" + id + "/download");
        response.put("status", report.getStatus());
        response.put("generatedAt", Instant.now());
        return response;
    }

    public Map<String, Object> deleteReport(String id) {
        ReportEntity report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found: " + id));
        reportRepository.delete(report);
        return Map.of("deleted", true, "id", id);
    }

    public List<Map<String, Object>> getTemplates() {
        return reportTemplateRepository.findAll().stream().map(this::toTemplateMap).toList();
    }

    public Map<String, Object> getTemplateById(String id) {
        ReportTemplateEntity template = reportTemplateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found: " + id));
        return toTemplateMap(template);
    }

    public Map<String, Object> createTemplate(Map<String, Object> payload) {
        ReportTemplateEntity template = new ReportTemplateEntity();
        template.setId(UUID.randomUUID().toString());
        template.setName(payload == null ? "Unnamed Template" : String.valueOf(payload.getOrDefault("name", "Unnamed Template")));
        template.setConfigJson(toJson(payload));
        template.setCreatedAt(Instant.now());
        template.setUpdatedAt(Instant.now());
        reportTemplateRepository.save(template);
        return toTemplateMap(template);
    }

    public Map<String, Object> updateTemplate(String id, Map<String, Object> payload) {
        ReportTemplateEntity template = reportTemplateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found: " + id));

        if (payload != null && payload.containsKey("name")) {
            template.setName(String.valueOf(payload.get("name")));
        }
        template.setConfigJson(toJson(payload == null ? fromJson(template.getConfigJson()) : payload));
        template.setUpdatedAt(Instant.now());

        reportTemplateRepository.save(template);
        return toTemplateMap(template);
    }

    public Map<String, Object> deleteTemplate(String id) {
        ReportTemplateEntity template = reportTemplateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found: " + id));
        reportTemplateRepository.delete(template);
        return Map.of("deleted", true, "id", id);
    }

    public Map<String, Object> scheduleReport(Map<String, Object> payload) {
        ReportScheduleEntity schedule = new ReportScheduleEntity();
        schedule.setId(UUID.randomUUID().toString());
        schedule.setStatus("scheduled");
        schedule.setCreatedAt(Instant.now());
        schedule.setScheduleJson(toJson(payload));
        reportScheduleRepository.save(schedule);
        return toScheduleMap(schedule);
    }

    public List<Map<String, Object>> getScheduledReports() {
        return reportScheduleRepository.findAll().stream().map(this::toScheduleMap).toList();
    }

    private Map<String, Object> toReportMap(ReportEntity entity) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("id", entity.getId());
        report.put("status", entity.getStatus());
        report.put("createdAt", entity.getCreatedAt());
        report.put("updatedAt", entity.getUpdatedAt());
        report.put("request", fromJson(entity.getRequestJson()));
        return report;
    }

    private Map<String, Object> toTemplateMap(ReportTemplateEntity entity) {
        Map<String, Object> template = new LinkedHashMap<>();
        template.put("id", entity.getId());
        template.put("name", entity.getName());
        template.put("config", fromJson(entity.getConfigJson()));
        template.put("createdAt", entity.getCreatedAt());
        template.put("updatedAt", entity.getUpdatedAt());
        return template;
    }

    private Map<String, Object> toScheduleMap(ReportScheduleEntity entity) {
        Map<String, Object> schedule = new LinkedHashMap<>();
        schedule.put("id", entity.getId());
        schedule.put("status", entity.getStatus());
        schedule.put("createdAt", entity.getCreatedAt());
        schedule.put("schedule", fromJson(entity.getScheduleJson()));
        return schedule;
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload == null ? Map.of() : payload);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to serialize JSON payload", ex);
        }
    }

    private Map<String, Object> fromJson(String json) {
        try {
            return objectMapper.readValue(json == null ? "{}" : json, new TypeReference<>() {
            });
        } catch (Exception ex) {
            return Map.of();
        }
    }
}
