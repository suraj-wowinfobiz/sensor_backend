package com.wowinfobiz.authenticationservice.controller;

import com.wowinfobiz.authenticationservice.dto.AuthResponse;
import com.wowinfobiz.authenticationservice.dto.LoginRequest;
import com.wowinfobiz.authenticationservice.dto.MessageResponseDTO;
import com.wowinfobiz.authenticationservice.dto.OtpRequest;
import com.wowinfobiz.authenticationservice.dto.SuperAdminOtpCreateRequest;
import com.wowinfobiz.authenticationservice.enums.AccessPrincipalType;
import com.wowinfobiz.authenticationservice.model.User;
import com.wowinfobiz.authenticationservice.security.CustomUserDetails;
import com.wowinfobiz.authenticationservice.security.CustomUserDetailsService;
import com.wowinfobiz.authenticationservice.security.JwtService;
import com.wowinfobiz.authenticationservice.security.OtpService;
import com.wowinfobiz.authenticationservice.services.SuperAdminService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final SuperAdminService superAdminService;
    private final String cookieName;
    private final boolean cookieSecure;
    private final int cookieMaxAgeSeconds;
    private final String superAdminEmail;

    public AuthController(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            OtpService otpService,
            SuperAdminService superAdminService,
            @Value("${security.jwt.cookie-name}") String cookieName,
            @Value("${security.jwt.cookie-secure}") boolean cookieSecure,
            @Value("${security.jwt.cookie-max-age-seconds}") int cookieMaxAgeSeconds,
            @Value("${security.otp.super-admin-email}") String superAdminEmail
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.superAdminService = superAdminService;
        this.cookieName = cookieName;
        this.cookieSecure = cookieSecure;
        this.cookieMaxAgeSeconds = cookieMaxAgeSeconds;
        this.superAdminEmail = superAdminEmail;
    }

    @PostMapping("/login")
    public ResponseEntity<MessageResponseDTO> loginAccount(
            @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AccessPrincipalType role;
        try {
            role = parseRole(request.getRole());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("Invalid role provided", "FAILED", Collections.emptyMap()));
        }

        CustomUserDetails principal;
        try {
            principal = (CustomUserDetails) userDetailsService
                    .loadUserByEmailAndRole(request.getEmail(), role);
        } catch (Exception ex) {
            return ResponseEntity.status(401)
                    .body(new MessageResponseDTO("Invalid credentials", "FAILED", Collections.emptyMap()));
        }

        if (!passwordEncoder.matches(request.getPassword(), principal.getPassword())) {
            return ResponseEntity.status(401)
                    .body(new MessageResponseDTO("Invalid credentials", "FAILED", Collections.emptyMap()));
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("principalId", principal.getPrincipalId());
        claims.put("principalType", principal.getPrincipalType().name());
        String token = jwtService.generateToken(principal.getUsername(), claims);

        ResponseCookie cookie = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(cookieMaxAgeSeconds)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        AuthResponse authResponse = new AuthResponse(principal.getPrincipalId(), principal.getPrincipalType().name(), token);
        return ResponseEntity.ok(new MessageResponseDTO("Login successful", "SUCCESS", authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDTO> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(new MessageResponseDTO("Logout successful", "SUCCESS", Collections.emptyMap()));
    }

    @PostMapping("/otp/super-admin")
    public ResponseEntity<MessageResponseDTO> sendSuperAdminOtp(@RequestBody OtpRequest request) {
        String configuredEmail = normalizeEmail(superAdminEmail);
        if (configuredEmail == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("Super admin email is not configured", "FAILED", Collections.emptyMap()));
        }
        otpService.sendOtp(configuredEmail);
        return ResponseEntity.accepted()
                .body(new MessageResponseDTO("OTP sent successfully", "SUCCESS", Collections.emptyMap()));
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }

    private AccessPrincipalType parseRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("role is required");
        }
        return AccessPrincipalType.valueOf(role.trim().toUpperCase());
    }

    @PostMapping("/verified")
    public ResponseEntity<MessageResponseDTO> createVerifiedSuperAdmin(
            @RequestBody SuperAdminOtpCreateRequest request
    ) {
        String configuredEmail = normalizeEmail(superAdminEmail);
        String requestedEmail = normalizeEmail(request.getEmail());
        if (requestedEmail == null || configuredEmail == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("Email is required", "FAILED", Collections.emptyMap()));
        }
        if (request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("Name is required", "FAILED", Collections.emptyMap()));
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("Password is required", "FAILED", Collections.emptyMap()));
        }
        if (request.getOtp() == null || request.getOtp().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("OTP is required", "FAILED", Collections.emptyMap()));
        }
        if (!otpService.verifyOtp(configuredEmail, request.getOtp())) {
            return ResponseEntity.status(401)
                    .body(new MessageResponseDTO("Invalid OTP", "FAILED", Collections.emptyMap()));
        }
        User superAdmin = new User();
        superAdmin.setName(request.getName());
        superAdmin.setEmail(requestedEmail);
        superAdmin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        User created = superAdminService.createSuperAdmin(superAdmin);
        return ResponseEntity.ok(new MessageResponseDTO("Super admin created successfully", "SUCCESS", created));
    }

}
