package com.wowinfobiz.ingestionservice.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class SensorReadingView {

    private UUID readingId;
    private String sensorId;
    private Instant timestamp;
    private Map<String, Object> parameters;

    public SensorReadingView() {
    }

    public SensorReadingView(UUID readingId, String sensorId, Instant timestamp, Map<String, Object> parameters) {
        this.readingId = readingId;
        this.sensorId = sensorId;
        this.timestamp = timestamp;
        this.parameters = parameters;
    }

    public UUID getReadingId() {
        return readingId;
    }

    public void setReadingId(UUID readingId) {
        this.readingId = readingId;
    }

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

    @Override
    public String toString() {
        return "SensorReadingView{" +
                "readingId=" + readingId +
                ", sensorId='" + sensorId + '\'' +
                ", timestamp=" + timestamp +
                ", parameters=" + parameters +
                '}';
    }
}
