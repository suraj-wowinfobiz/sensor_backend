package com.wowinfobiz.authenticationservice.controller;

import com.wowinfobiz.authenticationservice.dto.AccessAssignmentRequest;
import com.wowinfobiz.authenticationservice.dto.AccessScopeRequest;
import com.wowinfobiz.authenticationservice.dto.MessageResponseDTO;
import com.wowinfobiz.authenticationservice.dto.UserCreateRequest;
import com.wowinfobiz.authenticationservice.dto.UserUpdateRequest;
import com.wowinfobiz.authenticationservice.enums.AccessPrincipalType;
import com.wowinfobiz.authenticationservice.model.SiteZoneAccess;
import com.wowinfobiz.authenticationservice.model.User;
import com.wowinfobiz.authenticationservice.repo.UserRepository;
import com.wowinfobiz.authenticationservice.security.CustomUserDetails;
import com.wowinfobiz.authenticationservice.services.AdminService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admins")
public class AdminController {

    private final AdminService adminService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AdminController(
            AdminService adminService,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository
    ) {
        this.adminService = adminService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users")
    public ResponseEntity<MessageResponseDTO> createUser(
            @RequestBody UserCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        UUID adminId = principal.getPrincipalId();
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setOrganizationId(request.getOrganizationId());
        User created = adminService.createUser(adminId, user);
        return ResponseEntity.ok(new MessageResponseDTO("User created successfully", "SUCCESS", created));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/get-all")
    public ResponseEntity<MessageResponseDTO> getAllUsers(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        List<User> users = userRepository.findByAdminIdAndRole(principal.getPrincipalId(), AccessPrincipalType.USER);
        return ResponseEntity.ok(new MessageResponseDTO("Users fetched successfully", "SUCCESS", users));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{userId}")
    public ResponseEntity<MessageResponseDTO> getUserById(
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        User user = resolveAdminUser(userId, principal.getPrincipalId());
        return ResponseEntity.ok(new MessageResponseDTO("User fetched successfully", "SUCCESS", user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{userId}")
    public ResponseEntity<MessageResponseDTO> updateUser(
            @PathVariable UUID userId,
            @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        User user = resolveAdminUser(userId, principal.getPrincipalId());
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getOrganizationId() != null) {
            user.setOrganizationId(request.getOrganizationId());
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
        User updated = userRepository.save(user);
        return ResponseEntity.ok(new MessageResponseDTO("User updated successfully", "SUCCESS", updated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<MessageResponseDTO> deleteUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        User user = resolveAdminUser(userId, principal.getPrincipalId());
        userRepository.delete(user);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("deletedUserId", userId);
        return ResponseEntity.ok(new MessageResponseDTO("User deleted successfully", "SUCCESS", body));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/access/assign")
    public ResponseEntity<MessageResponseDTO> assignAccess(
            @RequestBody AccessAssignmentRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        UUID adminId = principal.getPrincipalId();
        AccessPrincipalType principalType = parsePrincipalType(request.getPrincipalType());
        if (principalType == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("Invalid principalType", "FAILED", Collections.emptyMap()));
        }
        if (principalType != AccessPrincipalType.USER) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("Admin can assign access only to USER", "FAILED", Collections.emptyMap()));
        }
        List<AccessScopeRequest> scopes = resolveScopes(request);
        if (scopes.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("At least one siteId/zoneId scope is required", "FAILED", Collections.emptyMap()));
        }
        List<SiteZoneAccess> created = new ArrayList<>();
        for (AccessScopeRequest scope : scopes) {
            SiteZoneAccess access = adminService.assignSiteZoneAccess(
                    adminId,
                    principalType,
                    request.getPrincipalId(),
                    scope.getSiteId(),
                    scope.getZoneId()
            );
            created.add(access);
        }
        return ResponseEntity.ok(new MessageResponseDTO("Access assigned successfully", "SUCCESS", created));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/access/revoke")
    public ResponseEntity<MessageResponseDTO> revokeAccess(
            @RequestBody AccessAssignmentRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        UUID adminId = principal.getPrincipalId();
        AccessPrincipalType principalType = parsePrincipalType(request.getPrincipalType());
        if (principalType == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("Invalid principalType", "FAILED", Collections.emptyMap()));
        }
        if (principalType != AccessPrincipalType.USER) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("Admin can revoke access only for USER", "FAILED", Collections.emptyMap()));
        }
        List<AccessScopeRequest> scopes = resolveScopes(request);
        if (scopes.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("At least one siteId/zoneId scope is required", "FAILED", Collections.emptyMap()));
        }
        for (AccessScopeRequest scope : scopes) {
            adminService.revokeSiteZoneAccess(
                    principalType,
                    request.getPrincipalId(),
                    scope.getSiteId(),
                    scope.getZoneId()
            );
        }
        return ResponseEntity.ok(new MessageResponseDTO("Access revoked successfully", "SUCCESS", Collections.emptyMap()));
    }

    private AccessPrincipalType parsePrincipalType(String principalType) {
        if (principalType == null || principalType.isBlank()) {
            return null;
        }
        try {
            return AccessPrincipalType.valueOf(principalType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private User resolveAdminUser(UUID userId, UUID adminId) {
        return userRepository.findByIdAndAdminIdAndRole(userId, adminId, AccessPrincipalType.USER)
                .orElseThrow(() -> new IllegalArgumentException("User not found for this admin: " + userId));
    }

    private List<AccessScopeRequest> resolveScopes(AccessAssignmentRequest request) {
        if (request.getScopes() != null && !request.getScopes().isEmpty()) {
            return request.getScopes().stream()
                    .filter(scope -> scope.getSiteId() != null || scope.getZoneId() != null)
                    .toList();
        }
        return Collections.emptyList();
    }
}
