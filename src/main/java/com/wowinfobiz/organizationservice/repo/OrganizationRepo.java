package com.wowinfobiz.organizationservice.repo;

import com.wowinfobiz.organizationservice.models.OrganizationsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrganizationRepo extends JpaRepository<OrganizationsEntity,UUID> {

}
