package com.wowinfobiz.configurationservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wowinfobiz.configurationservice.service.GenericResourceService;
import com.wowinfobiz.configurationservice.service.JsonMapperService;
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
@RequestMapping("/api/v1/maintenance")
public class MaintenanceController {

    private final GenericResourceService genericResourceService;
    private final JsonMapperService jsonMapperService;

    public MaintenanceController(GenericResourceService genericResourceService, JsonMapperService jsonMapperService) {
        this.genericResourceService = genericResourceService;
        this.jsonMapperService = jsonMapperService;
    }

    @GetMapping
    public List<JsonNode> list() {
        return genericResourceService.list("maintenance");
    }

    @GetMapping("/{id}")
    public JsonNode get(@PathVariable UUID id) {
        return genericResourceService.get("maintenance", id);
    }

    @PostMapping
    public JsonNode create(@RequestBody JsonNode payload) {
        return genericResourceService.create("maintenance", payload);
    }

    @PutMapping("/{id}")
    public JsonNode update(@PathVariable UUID id, @RequestBody JsonNode payload) {
        return genericResourceService.upsert("maintenance", id, payload);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        genericResourceService.delete("maintenance", id);
    }

    @GetMapping("/schedule")
    public List<JsonNode> schedule() {
        return genericResourceService.list("maintenance-schedule");
    }

    @PostMapping("/{id}/complete")
    public JsonNode complete(@PathVariable UUID id, @RequestBody(required = false) JsonNode payload) {
        ObjectNode response = (ObjectNode) jsonMapperService.fromJson("{}");
        response.put("maintenanceId", id.toString());
        response.put("status", "completed");
        if (payload != null) {
            response.set("details", payload);
        }
        return genericResourceService.create("maintenance-complete", response);
    }
}
