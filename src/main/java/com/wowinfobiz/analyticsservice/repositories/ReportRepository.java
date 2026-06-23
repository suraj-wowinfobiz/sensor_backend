package com.wowinfobiz.analyticsservice.repositories;

import com.wowinfobiz.analyticsservice.entities.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, String> {

    long countByCreatedAtAfter(Instant timestamp);
}
