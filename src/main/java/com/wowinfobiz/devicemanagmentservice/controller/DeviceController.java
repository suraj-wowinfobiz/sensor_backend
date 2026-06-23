package com.wowinfobiz.devicemanagmentservice.controller;

import com.wowinfobiz.devicemanagmentservice.dto.DeviceDTO;
import com.wowinfobiz.devicemanagmentservice.models.DeviceDocument;
import com.wowinfobiz.devicemanagmentservice.services.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
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
@RequestMapping("/api/v1/device")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/devices")
    public ResponseEntity<DeviceDTO> createDevice(@RequestBody DeviceDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.createDevice(request));
    }

    @GetMapping("/getall/")
    public ResponseEntity<List<DeviceDocument>> getAllDevice(){
        return ResponseEntity.status(HttpStatus.OK).body(deviceService.getAllDevice());
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<DeviceDocument>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevice());
    }

    @GetMapping("/devices/{deviceId}")
    public ResponseEntity<DeviceDTO> getDevice(@PathVariable UUID deviceId) {
        return ResponseEntity.ok(deviceService.getDevice(deviceId));
    }

    @GetMapping("/sites/{siteId}/devices")
    public ResponseEntity<List<DeviceDTO>> getDevicesBySite(@PathVariable UUID siteId) {
        return ResponseEntity.ok(deviceService.getDevicesBySite(siteId));
    }

    @PutMapping("/devices/{deviceId}")
    public ResponseEntity<DeviceDTO> updateDevice(@PathVariable UUID deviceId, @RequestBody DeviceDTO request) {
        return ResponseEntity.ok(deviceService.updateDevice(deviceId, request));
    }



    @DeleteMapping("/devices/{deviceId}")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID deviceId) {
        deviceService.deleteDevice(deviceId);
        return ResponseEntity.noContent().build();
    }
}
