package com.wowinfobiz.configurationservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.wowinfobiz.configurationservice.service.GenericResourceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ImportExportController {

    private final GenericResourceService genericResourceService;

    public ImportExportController(GenericResourceService genericResourceService) {
        this.genericResourceService = genericResourceService;
    }

    @PostMapping("/import/sensors")
    public JsonNode importSensors(@RequestBody JsonNode payload) {
        return genericResourceService.create("import-sensors", payload);
    }

    @PostMapping("/import/devices")
    public JsonNode importDevices(@RequestBody JsonNode payload) {
        return genericResourceService.create("import-devices", payload);
    }

    @PostMapping("/import/users")
    public JsonNode importUsers(@RequestBody JsonNode payload) {
        return genericResourceService.create("import-users", payload);
    }

    @GetMapping("/export/sensors")
    public JsonNode exportSensors() {
        return genericResourceService.create("export-sensors", com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode().put("type", "sensors"));
    }

    @GetMapping("/export/devices")
    public JsonNode exportDevices() {
        return genericResourceService.create("export-devices", com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode().put("type", "devices"));
    }

    @GetMapping("/export/readings")
    public JsonNode exportReadings() {
        return genericResourceService.create("export-readings", com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode().put("type", "readings"));
    }
}
