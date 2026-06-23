package com.wowinfobiz.configurationservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.wowinfobiz.configurationservice.service.GenericResourceService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/favorites")
public class FavoriteController {

    private final GenericResourceService genericResourceService;

    public FavoriteController(GenericResourceService genericResourceService) {
        this.genericResourceService = genericResourceService;
    }

    @GetMapping
    public List<JsonNode> list() {
        return genericResourceService.list("favorites");
    }

    @PostMapping
    public JsonNode create(@RequestBody JsonNode payload) {
        return genericResourceService.create("favorites", payload);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        genericResourceService.delete("favorites", id);
    }

    @GetMapping("/sensors")
    public List<JsonNode> sensors() {
        return genericResourceService.list("favorites-sensors");
    }

    @GetMapping("/devices")
    public List<JsonNode> devices() {
        return genericResourceService.list("favorites-devices");
    }

    @GetMapping("/sites")
    public List<JsonNode> sites() {
        return genericResourceService.list("favorites-sites");
    }
}
