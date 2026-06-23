package com.wowinfobiz.configurationservice.thresholdalert.dto;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ThresholdProfileResponse {
    private UUID thresholdProfileId;
    private String name;
    private String description;
    private Date createdAt;
    private Date updatedAt;
    private List<UUID> thresholdValueIds;

    public UUID getThresholdProfileId() {
        return thresholdProfileId;
    }

    public void setThresholdProfileId(UUID thresholdProfileId) {
        this.thresholdProfileId = thresholdProfileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<UUID> getThresholdValueIds() {
        return thresholdValueIds;
    }

    public void setThresholdValueIds(List<UUID> thresholdValueIds) {
        this.thresholdValueIds = thresholdValueIds;
    }
}

