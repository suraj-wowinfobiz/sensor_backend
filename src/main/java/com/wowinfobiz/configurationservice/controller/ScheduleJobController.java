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
@RequestMapping("/api/v1")
public class ScheduleJobController {

    private final GenericResourceService genericResourceService;
    private final JsonMapperService jsonMapperService;

    public ScheduleJobController(GenericResourceService genericResourceService, JsonMapperService jsonMapperService) {
        this.genericResourceService = genericResourceService;
        this.jsonMapperService = jsonMapperService;
    }

    @GetMapping("/schedules")
    public List<JsonNode> listSchedules() {
        return genericResourceService.list("schedules");
    }

    @GetMapping("/schedules/{id}")
    public JsonNode getSchedule(@PathVariable UUID id) {
        return genericResourceService.get("schedules", id);
    }

    @PostMapping("/schedules")
    public JsonNode createSchedule(@RequestBody JsonNode payload) {
        return genericResourceService.create("schedules", payload);
    }

    @PutMapping("/schedules/{id}")
    public JsonNode updateSchedule(@PathVariable UUID id, @RequestBody JsonNode payload) {
        return genericResourceService.upsert("schedules", id, payload);
    }

    @DeleteMapping("/schedules/{id}")
    public void deleteSchedule(@PathVariable UUID id) {
        genericResourceService.delete("schedules", id);
    }

    @PostMapping("/schedules/{id}/run")
    public JsonNode runSchedule(@PathVariable UUID id) {
        ObjectNode action = (ObjectNode) jsonMapperService.fromJson("{}");
        action.put("scheduleId", id.toString());
        action.put("action", "run");
        return genericResourceService.create("schedule-actions", action);
    }

    @GetMapping("/schedules/{id}/history")
    public List<JsonNode> scheduleHistory(@PathVariable UUID id) {
        return genericResourceService.list("schedule-history-" + id);
    }

    @GetMapping("/jobs/status")
    public JsonNode jobStatus() {
        ObjectNode response = (ObjectNode) jsonMapperService.fromJson("{}");
        response.put("activeSchedules", genericResourceService.count("schedules"));
        response.put("recentActions", genericResourceService.count("schedule-actions"));
        return response;
    }
}
