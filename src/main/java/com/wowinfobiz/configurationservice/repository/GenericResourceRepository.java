package com.wowinfobiz.configurationservice.repository;

import com.wowinfobiz.configurationservice.model.GenericResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GenericResourceRepository extends JpaRepository<GenericResourceEntity, UUID> {
    List<GenericResourceEntity> findByDomainOrderByCreatedAtDesc(String domain);
    Optional<GenericResourceEntity> findByDomainAndId(String domain, UUID id);
    long countByDomain(String domain);
}
