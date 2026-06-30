package com.wowinfobiz.authenticationservice.repo;

import com.wowinfobiz.authenticationservice.model.UserSensorAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserSensorAccessRepository extends JpaRepository<UserSensorAccess, UUID> {
    List<UserSensorAccess> findByUserId(UUID userId);

    boolean existsByUserIdAndSensorId(UUID userId, UUID sensorId);

    void deleteByUserId(UUID userId);
}
