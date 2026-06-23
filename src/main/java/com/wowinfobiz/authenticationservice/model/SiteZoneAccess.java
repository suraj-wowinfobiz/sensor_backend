package com.wowinfobiz.authenticationservice.model;

import com.wowinfobiz.authenticationservice.enums.AccessPrincipalType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "site_zone_access")
public class SiteZoneAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessPrincipalType principalType;

    @Column(nullable = false)
    private UUID principalId;

    @Column(nullable = false)
    private UUID siteId;

    @Column(nullable = false)
    private UUID zoneId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessPrincipalType assignedByType;

    @Column(nullable = false)
    private UUID assignedById;

    @Column(nullable = false)
    private LocalDateTime assignedAt;

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public AccessPrincipalType getPrincipalType() {
        return principalType;
    }

    public void setPrincipalType(AccessPrincipalType principalType) {
        this.principalType = principalType;
    }

    public UUID getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(UUID principalId) {
        this.principalId = principalId;
    }

    public UUID getSiteId() {
        return siteId;
    }

    public void setSiteId(UUID siteId) {
        this.siteId = siteId;
    }

    public UUID getZoneId() {
        return zoneId;
    }

    public void setZoneId(UUID zoneId) {
        this.zoneId = zoneId;
    }

    public AccessPrincipalType getAssignedByType() {
        return assignedByType;
    }

    public void setAssignedByType(AccessPrincipalType assignedByType) {
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
}
