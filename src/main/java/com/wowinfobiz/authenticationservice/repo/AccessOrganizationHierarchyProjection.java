package com.wowinfobiz.authenticationservice.repo;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AccessOrganizationHierarchyProjection {
    UUID getAccessId();
    String getPrincipalType();
    UUID getPrincipalId();
    String getAssignedByType();
    UUID getAssignedById();
    LocalDateTime getAssignedAt();
    UUID getOrganizationId();
    String getOrganizationName();
    UUID getSiteId();
    String getSiteName();
    String getSiteLocation();
    UUID getZoneId();
    String getZoneName();
}
