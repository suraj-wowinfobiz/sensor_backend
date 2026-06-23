package com.wowinfobiz.organizationservice.repo;

import com.wowinfobiz.organizationservice.models.SitesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SitesRepo extends JpaRepository<SitesEntity, UUID> {
}
