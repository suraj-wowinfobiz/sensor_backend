package com.wowinfobiz.analyticsservice.repositories;

import com.wowinfobiz.analyticsservice.entities.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface AuditLogStatsRepository extends JpaRepository<AuditLogEntity, String> {

    long countByTimestampAfter(Instant timestamp);

    @Query("select count(distinct a.userId) from AuditLogEntity a")
    long countDistinctUsers();

    @Query("select count(distinct a.resourceType) from AuditLogEntity a")
    long countDistinctResourceTypes();
}
