package com.wowinfobiz.organizationservice.serviceImp;

import com.wowinfobiz.organizationservice.dto.CreateZoneRequest;
import com.wowinfobiz.organizationservice.dto.MessageResponse;
import com.wowinfobiz.organizationservice.models.SitesEntity;
import com.wowinfobiz.organizationservice.models.ZonesEntity;
import com.wowinfobiz.organizationservice.repo.SitesRepo;
import com.wowinfobiz.organizationservice.repo.ZoneRepo;
import com.wowinfobiz.organizationservice.services.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ZoneServiceImp implements ZoneService {
    @Autowired
    private ZoneRepo zoneRepo;
    @Autowired
    private SitesRepo sitesRepo;

    @Override
    public MessageResponse<?> updateZoneDetails(UUID zoneId, CreateZoneRequest zoneRequest) {
        if (zoneRequest == null) throw new RuntimeException("zone data can't be empty");
        if (zoneRequest.getName() == null || zoneRequest.getName().isBlank()) {
            throw new RuntimeException("zone name can't be empty");
        }

        ZonesEntity zone = zoneRepo.findById(zoneId)
                .orElseThrow(() -> new RuntimeException("Zone Not Found"));
        zone.setName(zoneRequest.getName());

        if (zoneRequest.getSiteId() != null) {
            SitesEntity site = sitesRepo.findById(zoneRequest.getSiteId())
                    .orElseThrow(() -> new RuntimeException("Site Not Found"));
            zone.setSite(site);
        }

        ZonesEntity savedZone = zoneRepo.save(zone);
        MessageResponse<ZonesEntity> messageResponse = new MessageResponse<>();
        messageResponse.setStatus(true);
        messageResponse.setMessage("Zone updated successfully");
        messageResponse.setBody(savedZone);
        return messageResponse;
    }

    @Override
    public MessageResponse<?> deletZoneDetails(UUID zoneId) {
        Optional<ZonesEntity> zone = zoneRepo.findById(zoneId);
        if (zone.isEmpty()) throw new RuntimeException("Zone Not Found");

        zoneRepo.delete(zone.get());
        MessageResponse<String> messageResponse = new MessageResponse<>();
        messageResponse.setStatus(true);
        messageResponse.setMessage("Zone deleted successfully");
        return messageResponse;
    }

    @Override
    public ZonesEntity getZoneDetails(UUID zoneId) {
        return zoneRepo.findById(zoneId).orElseThrow(() -> new RuntimeException("Zone Not Found"));
    }

    @Override
    public List<ZonesEntity> getAllZoneDetails(UUID siteId) {
        sitesRepo.findById(siteId).orElseThrow(() -> new RuntimeException("Site Not Found"));
        return zoneRepo.findBySiteSitesID(siteId);
    }

    @Override
    public MessageResponse<?> addZone(UUID siteId, CreateZoneRequest zoneRequest) {
        if (zoneRequest == null) throw new RuntimeException("zone data can't be empty");
        if (zoneRequest.getName() == null || zoneRequest.getName().isBlank()) {
            throw new RuntimeException("zone name can't be empty");
        }

        SitesEntity site = sitesRepo.findById(siteId)
                .orElseThrow(() -> new RuntimeException("Site Not Found"));

        ZonesEntity zone = new ZonesEntity();
        zone.setZoneId(UUID.randomUUID());
        zone.setSite(site);
        zone.setName(zoneRequest.getName());

        ZonesEntity savedZone = zoneRepo.save(zone);
        MessageResponse<ZonesEntity> messageResponse = new MessageResponse<>();
        messageResponse.setStatus(true);
        messageResponse.setMessage("Zone added successfully");
        messageResponse.setBody(savedZone);
        return messageResponse;
    }
}
