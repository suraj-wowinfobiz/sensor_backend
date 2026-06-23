package com.wowinfobiz.devicemanagmentservice.repository;

import com.wowinfobiz.devicemanagmentservice.models.SensorTypeDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SensorTypeDocumentRepository extends JpaRepository<SensorTypeDocument, UUID> {
}
