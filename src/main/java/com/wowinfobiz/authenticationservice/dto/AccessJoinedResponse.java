package com.wowinfobiz.authenticationservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class AccessJoinedResponse {

    private UUID accessId;
    private String principalType;
    private UUID principalId;
    private String principalName;
    private UUID principalOrganizationId;
    private String assignedByType;
    private UUID assignedById;
    private LocalDateTime assignedAt;
    private UUID organizationId;
    private String organizationName;
    private UUID siteId;
    private String siteName;
    private String siteLocation;
    private UUID zoneId;
    private String zoneName;

    public UUID getAccessId() {
        return accessId;
    }

    public void setAccessId(UUID accessId) {
        this.accessId = accessId;
    }

    public String getPrincipalType() {
        return principalType;
    }

    public void setPrincipalType(String principalType) {
        this.principalType = principalType;
    }

    public UUID getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(UUID principalId) {
        this.principalId = principalId;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public UUID getPrincipalOrganizationId() {
        return principalOrganizationId;
    }

    public void setPrincipalOrganizationId(UUID principalOrganizationId) {
        this.principalOrganizationId = principalOrganizationId;
    }

    public String getAssignedByType() {
        return assignedByType;
    }

    public void setAssignedByType(String assignedByType) {
        this.assignedByType = assignedByType;
    }

    public UUID getAssignedById() {
        return assignedById;
    }

    public void setAssignedById(UUID assignedById) {
        this.assignedById = assignedById;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public UUID getSiteId() {
        return siteId;
    }

    public void setSiteId(UUID siteId) {
        this.siteId = siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteLocation() {
        return siteLocation;
    }

    public void setSiteLocation(String siteLocation) {
        this.siteLocation = siteLocation;
    }

    public UUID getZoneId() {
        return zoneId;
    }

    public void setZoneId(UUID zoneId) {
        this.zoneId = zoneId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }
}
