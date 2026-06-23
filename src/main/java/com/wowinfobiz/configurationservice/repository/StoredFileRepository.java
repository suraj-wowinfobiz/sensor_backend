package com.wowinfobiz.configurationservice.repository;

import com.wowinfobiz.configurationservice.model.StoredFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoredFileRepository extends JpaRepository<StoredFileEntity, UUID> {
}
