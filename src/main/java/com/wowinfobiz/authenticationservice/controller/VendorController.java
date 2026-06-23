package com.wowinfobiz.authenticationservice.controller;

import com.wowinfobiz.authenticationservice.dto.MessageResponseDTO;
import com.wowinfobiz.authenticationservice.dto.UserUpdateRequest;
import com.wowinfobiz.authenticationservice.dto.VendorCreateRequest;
import com.wowinfobiz.authenticationservice.model.User;
import com.wowinfobiz.authenticationservice.services.VendorService;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/vendors")
public class VendorController {

    private final VendorService vendorService;
    private final PasswordEncoder passwordEncoder;

    public VendorController(VendorService vendorService, PasswordEncoder passwordEncoder) {
        this.vendorService = vendorService;
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<MessageResponseDTO> createVendor(@RequestBody VendorCreateRequest request) {
        User vendor = new User();
        vendor.setName(request.getName());
        vendor.setEmail(request.getEmail());
        vendor.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        vendor.setOrganizationId(request.getOrganizationId());
        if (vendor.getOrganizationId() == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("organizationId is required for vendor", "FAILED", Collections.emptyMap()));
        }
        User created = vendorService.createVendor(vendor);
        return ResponseEntity.ok(new MessageResponseDTO("Vendor created successfully", "SUCCESS", created));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/get-all")
    public ResponseEntity<MessageResponseDTO> getAllVendors() {
        List<User> vendors = vendorService.getAllVendors();
        return ResponseEntity.ok(new MessageResponseDTO("Vendors fetched successfully", "SUCCESS", vendors));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{vendorId}")
    public ResponseEntity<MessageResponseDTO> getVendorById(@PathVariable UUID vendorId) {
        User vendor = vendorService.getVendor(vendorId);
        return ResponseEntity.ok(new MessageResponseDTO("Vendor fetched successfully", "SUCCESS", vendor));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{vendorId}")
    public ResponseEntity<MessageResponseDTO> updateVendor(
            @PathVariable UUID vendorId,
            @RequestBody UserUpdateRequest request
    ) {
        User vendor = new User();
        if (request.getName() != null && !request.getName().isBlank()) {
            vendor.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            vendor.setEmail(request.getEmail());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            vendor.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getOrganizationId() != null) {
            vendor.setOrganizationId(request.getOrganizationId());
        }
        User updated = vendorService.updateVendor(vendorId, vendor, request.getActive());
        return ResponseEntity.ok(new MessageResponseDTO("Vendor updated successfully", "SUCCESS", updated));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{vendorId}")
    public ResponseEntity<MessageResponseDTO> deleteVendor(@PathVariable UUID vendorId) {
        vendorService.deleteVendor(vendorId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("deletedVendorId", vendorId);
        return ResponseEntity.ok(new MessageResponseDTO("Vendor deleted successfully", "SUCCESS", body));
    }
}
