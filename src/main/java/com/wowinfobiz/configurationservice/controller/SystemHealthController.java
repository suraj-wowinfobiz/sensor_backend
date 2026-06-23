package com.wowinfobiz.configurationservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wowinfobiz.configurationservice.service.ConfigurationSettingsService;
import com.wowinfobiz.configurationservice.service.GenericResourceService;
import com.wowinfobiz.configurationservice.service.JsonMapperService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SystemHealthController {

    private final GenericResourceService genericResourceService;
    private final JsonMapperService jsonMapperService;
    private final ConfigurationSettingsService configurationSettingsService;

    public SystemHealthController(GenericResourceService genericResourceService,
                                  JsonMapperService jsonMapperService,
                                  ConfigurationSettingsService configurationSettingsService) {
        this.genericResourceService = genericResourceService;
        this.jsonMapperService = jsonMapperService;
        this.configurationSettingsService = configurationSettingsService;
    }

    @GetMapping("/health")
    public JsonNode health() {
        ObjectNode response = (ObjectNode) jsonMapperService.fromJson("{}");
        response.put("status", "UP");
        return response;
    }

    @GetMapping("/health/detailed")
    public JsonNode healthDetailed() {
        ObjectNode response = (ObjectNode) jsonMapperService.fromJson("{}");
        response.put("status", "UP");
        response.put("configKeys", configurationSettingsService.configKeyCount());
        response.put("integrations", genericResourceService.count("integrations"));
        return response;
    }

    @GetMapping("/version")
    public JsonNode version() {
        return jsonMapperService.fromJson("{\"service\":\"configuration-service\",\"version\":\"0.0.1-SNAPSHOT\"}");
    }

    @GetMapping("/system/status")
    public JsonNode systemStatus() {
        return jsonMapperService.fromJson("{\"status\":\"operational\"}");
    }

    @GetMapping("/system/info")
    public JsonNode systemInfo() {
        ObjectNode node = (ObjectNode) jsonMapperService.fromJson("{}");
        node.put("javaVersion", System.getProperty("java.version"));
        node.put("service", "configuration-service");
        return node;
    }

    @GetMapping("/system/metrics")
    public JsonNode metrics() {
        return jsonMapperService.fromJson("{\"metricsSource\":\"database\"}");
    }

    @GetMapping("/system/logs")
    public JsonNode logs() {
        return jsonMapperService.fromJson("{\"message\":\"Logs are managed externally\"}");
    }

    @GetMapping("/system/performance")
    public JsonNode performance() {
        return jsonMapperService.fromJson("{\"state\":\"normal\"}");
    }

    @GetMapping("/system/database-status")
    public JsonNode databaseStatus() {
        return jsonMapperService.fromJson("{\"database\":\"connected\"}");
    }

    @GetMapping("/system/cache-status")
    public JsonNode cacheStatus() {
        return jsonMapperService.fromJson("{\"cache\":\"reachable\"}");
    }

    @PostMapping("/system/cache-clear")
    public JsonNode cacheClear() {
        return jsonMapperService.fromJson("{\"cache\":\"clear-requested\"}");
    }

    @PostMapping("/system/maintenance-mode")
    public JsonNode maintenanceMode(@RequestBody JsonNode payload) {
        ObjectNode response = (ObjectNode) jsonMapperService.fromJson("{}");
        response.put("status", "updated");
        response.set("maintenance", payload);
        return response;
    }
}
