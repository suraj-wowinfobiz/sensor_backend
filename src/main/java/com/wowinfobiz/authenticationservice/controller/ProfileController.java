package com.wowinfobiz.authenticationservice.controller;

import com.wowinfobiz.authenticationservice.dto.MessageResponseDTO;
import com.wowinfobiz.authenticationservice.dto.UserUpdateRequest;
import com.wowinfobiz.authenticationservice.model.User;
import com.wowinfobiz.authenticationservice.repo.UserRepository;
import com.wowinfobiz.authenticationservice.security.CustomUserDetails;
import java.util.Collections;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/me")
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<MessageResponseDTO> getCurrentProfile(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }

        User user = userRepository.findByIdAndRole(principal.getPrincipalId(), principal.getPrincipalType())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        return ResponseEntity.ok(new MessageResponseDTO("Profile fetched successfully", "SUCCESS", user));
    }

    @PutMapping
    public ResponseEntity<MessageResponseDTO> updateCurrentProfile(
            @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }

        User user = userRepository.findByIdAndRole(principal.getPrincipalId(), principal.getPrincipalType())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

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
        return ResponseEntity.ok(new MessageResponseDTO("Profile updated successfully", "SUCCESS", updated));
    }
}
