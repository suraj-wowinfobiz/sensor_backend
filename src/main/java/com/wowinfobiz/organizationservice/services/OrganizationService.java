package com.wowinfobiz.organizationservice.services;


import com.wowinfobiz.organizationservice.dto.CreateOrganizationRequest;
import com.wowinfobiz.organizationservice.dto.MessageResponse;
import com.wowinfobiz.organizationservice.models.OrganizationsEntity;

import java.util.List;
import java.util.UUID;

public interface OrganizationService {
    MessageResponse<?> createOrganization(CreateOrganizationRequest orgReq);
    MessageResponse<?> updateOrganizationDetails(CreateOrganizationRequest orgReq, UUID orgId);
    MessageResponse<?> deleteOrganization(UUID orgId);
    OrganizationsEntity getOrganization(UUID orgId);
    List<OrganizationsEntity> getAllOrganizations();
}
