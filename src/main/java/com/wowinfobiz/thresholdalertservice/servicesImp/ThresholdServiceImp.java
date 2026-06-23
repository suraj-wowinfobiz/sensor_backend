package com.wowinfobiz.thresholdalertservice.servicesImp;

import com.wowinfobiz.thresholdalertservice.dto.ThresholdProfileCreateRequest;
import com.wowinfobiz.thresholdalertservice.dto.ThresholdProfileResponse;
import com.wowinfobiz.thresholdalertservice.dto.ThresholdCreateRequest;
import com.wowinfobiz.thresholdalertservice.dto.ThresholdValueResponse;
import com.wowinfobiz.thresholdalertservice.models.ThresholdProfileEntity;
import com.wowinfobiz.thresholdalertservice.models.ThresholdValueEntity;
import com.wowinfobiz.thresholdalertservice.repository.ThresholdProfileRepository;
import com.wowinfobiz.thresholdalertservice.repository.ThresholdValueRepository;
import com.wowinfobiz.thresholdalertservice.services.ThresholdService;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ThresholdServiceImp implements ThresholdService {

    @Autowired
    private ThresholdValueRepository thresholdValueRepository;

    @Autowired
    private ThresholdProfileRepository thresholdProfileRepository;

    @Override
    public ResponseEntity<?> getThresholds() {
        List<ThresholdValueResponse> response = thresholdValueRepository.findAll()
                .stream()
                .map(this::toThresholdValueResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> getThresholdById(UUID id) {
        return thresholdValueRepository.findById(id)
                .map(this::toThresholdValueResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @Transactional
    public ResponseEntity<?> createThreshold(ThresholdCreateRequest threshold) {
        ThresholdValueEntity entity = new ThresholdValueEntity();
        entity.setThresholdValueId(UUID.randomUUID());
        entity.setSensorParameterId(threshold.getSensorParameterId());
        entity.setMinThresholdValue(threshold.getMinThresholdValue());
        entity.setMaxThresholdValue(threshold.getMaxThresholdValue());
        entity.setWarrningLevel(threshold.getWarningLevel());
        entity.setCriticalLevel(threshold.getCriticalLevel());

        if (threshold.getThresholdProfileId() != null) {
            thresholdProfileRepository.findById(threshold.getThresholdProfileId())
                    .ifPresent(entity::setThresholdProfile);
        }

        ThresholdValueEntity saved = thresholdValueRepository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toThresholdValueResponse(saved));
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateThreshold(UUID id, ThresholdCreateRequest threshold) {
        return thresholdValueRepository.findById(id).map(existing -> {
            existing.setMinThresholdValue(threshold.getMinThresholdValue());
            existing.setMaxThresholdValue(threshold.getMaxThresholdValue());
            existing.setWarrningLevel(threshold.getWarningLevel());
            existing.setCriticalLevel(threshold.getCriticalLevel());
            if (threshold.getSensorParameterId() != null) {
                existing.setSensorParameterId(threshold.getSensorParameterId());
            }
            if (threshold.getThresholdProfileId() != null) {
                thresholdProfileRepository.findById(threshold.getThresholdProfileId())
                        .ifPresent(existing::setThresholdProfile);
            }
            ThresholdValueEntity saved = thresholdValueRepository.save(existing);
            return ResponseEntity.ok(toThresholdValueResponse(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteThreshold(UUID id) {
        if (thresholdValueRepository.existsById(id)) {
            thresholdValueRepository.deleteById(id);
            return ResponseEntity.ok("Threshold deleted");
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<?> getThresholdProfiles() {
        List<ThresholdProfileResponse> response = thresholdProfileRepository.findAll()
                .stream()
                .map(this::toThresholdProfileResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> getThresholdProfileById(UUID id) {
        return thresholdProfileRepository.findById(id)
                .map(this::toThresholdProfileResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @Transactional
    public ResponseEntity<?> createThresholdProfile(ThresholdProfileCreateRequest profile) {
        if (profile == null) {
            return ResponseEntity.badRequest().body("Request body is required");
        }
        if (profile.getName() == null || profile.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Profile name is required");
        }
        ThresholdProfileEntity thresholdProfileEntity=new ThresholdProfileEntity();
        thresholdProfileEntity.setName(profile.getName());
        thresholdProfileEntity.setDescription(profile.getDescription());
        thresholdProfileEntity.setCreatedAt(new Date());
        thresholdProfileEntity.setThresholdProfileId(UUID.randomUUID());
        thresholdProfileEntity.setUpdatedAt(new Date());
        ThresholdProfileEntity saved = thresholdProfileRepository.save(thresholdProfileEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toThresholdProfileResponse(saved));
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateThresholdProfile(UUID id, ThresholdProfileCreateRequest profile) {
        if (profile == null) {
            return ResponseEntity.badRequest().body("Request body is required");
        }
        if (profile.getName() == null || profile.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Profile name is required");
        }
        return thresholdProfileRepository.findById(id).map(existing -> {
            existing.setName(profile.getName());
            existing.setDescription(profile.getDescription());
            existing.setUpdatedAt(new Date());
            ThresholdProfileEntity saved = thresholdProfileRepository.save(existing);
            return ResponseEntity.ok(toThresholdProfileResponse(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteThresholdProfile(UUID id) {
        if (thresholdProfileRepository.existsById(id)) {
            thresholdProfileRepository.deleteById(id);
            return ResponseEntity.ok("Profile deleted");
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<?> applyThreshold(UUID id, UUID sensorId) {
        return thresholdValueRepository.findById(id)
                .map(threshold -> ResponseEntity.ok("Threshold applied to sensor: " + sensorId))
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<?> bulkApplyThresholds(Map<String, Object> request) {
        return ResponseEntity.ok("Thresholds applied in bulk");
    }

    @Override
    public ResponseEntity<?> getDefaultThresholds() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("temperature", Map.of("min", 0, "max", 50, "warning", 40, "critical", 45));
        defaults.put("humidity", Map.of("min", 20, "max", 80, "warning", 70, "critical", 75));
        defaults.put("pressure", Map.of("min", 900, "max", 1100, "warning", 1050, "critical", 1080));
        return ResponseEntity.ok(defaults);
    }

    private ThresholdValueResponse toThresholdValueResponse(ThresholdValueEntity entity) {
        ThresholdValueResponse response = new ThresholdValueResponse();
        response.setThresholdValueId(entity.getThresholdValueId());
        response.setSensorParameterId(entity.getSensorParameterId());
        response.setThresholdProfileId(entity.getThresholdProfile() == null ? null : entity.getThresholdProfile().getThresholdProfileId());
        response.setMinThresholdValue(entity.getMinThresholdValue());
        response.setMaxThresholdValue(entity.getMaxThresholdValue());
        response.setWarningLevel(entity.getWarrningLevel());
        response.setCriticalLevel(entity.getCriticalLevel());
        return response;
    }

    private ThresholdProfileResponse toThresholdProfileResponse(ThresholdProfileEntity entity) {
        ThresholdProfileResponse response = new ThresholdProfileResponse();
        response.setThresholdProfileId(entity.getThresholdProfileId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        List<UUID> thresholdValueIds;
        if (entity.getThresholdValueEntities() == null || !Hibernate.isInitialized(entity.getThresholdValueEntities())) {
            thresholdValueIds = List.of();
        } else {
            thresholdValueIds = entity.getThresholdValueEntities().stream()
                    .map(ThresholdValueEntity::getThresholdValueId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        response.setThresholdValueIds(thresholdValueIds);
        return response;
    }
}
