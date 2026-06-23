package com.wowinfobiz.analyticsservice.repositories;

import com.wowinfobiz.analyticsservice.entities.ReportScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportScheduleRepository extends JpaRepository<ReportScheduleEntity, String> {
}
