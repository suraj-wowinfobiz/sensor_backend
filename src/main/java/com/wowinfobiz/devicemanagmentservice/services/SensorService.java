package com.wowinfobiz.devicemanagmentservice.services;

import com.wowinfobiz.devicemanagmentservice.dto.SensorDTO;

import java.util.List;
import java.util.UUID;

public interface SensorService {
    SensorDTO createSensor(UUID deviceId, SensorDTO request);

    List<SensorDTO> getAllSensors();

    SensorDTO getSensor(UUID sensorId);

    SensorDTO getSensorByEndpointKey(String endpointKey);

    List<SensorDTO> getSensorsByDevice(UUID deviceId);

    SensorDTO updateSensor(UUID sensorId, SensorDTO request);

    void deleteSensor(UUID sensorId);
}
