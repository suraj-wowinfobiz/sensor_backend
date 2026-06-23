package com.wowinfobiz.configurationservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.wowinfobiz.configurationservice.service.RolePermissionService;
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
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    public RolePermissionController(RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    @GetMapping("/roles")
    public List<JsonNode> listRoles() {
        return rolePermissionService.listRoles();
    }

    @GetMapping("/roles/{id}")
    public JsonNode getRole(@PathVariable UUID id) {
        return rolePermissionService.getRole(id);
    }

    @PostMapping("/roles")
    public JsonNode createRole(@RequestBody JsonNode payload) {
        return rolePermissionService.createRole(payload);
    }

    @PutMapping("/roles/{id}")
    public JsonNode updateRole(@PathVariable UUID id, @RequestBody JsonNode payload) {
        return rolePermissionService.updateRole(id, payload);
    }

    @DeleteMapping("/roles/{id}")
    public void deleteRole(@PathVariable UUID id) {
        rolePermissionService.deleteRole(id);
    }

    @GetMapping("/permissions")
    public List<JsonNode> listPermissions() {
        return rolePermissionService.listPermissions();
    }

    @GetMapping("/permissions/{id}")
    public JsonNode getPermission(@PathVariable UUID id) {
        return rolePermissionService.getPermission(id);
    }

    @PostMapping("/roles/{id}/permissions")
    public JsonNode addRolePermission(@PathVariable UUID id, @RequestBody JsonNode payload) {
        UUID permissionId = UUID.fromString(payload.path("permissionId").asText());
        return rolePermissionService.assignPermissionToRole(id, permissionId);
    }

    @GetMapping("/roles/{id}/users")
    public JsonNode roleUsers(@PathVariable UUID id) {
        return rolePermissionService.usersByRole(id);
    }

    @PostMapping("/roles/{id}/assign-user")
    public JsonNode assignUser(@PathVariable UUID id, @RequestBody JsonNode payload) {
        return rolePermissionService.assignUserToRole(id, payload.path("userId").asText());
    }
}
