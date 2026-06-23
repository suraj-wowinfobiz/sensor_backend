package com.wowinfobiz.devicemanagmentservice.controller;

import com.wowinfobiz.devicemanagmentservice.dto.SensorReadingDTO;
import com.wowinfobiz.devicemanagmentservice.services.IngestionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/device/ingestion")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/readings")
    public ResponseEntity<SensorReadingDTO> createReading(@RequestBody SensorReadingDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ingestionService.createReading(request));
    }

    @PostMapping("/readings/batch")
    public ResponseEntity<List<SensorReadingDTO>> createBatch(@RequestBody List<SensorReadingDTO> requests) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ingestionService.createBatch(requests));
    }

    @GetMapping("/readings/get-all")
    public ResponseEntity<List<SensorReadingDTO>> getAllReadings() {
        return ResponseEntity.ok(ingestionService.getAllReadings());
    }

    @GetMapping("/readings")
    public ResponseEntity<List<SensorReadingDTO>> getReadings(
            @RequestParam UUID sensorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return ResponseEntity.ok(ingestionService.getReadings(sensorId, from, to));
    }

    @GetMapping("/readings/{readingId}")
    public ResponseEntity<SensorReadingDTO> getReading(@PathVariable UUID readingId) {
        return ResponseEntity.ok(ingestionService.getReading(readingId));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(ingestionService.health());
    }
}
