package com.wowinfobiz.configurationservice.repository;

import com.wowinfobiz.configurationservice.model.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PermissionRepository extends JpaRepository<PermissionEntity, UUID> {
}
