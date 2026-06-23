package com.wowinfobiz.configurationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wowinfobiz.configurationservice.model.PermissionEntity;
import com.wowinfobiz.configurationservice.model.RoleEntity;
import com.wowinfobiz.configurationservice.model.RolePermissionEntity;
import com.wowinfobiz.configurationservice.model.RoleUserEntity;
import com.wowinfobiz.configurationservice.repository.PermissionRepository;
import com.wowinfobiz.configurationservice.repository.RolePermissionRepository;
import com.wowinfobiz.configurationservice.repository.RoleRepository;
import com.wowinfobiz.configurationservice.repository.RoleUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RolePermissionService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RoleUserRepository roleUserRepository;
    private final JsonMapperService jsonMapperService;

    public RolePermissionService(RoleRepository roleRepository,
                                 PermissionRepository permissionRepository,
                                 RolePermissionRepository rolePermissionRepository,
                                 RoleUserRepository roleUserRepository,
                                 JsonMapperService jsonMapperService) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.roleUserRepository = roleUserRepository;
        this.jsonMapperService = jsonMapperService;
    }

    public List<JsonNode> listRoles() {
        return roleRepository.findAll().stream().map(this::toRoleNode).toList();
    }

    public JsonNode getRole(UUID id) {
        return toRoleNode(roleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Role not found")));
    }

    public JsonNode createRole(JsonNode payload) {
        RoleEntity role = new RoleEntity();
        role.setName(payload.path("name").asText("role-" + UUID.randomUUID()));
        role.setPayloadJson(jsonMapperService.toJson(payload));
        return toRoleNode(roleRepository.save(role));
    }

    public JsonNode updateRole(UUID id, JsonNode payload) {
        RoleEntity role = roleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Role not found"));
        role.setName(payload.path("name").asText(role.getName()));
        role.setPayloadJson(jsonMapperService.toJson(payload));
        return toRoleNode(roleRepository.save(role));
    }

    public void deleteRole(UUID id) {
        roleRepository.deleteById(id);
    }

    public List<JsonNode> listPermissions() {
        return permissionRepository.findAll().stream().map(this::toPermissionNode).toList();
    }

    public JsonNode getPermission(UUID id) {
        PermissionEntity entity = permissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found"));
        return toPermissionNode(entity);
    }

    public JsonNode createPermission(JsonNode payload) {
        PermissionEntity permission = new PermissionEntity();
        permission.setName(payload.path("name").asText("permission-" + UUID.randomUUID()));
        permission.setPayloadJson(jsonMapperService.toJson(payload));
        return toPermissionNode(permissionRepository.save(permission));
    }

    public JsonNode assignPermissionToRole(UUID roleId, UUID permissionId) {
        RolePermissionEntity assignment = new RolePermissionEntity();
        assignment.setRoleId(roleId);
        assignment.setPermissionId(permissionId);
        rolePermissionRepository.save(assignment);

        ObjectNode node = (ObjectNode) jsonMapperService.fromJson("{}");
        node.put("roleId", roleId.toString());
        node.put("permissionId", permissionId.toString());
        node.put("status", "assigned");
        return node;
    }

    public JsonNode assignUserToRole(UUID roleId, String userId) {
        RoleUserEntity assignment = new RoleUserEntity();
        assignment.setRoleId(roleId);
        assignment.setUserId(userId);
        roleUserRepository.save(assignment);

        ObjectNode node = (ObjectNode) jsonMapperService.fromJson("{}");
        node.put("roleId", roleId.toString());
        node.put("userId", userId);
        node.put("status", "assigned");
        return node;
    }

    public JsonNode usersByRole(UUID roleId) {
        ArrayNode users = (ArrayNode) jsonMapperService.fromJson("[]");
        roleUserRepository.findByRoleId(roleId).forEach(roleUser -> {
            ObjectNode user = (ObjectNode) jsonMapperService.fromJson("{}");
            user.put("userId", roleUser.getUserId());
            user.put("assignedAt", roleUser.getCreatedAt().toString());
            users.add(user);
        });

        ObjectNode response = (ObjectNode) jsonMapperService.fromJson("{}");
        response.put("roleId", roleId.toString());
        response.set("users", users);
        return response;
    }

    private JsonNode toRoleNode(RoleEntity role) {
        JsonNode parsed = jsonMapperService.fromJson(role.getPayloadJson());
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        if (parsed.isObject()) {
            node.setAll((ObjectNode) parsed);
        } else {
            node.set("data", parsed);
        }
        node.put("id", role.getId().toString());
        node.put("name", role.getName());
        return node;
    }

    private JsonNode toPermissionNode(PermissionEntity permission) {
        JsonNode parsed = jsonMapperService.fromJson(permission.getPayloadJson());
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        if (parsed.isObject()) {
            node.setAll((ObjectNode) parsed);
        } else {
            node.set("data", parsed);
        }
        node.put("id", permission.getId().toString());
        node.put("name", permission.getName());
        return node;
    }
}
