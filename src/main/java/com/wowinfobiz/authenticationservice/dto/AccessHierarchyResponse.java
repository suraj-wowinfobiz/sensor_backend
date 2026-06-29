package com.wowinfobiz.authenticationservice.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccessHierarchyResponse {
    private UUID id; 
    private String principalType;
    private UUID principalId;
    private String name;
    private String email;
    private PrincipalDetails principal;
    private String assignedByType;
    private UUID assignedById;
    private LocalDateTime assignedAt;
    private Organization organization;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PrincipalDetails getPrincipal() {
        return principal;
    }

    public void setPrincipal(PrincipalDetails principal) {
        this.principal = principal;
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

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public static class PrincipalDetails {
        private UUID id;
        private String name;
        private String email;
        private String role;
        private Boolean active;
        private UUID adminId;
        private UUID vendorId;
        private Integer maxUsersAllowed;
        private UUID createdBySuperAdminId;
        private UUID organizationId;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }

        public UUID getAdminId() {
            return adminId;
        }

        public void setAdminId(UUID adminId) {
            this.adminId = adminId;
        }

        public UUID getVendorId() {
            return vendorId;
        }

        public void setVendorId(UUID vendorId) {
            this.vendorId = vendorId;
        }

        public Integer getMaxUsersAllowed() {
            return maxUsersAllowed;
        }

        public void setMaxUsersAllowed(Integer maxUsersAllowed) {
            this.maxUsersAllowed = maxUsersAllowed;
        }

        public UUID getCreatedBySuperAdminId() {
            return createdBySuperAdminId;
        }

        public void setCreatedBySuperAdminId(UUID createdBySuperAdminId) {
            this.createdBySuperAdminId = createdBySuperAdminId;
        }

        public UUID getOrganizationId() {
            return organizationId;
        }

        public void setOrganizationId(UUID organizationId) {
            this.organizationId = organizationId;
        }
    }

    public static class Organization {
        private UUID organizationId;
        private String name;
        private List<Site> sites = new ArrayList<>();

        public UUID getOrganizationId() {
            return organizationId;
        }

        public void setOrganizationId(UUID organizationId) {
            this.organizationId = organizationId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Site> getSites() {
            return sites;
        }

        public void setSites(List<Site> sites) {
            this.sites = sites;
        }
    }

    public static class Site {
        private UUID siteId;
        private String name;
        private String location;
        private List<Zone> zones = new ArrayList<>();

        public UUID getSiteId() {
            return siteId;
        }

        public void setSiteId(UUID siteId) {
            this.siteId = siteId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public List<Zone> getZones() {
            return zones;
        }

        public void setZones(List<Zone> zones) {
            this.zones = zones;
        }
    }

    public static class Zone {
        private UUID zoneId;
        private String name;

        public UUID getZoneId() {
            return zoneId;
        }

        public void setZoneId(UUID zoneId) {
            this.zoneId = zoneId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
