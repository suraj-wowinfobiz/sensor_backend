package com.wowinfobiz.processingservice.dto;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class ProcessedFeatureResponse {
    private UUID id;
    private UUID sensorId;
    private Date timestamp;
    private Map<String,Double> calculatesValue;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSensorId() {
        return sensorId;
    }

    public void setSensorId(UUID sensorId) {
        this.sensorId = sensorId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Double> getCalculatesValue() {
        return calculatesValue;
    }

    public void setCalculatesValue(Map<String, Double> calculatesValue) {
        this.calculatesValue = calculatesValue;
    }

    public ProcessedFeatureResponse(UUID id, UUID sensorId, Date timestamp, Map<String, Double> calculatesValue) {
        this.id = id;
        this.sensorId = sensorId;
        this.timestamp = timestamp;
        this.calculatesValue = calculatesValue;
    }
}
