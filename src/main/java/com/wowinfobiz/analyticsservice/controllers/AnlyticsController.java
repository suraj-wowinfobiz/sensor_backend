package com.wowinfobiz.analyticsservice.controllers;

import com.wowinfobiz.analyticsservice.services.AnalyticsEventStore;
import com.wowinfobiz.analyticsservice.services.observer.LiveAnalyticsObserver;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnlyticsController {
    private final AnalyticsEventStore eventStore;
    private final LiveAnalyticsObserver liveAnalyticsObserver;

    public AnlyticsController(AnalyticsEventStore eventStore,
                              LiveAnalyticsObserver liveAnalyticsObserver) {
        this.eventStore = eventStore;
        this.liveAnalyticsObserver = liveAnalyticsObserver;
    }

    @GetMapping("/events")
    public ResponseEntity<List<Map<String, Object>>> getAllEvents() {
        return ResponseEntity.ok(eventStore.getAllEvents());
    }

    @GetMapping(value = "/events/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents() {
        return liveAnalyticsObserver.subscribe();
    }

    @GetMapping("/events/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentEvents(@RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(eventStore.getRecentEvents(limit));
    }

    @GetMapping("/events/alerts")
    public ResponseEntity<List<Map<String, Object>>> getAlertEvents() {
        return ResponseEntity.ok(eventStore.getAlertEvents());
    }

    @GetMapping("/runtime/status")
    public ResponseEntity<Map<String, Object>> getRuntimeStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("mode", "direct-websocket-sse");
        status.put("totalEvents", eventStore.count());
        status.put("liveEndpoint", "/api/v1/analytics/events/live");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalEvents", eventStore.count());
        response.put("totalAlertEvents", eventStore.getAlertEvents().size());
        response.put("generatedAt", Instant.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(eventStore.getDashboard());
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        return ResponseEntity.ok(eventStore.getOverview());
    }

    @GetMapping("/sensors/{id}/trends")
    public ResponseEntity<Map<String, Object>> getSensorTrends(@PathVariable String id,
                                                                @RequestParam(defaultValue = "7d") String range) {
        return ResponseEntity.ok(eventStore.getSensorTrends(id, range));
    }

    @GetMapping("/sensors/{id}/predictions")
    public ResponseEntity<Map<String, Object>> getSensorPredictions(@PathVariable String id,
                                                                     @RequestParam(defaultValue = "24h") String horizon) {
        return ResponseEntity.ok(eventStore.getSensorPredictions(id, horizon));
    }

    @GetMapping("/sensors/compare")
    public ResponseEntity<Map<String, Object>> compareSensors(@RequestParam String sensorIds) {
        List<String> ids = Arrays.stream(sensorIds.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
        return ResponseEntity.ok(eventStore.compareSensors(ids));
    }

    @GetMapping("/anomalies")
    public ResponseEntity<Map<String, Object>> getAnomalies() {
        return ResponseEntity.ok(eventStore.getAnomalies());
    }

    @GetMapping("/health-score")
    public ResponseEntity<Map<String, Object>> getHealthScore() {
        return ResponseEntity.ok(eventStore.getHealthScore());
    }

    @GetMapping("/distribution")
    public ResponseEntity<Map<String, Object>> getDistribution() {
        return ResponseEntity.ok(eventStore.getDistribution());
    }

    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformance() {
        return ResponseEntity.ok(eventStore.getPerformance());
    }

    @GetMapping("/utilization")
    public ResponseEntity<Map<String, Object>> getUtilization() {
        return ResponseEntity.ok(eventStore.getUtilization());
    }

    @GetMapping("/downtime")
    public ResponseEntity<Map<String, Object>> getDowntime() {
        return ResponseEntity.ok(eventStore.getDowntime());
    }

    @GetMapping("/alerts-trend")
    public ResponseEntity<Map<String, Object>> getAlertsTrend() {
        return ResponseEntity.ok(eventStore.getAlertsTrend());
    }

    @GetMapping("/sensor-reliability")
    public ResponseEntity<Map<String, Object>> getSensorReliability() {
        return ResponseEntity.ok(eventStore.getSensorReliability());
    }

    @GetMapping("/device-uptime")
    public ResponseEntity<Map<String, Object>> getDeviceUptime() {
        return ResponseEntity.ok(eventStore.getDeviceUptime());
    }

    @GetMapping("/custom-query")
    public ResponseEntity<Map<String, Object>> runCustomQuery(@RequestParam(defaultValue = "") String query,
                                                               @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(eventStore.runCustomQuery(query, limit));
    }
}
