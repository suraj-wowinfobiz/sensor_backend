package com.wowinfobiz.thresholdalertservice.repository;

import com.wowinfobiz.thresholdalertservice.models.ThresholdProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository("thresholdAlertServiceThresholdProfileRepository")
public interface ThresholdProfileRepository extends JpaRepository<ThresholdProfileEntity, UUID> {
}
