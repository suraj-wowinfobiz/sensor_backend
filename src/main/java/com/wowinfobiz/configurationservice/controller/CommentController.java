package com.wowinfobiz.configurationservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.wowinfobiz.configurationservice.service.GenericResourceService;
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
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final GenericResourceService genericResourceService;

    public CommentController(GenericResourceService genericResourceService) {
        this.genericResourceService = genericResourceService;
    }

    @GetMapping
    public List<JsonNode> list() {
        return genericResourceService.list("comments");
    }

    @GetMapping("/{id}")
    public JsonNode get(@PathVariable UUID id) {
        return genericResourceService.get("comments", id);
    }

    @PostMapping
    public JsonNode create(@RequestBody JsonNode payload) {
        return genericResourceService.create("comments", payload);
    }

    @PutMapping("/{id}")
    public JsonNode update(@PathVariable UUID id, @RequestBody JsonNode payload) {
        return genericResourceService.upsert("comments", id, payload);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        genericResourceService.delete("comments", id);
    }

    @GetMapping("/resource/{resourceType}/{resourceId}")
    public List<JsonNode> byResource(@PathVariable String resourceType, @PathVariable String resourceId) {
        return genericResourceService.list("comments-" + resourceType + "-" + resourceId);
    }

    @PostMapping("/{id}/reply")
    public JsonNode reply(@PathVariable UUID id, @RequestBody JsonNode payload) {
        return genericResourceService.create("comment-replies-" + id, payload);
    }
}
