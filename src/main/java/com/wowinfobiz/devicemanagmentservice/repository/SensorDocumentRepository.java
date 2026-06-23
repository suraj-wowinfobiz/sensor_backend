package com.wowinfobiz.devicemanagmentservice.repository;

import com.wowinfobiz.devicemanagmentservice.models.SensorDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SensorDocumentRepository extends JpaRepository<SensorDocument, UUID> {
    List<SensorDocument> findByDeviceId(UUID deviceId);
}
