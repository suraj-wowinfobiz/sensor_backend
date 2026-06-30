package com.wowinfobiz.authenticationservice.controller;

import com.wowinfobiz.authenticationservice.dto.MessageResponseDTO;
import com.wowinfobiz.authenticationservice.dto.UserSensorAccessRequest;
import com.wowinfobiz.authenticationservice.enums.AccessPrincipalType;
import com.wowinfobiz.authenticationservice.model.User;
import com.wowinfobiz.authenticationservice.model.UserSensorAccess;
import com.wowinfobiz.authenticationservice.repo.UserRepository;
import com.wowinfobiz.authenticationservice.repo.UserSensorAccessRepository;
import com.wowinfobiz.authenticationservice.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping
public class UserSensorAccessController {

    private final UserRepository userRepository;
    private final UserSensorAccessRepository userSensorAccessRepository;

    public UserSensorAccessController(UserRepository userRepository,
                                      UserSensorAccessRepository userSensorAccessRepository) {
        this.userRepository = userRepository;
        this.userSensorAccessRepository = userSensorAccessRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping("/api/v1/users/{userId}/sensor-access")
    public ResponseEntity<MessageResponseDTO> getUserSensorAccess(@PathVariable UUID userId,
                                                                  @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return forbidden();
        }
        User user = resolveManagedUser(userId, principal);
        return ResponseEntity.ok(new MessageResponseDTO("User sensor access fetched successfully", "SUCCESS", buildResponse(user)));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PutMapping("/api/v1/users/{userId}/sensor-access")
    public ResponseEntity<MessageResponseDTO> updateUserSensorAccess(@PathVariable UUID userId,
                                                                     @RequestBody UserSensorAccessRequest request,
                                                                     @AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return forbidden();
        }
        User user = resolveManagedUser(userId, principal);
        userSensorAccessRepository.deleteByUserId(user.getId());

        Set<UUID> sensorIds = normalizeSensorIds(request == null ? null : request.getSensorIds());
        for (UUID sensorId : sensorIds) {
            UserSensorAccess access = new UserSensorAccess();
            access.setUserId(user.getId());
            access.setSensorId(sensorId);
            userSensorAccessRepository.save(access);
        }

        return ResponseEntity.ok(new MessageResponseDTO("User sensor access updated successfully", "SUCCESS", buildResponse(user)));
    }

    @GetMapping("/api/v1/auth/me/sensor-access")
    public ResponseEntity<MessageResponseDTO> getMySensorAccess(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            return forbidden();
        }
        User user = userRepository.findByIdAndRole(principal.getPrincipalId(), principal.getPrincipalType())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        return ResponseEntity.ok(new MessageResponseDTO("Current user sensor access fetched successfully", "SUCCESS", buildResponse(user)));
    }

    private User resolveManagedUser(UUID userId, CustomUserDetails principal) {
        if (principal.getPrincipalType() == AccessPrincipalType.SUPER_ADMIN) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        }
        if (principal.getPrincipalType() == AccessPrincipalType.ADMIN) {
            return userRepository.findByIdAndAdminIdAndRole(userId, principal.getPrincipalId(), AccessPrincipalType.USER)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        }
        throw new IllegalArgumentException("Unsupported principal type for this endpoint.");
    }

    private Map<String, Object> buildResponse(User user) {
        List<String> sensorIds = userSensorAccessRepository.findByUserId(user.getId()).stream()
                .map(UserSensorAccess::getSensorId)
                .map(UUID::toString)
                .toList();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("userId", user.getId());
        body.put("sensorIds", sensorIds);
        return body;
    }

    private Set<UUID> normalizeSensorIds(List<String> rawSensorIds) {
        if (rawSensorIds == null || rawSensorIds.isEmpty()) {
            return Collections.emptySet();
        }
        Set<UUID> sensorIds = new LinkedHashSet<>();
        for (String rawSensorId : rawSensorIds) {
            if (rawSensorId == null || rawSensorId.trim().isBlank()) {
                continue;
            }
            sensorIds.add(UUID.fromString(rawSensorId.trim()));
        }
        return sensorIds;
    }

    private ResponseEntity<MessageResponseDTO> forbidden() {
        return ResponseEntity.status(403)
                .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
    }
}
