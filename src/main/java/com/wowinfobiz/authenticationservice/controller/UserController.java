package com.wowinfobiz.authenticationservice.controller;

import com.wowinfobiz.authenticationservice.dto.MessageResponseDTO;
import com.wowinfobiz.authenticationservice.dto.UserCreateRequest;
import com.wowinfobiz.authenticationservice.dto.UserUpdateRequest;
import com.wowinfobiz.authenticationservice.enums.AccessPrincipalType;
import com.wowinfobiz.authenticationservice.model.User;
import com.wowinfobiz.authenticationservice.repo.UserRepository;
import com.wowinfobiz.authenticationservice.repo.UserSensorAccessRepository;
import com.wowinfobiz.authenticationservice.security.CustomUserDetails;
import com.wowinfobiz.authenticationservice.services.AdminService;
import com.wowinfobiz.authenticationservice.services.SuperAdminService;

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
@RequestMapping("/api/v1/users")
public class UserController {

    private final AdminService adminService;
    private final SuperAdminService superAdminService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserSensorAccessRepository userSensorAccessRepository;

    public UserController(
            AdminService adminService,
            SuperAdminService superAdminService,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            UserSensorAccessRepository userSensorAccessRepository
    ) {
        this.adminService = adminService;
        this.superAdminService = superAdminService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userSensorAccessRepository = userSensorAccessRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','VENDOR')")
    @GetMapping("/get-all")
    public ResponseEntity<List<User>> getAllUsers(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403).body(Collections.emptyList());
        }
        if (principal.getPrincipalType() == AccessPrincipalType.SUPER_ADMIN) {
            return ResponseEntity.ok(userRepository.findAll());
        }
        if (principal.getPrincipalType() == AccessPrincipalType.ADMIN) {
            return ResponseEntity.ok(
                    userRepository.findByAdminIdAndRole(principal.getPrincipalId(), AccessPrincipalType.USER)
            );
        }
        if (principal.getPrincipalType() == AccessPrincipalType.VENDOR) {
            return ResponseEntity.ok(
                    userRepository.findByVendorIdAndRole(principal.getPrincipalId(), AccessPrincipalType.VENDOR_ENGINEER)
            );
        }
        return ResponseEntity.ok(Collections.emptyList());
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<MessageResponseDTO> getUserById(
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        User user = resolveUserByScope(userId, principal);
        return ResponseEntity.ok(new MessageResponseDTO("User fetched successfully", "SUCCESS", user));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<MessageResponseDTO> createUser(
            @RequestBody UserCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        if (principal.getPrincipalType() == AccessPrincipalType.SUPER_ADMIN) {
            if (request.getOrganizationId() == null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponseDTO(
                                "organizationId is required for SUPER_ADMIN admin creation",
                                "FAILED",
                                Collections.emptyMap()
                        ));
            }
            User admin = new User();
            admin.setName(request.getName());
            admin.setEmail(request.getEmail());
            admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            admin.setOrganizationId(request.getOrganizationId());
            admin.setMaxUsersAllowed(request.getMaxUsersAllowed() == null ? 0 : request.getMaxUsersAllowed());
            User createdAdmin = superAdminService.createAdmin(principal.getPrincipalId(), admin);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("adminId", createdAdmin.getId());
            body.put("admin", createdAdmin);
            return ResponseEntity.ok(new MessageResponseDTO("Admin created successfully", "SUCCESS", body));
        }
        if (principal.getPrincipalType() != AccessPrincipalType.ADMIN) {
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

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PutMapping("/{userId}")
    public ResponseEntity<MessageResponseDTO> updateUser(
            @PathVariable UUID userId,
            @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        User user = resolveUserByScope(userId, principal);
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
        if (request.getMaxUsersAllowed() != null) {
            if (request.getMaxUsersAllowed() < 0) {
                throw new IllegalArgumentException("maxUsersAllowed must be 0 or greater.");
            }
            user.setMaxUsersAllowed(request.getMaxUsersAllowed());
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        User updated = userRepository.save(user);
        return ResponseEntity.ok(new MessageResponseDTO("User updated successfully", "SUCCESS", updated));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<MessageResponseDTO> deleteUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        User user = resolveUserByScope(userId, principal);
        userSensorAccessRepository.deleteByUserId(user.getId());
        userRepository.delete(user);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("deletedUserId", userId);
        return ResponseEntity.ok(new MessageResponseDTO("User deleted successfully", "SUCCESS", body));
    }

    private User resolveUserByScope(UUID userId, CustomUserDetails principal) {
        if (principal.getPrincipalType() == AccessPrincipalType.SUPER_ADMIN) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        }
        if (principal.getPrincipalType() == AccessPrincipalType.ADMIN) {
            User user = userRepository.findByIdAndRole(userId, AccessPrincipalType.USER)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            if (!principal.getPrincipalId().equals(user.getAdminId())) {
                throw new IllegalArgumentException("User does not belong to this admin: " + userId);
            }
            return user;
        }
        throw new IllegalArgumentException("Unsupported principal type for this endpoint.");
    }
}
