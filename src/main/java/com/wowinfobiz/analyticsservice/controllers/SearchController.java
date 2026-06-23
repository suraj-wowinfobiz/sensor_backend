package com.wowinfobiz.analyticsservice.controllers;

import com.wowinfobiz.analyticsservice.services.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> search(@RequestParam(defaultValue = "") String q,
                                                      @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(searchService.search(q, limit));
    }

    @GetMapping("/sensors")
    public ResponseEntity<Map<String, Object>> searchSensors(@RequestParam(defaultValue = "") String q,
                                                             @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(searchService.searchSensors(q, limit));
    }

    @GetMapping("/devices")
    public ResponseEntity<Map<String, Object>> searchDevices(@RequestParam(defaultValue = "") String q,
                                                             @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(searchService.searchDevices(q, limit));
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> searchUsers(@RequestParam(defaultValue = "") String q,
                                                           @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(searchService.searchUsers(q, limit));
    }

    @GetMapping("/organizations")
    public ResponseEntity<Map<String, Object>> searchOrganizations(@RequestParam(defaultValue = "") String q,
                                                                   @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(searchService.searchOrganizations(q, limit));
    }

    @GetMapping("/global")
    public ResponseEntity<Map<String, Object>> globalSearch(@RequestParam(defaultValue = "") String q,
                                                            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(searchService.globalSearch(q, limit));
    }
}
