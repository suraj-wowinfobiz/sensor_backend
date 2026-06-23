package com.wowinfobiz.processingservice.repositories;

import com.wowinfobiz.processingservice.models.ProcessedSensorReadingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProcessedSensorReadingRepository extends JpaRepository<ProcessedSensorReadingEntity, UUID> {
    List<ProcessedSensorReadingEntity> findAllByOrderByTimestampDesc();

    List<ProcessedSensorReadingEntity> findBySensorIdOrderByTimestampDesc(UUID sensorId);
}
