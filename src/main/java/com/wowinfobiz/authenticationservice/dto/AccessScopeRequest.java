package com.wowinfobiz.authenticationservice.dto;

import java.util.UUID;

public class AccessScopeRequest {

    private UUID siteId;
    private UUID zoneId;

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
}
