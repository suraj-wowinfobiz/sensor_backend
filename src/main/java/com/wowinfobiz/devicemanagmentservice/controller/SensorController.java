package com.wowinfobiz.devicemanagmentservice.controller;

import com.wowinfobiz.devicemanagmentservice.dto.SensorDTO;
import com.wowinfobiz.devicemanagmentservice.models.SensorDocument;
import com.wowinfobiz.devicemanagmentservice.models.SensorsEntity;
import com.wowinfobiz.devicemanagmentservice.services.SensorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sensors")
public class SensorController {

    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<SensorDocument>> getAllSensors(){
        return  ResponseEntity.status(HttpStatus.ACCEPTED).body(sensorService.getAllSensors());
    }

    @PostMapping("/devices/{deviceId}/sensors")
    public ResponseEntity<SensorDTO> createSensor(@PathVariable UUID deviceId, @RequestBody SensorDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sensorService.createSensor(deviceId, request));
    }

    @GetMapping("/sensors/{sensorId}")
    public ResponseEntity<SensorDTO> getSensor(@PathVariable UUID sensorId) {
        return ResponseEntity.ok(sensorService.getSensor(sensorId));
    }

    @GetMapping("/devices/{deviceId}/sensors")
    public ResponseEntity<List<SensorDTO>> getSensorsByDevice(@PathVariable UUID deviceId) {
        return ResponseEntity.ok(sensorService.getSensorsByDevice(deviceId));
    }

    @PutMapping("/sensors/{sensorId}")
    public ResponseEntity<SensorDTO> updateSensor(@PathVariable UUID sensorId, @RequestBody SensorDTO request) {
        return ResponseEntity.ok(sensorService.updateSensor(sensorId, request));
    }

    @DeleteMapping("/sensors/{sensorId}")
    public ResponseEntity<Void> deleteSensor(@PathVariable UUID sensorId) {
        sensorService.deleteSensor(sensorId);
        return ResponseEntity.noContent().build();
    }
}
