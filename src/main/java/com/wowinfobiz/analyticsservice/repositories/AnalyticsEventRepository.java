package com.wowinfobiz.analyticsservice.repositories;

import com.wowinfobiz.analyticsservice.entities.AnalyticsEventEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEventEntity, Long> {

    Slice<AnalyticsEventEntity> findAllByOrderByReceivedAtDesc(Pageable pageable);

    Slice<AnalyticsEventEntity> findByAlertCountGreaterThanOrderByReceivedAtDesc(Long minimumAlertCount, Pageable pageable);

    List<AnalyticsEventEntity> findByAlertCountGreaterThanOrderByReceivedAtDesc(Long minimumAlertCount);

    @Query("select count(distinct e.sensorId) from AnalyticsEventEntity e where e.sensorId is not null")
    long countDistinctSensorId();

    @Query("select e from AnalyticsEventEntity e where e.sensorId = :sensorId order by e.receivedAt desc")
    List<AnalyticsEventEntity> findRecentBySensorId(@Param("sensorId") String sensorId, Pageable pageable);

    List<AnalyticsEventEntity> findBySensorIdInOrderByReceivedAtDesc(List<String> sensorIds);

    @Query(value = "select * from analytics_events e where e.payload_json like concat('%', :query, '%') order by e.received_at desc", nativeQuery = true)
    List<AnalyticsEventEntity> searchByPayload(@Param("query") String query, Pageable pageable);

    long countByReceivedAtAfter(Instant instant);

    long countByAlertCountGreaterThanAndReceivedAtAfter(Long minAlertCount, Instant instant);

    long countByAlertCountGreaterThan(Long minAlertCount);
}
