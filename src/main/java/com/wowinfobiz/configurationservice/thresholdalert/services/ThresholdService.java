package com.wowinfobiz.configurationservice.thresholdalert.services;

import com.wowinfobiz.configurationservice.thresholdalert.dto.ThresholdProfileCreateRequest;
import com.wowinfobiz.configurationservice.thresholdalert.dto.ThresholdCreateRequest;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

public interface ThresholdService {
    ResponseEntity<?> getThresholds();
    ResponseEntity<?> getThresholdById(UUID id);
    ResponseEntity<?> createThreshold(ThresholdCreateRequest threshold);
    ResponseEntity<?> updateThreshold(UUID id, ThresholdCreateRequest threshold);
    ResponseEntity<?> deleteThreshold(UUID id);
    ResponseEntity<?> getThresholdProfiles();
    ResponseEntity<?> getThresholdProfileById(UUID id);
    ResponseEntity<?> createThresholdProfile(ThresholdProfileCreateRequest profile);
    ResponseEntity<?> updateThresholdProfile(UUID id, ThresholdProfileCreateRequest profile);
    ResponseEntity<?> deleteThresholdProfile(UUID id);
    ResponseEntity<?> applyThreshold(UUID id, UUID sensorId);
    ResponseEntity<?> bulkApplyThresholds(Map<String, Object> request);
    ResponseEntity<?> getDefaultThresholds();
}

