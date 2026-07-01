package com.wowinfobiz.configurationservice.thresholdalert.repository;

import com.wowinfobiz.configurationservice.thresholdalert.models.ThresholdValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository("configurationThresholdValueRepository")
public interface ThresholdValueRepository extends JpaRepository<ThresholdValueEntity, UUID> {
    List<ThresholdValueEntity> findAllBySensorIdAndSensorParameterId(UUID sensorId, UUID sensorParameterId);
    List<ThresholdValueEntity> findAllBySensorIdIsNullAndSensorParameterId(UUID sensorParameterId);
}
