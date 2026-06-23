package com.wowinfobiz.authenticationservice.dto;

import java.util.UUID;

public class AuthResponse {

    private UUID principalId;
    private String principalType;
    private String token;

    public AuthResponse(UUID principalId, String principalType, String token) {
        this.principalId = principalId;
        this.principalType = principalType;
        this.token = token;
    }

    public UUID getPrincipalId() {
        return principalId;
    }

    public String getPrincipalType() {
        return principalType;
    }

    public String getToken() {
        return token;
    }
}
