package com.wowinfobiz.devicemanagmentservice.services;

import com.wowinfobiz.devicemanagmentservice.dto.SensorReadingDTO;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IngestionService {
    SensorReadingDTO createReading(SensorReadingDTO request);

    List<SensorReadingDTO> createBatch(List<SensorReadingDTO> requests);

    List<SensorReadingDTO> getAllReadings();

    List<SensorReadingDTO> getReadings(UUID sensorId, Instant from, Instant to);

    SensorReadingDTO getReading(UUID readingId);

    Map<String, Object> health();
}
