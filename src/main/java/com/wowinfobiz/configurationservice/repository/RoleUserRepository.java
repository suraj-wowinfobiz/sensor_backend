package com.wowinfobiz.configurationservice.repository;

import com.wowinfobiz.configurationservice.model.RoleUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoleUserRepository extends JpaRepository<RoleUserEntity, UUID> {
    List<RoleUserEntity> findByRoleId(UUID roleId);
}
