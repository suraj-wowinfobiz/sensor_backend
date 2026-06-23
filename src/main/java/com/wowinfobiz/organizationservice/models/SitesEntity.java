package com.wowinfobiz.organizationservice.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sites")
public class SitesEntity {

    @Id
    @Column(name = "sites_id")
    private UUID sitesID;

    @JsonBackReference(value = "organization-sites")
    @ManyToOne
    @JoinColumn(name = "organization_id")
    private OrganizationsEntity organization;

    private  String name;

    private String location;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @JsonManagedReference(value = "site-zones")
    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL)
    private List<ZonesEntity> zones;

    public UUID getSitesID() {
        return sitesID;
    }

    public void setSitesID(UUID sitesID) {
        this.sitesID = sitesID;
    }

    public OrganizationsEntity getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationsEntity organization) {
        this.organization = organization;
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public List<ZonesEntity> getZones() {
        return zones;
    }

    public void setZones(List<ZonesEntity> zones) {
        this.zones = zones;
    }

    public SitesEntity(UUID sitesID, OrganizationsEntity organization, String name, String location, Timestamp createdAt) {
        this.sitesID = sitesID;
        this.organization = organization;
        this.name = name;
        this.location = location;
        this.createdAt = createdAt;
    }

    public SitesEntity(){
        super();
    }
}
