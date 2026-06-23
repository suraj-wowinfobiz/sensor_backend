package com.wowinfobiz.ingestionservice.dto;

import java.util.UUID;

public class SensorReadingResponse {

    private String status;
    private String message;
    private UUID readingId;

    public SensorReadingResponse() {
    }

    public SensorReadingResponse(String status, String message, UUID readingId) {
        this.status = status;
        this.message = message;
        this.readingId = readingId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getReadingId() {
        return readingId;
    }

    public void setReadingId(UUID readingId) {
        this.readingId = readingId;
    }
}
