package com.wowinfobiz.authenticationservice.controller;

import com.wowinfobiz.authenticationservice.dto.AdminCreateRequest;
import com.wowinfobiz.authenticationservice.dto.AccessAssignmentRequest;
import com.wowinfobiz.authenticationservice.dto.AccessScopeRequest;
import com.wowinfobiz.authenticationservice.dto.MessageResponseDTO;
import com.wowinfobiz.authenticationservice.dto.SuperAdminCreateRequest;
import com.wowinfobiz.authenticationservice.dto.UserCreateRequest;
import com.wowinfobiz.authenticationservice.dto.UserUpdateRequest;
import com.wowinfobiz.authenticationservice.enums.AccessPrincipalType;
import com.wowinfobiz.authenticationservice.model.SiteZoneAccess;
import com.wowinfobiz.authenticationservice.model.User;
import com.wowinfobiz.authenticationservice.repo.UserRepository;
import com.wowinfobiz.authenticationservice.security.CustomUserDetails;
import com.wowinfobiz.authenticationservice.services.SuperAdminService;
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
@RequestMapping("/api/v1/super-admins")
public class SuperAdminController {

    private final SuperAdminService superAdminService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public SuperAdminController(
            SuperAdminService superAdminService,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository
    ) {
        this.superAdminService = superAdminService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<MessageResponseDTO> createSuperAdmin(@RequestBody SuperAdminCreateRequest request) {
        return ResponseEntity.status(403)
                .body(new MessageResponseDTO("Use OTP verified route at /api/v1/auth/verified", "FAILED", Collections.emptyMap()));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/admins")
    public ResponseEntity<MessageResponseDTO> createAdmin(
            @RequestBody AdminCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        UUID superAdminId = principal.getPrincipalId();
        User admin = new User();
        admin.setName(request.getName());
        admin.setEmail(request.getEmail());
        admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        admin.setMaxUsersAllowed(request.getMaxUsersAllowed());
        admin.setOrganizationId(request.getOrganizationId());
        User created = superAdminService.createAdmin(superAdminId, admin);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("adminId", created.getId());
        body.put("admin", created);
        return ResponseEntity.ok(new MessageResponseDTO("Admin created successfully", "SUCCESS", body));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/admins/get-all")
    public ResponseEntity<MessageResponseDTO> getAllAdmins() {
        List<User> admins = userRepository.findByRole(AccessPrincipalType.ADMIN);
        return ResponseEntity.ok(new MessageResponseDTO("Admins fetched successfully", "SUCCESS", admins));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/admins/{adminId}")
    public ResponseEntity<MessageResponseDTO> getAdminById(@PathVariable UUID adminId) {
        User admin = resolveAdmin(adminId);
        return ResponseEntity.ok(new MessageResponseDTO("Admin fetched successfully", "SUCCESS", admin));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/admins/{adminId}")
    public ResponseEntity<MessageResponseDTO> updateAdmin(
            @PathVariable UUID adminId,
            @RequestBody UserUpdateRequest request
    ) {
        User admin = resolveAdmin(adminId);
        if (request.getName() != null && !request.getName().isBlank()) {
            admin.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            admin.setEmail(request.getEmail());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getOrganizationId() != null) {
            admin.setOrganizationId(request.getOrganizationId());
        }
        if (request.getMaxUsersAllowed() != null) {
            if (request.getMaxUsersAllowed() < 0) {
                throw new IllegalArgumentException("maxUsersAllowed must be 0 or greater.");
            }
            admin.setMaxUsersAllowed(request.getMaxUsersAllowed());
        }
        if (request.getActive() != null) {
            admin.setActive(request.getActive());
        }
        User updated = userRepository.save(admin);
        return ResponseEntity.ok(new MessageResponseDTO("Admin updated successfully", "SUCCESS", updated));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/admins/{adminId}")
    public ResponseEntity<MessageResponseDTO> deleteAdmin(@PathVariable UUID adminId) {
        User admin = resolveAdmin(adminId);
        userRepository.delete(admin);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("deletedAdminId", adminId);
        return ResponseEntity.ok(new MessageResponseDTO("Admin deleted successfully", "SUCCESS", body));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/admins/{adminId}/users")
    public ResponseEntity<MessageResponseDTO> createUser(
            @PathVariable UUID adminId,
            @RequestBody UserCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        UUID superAdminId = principal.getPrincipalId();
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setOrganizationId(request.getOrganizationId());
        User created = superAdminService.createUserForAdmin(superAdminId, adminId, user);
        return ResponseEntity.ok(new MessageResponseDTO("User created successfully", "SUCCESS", created));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/admins/{adminId}/users/get-all")
    public ResponseEntity<MessageResponseDTO> getAllUsersByAdmin(@PathVariable UUID adminId) {
        resolveAdmin(adminId);
        List<User> users = userRepository.findByAdminIdAndRole(adminId, AccessPrincipalType.USER);
        return ResponseEntity.ok(new MessageResponseDTO("Users fetched successfully", "SUCCESS", users));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/admins/{adminId}/users/{userId}")
    public ResponseEntity<MessageResponseDTO> getUserById(
            @PathVariable UUID adminId,
            @PathVariable UUID userId
    ) {
        resolveAdmin(adminId);
        User user = resolveAdminUser(userId, adminId);
        return ResponseEntity.ok(new MessageResponseDTO("User fetched successfully", "SUCCESS", user));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/admins/{adminId}/users/{userId}")
    public ResponseEntity<MessageResponseDTO> updateUser(
            @PathVariable UUID adminId,
            @PathVariable UUID userId,
            @RequestBody UserUpdateRequest request
    ) {
        resolveAdmin(adminId);
        User user = resolveAdminUser(userId, adminId);
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

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/admins/{adminId}/users/{userId}")
    public ResponseEntity<MessageResponseDTO> deleteUser(
            @PathVariable UUID adminId,
            @PathVariable UUID userId
    ) {
        resolveAdmin(adminId);
        User user = resolveAdminUser(userId, adminId);
        userRepository.delete(user);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("deletedUserId", userId);
        return ResponseEntity.ok(new MessageResponseDTO("User deleted successfully", "SUCCESS", body));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/access/assign")
    public ResponseEntity<MessageResponseDTO> assignAccess(
            @RequestBody AccessAssignmentRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        UUID superAdminId = principal.getPrincipalId();
        AccessPrincipalType principalType = parsePrincipalType(request.getPrincipalType());
        if (principalType == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("Invalid principalType", "FAILED", Collections.emptyMap()));
        }
        List<AccessScopeRequest> scopes = resolveScopes(request);
        if (scopes.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("At least one siteId/zoneId scope is required", "FAILED", Collections.emptyMap()));
        }
        List<SiteZoneAccess> created = new ArrayList<>();
        for (AccessScopeRequest scope : scopes) {
            SiteZoneAccess access = superAdminService.assignSiteZoneAccess(
                    superAdminId,
                    principalType,
                    request.getPrincipalId(),
                    scope.getSiteId(),
                    scope.getZoneId()
            );
            created.add(access);
        }
        return ResponseEntity.ok(new MessageResponseDTO("Access assigned successfully", "SUCCESS", created));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/access/revoke")
    public ResponseEntity<MessageResponseDTO> revokeAccess(
            @RequestBody AccessAssignmentRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        UUID superAdminId = principal.getPrincipalId();
        AccessPrincipalType principalType = parsePrincipalType(request.getPrincipalType());
        if (principalType == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("Invalid principalType", "FAILED", Collections.emptyMap()));
        }
        List<AccessScopeRequest> scopes = resolveScopes(request);
        if (scopes.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("At least one siteId/zoneId scope is required", "FAILED", Collections.emptyMap()));
        }
        for (AccessScopeRequest scope : scopes) {
            superAdminService.revokeSiteZoneAccess(
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

    private User resolveAdmin(UUID adminId) {
        return userRepository.findByIdAndRole(adminId, AccessPrincipalType.ADMIN)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + adminId));
    }

    private User resolveAdminUser(UUID userId, UUID adminId) {
        return userRepository.findByIdAndAdminIdAndRole(userId, adminId, AccessPrincipalType.USER)
                .orElseThrow(() -> new IllegalArgumentException("User not found for admin: " + userId));
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
