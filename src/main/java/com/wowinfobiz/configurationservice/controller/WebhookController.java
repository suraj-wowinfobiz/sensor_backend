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
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private final GenericResourceService genericResourceService;
    private final JsonMapperService jsonMapperService;

    public WebhookController(GenericResourceService genericResourceService, JsonMapperService jsonMapperService) {
        this.genericResourceService = genericResourceService;
        this.jsonMapperService = jsonMapperService;
    }

    @GetMapping
    public List<JsonNode> list() {
        return genericResourceService.list("webhooks");
    }

    @GetMapping("/{id}")
    public JsonNode get(@PathVariable UUID id) {
        return genericResourceService.get("webhooks", id);
    }

    @PostMapping
    public JsonNode create(@RequestBody JsonNode payload) {
        return genericResourceService.create("webhooks", payload);
    }

    @PutMapping("/{id}")
    public JsonNode update(@PathVariable UUID id, @RequestBody JsonNode payload) {
        return genericResourceService.upsert("webhooks", id, payload);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        genericResourceService.delete("webhooks", id);
    }

    @PostMapping("/{id}/test")
    public JsonNode test(@PathVariable UUID id) {
        ObjectNode request = (ObjectNode) jsonMapperService.fromJson("{}");
        request.put("webhookId", id.toString());
        request.put("action", "test");
        return genericResourceService.create("webhook-actions", request);
    }

    @GetMapping("/{id}/logs")
    public List<JsonNode> logs(@PathVariable UUID id) {
        return genericResourceService.list("webhook-logs-" + id);
    }
}
