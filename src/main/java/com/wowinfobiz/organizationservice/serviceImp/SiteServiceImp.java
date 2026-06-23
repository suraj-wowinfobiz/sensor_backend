package com.wowinfobiz.organizationservice.serviceImp;

import com.wowinfobiz.organizationservice.dto.CreateSiteRequest;
import com.wowinfobiz.organizationservice.dto.MessageResponse;
import com.wowinfobiz.organizationservice.models.OrganizationsEntity;
import com.wowinfobiz.organizationservice.models.SitesEntity;
import com.wowinfobiz.organizationservice.repo.OrganizationRepo;
import com.wowinfobiz.organizationservice.repo.SitesRepo;
import com.wowinfobiz.organizationservice.services.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SiteServiceImp implements SiteService {
    @Autowired
    SitesRepo sitesRepo;
    @Autowired
    OrganizationRepo organizationRepo;

    @Override
    public SitesEntity getSite(UUID siteId) {
        if(siteId==null) throw new RuntimeException("Site");
        try{
            Optional<SitesEntity> site=sitesRepo.findById(siteId);
            if(site.isEmpty()) throw  new RuntimeException("No Site Found for this organization");
            return site.get();

        }
        catch (IllegalArgumentException e)
        {
            throw new RuntimeException("Illegal Parameters");
        }

    }

    @Override
    public List<SitesEntity> getAllSites() {
        return sitesRepo.findAll();
    }

    @Override
    public MessageResponse<?> updateSiteDetails(CreateSiteRequest siteRequest, UUID siteId) {
        if(siteRequest ==null) throw new RuntimeException("site data can't be empty");
        if (siteRequest.getName() == null || siteRequest.getName().isBlank()) {
            throw new RuntimeException("site name can't be empty");
        }
        if (siteRequest.getLocation() == null || siteRequest.getLocation().isBlank()) {
            throw new RuntimeException("site location can't be empty");
        }

        Optional<SitesEntity> site = sitesRepo.findById(siteId);
        if (site.isEmpty()) throw new RuntimeException("No Site Found for update");

        SitesEntity siteEntity = site.get();
        siteEntity.setName(siteRequest.getName());
        siteEntity.setLocation(siteRequest.getLocation());

        if (siteRequest.getOrgId() != null) {
            OrganizationsEntity organization = organizationRepo.findById(siteRequest.getOrgId())
                    .orElseThrow(() -> new RuntimeException("No Organization Found"));
            siteEntity.setOrganization(organization);
        }

        SitesEntity updatedSite = sitesRepo.save(siteEntity);
        MessageResponse<SitesEntity> messageResponse = new MessageResponse<>();
        messageResponse.setStatus(true);
        messageResponse.setMessage("Site updated successfully");
        messageResponse.setBody(updatedSite);
        return messageResponse;
    }

    @Override
    public MessageResponse<?> deleteSiteDetail(UUID siteId) {
        Optional<SitesEntity> site = sitesRepo.findById(siteId);
        if (site.isEmpty()) throw new RuntimeException("No Site Found");

        sitesRepo.delete(site.get());
        MessageResponse<String> messageResponse = new MessageResponse<>();
        messageResponse.setStatus(true);
        messageResponse.setMessage("Site deleted successfully");
        return messageResponse;
    }

    @Override
    public MessageResponse<?> deleteAllSites() {
        sitesRepo.deleteAll();
        MessageResponse<String> messageResponse = new MessageResponse<>();
        messageResponse.setStatus(true);
        messageResponse.setMessage("All sites deleted successfully");
        return messageResponse;
    }

    @Override
    public MessageResponse<?> addOrganizationSites(UUID orgId, CreateSiteRequest createSiteRequest) {
        if (createSiteRequest == null) throw new RuntimeException("site data can't be empty");
        if (createSiteRequest.getName() == null || createSiteRequest.getName().isBlank()) {
            throw new RuntimeException("site name can't be empty");
        }
        if (createSiteRequest.getLocation() == null || createSiteRequest.getLocation().isBlank()) {
            throw new RuntimeException("site location can't be empty");
        }

        OrganizationsEntity organization = organizationRepo.findById(orgId)
                .orElseThrow(() -> new RuntimeException("No Organization Found"));

        SitesEntity site = new SitesEntity(
                UUID.randomUUID(),
                organization,
                createSiteRequest.getName(),
                createSiteRequest.getLocation(),
                Timestamp.valueOf(LocalDateTime.now())
        );

        SitesEntity savedSite = sitesRepo.save(site);

        MessageResponse<SitesEntity> messageResponse = new MessageResponse<>();
        messageResponse.setStatus(true);
        messageResponse.setMessage("Site added successfully");
        messageResponse.setBody(savedSite);
        return messageResponse;
    }


}
