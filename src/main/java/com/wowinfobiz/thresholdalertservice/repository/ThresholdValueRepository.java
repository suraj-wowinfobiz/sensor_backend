package com.wowinfobiz.thresholdalertservice.repository;

import com.wowinfobiz.thresholdalertservice.models.ThresholdValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository("thresholdAlertServiceThresholdValueRepository")
public interface ThresholdValueRepository extends JpaRepository<ThresholdValueEntity, UUID> {
    Optional<ThresholdValueEntity> findBySensorParameterId(UUID sensorParameterId);
}
