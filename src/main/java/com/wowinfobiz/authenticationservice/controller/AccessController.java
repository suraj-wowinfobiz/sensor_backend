package com.wowinfobiz.authenticationservice.controller;

import com.wowinfobiz.authenticationservice.dto.AccessAssignmentRequest;
import com.wowinfobiz.authenticationservice.dto.AccessHierarchyResponse;
import com.wowinfobiz.authenticationservice.dto.AccessJoinedResponse;
import com.wowinfobiz.authenticationservice.dto.AccessScopeRequest;
import com.wowinfobiz.authenticationservice.dto.AccessUpdateRequest;
import com.wowinfobiz.authenticationservice.dto.MessageResponseDTO;
import com.wowinfobiz.authenticationservice.enums.AccessPrincipalType;
import com.wowinfobiz.authenticationservice.model.SiteZoneAccess;
import com.wowinfobiz.authenticationservice.model.User;
import com.wowinfobiz.authenticationservice.repo.AccessJoinedProjection;
import com.wowinfobiz.authenticationservice.repo.AccessOrganizationHierarchyProjection;
import com.wowinfobiz.authenticationservice.repo.SiteZoneAccessRepository;
import com.wowinfobiz.authenticationservice.repo.UserRepository;
import com.wowinfobiz.authenticationservice.security.CustomUserDetails;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/access")
public class AccessController {

    private final SiteZoneAccessRepository siteZoneAccessRepository;
    private final UserRepository userRepository;

    public AccessController(SiteZoneAccessRepository siteZoneAccessRepository, UserRepository userRepository) {
        this.siteZoneAccessRepository = siteZoneAccessRepository;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<MessageResponseDTO> createAccess(
            @RequestBody AccessAssignmentRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        AccessPrincipalType principalType = parsePrincipalType(request.getPrincipalType());
        if (principalType == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("Invalid principalType", "FAILED", Collections.emptyMap()));
        }
        if (principal.getPrincipalType() == AccessPrincipalType.ADMIN
                && principalType != AccessPrincipalType.USER) {
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
            SiteZoneAccess access = new SiteZoneAccess();
            access.setPrincipalType(principalType);
            access.setPrincipalId(request.getPrincipalId());
            access.setSiteId(scope.getSiteId());
            access.setZoneId(scope.getZoneId());
            access.setAssignedByType(principal.getPrincipalType());
            access.setAssignedById(principal.getPrincipalId());
            created.add(siteZoneAccessRepository.save(access));
        }

        return ResponseEntity.ok(new MessageResponseDTO("Access created successfully", "SUCCESS", created));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','VENDOR','VENDOR_ENGINEER')")
    @GetMapping
    public ResponseEntity<MessageResponseDTO> getAllAccess(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }

        List<AccessOrganizationHierarchyProjection> rows = loadAccessHierarchyRows(principal);
        List<AccessHierarchyResponse> accessList = toHierarchyResponse(rows);
        return ResponseEntity.ok(new MessageResponseDTO("Access list fetched successfully", "SUCCESS", accessList));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping("/details")
    public ResponseEntity<MessageResponseDTO> getAllAccessWithDetails(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }

        List<AccessJoinedProjection> rows = principal.getPrincipalType() == AccessPrincipalType.SUPER_ADMIN
                ? siteZoneAccessRepository.findAllAccessWithDetails()
                : siteZoneAccessRepository.findAllAccessWithDetailsByAssigner(
                        AccessPrincipalType.ADMIN.name(),
                        principal.getPrincipalId()
                );

        List<AccessJoinedResponse> body = rows.stream().map(this::toJoinedResponse).toList();
        return ResponseEntity.ok(new MessageResponseDTO("Access list with details fetched successfully", "SUCCESS", body));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping("/{accessId}")
    public ResponseEntity<MessageResponseDTO> getAccessById(
            @PathVariable UUID accessId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        SiteZoneAccess access = resolveAccessByScope(accessId, principal);
        return ResponseEntity.ok(new MessageResponseDTO("Access fetched successfully", "SUCCESS", access));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @PutMapping("/{accessId}")
    public ResponseEntity<MessageResponseDTO> updateAccess(
            @PathVariable UUID accessId,
            @RequestBody AccessUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        SiteZoneAccess access = resolveAccessByScope(accessId, principal);

        AccessPrincipalType updatedPrincipalType = request.getPrincipalType() == null
                ? access.getPrincipalType()
                : parsePrincipalType(request.getPrincipalType());
        if (updatedPrincipalType == null) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("Invalid principalType", "FAILED", Collections.emptyMap()));
        }
        if (principal.getPrincipalType() == AccessPrincipalType.ADMIN
                && updatedPrincipalType != AccessPrincipalType.USER) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponseDTO("Admin can update access only for USER", "FAILED", Collections.emptyMap()));
        }

        access.setPrincipalType(updatedPrincipalType);
        if (request.getPrincipalId() != null) {
            access.setPrincipalId(request.getPrincipalId());
        }
        if (request.getSiteId() != null) {
            access.setSiteId(request.getSiteId());
        }
        if (request.getZoneId() != null) {
            access.setZoneId(request.getZoneId());
        }

        SiteZoneAccess updated = siteZoneAccessRepository.save(access);
        return ResponseEntity.ok(new MessageResponseDTO("Access updated successfully", "SUCCESS", updated));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{accessId}")
    public ResponseEntity<MessageResponseDTO> deleteAccess(
            @PathVariable UUID accessId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(403)
                    .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
        }
        SiteZoneAccess access = resolveAccessByScope(accessId, principal);
        siteZoneAccessRepository.delete(access);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("deletedAccessId", accessId);
        return ResponseEntity.ok(new MessageResponseDTO("Access deleted successfully", "SUCCESS", body));
    }

    private SiteZoneAccess resolveAccessByScope(UUID accessId, CustomUserDetails principal) {
        if (principal.getPrincipalType() == AccessPrincipalType.SUPER_ADMIN) {
            return siteZoneAccessRepository.findById(accessId)
                    .orElseThrow(() -> new IllegalArgumentException("Access not found: " + accessId));
        }
        if (principal.getPrincipalType() == AccessPrincipalType.ADMIN) {
            return siteZoneAccessRepository.findByIdAndAssignedByTypeAndAssignedById(
                            accessId,
                            AccessPrincipalType.ADMIN,
                            principal.getPrincipalId()
                    )
                    .orElseThrow(() -> new IllegalArgumentException("Access not found: " + accessId));
        }
        throw new IllegalArgumentException("Unsupported principal type for this endpoint.");
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

    private List<AccessScopeRequest> resolveScopes(AccessAssignmentRequest request) {
        if (request.getScopes() != null && !request.getScopes().isEmpty()) {
            return request.getScopes().stream()
                    .filter(scope -> scope.getSiteId() != null || scope.getZoneId() != null)
                    .toList();
        }
        return Collections.emptyList();
    }

    private AccessJoinedResponse toJoinedResponse(AccessJoinedProjection row) {
        AccessJoinedResponse response = new AccessJoinedResponse();
        response.setAccessId(row.getAccessId());
        response.setPrincipalType(row.getPrincipalType());
        response.setPrincipalId(row.getPrincipalId());
        response.setPrincipalName(row.getPrincipalName());
        response.setPrincipalOrganizationId(row.getPrincipalOrganizationId());
        response.setAssignedByType(row.getAssignedByType());
        response.setAssignedById(row.getAssignedById());
        response.setAssignedAt(row.getAssignedAt());
        response.setOrganizationId(row.getOrganizationId());
        response.setOrganizationName(row.getOrganizationName());
        response.setSiteId(row.getSiteId());
        response.setSiteName(row.getSiteName());
        response.setSiteLocation(row.getSiteLocation());
        response.setZoneId(row.getZoneId());
        response.setZoneName(row.getZoneName());
        return response;
    }

    private List<AccessHierarchyResponse> toHierarchyResponse(List<AccessOrganizationHierarchyProjection> rows) {
        Map<UUID, User> principalsById = new LinkedHashMap<>();
        Set<UUID> principalIds = new HashSet<>();
        for (AccessOrganizationHierarchyProjection row : rows) {
            if (row.getPrincipalId() != null) {
                principalIds.add(row.getPrincipalId());
            }
        }
        for (User user : userRepository.findAllById(principalIds)) {
            principalsById.put(user.getId(), user);
        }

        Map<UUID, AccessHierarchyResponse> accessMap = new LinkedHashMap<>();
        Map<UUID, Map<UUID, AccessHierarchyResponse.Site>> sitesByAccessId = new LinkedHashMap<>();
        Map<UUID, Map<UUID, Set<UUID>>> zonesBySiteByAccessId = new LinkedHashMap<>();

        for (AccessOrganizationHierarchyProjection row : rows) {
            AccessHierarchyResponse access = accessMap.computeIfAbsent(row.getAccessId(), ignored -> {
                AccessHierarchyResponse dto = new AccessHierarchyResponse();
                dto.setId(row.getAccessId());
                dto.setPrincipalType(row.getPrincipalType());
                dto.setPrincipalId(row.getPrincipalId());
                dto.setAssignedByType(row.getAssignedByType());
                dto.setAssignedById(row.getAssignedById());
                dto.setAssignedAt(row.getAssignedAt());
                User principalUser = principalsById.get(row.getPrincipalId());
                dto.setName(principalUser == null ? null : principalUser.getName());
                dto.setEmail(principalUser == null ? null : principalUser.getEmail());
                dto.setPrincipal(toPrincipalDetails(principalUser));

                AccessHierarchyResponse.Organization organization = new AccessHierarchyResponse.Organization();
                organization.setOrganizationId(row.getOrganizationId());
                organization.setName(row.getOrganizationName());
                dto.setOrganization(organization);
                return dto;
            });

            if (row.getSiteId() == null) {
                continue;
            }

            Map<UUID, AccessHierarchyResponse.Site> siteMap = sitesByAccessId.computeIfAbsent(
                    row.getAccessId(),
                    ignored -> new LinkedHashMap<>()
            );
            Map<UUID, Set<UUID>> zoneMap = zonesBySiteByAccessId.computeIfAbsent(
                    row.getAccessId(),
                    ignored -> new LinkedHashMap<>()
            );

            AccessHierarchyResponse.Site site = siteMap.computeIfAbsent(row.getSiteId(), ignored -> {
                AccessHierarchyResponse.Site created = new AccessHierarchyResponse.Site();
                created.setSiteId(row.getSiteId());
                created.setName(row.getSiteName());
                created.setLocation(row.getSiteLocation());
                access.getOrganization().getSites().add(created);
                return created;
            });

            if (row.getZoneId() == null) {
                continue;
            }

            Set<UUID> seenZones = zoneMap.computeIfAbsent(row.getSiteId(), ignored -> new HashSet<>());
            if (!seenZones.add(row.getZoneId())) {
                continue;
            }
            AccessHierarchyResponse.Zone zone = new AccessHierarchyResponse.Zone();
            zone.setZoneId(row.getZoneId());
            zone.setName(row.getZoneName());
            site.getZones().add(zone);
        }

        return new ArrayList<>(accessMap.values());
    }

    private AccessHierarchyResponse.PrincipalDetails toPrincipalDetails(User user) {
        if (user == null) {
            return null;
        }
        AccessHierarchyResponse.PrincipalDetails principal = new AccessHierarchyResponse.PrincipalDetails();
        principal.setId(user.getId());
        principal.setName(user.getName());
        principal.setEmail(user.getEmail());
        principal.setRole(user.getRole() == null ? null : user.getRole().name());
        principal.setActive(user.isActive());
        principal.setAdminId(user.getAdminId());
        principal.setVendorId(user.getVendorId());
        principal.setMaxUsersAllowed(user.getMaxUsersAllowed());
        principal.setCreatedBySuperAdminId(user.getCreatedBySuperAdminId());
        principal.setOrganizationId(user.getOrganizationId());
        return principal;
    }

    private List<AccessOrganizationHierarchyProjection> loadAccessHierarchyRows(CustomUserDetails principal) {
        return switch (principal.getPrincipalType()) {
            case SUPER_ADMIN -> siteZoneAccessRepository.findAllAccessWithOrganizationHierarchy();
            case ADMIN -> siteZoneAccessRepository.findAllAccessWithOrganizationHierarchyByAssigner(
                    AccessPrincipalType.ADMIN.name(),
                    principal.getPrincipalId()
            );
            case VENDOR, VENDOR_ENGINEER -> siteZoneAccessRepository.findAllAccessWithOrganizationHierarchyByPrincipal(
                    principal.getPrincipalType().name(),
                    principal.getPrincipalId()
            );
            default -> Collections.emptyList();
        };
    }
}
