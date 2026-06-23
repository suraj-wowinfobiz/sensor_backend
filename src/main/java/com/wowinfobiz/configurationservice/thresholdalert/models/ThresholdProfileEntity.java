package com.wowinfobiz.configurationservice.thresholdalert.models;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "threshold_profile")
public class ThresholdProfileEntity {

    @Id
    @Column(name = "threshold_profile_id")
    private UUID thresholdProfileId;

    private String name;

    private  String description;

    @OneToMany(mappedBy = "thresholdProfile",cascade = CascadeType.ALL)
    private List<ThresholdValueEntity> thresholdValueEntities;

    public List<AlertEntity> getAlertEntities() {
        return alertEntities;
    }

    public void setAlertEntities(List<AlertEntity> alertEntities) {
        this.alertEntities = alertEntities;
    }

    @OneToMany(mappedBy = "thresholdProfile",cascade = CascadeType.ALL)
    private List<AlertEntity> alertEntities;

    @JsonAlias({"CreatedAt"})
    private Date createdAt;
    @JsonAlias({"UpdatedAt"})
    private Date updatedAt;

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

    public List<ThresholdValueEntity> getThresholdValueEntities() {
        return thresholdValueEntities;
    }

    public void setThresholdValueEntities(List<ThresholdValueEntity> thresholdValueEntities) {
        this.thresholdValueEntities = thresholdValueEntities;
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

    public  ThresholdProfileEntity()
    {
        super();

    }

    public ThresholdProfileEntity(UUID thresholdProfileId, String name, String description, List<ThresholdValueEntity> thresholdValueEntities, Date createdAt, Date updatedAt) {
        this.thresholdProfileId = thresholdProfileId;
        this.name = name;
        this.description = description;
        this.thresholdValueEntities = thresholdValueEntities;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}

