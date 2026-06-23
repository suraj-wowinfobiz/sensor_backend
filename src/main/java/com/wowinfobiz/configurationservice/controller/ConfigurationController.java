package com.wowinfobiz.configurationservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wowinfobiz.configurationservice.service.ConfigurationSettingsService;
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
@RequestMapping("/api/v1")
public class ConfigurationController {

    private final ConfigurationSettingsService configurationSettingsService;
    private final GenericResourceService genericResourceService;
    private final JsonMapperService jsonMapperService;

    public ConfigurationController(ConfigurationSettingsService configurationSettingsService,
                                   GenericResourceService genericResourceService,
                                   JsonMapperService jsonMapperService) {
        this.configurationSettingsService = configurationSettingsService;
        this.genericResourceService = genericResourceService;
        this.jsonMapperService = jsonMapperService;
    }

    @GetMapping("/config")
    public JsonNode getConfig() {
        return configurationSettingsService.getConfig("config");
    }

    @PutMapping("/config")
    public JsonNode putConfig(@RequestBody JsonNode payload) {
        return configurationSettingsService.saveConfig("config", payload);
    }

    @GetMapping("/configsystem")
    public JsonNode getConfigSystem() {
        return configurationSettingsService.getConfig("configsystem");
    }

    @PutMapping("/configsystem")
    public JsonNode putConfigSystem(@RequestBody JsonNode payload) {
        return configurationSettingsService.saveConfig("configsystem", payload);
    }

    @GetMapping("/confignotifications")
    public JsonNode getConfigNotifications() {
        return configurationSettingsService.getConfig("confignotifications");
    }

    @PutMapping("/confignotifications")
    public JsonNode putConfigNotifications(@RequestBody JsonNode payload) {
        return configurationSettingsService.saveConfig("confignotifications", payload);
    }

    @GetMapping("/configthresholds")
    public JsonNode getConfigThresholds() {
        return configurationSettingsService.getConfig("configthresholds");
    }

    @PutMapping("/configthresholds")
    public JsonNode putConfigThresholds(@RequestBody JsonNode payload) {
        return configurationSettingsService.saveConfig("configthresholds", payload);
    }

    @GetMapping("/configalerts")
    public JsonNode getConfigAlerts() {
        return configurationSettingsService.getConfig("configalerts");
    }

    @PutMapping("/configalerts")
    public JsonNode putConfigAlerts(@RequestBody JsonNode payload) {
        return configurationSettingsService.saveConfig("configalerts", payload);
    }

    @GetMapping("/configbackup")
    public List<JsonNode> getBackups() {
        return genericResourceService.list("configbackup");
    }

    @PostMapping("/configbackup/create")
    public JsonNode createBackup(@RequestBody JsonNode payload) {
        return genericResourceService.create("configbackup", payload);
    }

    @PostMapping("/configbackup/restore")
    public JsonNode restoreBackup(@RequestBody JsonNode payload) {
        ObjectNode response = (ObjectNode) jsonMapperService.fromJson("{}");
        response.put("status", "restore-requested");
        response.set("request", payload);
        return response;
    }

    @GetMapping("/configbackup/list")
    public List<JsonNode> listBackups() {
        return genericResourceService.list("configbackup");
    }

    @DeleteMapping("/configbackup/{id}")
    public void deleteBackup(@PathVariable UUID id) {
        genericResourceService.delete("configbackup", id);
    }

    @GetMapping("/configemail")
    public JsonNode getConfigEmail() {
        return configurationSettingsService.getConfig("configemail");
    }

    @PutMapping("/configemail")
    public JsonNode putConfigEmail(@RequestBody JsonNode payload) {
        return configurationSettingsService.saveConfig("configemail", payload);
    }

    @PostMapping("/config/test-email")
    public JsonNode testEmail(@RequestBody JsonNode payload) {
        return configurationSettingsService.testEmail(payload);
    }
}
