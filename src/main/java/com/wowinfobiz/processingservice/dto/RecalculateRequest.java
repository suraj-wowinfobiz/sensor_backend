package com.wowinfobiz.processingservice.dto;

import java.util.Date;
import java.util.UUID;

public class RecalculateRequest {

    private UUID sensorId;
    private Date from;
    private Date to;

    public UUID getSensorId() {
        return sensorId;
    }

    public void setSensorId(UUID sensorId) {
        this.sensorId = sensorId;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public RecalculateRequest(UUID sensorId, Date from, Date to) {
        this.sensorId = sensorId;
        this.from = from;
        this.to = to;
    }
}
