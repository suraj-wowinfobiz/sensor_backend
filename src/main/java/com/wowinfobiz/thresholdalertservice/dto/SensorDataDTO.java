package com.wowinfobiz.thresholdalertservice.dto;

import java.util.UUID;

public class SensorDataDTO {
    private UUID sensorId;
    private UUID sensorParameterId;
    private double value;
    private String parameterName;

    public UUID getSensorId() {
        return sensorId;
    }

    public void setSensorId(UUID sensorId) {
        this.sensorId = sensorId;
    }

    public UUID getSensorParameterId() {
        return sensorParameterId;
    }

    public void setSensorParameterId(UUID sensorParameterId) {
        this.sensorParameterId = sensorParameterId;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }
}
