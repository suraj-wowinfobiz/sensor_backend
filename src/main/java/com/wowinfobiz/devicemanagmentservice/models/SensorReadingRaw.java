package com.wowinfobiz.devicemanagmentservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import java.util.Date;
import java.util.UUID;

public class SensorReadingRaw {

    @Id
    @Column(name = "sensor_reading_raw_id")
    UUID sensorReadingRawId;

    @Column(name = "sensor_id")
    UUID sensorId;

    @Column(name = "sensor_parameter_id")
    UUID sensorParameterId;

    double value;

    Date timestamp;

    @Column(name = "ingestion_time")
    Date ingestionTime;


    public UUID getSensorReadingRawId() {
        return sensorReadingRawId;
    }

    public void setSensorReadingRawId(UUID sensorReadingRawId) {
        this.sensorReadingRawId = sensorReadingRawId;
    }

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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getIngestionTime() {
        return ingestionTime;
    }

    public void setIngestionTime(Date ingestionTime) {
        this.ingestionTime = ingestionTime;
    }

    public SensorReadingRaw(UUID sensorReadingRawId, UUID sensorId, UUID sensorParameterId, double value, Date timestamp, Date ingestionTime) {
        this.sensorReadingRawId = sensorReadingRawId;
        this.sensorId = sensorId;
        this.sensorParameterId = sensorParameterId;
        this.value = value;
        this.timestamp = timestamp;
        this.ingestionTime = ingestionTime;
    }
}
