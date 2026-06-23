package com.wowinfobiz.organizationservice.services;

import com.wowinfobiz.organizationservice.dto.CreateSiteRequest;
import com.wowinfobiz.organizationservice.dto.MessageResponse;
import com.wowinfobiz.organizationservice.models.SitesEntity;

import java.util.List;
import java.util.UUID;

public interface SiteService {
    SitesEntity getSite(UUID siteId);
    List<SitesEntity> getAllSites();
    MessageResponse<?> updateSiteDetails(CreateSiteRequest siteRequest,UUID siteId);
    MessageResponse<?> deleteSiteDetail(UUID siteId);
    MessageResponse<?> deleteAllSites();
    MessageResponse<?> addOrganizationSites(UUID orgId,CreateSiteRequest  createSiteRequest);



}
