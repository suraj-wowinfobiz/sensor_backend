package com.wowinfobiz.processingservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wowinfobiz.processingservice.dto.ProcessDataResponse;
import com.wowinfobiz.processingservice.dto.SensorRawDataRequest;
import com.wowinfobiz.processingservice.models.ProcessedSensorReadingEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProcessedReadingStoreService {

    private final ProcessedReadingCsvStoreService csvStoreService;
    private final ObjectMapper objectMapper;

    public ProcessedReadingStoreService(
            ProcessedReadingCsvStoreService csvStoreService
    ) {
        this.csvStoreService = csvStoreService;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    public void save(SensorRawDataRequest<?> request,
                     ProcessDataResponse<?> response,
                     Map<String, Object> rawPayload) {
        ProcessedSensorReadingEntity entity = new ProcessedSensorReadingEntity();
        UUID readingId = request.getReadingId();
        entity.setReadingId(readingId);
        entity.setSensorId(request.getSensorId());
        entity.setDataType(request.getDataType());
        entity.setTimestamp(request.getTimestamp() == null ? Instant.now() : request.getTimestamp());
        entity.setProcessedSuccess(response.isStatus());
        entity.setMessage(response.getMessage());
        entity.setRawPayload(writeAsJson(rawPayload));
        entity.setProcessedPayload(writeAsJson(response.getBody()));

        csvStoreService.append(entity);
    }

    public List<Map<String, Object>> findReadings(@Nullable UUID sensorId, @Nullable Instant from, @Nullable Instant to) {
        List<ProcessedSensorReadingEntity> entities = csvStoreService.findAll(sensorId, from, to);
        return entities.stream().map(this::toApiRecord).toList();
    }

    public Optional<Map<String, Object>> findReadingById(UUID readingId) {
        return csvStoreService.findByReadingId(readingId).map(this::toApiRecord);
    }

    public Optional<Map<String, Object>> findRawPayloadByReadingId(UUID readingId) {
        return csvStoreService.findByReadingId(readingId)
                .map(ProcessedSensorReadingEntity::getRawPayload)
                .map(this::readAsMap);
    }

    public List<Map<String, Object>> findRawPayloads(@Nullable UUID sensorId, @Nullable Instant from, @Nullable Instant to) {
        List<ProcessedSensorReadingEntity> entities = csvStoreService.findAll(sensorId, from, to);
        return entities.stream()
                .map(ProcessedSensorReadingEntity::getRawPayload)
                .map(this::readAsMap)
                .toList();
    }

    private String writeAsJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload == null ? Map.of() : payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize payload", ex);
        }
    }

    private Map<String, Object> readAsMap(String payload) {
        if (payload == null || payload.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(payload, new TypeReference<>() {
            });
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to deserialize stored payload", ex);
        }
    }

    private Map<String, Object> toApiRecord(ProcessedSensorReadingEntity entity) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("readingId", entity.getReadingId());
        body.put("sensorId", entity.getSensorId());
        body.put("dataType", entity.getDataType());
        body.put("timestamp", entity.getTimestamp());
        body.put("processedSuccess", entity.isProcessedSuccess());
        body.put("message", entity.getMessage());
        body.put("rawPayload", readAsMap(entity.getRawPayload()));
        body.put("processedPayload", readAsMap(entity.getProcessedPayload()));
        return body;
    }
}
