package com.wowinfobiz.ingestionservice.dto;

import java.time.Instant;
import java.util.Map;

public class SensorReadingRequest {

    private String sensorId;
    private Instant timestamp;
    private Map<String, Object> parameters;

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
