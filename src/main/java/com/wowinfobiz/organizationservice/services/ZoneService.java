package com.wowinfobiz.organizationservice.services;

import com.wowinfobiz.organizationservice.dto.CreateZoneRequest;
import com.wowinfobiz.organizationservice.dto.MessageResponse;
import com.wowinfobiz.organizationservice.models.ZonesEntity;

import java.util.List;
import java.util.UUID;

public interface ZoneService {
    MessageResponse<?> updateZoneDetails(UUID zoneId, CreateZoneRequest zoneRequest);
    MessageResponse<?> deletZoneDetails(UUID zoneId);
    ZonesEntity getZoneDetails(UUID zoneId);
    List<ZonesEntity> getAllZoneDetails(UUID siteId);
    MessageResponse<?> addZone(UUID siteId, CreateZoneRequest zoneRequest);


}
