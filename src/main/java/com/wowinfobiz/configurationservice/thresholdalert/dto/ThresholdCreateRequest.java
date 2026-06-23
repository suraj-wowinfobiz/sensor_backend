package com.wowinfobiz.configurationservice.thresholdalert.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.UUID;

public class ThresholdCreateRequest {

    private double minThresholdValue;
    @JsonAlias({"sensorParamterId"})
    private UUID sensorParameterId;
    private UUID thresholdProfileId;
    private double maxThresholdValue;

    @JsonAlias({"warrningLevel"})
    private double warningLevel;

    private double criticalLevel;

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

    public double getWarrningLevel() {
        return warningLevel;
    }

    public void setWarrningLevel(double warrningLevel) {
        this.warningLevel = warrningLevel;
    }

    public double getCriticalLevel() {
        return criticalLevel;
    }

    public void setCriticalLevel(double criticalLevel) {
        this.criticalLevel = criticalLevel;
    }


    public UUID getSensorParameterId() {
        return sensorParameterId;
    }

    public void setSensorParameterId(UUID sensorParameterId) {
        this.sensorParameterId = sensorParameterId;
    }

    public UUID getSensorParamterId() {
        return sensorParameterId;
    }

    public void setSensorParamterId(UUID sensorParamterId) {
        this.sensorParameterId = sensorParamterId;
    }

    public UUID getThresholdProfileId() {
        return thresholdProfileId;
    }

    public void setThresholdProfileId(UUID thresholdProfileId) {
        this.thresholdProfileId = thresholdProfileId;
    }
}

