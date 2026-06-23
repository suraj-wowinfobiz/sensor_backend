package com.wowinfobiz.configurationservice.thresholdalert.controller;

import com.wowinfobiz.configurationservice.thresholdalert.dto.ThresholdCreateRequest;
import com.wowinfobiz.configurationservice.thresholdalert.dto.ThresholdProfileCreateRequest;
import com.wowinfobiz.configurationservice.thresholdalert.services.ThresholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/thresholds")
public class ThresholdController {

    @Autowired
    private ThresholdService thresholdService;

    @GetMapping
    public ResponseEntity<?> getThresholds() {
        return thresholdService.getThresholds();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getThresholdById(@PathVariable UUID id) {
        return thresholdService.getThresholdById(id);
    }

    @PostMapping
    public ResponseEntity<?> createThreshold(@RequestBody ThresholdCreateRequest threshold) {
        return thresholdService.createThreshold(threshold);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateThreshold(@PathVariable UUID id, @RequestBody ThresholdCreateRequest threshold) {
        return thresholdService.updateThreshold(id, threshold);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteThreshold(@PathVariable UUID id) {
        return thresholdService.deleteThreshold(id);
    }

    @GetMapping("/profiles")
    public ResponseEntity<?> getThresholdProfiles() {
        return thresholdService.getThresholdProfiles();
    }

    @GetMapping("/profiles/{id}")
    public ResponseEntity<?> getThresholdProfileById(@PathVariable UUID id) {
        return thresholdService.getThresholdProfileById(id);
    }

    @PostMapping("/profiles")
    public ResponseEntity<?> createThresholdProfile(@RequestBody ThresholdProfileCreateRequest profile) {
        return thresholdService.createThresholdProfile(profile);
    }

    @PutMapping("/profiles/{id}")
    public ResponseEntity<?> updateThresholdProfile(@PathVariable UUID id, @RequestBody ThresholdProfileCreateRequest profile) {
        return thresholdService.updateThresholdProfile(id, profile);
    }

    @DeleteMapping("/profiles/{id}")
    public ResponseEntity<?> deleteThresholdProfile(@PathVariable UUID id) {
        return thresholdService.deleteThresholdProfile(id);
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<?> applyThreshold(@PathVariable UUID id, @RequestBody UUID sensorId) {
        return thresholdService.applyThreshold(id, sensorId);
    }

    @PostMapping("/bulk-apply")
    public ResponseEntity<?> bulkApplyThresholds(@RequestBody java.util.Map<String, Object> request) {
        return thresholdService.bulkApplyThresholds(request);
    }

    @GetMapping("/defaults")
    public ResponseEntity<?> getDefaultThresholds() {
        return thresholdService.getDefaultThresholds();
    }

}

