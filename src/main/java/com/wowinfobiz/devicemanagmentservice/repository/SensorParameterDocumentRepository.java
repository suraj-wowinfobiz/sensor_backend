package com.wowinfobiz.devicemanagmentservice.repository;

import com.wowinfobiz.devicemanagmentservice.models.SensorParameterDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SensorParameterDocumentRepository extends JpaRepository<SensorParameterDocument, UUID> {
    List<SensorParameterDocument> findByTypeId(UUID typeId);
}
