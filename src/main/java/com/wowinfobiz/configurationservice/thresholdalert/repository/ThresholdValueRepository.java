package com.wowinfobiz.configurationservice.thresholdalert.repository;

import com.wowinfobiz.configurationservice.thresholdalert.models.ThresholdValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository("configurationThresholdValueRepository")
public interface ThresholdValueRepository extends JpaRepository<ThresholdValueEntity, UUID> {
    Optional<ThresholdValueEntity> findBySensorParameterId(UUID sensorParameterId);
}
