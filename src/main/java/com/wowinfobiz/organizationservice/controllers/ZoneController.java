package com.wowinfobiz.organizationservice.controllers;

import com.wowinfobiz.organizationservice.dto.CreateZoneRequest;
import com.wowinfobiz.organizationservice.dto.MessageResponse;
import com.wowinfobiz.organizationservice.models.ZonesEntity;
import com.wowinfobiz.organizationservice.services.ZoneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/org/zone")
public class ZoneController {

    private final ZoneService zoneService;

    public ZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @GetMapping
    public ResponseEntity<?> getZoneList(@RequestParam(required = true) UUID siteId)
    {
        MessageResponse<List<ZonesEntity>> messageResponse = new MessageResponse<>();
        try {
            List<ZonesEntity> zones = zoneService.getAllZoneDetails(siteId);
            messageResponse.setStatus(true);
            messageResponse.setMessage("Successfully fetched zones");
            messageResponse.setBody(zones);
            return ResponseEntity.ok(messageResponse);
        } catch (RuntimeException e) {
            messageResponse.setStatus(false);
            messageResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(messageResponse);
        }
    }

    @PostMapping
    public ResponseEntity<?> addZone(@RequestBody CreateZoneRequest zoneRequest)
    {
        MessageResponse<?> messageResponse = new MessageResponse<>();
        try {
            if (zoneRequest.getSiteId() == null) {
                throw new RuntimeException("siteId is required");
            }
            messageResponse = zoneService.addZone(zoneRequest.getSiteId(), zoneRequest);
            return ResponseEntity.ok(messageResponse);
        } catch (RuntimeException e) {
            messageResponse.setStatus(false);
            messageResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(messageResponse);
        }
    }

    @GetMapping("/{zoneId}")
    public ResponseEntity<?> getZoneDetails(@PathVariable(name = "zoneId")UUID zoneId)
    {
        MessageResponse<ZonesEntity> messageResponse = new MessageResponse<>();
        try {
            ZonesEntity zone = zoneService.getZoneDetails(zoneId);
            messageResponse.setStatus(true);
            messageResponse.setMessage("Successfully fetched zone");
            messageResponse.setBody(zone);
            return ResponseEntity.ok(messageResponse);
        } catch (RuntimeException e) {
            messageResponse.setStatus(false);
            messageResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(messageResponse);
        }
    }

    @PutMapping("/{zoneId}")
    public ResponseEntity<?> updateZoneDetails(@PathVariable(name = "zoneId")UUID zoneId, @RequestBody CreateZoneRequest zoneRequest)
    {
        MessageResponse<?> messageResponse = new MessageResponse<>();
        try {
            messageResponse = zoneService.updateZoneDetails(zoneId, zoneRequest);
            return ResponseEntity.ok(messageResponse);
        } catch (RuntimeException e) {
            messageResponse.setStatus(false);
            messageResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(messageResponse);
        }
    }

    @DeleteMapping("/{zoneId}")
    public ResponseEntity<?> deleteZone(@PathVariable(name = "zoneId")UUID zoneId)
    {
        MessageResponse<?> messageResponse = new MessageResponse<>();
        try {
            messageResponse = zoneService.deletZoneDetails(zoneId);
            return ResponseEntity.ok(messageResponse);
        } catch (RuntimeException e) {
            messageResponse.setStatus(false);
            messageResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(messageResponse);
        }
    }


}
