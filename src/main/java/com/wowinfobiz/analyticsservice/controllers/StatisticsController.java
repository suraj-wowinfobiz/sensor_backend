package com.wowinfobiz.analyticsservice.controllers;

import com.wowinfobiz.analyticsservice.services.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/stats")
public class StatisticsController {
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        return ResponseEntity.ok(statisticsService.overview());
    }

    @GetMapping("/sensors")
    public ResponseEntity<Map<String, Object>> getSensorsStats() {
        return ResponseEntity.ok(statisticsService.sensors());
    }

    @GetMapping("/devices")
    public ResponseEntity<Map<String, Object>> getDevicesStats() {
        return ResponseEntity.ok(statisticsService.devices());
    }

    @GetMapping("/alerts")
    public ResponseEntity<Map<String, Object>> getAlertsStats() {
        return ResponseEntity.ok(statisticsService.alerts());
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUsersStats() {
        return ResponseEntity.ok(statisticsService.users());
    }

    @GetMapping("/organizations")
    public ResponseEntity<Map<String, Object>> getOrganizationsStats() {
        return ResponseEntity.ok(statisticsService.organizations());
    }

    @GetMapping("/readings")
    public ResponseEntity<Map<String, Object>> getReadingsStats() {
        return ResponseEntity.ok(statisticsService.readings());
    }

    @GetMapping("/custom")
    public ResponseEntity<Map<String, Object>> getCustomStats(@RequestParam(defaultValue = "") String metric,
                                                              @RequestParam(defaultValue = "7d") String range) {
        return ResponseEntity.ok(statisticsService.custom(metric, range));
    }
}
