package com.wowinfobiz.configurationservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.wowinfobiz.configurationservice.service.GenericResourceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/calibrations")
public class CalibrationController {

    private final GenericResourceService genericResourceService;

    public CalibrationController(GenericResourceService genericResourceService) {
        this.genericResourceService = genericResourceService;
    }

    @GetMapping
    public List<JsonNode> list() {
        return genericResourceService.list("calibrations");
    }

    @GetMapping("/{id}")
    public JsonNode get(@PathVariable UUID id) {
        return genericResourceService.get("calibrations", id);
    }

    @PostMapping
    public JsonNode create(@RequestBody JsonNode payload) {
        return genericResourceService.create("calibrations", payload);
    }

    @GetMapping("/history")
    public List<JsonNode> history() {
        return genericResourceService.list("calibration-history");
    }

    @GetMapping("/due")
    public List<JsonNode> due() {
        return genericResourceService.list("calibration-due");
    }

    @PostMapping("/bulk")
    public JsonNode bulk(@RequestBody JsonNode payload) {
        return genericResourceService.create("calibration-bulk", payload);
    }
}
