package com.wowinfobiz.thresholdalertservice.models;


import jakarta.persistence.*;

import java.util.UUID;

@Entity(name = "thresholdAlertServiceThresholdValueEntity")
@Table(name = "threshold_values")
public class ThresholdValueEntity {

    @Id
    @Column(name = "threshold_value_id")
    private UUID thresholdValueId;

    @Column(name = "sensor_parameter_id")
    private UUID sensorParameterId;

    @ManyToOne
    @JoinColumn(name = "threshold_profile_id")
    private ThresholdProfileEntity thresholdProfile;

    @Column(name = "min_threshold_value")
    private double minThresholdValue;

    @Column(name = "max_threshold_value")
    private double maxThresholdValue;

    @Column(name = "warning_level")
    private double warrningLevel;

    @Column(name = "critical_level")
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

    public double getWarrningLevel() {
        return warrningLevel;
    }

    public void setWarrningLevel(double warrningLevel) {
        this.warrningLevel = warrningLevel;
    }

    public double getCriticalLevel() {
        return criticalLevel;
    }

    public void setCriticalLevel(double criticalLevel) {
        this.criticalLevel = criticalLevel;
    }

    public ThresholdProfileEntity getThresholdProfile() {
        return thresholdProfile;
    }

    public void setThresholdProfile(ThresholdProfileEntity thresholdProfile) {
        this.thresholdProfile = thresholdProfile;
    }
    public ThresholdValueEntity(){
        super();
    }

    public ThresholdValueEntity(UUID thresholdValueId, UUID sensorParameterId, ThresholdProfileEntity thresholdProfile, double minThresholdValue, double maxThresholdValue, double warrningLevel, double criticalLevel) {
        this.thresholdValueId = thresholdValueId;
        this.sensorParameterId = sensorParameterId;
        this.thresholdProfile = thresholdProfile;
        this.minThresholdValue = minThresholdValue;
        this.maxThresholdValue = maxThresholdValue;
        this.warrningLevel = warrningLevel;
        this.criticalLevel = criticalLevel;
    }
}
