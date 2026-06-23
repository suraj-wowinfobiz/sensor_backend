package com.wowinfobiz.devicemanagmentservice.repository;

import com.wowinfobiz.devicemanagmentservice.models.ReadingDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReadingDocumentRepository extends JpaRepository<ReadingDocument, UUID> {
    List<ReadingDocument> findBySensorId(UUID sensorId);

    List<ReadingDocument> findBySensorIdAndReadingTimeBetween(UUID sensorId, Instant from, Instant to);
}
