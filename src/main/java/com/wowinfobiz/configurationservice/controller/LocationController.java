package com.wowinfobiz.configurationservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.wowinfobiz.configurationservice.service.GenericResourceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

    private final GenericResourceService genericResourceService;

    public LocationController(GenericResourceService genericResourceService) {
        this.genericResourceService = genericResourceService;
    }

    @GetMapping
    public List<JsonNode> list() {
        return genericResourceService.list("locations");
    }

    @GetMapping("/nearby")
    public List<JsonNode> nearby() {
        return genericResourceService.list("locations-nearby");
    }

    @PostMapping("/geocode")
    public JsonNode geocode(@RequestBody JsonNode payload) {
        return genericResourceService.create("locations-geocode", payload);
    }

    @PostMapping("/reverse-geocode")
    public JsonNode reverseGeocode(@RequestBody JsonNode payload) {
        return genericResourceService.create("locations-reverse-geocode", payload);
    }

    @GetMapping("/map-data")
    public List<JsonNode> mapData() {
        return genericResourceService.list("locations-map-data");
    }
}
