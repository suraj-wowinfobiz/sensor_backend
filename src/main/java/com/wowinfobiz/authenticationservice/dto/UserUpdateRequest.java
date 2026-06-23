package com.wowinfobiz.authenticationservice.dto;

import java.util.UUID;

public class UserUpdateRequest {

    private String name;
    private String email;
    private String password;
    private UUID organizationId;
    private Integer maxUsersAllowed;
    private Boolean active;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public Integer getMaxUsersAllowed() {
        return maxUsersAllowed;
    }

    public void setMaxUsersAllowed(Integer maxUsersAllowed) {
        this.maxUsersAllowed = maxUsersAllowed;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
