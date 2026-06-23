package com.wowinfobiz.devicemanagmentservice.models;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "sensor_parameter")
public class SensorParameters {

    @Id
    @Column(name = "sensor_parameter_id")
    UUID sensorParameterId;


    @Column(name = "sensor_type_id")
    UUID sensorTypeId;


    String name;

    String unit;

    @Column(name = "min_value")
    double minValue;

    @Column(name = "max_value")
    double maxValue;

    public SensorParameters() {
    }


    public UUID getSensorParameterId() {
        return sensorParameterId;
    }

    public void setSensorParameterId(UUID sensorParameterId) {
        this.sensorParameterId = sensorParameterId;
    }

    public UUID getSensorTypeId() {
        return sensorTypeId;
    }

    public void setSensorTypeId(UUID sensorTypeId) {
        this.sensorTypeId = sensorTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public SensorParameters(UUID sensorParameterId, UUID sensorTypeId, String name, String unit, double minValue, double maxValue) {
        this.sensorParameterId = sensorParameterId;
        this.sensorTypeId = sensorTypeId;
        this.name = name;
        this.unit = unit;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
}
