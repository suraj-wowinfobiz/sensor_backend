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
@RequestMapping("/api/v1/integrations")
public class IntegrationController {

    private final GenericResourceService genericResourceService;
    private final JsonMapperService jsonMapperService;

    public IntegrationController(GenericResourceService genericResourceService, JsonMapperService jsonMapperService) {
        this.genericResourceService = genericResourceService;
        this.jsonMapperService = jsonMapperService;
    }

    @GetMapping
    public List<JsonNode> list() {
        return genericResourceService.list("integrations");
    }

    @GetMapping("/{id}")
    public JsonNode get(@PathVariable UUID id) {
        return genericResourceService.get("integrations", id);
    }

    @PostMapping
    public JsonNode create(@RequestBody JsonNode payload) {
        return genericResourceService.create("integrations", payload);
    }

    @PutMapping("/{id}")
    public JsonNode update(@PathVariable UUID id, @RequestBody JsonNode payload) {
        return genericResourceService.upsert("integrations", id, payload);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        genericResourceService.delete("integrations", id);
    }

    @PostMapping("/{id}/test")
    public JsonNode test(@PathVariable UUID id) {
        ObjectNode request = (ObjectNode) jsonMapperService.fromJson("{}");
        request.put("integrationId", id.toString());
        request.put("action", "test");
        return genericResourceService.create("integration-actions", request);
    }

    @GetMapping("/{id}/logs")
    public List<JsonNode> logs(@PathVariable UUID id) {
        return genericResourceService.list("integration-logs-" + id);
    }

    @PostMapping("/{id}/sync")
    public JsonNode sync(@PathVariable UUID id) {
        ObjectNode request = (ObjectNode) jsonMapperService.fromJson("{}");
        request.put("integrationId", id.toString());
        request.put("action", "sync");
        return genericResourceService.create("integration-actions", request);
    }
}
