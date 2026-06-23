package com.wowinfobiz.devicemanagmentservice.services;

import com.wowinfobiz.devicemanagmentservice.dto.SensorDTO;
import com.wowinfobiz.devicemanagmentservice.models.SensorDocument;
import com.wowinfobiz.devicemanagmentservice.models.SensorsEntity;

import java.util.List;
import java.util.UUID;

public interface SensorService {
    SensorDTO createSensor(UUID deviceId, SensorDTO request);

    List<SensorDocument> getAllSensors();

    SensorDTO getSensor(UUID sensorId);

    List<SensorDTO> getSensorsByDevice(UUID deviceId);

    SensorDTO updateSensor(UUID sensorId, SensorDTO request);

    void deleteSensor(UUID sensorId);
}
