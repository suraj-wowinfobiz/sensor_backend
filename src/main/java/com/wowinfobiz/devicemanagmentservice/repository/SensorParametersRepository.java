package com.wowinfobiz.devicemanagmentservice.repository;

import com.wowinfobiz.devicemanagmentservice.models.SensorParameters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SensorParametersRepository extends JpaRepository<SensorParameters, UUID> {
    List<SensorParameters> findByNameIgnoreCase(String name);
}
