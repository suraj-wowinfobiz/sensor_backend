package com.wowinfobiz.processingservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class SensorRawDataRequest<T> {
    private String dataType;
    private UUID sensorId;
    private UUID readingId;
    private Instant timestamp;
    @JsonAlias({"parameters", "paramters"})
    private Map<String, Object> paramters;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public UUID getSensorId() {
        return sensorId;
    }

    public void setSensorId(UUID sensorId) {
        this.sensorId = sensorId;
    }

    public UUID getReadingId() {
        return readingId;
    }

    public void setReadingId(UUID readingId) {
        this.readingId = readingId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getParamters() {
        return paramters;
    }

    public void setParamters(Map<String, Object> paramters) {
        this.paramters = paramters;
    }

    public Map<String, Object> getParameters() {
        return paramters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.paramters = parameters;
    }

    public SensorRawDataRequest() {
    }

    public SensorRawDataRequest(String dataType, UUID sensorId, UUID readingId, Instant timestamp, Map<String, Object> paramters) {
        this.dataType = dataType;
        this.sensorId = sensorId;
        this.readingId = readingId;
        this.timestamp = timestamp;
        this.paramters = paramters;
    }
}
