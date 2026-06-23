package com.wowinfobiz.configurationservice.thresholdalert.repository;

import com.wowinfobiz.configurationservice.thresholdalert.models.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository("configurationThresholdAlertRepository")
public interface AlertRepository extends JpaRepository<AlertEntity, UUID> {
    List<AlertEntity> findByAlertLevel(String alertLevel);
    List<AlertEntity> findByResolvedAtIsNull();
    List<AlertEntity> findByResolvedAtIsNotNull();
}
