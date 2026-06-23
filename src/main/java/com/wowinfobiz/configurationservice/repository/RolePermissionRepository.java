package com.wowinfobiz.configurationservice.repository;

import com.wowinfobiz.configurationservice.model.RolePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, UUID> {
    List<RolePermissionEntity> findByRoleId(UUID roleId);
}
