package com.wowinfobiz.thresholdalertservice.dto;

import java.util.UUID;

public class ThresholdValueResponse {
    private UUID thresholdValueId;
    private UUID sensorParameterId;
    private UUID thresholdProfileId;
    private double minThresholdValue;
    private double maxThresholdValue;
    private double warningLevel;
    private double criticalLevel;

    public UUID getThresholdValueId() {
        return thresholdValueId;
    }

    public void setThresholdValueId(UUID thresholdValueId) {
        this.thresholdValueId = thresholdValueId;
    }

    public UUID getSensorParameterId() {
        return sensorParameterId;
    }

    public void setSensorParameterId(UUID sensorParameterId) {
        this.sensorParameterId = sensorParameterId;
    }

    public UUID getThresholdProfileId() {
        return thresholdProfileId;
    }

    public void setThresholdProfileId(UUID thresholdProfileId) {
        this.thresholdProfileId = thresholdProfileId;
    }

    public double getMinThresholdValue() {
        return minThresholdValue;
    }

    public void setMinThresholdValue(double minThresholdValue) {
        this.minThresholdValue = minThresholdValue;
    }

    public double getMaxThresholdValue() {
        return maxThresholdValue;
    }

    public void setMaxThresholdValue(double maxThresholdValue) {
        this.maxThresholdValue = maxThresholdValue;
    }

    public double getWarningLevel() {
        return warningLevel;
    }

    public void setWarningLevel(double warningLevel) {
        this.warningLevel = warningLevel;
    }

    public double getCriticalLevel() {
        return criticalLevel;
    }

    public void setCriticalLevel(double criticalLevel) {
        this.criticalLevel = criticalLevel;
    }
}
