package com.wowinfobiz.processingservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_sensor_readings")
public class ProcessedSensorReadingEntity {

    @Id
    @Column(name = "reading_id", nullable = false)
    private UUID readingId;

    @Column(name = "sensor_id", nullable = false)
    private UUID sensorId;

    @Column(name = "data_type", nullable = false)
    private String dataType;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "processed_success", nullable = false)
    private boolean processedSuccess;

    @Column(name = "message")
    private String message;

    @Column(name = "raw_payload", columnDefinition = "TEXT", nullable = false)
    private String rawPayload;

    @Column(name = "processed_payload", columnDefinition = "TEXT", nullable = false)
    private String processedPayload;

    public UUID getReadingId() {
        return readingId;
    }

    public void setReadingId(UUID readingId) {
        this.readingId = readingId;
    }

    public UUID getSensorId() {
        return sensorId;
    }

    public void setSensorId(UUID sensorId) {
        this.sensorId = sensorId;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isProcessedSuccess() {
        return processedSuccess;
    }

    public void setProcessedSuccess(boolean processedSuccess) {
        this.processedSuccess = processedSuccess;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public String getProcessedPayload() {
        return processedPayload;
    }

    public void setProcessedPayload(String processedPayload) {
        this.processedPayload = processedPayload;
    }
}
