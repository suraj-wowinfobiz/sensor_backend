package com.wowinfobiz.ingestionservice.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class SensorReading {

    private final UUID readingId;
    private final String sensorId;
    private final Instant timestamp;
    private final Map<String, Object> parameters;

    public SensorReading(UUID readingId, String sensorId, Instant timestamp, Map<String, Object> parameters) {
        this.readingId = readingId;
        this.sensorId = sensorId;
        this.timestamp = timestamp;
        this.parameters = parameters;
    }

    public UUID getReadingId() {
        return readingId;
    }

    public String getSensorId() {
        return sensorId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
