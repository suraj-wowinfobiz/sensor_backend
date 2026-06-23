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
@RequestMapping("/api/v1/tags")
public class TagController {

    private final GenericResourceService genericResourceService;
    private final JsonMapperService jsonMapperService;

    public TagController(GenericResourceService genericResourceService, JsonMapperService jsonMapperService) {
        this.genericResourceService = genericResourceService;
        this.jsonMapperService = jsonMapperService;
    }

    @GetMapping
    public List<JsonNode> list() {
        return genericResourceService.list("tags");
    }

    @GetMapping("/{id}")
    public JsonNode get(@PathVariable UUID id) {
        return genericResourceService.get("tags", id);
    }

    @PostMapping
    public JsonNode create(@RequestBody JsonNode payload) {
        return genericResourceService.create("tags", payload);
    }

    @PutMapping("/{id}")
    public JsonNode update(@PathVariable UUID id, @RequestBody JsonNode payload) {
        return genericResourceService.upsert("tags", id, payload);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        genericResourceService.delete("tags", id);
    }

    @PostMapping("/assign")
    public JsonNode assign(@RequestBody JsonNode payload) {
        return genericResourceService.create("tag-assign", payload);
    }

    @PostMapping("/unassign")
    public JsonNode unassign(@RequestBody JsonNode payload) {
        return genericResourceService.create("tag-unassign", payload);
    }

    @GetMapping("/{id}/resources")
    public JsonNode resources(@PathVariable UUID id) {
        ObjectNode response = (ObjectNode) jsonMapperService.fromJson("{}");
        response.put("tagId", id.toString());
        response.set("resources", jsonMapperService.fromJson("[]"));
        return response;
    }
}
