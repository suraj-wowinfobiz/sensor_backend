package com.wowinfobiz.organizationservice.serviceImp;

import com.wowinfobiz.organizationservice.dto.CreateOrganizationRequest;
import com.wowinfobiz.organizationservice.dto.MessageResponse;
import com.wowinfobiz.organizationservice.enums.Status;
import com.wowinfobiz.organizationservice.models.OrganizationsEntity;
import com.wowinfobiz.organizationservice.models.SitesEntity;
import com.wowinfobiz.organizationservice.repo.OrganizationRepo;
import com.wowinfobiz.organizationservice.services.OrganizationService;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrganizationServiceImp implements OrganizationService {

    @Autowired
    OrganizationRepo organizationRepo;

    @Override
    public MessageResponse<OrganizationsEntity> createOrganization(CreateOrganizationRequest orgReq) {
        if(orgReq==null) throw new RuntimeException("Fields can't be empty");
        if( ! orgReq.getEmail().matches("^[0-9a-zA-z_#]+@+[a-z0-9]+\\.+[a-z]{0,3}$")) {
            throw new RuntimeException("Invalid Email !!");
        }
        if(orgReq.getName().isEmpty()) throw new RuntimeException("name can not be empty");
        OrganizationsEntity organizationsEntity=new OrganizationsEntity(
               UUID.randomUUID(),
               orgReq.getName(),
                orgReq.getEmail(),
                Status.ACTIVE,
                Timestamp.valueOf(LocalDateTime.now())
        );

        try{
            OrganizationsEntity organizationsEntity1 =organizationRepo.save(organizationsEntity);
            MessageResponse<OrganizationsEntity> messageResponse=new MessageResponse<>();
            messageResponse.setStatus(true);
            messageResponse.setMessage("Organization Added Successfully..");
            messageResponse.setBody(organizationsEntity1);
            return messageResponse;
        }
        catch (IllegalArgumentException e)
        {
            throw new RuntimeException("All Fields are Required");
        }
        catch (OptimisticLockingFailureException e)
        {
            throw  new RuntimeException("Fields Mismatch");
        }

    }

    @Override
    public MessageResponse<?> updateOrganizationDetails(CreateOrganizationRequest orgReq, UUID orgId) {

        if(orgReq==null) throw new RuntimeException("Fields can't be empty");
        if( ! orgReq.getEmail().matches("^[0-9a-zA-z_#]+@+[a-z0-9]+\\.+[a-z]{0,3}$")) {
            throw new RuntimeException("Invalid Email !!");
        }
        if(orgReq.getName().isEmpty()) throw new RuntimeException("name can not be empty");
        Optional<OrganizationsEntity> org=organizationRepo.findById(orgId);

        if(org.isEmpty()) throw new RuntimeException("No Orgnization Found for Update");

        org.get().setEmail(orgReq.getEmail());
        org.get().setName(orgReq.getName());


        try{
            OrganizationsEntity organizationsEntity1 =organizationRepo.save(org.get());
            MessageResponse<OrganizationsEntity> messageResponse=new MessageResponse<>();
            messageResponse.setStatus(true);
            messageResponse.setMessage("Organization Updated Successfully..");
            messageResponse.setBody(organizationsEntity1);
            return messageResponse;
        }
        catch (IllegalArgumentException e)
        {
            throw new RuntimeException("All Fields are Required");
        }
        catch (OptimisticLockingFailureException e)
        {
            throw  new RuntimeException("Fields Mismatch");
        }

    }

    @Override
    public MessageResponse<?> deleteOrganization(UUID orgId) {
        Optional<OrganizationsEntity> org=organizationRepo.findById(orgId);
        if(org.isEmpty()) throw new RuntimeException("No Organization Found ");
        try{
            organizationRepo.delete(org.get());
           MessageResponse<String> messageResponse=new MessageResponse<>();
           messageResponse.setStatus(true);
           messageResponse.setMessage("Deleted Successfully");

           return messageResponse;
        }
        catch (IllegalArgumentException e){
            throw new RuntimeException("Organization Already Deleted");
        }
        catch (OptimisticLockingFailureException e)
        {
            throw new RuntimeException("Fileds Mismatch or Smothing went wrong ");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationsEntity getOrganization(UUID orgId) {
        Optional<OrganizationsEntity> organizationsEntity=organizationRepo.findById(orgId);
        if(organizationsEntity.isEmpty()) throw new RuntimeException("Organization Not Found");
        OrganizationsEntity organization = organizationsEntity.get();
        initializeOrganizationGraph(organization);
        return organization;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationsEntity> getAllOrganizations() {
        List<OrganizationsEntity> organizations = organizationRepo.findAll();
        organizations.forEach(this::initializeOrganizationGraph);
        return organizations;
    }

    private void initializeOrganizationGraph(OrganizationsEntity organization) {
        Hibernate.initialize(organization.getSites());
        if (organization.getSites() == null) {
            return;
        }
        for (SitesEntity site : organization.getSites()) {
            Hibernate.initialize(site.getZones());
        }
    }
}
