package com.wowinfobiz.organizationservice.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "zones")
public class ZonesEntity {

    @Id
    @Column(name = "zone_id")
    private UUID zoneId;

    @JsonBackReference(value = "site-zones")
    @ManyToOne
    @JoinColumn(name = "site_id", nullable = true)
    private SitesEntity site;

    private String name;

    public UUID getZoneId() {
        return zoneId;
    }

    public void setZoneId(UUID zoneId) {
        this.zoneId = zoneId;
    }

    public SitesEntity getSite() {
        return site;
    }

    public void setSite(SitesEntity site) {
        this.site = site;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ZonesEntity(UUID zoneId, SitesEntity site, String name) {
        this.zoneId = zoneId;
        this.site = site;
        this.name = name;
    }

    public  ZonesEntity(){
        super();
    }

}
