package com.wowinfobiz.thresholdalertservice.repository;

import com.wowinfobiz.thresholdalertservice.models.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository("thresholdAlertServiceAlertRepository")
public interface AlertRepository extends JpaRepository<AlertEntity, UUID> {
    List<AlertEntity> findByAlertLevel(String alertLevel);
    List<AlertEntity> findByResolvedAtIsNull();
    List<AlertEntity> findByResolvedAtIsNotNull();
    List<AlertEntity> findBySensorIdAndSensorParameterIdAndResolvedAtIsNull(UUID sensorId, UUID sensorParameterId);
    List<AlertEntity> findBySensorParameterIdAndResolvedAtIsNull(UUID sensorParameterId);
}
