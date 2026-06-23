package com.wowinfobiz.organizationservice.dto;

import java.util.UUID;

public class CreateZoneRequest {

    UUID siteId;

    String name;

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
}
