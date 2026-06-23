package com.wowinfobiz.devicemanagmentservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "sensor_type")
public class SensorType {

    @Id
    @Column(name = "sensor_type_id")
    UUID sensorTypeId;

    String name;

    String category;

    String description;

    public SensorType() {
    }

    public SensorType(UUID sensorTypeId, String name, String category, String description) {
        this.sensorTypeId = sensorTypeId;
        this.name = name;
        this.category = category;
        this.description = description;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
