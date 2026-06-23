package com.wowinfobiz.authenticationservice.dto;

import java.util.UUID;
import java.util.List;

public class AccessAssignmentRequest {

    private String principalType;
    private UUID principalId;
    private List<AccessScopeRequest> scopes;

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

    public List<AccessScopeRequest> getScopes() {
        return scopes;
    }

    public void setScopes(List<AccessScopeRequest> scopes) {
        this.scopes = scopes;
    }
}
