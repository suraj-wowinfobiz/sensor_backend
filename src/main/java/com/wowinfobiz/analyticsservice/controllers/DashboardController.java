package com.wowinfobiz.analyticsservice.controllers;

import com.wowinfobiz.analyticsservice.services.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        return ResponseEntity.ok(dashboardService.getOverview());
    }

    @GetMapping("/recent-alerts")
    public ResponseEntity<Map<String, Object>> getRecentAlerts() {
        return ResponseEntity.ok(dashboardService.getRecentAlerts());
    }

    @GetMapping("/sensor-status")
    public ResponseEntity<Map<String, Object>> getSensorStatus() {
        return ResponseEntity.ok(dashboardService.getSensorStatus());
    }

    @GetMapping("/device-status")
    public ResponseEntity<Map<String, Object>> getDeviceStatus() {
        return ResponseEntity.ok(dashboardService.getDeviceStatus());
    }

    @GetMapping("/system-health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        return ResponseEntity.ok(dashboardService.getSystemHealth());
    }

    @GetMapping("/charts/tilt-readings")
    public ResponseEntity<Map<String, Object>> getTiltReadingsChart() {
        return ResponseEntity.ok(dashboardService.getTiltReadingsChart());
    }

    @GetMapping("/charts/sensor-distribution")
    public ResponseEntity<Map<String, Object>> getSensorDistributionChart() {
        return ResponseEntity.ok(dashboardService.getSensorDistributionChart());
    }

    @GetMapping("/charts/alerts-trend")
    public ResponseEntity<Map<String, Object>> getAlertsTrendChart() {
        return ResponseEntity.ok(dashboardService.getAlertsTrendChart());
    }

    @GetMapping("/charts/device-uptime")
    public ResponseEntity<Map<String, Object>> getDeviceUptimeChart() {
        return ResponseEntity.ok(dashboardService.getDeviceUptimeChart());
    }

    @GetMapping("/activity-feed")
    public ResponseEntity<Map<String, Object>> getActivityFeed() {
        return ResponseEntity.ok(dashboardService.getActivityFeed());
    }

    @GetMapping("/quick-stats")
    public ResponseEntity<Map<String, Object>> getQuickStats() {
        return ResponseEntity.ok(dashboardService.getQuickStats());
    }
}
