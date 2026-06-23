package com.wowinfobiz.organizationservice.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.wowinfobiz.organizationservice.enums.Status;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "organization")
public class OrganizationsEntity {

    @Id
    @Column(name = "organization_id")
    private UUID organizationId;

    private String name;

    private String email;

    private Status status;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @JsonManagedReference(value = "organization-sites")
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<SitesEntity> sites;

    public String getEmail() {
        return email;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public OrganizationsEntity(UUID organizationId, String name, String email, Status status, Timestamp createdAt) {
        this.organizationId = organizationId;
        this.name = name;
        this.email = email;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public List<SitesEntity> getSites() {
        return sites;
    }

    public void setSites(List<SitesEntity> sites) {
        this.sites = sites;
    }


    public  OrganizationsEntity()
    {
        super();
    }

}
