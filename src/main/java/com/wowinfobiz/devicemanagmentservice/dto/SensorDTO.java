package com.wowinfobiz.devicemanagmentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class SensorDTO {

    private UUID sensorId;
    private UUID deviceId;
    private UUID sensorTypeId;
    private String name;
    private String status;
    private String unit;
    private Double lat;
    private Double longitude;

    public UUID getSensorId() {
        return sensorId;
    }

    public void setSensorId(UUID sensorId) {
        this.sensorId = sensorId;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    @JsonProperty("long")
    public Double getLong() {
        return longitude;
    }

    @JsonProperty("long")
    public void setLong(Double longitudeValue) {
        this.longitude = longitudeValue;
    }
}
