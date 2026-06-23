package com.wowinfobiz.devicemanagmentservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "sensors")
public class SensorsEntity {


    @Id
    @Column(name = "sensor_id")
    UUID sensorId;

    @Column(name = "sensor_type_id")
    UUID sensorTypeId;

    @Column(name = "device_id")
    UUID deviceId;

    @Column(name = "serial_number")
    String serialNumber;

    @Column(name = "installed_at")
    String installedAt;

    public SensorsEntity() {
    }

    public SensorsEntity(UUID sensorId, UUID sensorTypeId, UUID deviceId, String serialNumber, String installedAt) {
        this.sensorId = sensorId;
        this.sensorTypeId = sensorTypeId;
        this.deviceId = deviceId;
        this.serialNumber = serialNumber;
        this.installedAt = installedAt;
    }

    public UUID getSensorId() {
        return sensorId;
    }

    public void setSensorId(UUID sensorId) {
        this.sensorId = sensorId;
    }

    public UUID getSensorTypeId() {
        return sensorTypeId;
    }

    public void setSensorTypeId(UUID sensorTypeId) {
        this.sensorTypeId = sensorTypeId;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getInstalledAt() {
        return installedAt;
    }

    public void setInstalledAt(String installedAt) {
        this.installedAt = installedAt;
    }
}
