package com.wowinfobiz.configurationservice.thresholdalert.repository;

import com.wowinfobiz.configurationservice.thresholdalert.models.ThresholdProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository("configurationThresholdProfileRepository")
public interface ThresholdProfileRepository extends JpaRepository<ThresholdProfileEntity, UUID> {
}
