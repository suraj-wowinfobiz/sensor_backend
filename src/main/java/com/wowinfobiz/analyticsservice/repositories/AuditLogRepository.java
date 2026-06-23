package com.wowinfobiz.analyticsservice.repositories;

import com.wowinfobiz.analyticsservice.entities.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, String> {

    List<AuditLogEntity> findByUserIdOrderByTimestampDesc(String userId);

    List<AuditLogEntity> findByResourceTypeIgnoreCaseAndResourceIdOrderByTimestampDesc(String resourceType, String resourceId);

    List<AuditLogEntity> findAllByOrderByTimestampDesc();

    long countByTimestampAfter(Instant timestamp);
}
