package com.wowinfobiz.configurationservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.wowinfobiz.configurationservice.service.GenericResourceService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/batch")
public class BatchController {

    private final GenericResourceService genericResourceService;

    public BatchController(GenericResourceService genericResourceService) {
        this.genericResourceService = genericResourceService;
    }

    @PostMapping("/sensors/create")
    public JsonNode createSensors(@RequestBody JsonNode payload) {
        return genericResourceService.create("batch-sensors-create", payload);
    }

    @PostMapping("/sensors/update")
    public JsonNode updateSensors(@RequestBody JsonNode payload) {
        return genericResourceService.create("batch-sensors-update", payload);
    }

    @PostMapping("/sensors/delete")
    public JsonNode deleteSensors(@RequestBody JsonNode payload) {
        return genericResourceService.create("batch-sensors-delete", payload);
    }

    @PostMapping("/devices/update")
    public JsonNode updateDevices(@RequestBody JsonNode payload) {
        return genericResourceService.create("batch-devices-update", payload);
    }

    @PostMapping("/alerts/resolve")
    public JsonNode resolveAlerts(@RequestBody JsonNode payload) {
        return genericResourceService.create("batch-alerts-resolve", payload);
    }

    @PostMapping("/users/create")
    public JsonNode createUsers(@RequestBody JsonNode payload) {
        return genericResourceService.create("batch-users-create", payload);
    }

    @PostMapping("/thresholds/apply")
    public JsonNode applyThresholds(@RequestBody JsonNode payload) {
        return genericResourceService.create("batch-thresholds-apply", payload);
    }

    @PostMapping("/export")
    public JsonNode exportBatch(@RequestBody JsonNode payload) {
        return genericResourceService.create("batch-export", payload);
    }
}
