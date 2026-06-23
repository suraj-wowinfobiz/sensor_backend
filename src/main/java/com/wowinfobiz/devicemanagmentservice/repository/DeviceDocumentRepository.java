package com.wowinfobiz.devicemanagmentservice.repository;

import com.wowinfobiz.devicemanagmentservice.models.DeviceDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeviceDocumentRepository extends JpaRepository<DeviceDocument, UUID> {
    List<DeviceDocument> findBySiteId(UUID siteId);
}
