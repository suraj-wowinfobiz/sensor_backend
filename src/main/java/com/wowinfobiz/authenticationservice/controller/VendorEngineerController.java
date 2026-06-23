package com.wowinfobiz.authenticationservice.controller;

import com.wowinfobiz.authenticationservice.dto.MessageResponseDTO;
import com.wowinfobiz.authenticationservice.dto.UserUpdateRequest;
import com.wowinfobiz.authenticationservice.dto.VendorEngineerCreateRequest;
import com.wowinfobiz.authenticationservice.model.User;
import com.wowinfobiz.authenticationservice.security.CustomUserDetails;
import com.wowinfobiz.authenticationservice.services.VendorEngineerService;
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
@RequestMapping("/api/v1/vendors-engineer")
public class VendorEngineerController {

    private final VendorEngineerService vendorEngineerService;
    private final PasswordEncoder passwordEncoder;

    public VendorEngineerController(
            VendorEngineerService vendorEngineerService,
            PasswordEncoder passwordEncoder
    ) {
        this.vendorEngineerService = vendorEngineerService;
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("hasRole('VENDOR')")
    @PostMapping("/engineers")
    public ResponseEntity<MessageResponseDTO> createVendorEngineer(
            @RequestBody VendorEngineerCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        UUID vendorId = principal.getPrincipalId();
        User vendorEngineer = new User();
        vendorEngineer.setName(request.getName());
        vendorEngineer.setEmail(request.getEmail());
        vendorEngineer.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        vendorEngineer.setOrganizationId(request.getOrganizationId());
        User created = vendorEngineerService.createVendorEngineer(vendorId, vendorEngineer);
        return ResponseEntity.ok(new MessageResponseDTO("Vendor engineer created successfully", "SUCCESS", created));
    }

    @PreAuthorize("hasRole('VENDOR')")
    @GetMapping("/engineers/get-all")
    public ResponseEntity<MessageResponseDTO> getAllVendorEngineers(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        UUID vendorId = principal.getPrincipalId();
        List<User> engineers = vendorEngineerService.getAllVendorEngineers(vendorId);
        return ResponseEntity.ok(new MessageResponseDTO("Vendor engineers fetched successfully", "SUCCESS", engineers));
    }

    @PreAuthorize("hasRole('VENDOR')")
    @GetMapping("/engineers/{engineerId}")
    public ResponseEntity<MessageResponseDTO> getVendorEngineerById(
            @PathVariable UUID engineerId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        UUID vendorId = principal.getPrincipalId();
        User engineer = vendorEngineerService.getVendorEngineer(vendorId, engineerId);
        return ResponseEntity.ok(new MessageResponseDTO("Vendor engineer fetched successfully", "SUCCESS", engineer));
    }

    @PreAuthorize("hasRole('VENDOR')")
    @PutMapping("/engineers/{engineerId}")
    public ResponseEntity<MessageResponseDTO> updateVendorEngineer(
            @PathVariable UUID engineerId,
            @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        UUID vendorId = principal.getPrincipalId();
        User vendorEngineer = new User();
        if (request.getName() != null && !request.getName().isBlank()) {
            vendorEngineer.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            vendorEngineer.setEmail(request.getEmail());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            vendorEngineer.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getOrganizationId() != null) {
            vendorEngineer.setOrganizationId(request.getOrganizationId());
        }
        User updated = vendorEngineerService.updateVendorEngineer(
                vendorId,
                engineerId,
                vendorEngineer,
                request.getActive()
        );
        return ResponseEntity.ok(new MessageResponseDTO("Vendor engineer updated successfully", "SUCCESS", updated));
    }

    @PreAuthorize("hasRole('VENDOR')")
    @DeleteMapping("/engineers/{engineerId}")
    public ResponseEntity<MessageResponseDTO> deleteVendorEngineer(
            @PathVariable UUID engineerId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        UUID vendorId = principal.getPrincipalId();
        vendorEngineerService.deleteVendorEngineer(vendorId, engineerId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("deletedVendorEngineerId", engineerId);
        return ResponseEntity.ok(new MessageResponseDTO("Vendor engineer deleted successfully", "SUCCESS", body));
    }
}
