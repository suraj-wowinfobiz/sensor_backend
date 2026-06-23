package com.wowinfobiz.organizationservice.repo;

import com.wowinfobiz.organizationservice.models.ZonesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ZoneRepo extends JpaRepository<ZonesEntity, UUID> {
    List<ZonesEntity> findBySiteSitesID(UUID siteId);
}
