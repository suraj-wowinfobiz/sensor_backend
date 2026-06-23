package com.wowinfobiz.devicemanagmentservice.controller;

import com.wowinfobiz.devicemanagmentservice.dto.SensorTypeDTO;
import com.wowinfobiz.devicemanagmentservice.services.SensorTypeService;
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
@RequestMapping("/api/v1/sensor-type")
public class SensorTypeController {

    private final SensorTypeService sensorTypeService;

    public SensorTypeController(SensorTypeService sensorTypeService) {
        this.sensorTypeService = sensorTypeService;
    }

    @PostMapping("/sensor-types")
    public ResponseEntity<SensorTypeDTO> createType(@RequestBody SensorTypeDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sensorTypeService.createType(request));
    }

    @GetMapping("/sensor-types")
    public ResponseEntity<List<SensorTypeDTO>> getTypes() {
        return ResponseEntity.ok(sensorTypeService.getTypes());
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<SensorTypeDTO>> getAllTypes() {
        return ResponseEntity.ok(sensorTypeService.getAllTypes());
    }

    @GetMapping("/sensor-types/{typeId}")
    public ResponseEntity<SensorTypeDTO> getType(@PathVariable UUID typeId) {
        return ResponseEntity.ok(sensorTypeService.getType(typeId));
    }

    @PutMapping("/sensor-types/{typeId}")
    public ResponseEntity<SensorTypeDTO> updateType(@PathVariable UUID typeId, @RequestBody SensorTypeDTO request) {
        return ResponseEntity.ok(sensorTypeService.updateType(typeId, request));
    }

    @DeleteMapping("/sensor-types/{typeId}")
    public ResponseEntity<Void> deleteType(@PathVariable UUID typeId) {
        sensorTypeService.deleteType(typeId);
        return ResponseEntity.noContent().build();
    }
}
