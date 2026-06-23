package com.wowinfobiz.devicemanagmentservice.controller;

import com.wowinfobiz.devicemanagmentservice.dto.SensorParameterDTO;
import com.wowinfobiz.devicemanagmentservice.services.SensorParameterService;
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
@RequestMapping("/api/v1/sensor-parameter")
public class SensorParameterController {

    private final SensorParameterService parameterService;

    public SensorParameterController(SensorParameterService parameterService) {
        this.parameterService = parameterService;
    }

    @PostMapping("/sensor-types/{typeId}/parameters")
    public ResponseEntity<SensorParameterDTO> createParameter(@PathVariable UUID typeId, @RequestBody SensorParameterDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parameterService.createParameter(typeId, request));
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<SensorParameterDTO>> getAllParameters() {
        return ResponseEntity.ok(parameterService.getAllParameters());
    }

    @GetMapping("/sensor-types/{typeId}/parameters")
    public ResponseEntity<List<SensorParameterDTO>> getParametersByType(@PathVariable UUID typeId) {
        return ResponseEntity.ok(parameterService.getParametersByType(typeId));
    }

    @PutMapping("/parameters/{parameterId}")
    public ResponseEntity<SensorParameterDTO> updateParameter(@PathVariable UUID parameterId, @RequestBody SensorParameterDTO request) {
        return ResponseEntity.ok(parameterService.updateParameter(parameterId, request));
    }

    @DeleteMapping("/parameters/{parameterId}")
    public ResponseEntity<Void> deleteParameter(@PathVariable UUID parameterId) {
        parameterService.deleteParameter(parameterId);
        return ResponseEntity.noContent().build();
    }
}
