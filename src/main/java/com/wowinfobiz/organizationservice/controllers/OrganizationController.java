package com.wowinfobiz.organizationservice.controllers;

import com.wowinfobiz.organizationservice.dto.CreateOrganizationRequest;
import com.wowinfobiz.organizationservice.dto.CreateSiteRequest;
import com.wowinfobiz.organizationservice.dto.MessageResponse;
import com.wowinfobiz.organizationservice.models.OrganizationsEntity;
import com.wowinfobiz.organizationservice.models.SitesEntity;
import com.wowinfobiz.organizationservice.services.OrganizationService;
import com.wowinfobiz.organizationservice.services.SiteService;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/org")
public class OrganizationController {
    private OrganizationService orgService;
    private SiteService siteService;

   public OrganizationController(OrganizationService orgService,SiteService siteService){
        this.orgService=orgService;
        this.siteService=siteService;
   }

   @GetMapping("/organization/{orgId}")
   public  ResponseEntity<MessageResponse<?>> getOrganization(@PathVariable(name = "orgId") UUID orgId){
       try{
           OrganizationsEntity organizationsEntity = orgService.getOrganization(orgId);
           MessageResponse<OrganizationsEntity> messageResponse=new MessageResponse<>();
           messageResponse.setMessage("Organization Fetched Successfully");
           messageResponse.setStatus(true);
           messageResponse.setBody(organizationsEntity);
           return ResponseEntity.ok(messageResponse);
       } catch (RuntimeException e) {
           MessageResponse<?> messageResponse=new MessageResponse<>();
           messageResponse.setMessage(e.getMessage());
           messageResponse.setStatus(false);
           return ResponseEntity.badRequest().body(messageResponse);
       }
   }

    @GetMapping("/organization")
    public ResponseEntity<MessageResponse<?>> getOrganizations(){

       try{
           List<OrganizationsEntity> org= orgService.getAllOrganizations();
           MessageResponse<List<OrganizationsEntity>> messageResponse=new MessageResponse<>();
            messageResponse.setBody(org);
            messageResponse.setMessage("Fetched List of all organizations");
            messageResponse.setStatus(true);
           return ResponseEntity.ok(messageResponse);
       }
       catch (RuntimeException e)
       {
            MessageResponse<Map<?,?>> messageResponse=new MessageResponse<>();
           messageResponse.setMessage(e.getMessage());
           messageResponse.setBody(Map.of());
           messageResponse.setStatus(false);
            return ResponseEntity.badRequest().body(messageResponse);
       }
    }


    @PostMapping("/organization")
    public ResponseEntity<MessageResponse<?>> createOrganization(@RequestBody CreateOrganizationRequest req){
        try{
            MessageResponse<?> messageResponse= orgService.createOrganization(req);
            return ResponseEntity.ok(messageResponse);
        } catch (RuntimeException e) {
            MessageResponse<?> messageResponse=new MessageResponse<>();
            messageResponse.setMessage(e.getMessage());
            messageResponse.setStatus(false);
            return ResponseEntity.badRequest().body(messageResponse);
        }

    }

    @PutMapping("/organization/{orgId}")
    public ResponseEntity<?> updateOrganizationDetails(@PathVariable UUID orgId, @RequestBody CreateOrganizationRequest org){
        MessageResponse<?> messageResponse=new MessageResponse<>();
       try{
            messageResponse=orgService.updateOrganizationDetails(org,orgId);
           return ResponseEntity.ok(messageResponse);
       }
       catch (RuntimeException e){
           messageResponse.setMessage(e.getMessage());
           messageResponse.setStatus(false);
           return ResponseEntity.badRequest().body(messageResponse);
       }


    }

    @DeleteMapping("/organization/{orgId}")
    public ResponseEntity<MessageResponse<?>> deleteOrganization(@PathVariable UUID orgId){
       MessageResponse<?> messageResponse=new MessageResponse<>();
       try{
           messageResponse= orgService.deleteOrganization(orgId);
           return ResponseEntity.ok(messageResponse);
       }
       catch (RuntimeException e)
       {
           messageResponse.setStatus(false);
           messageResponse.setMessage(e.getMessage());
           return ResponseEntity.badRequest().body(messageResponse);
       }

    }


    @GetMapping("/organization/{orgId}/sites")
    public ResponseEntity<MessageResponse<?>> getOrganizationSites(@PathVariable UUID orgId){
       MessageResponse<OrganizationsEntity> messageResponse=new MessageResponse<>();
       try{
          OrganizationsEntity organizationsEntity=orgService.getOrganization(orgId);
          messageResponse.setBody(organizationsEntity);
          messageResponse.setStatus(true);
          messageResponse.setMessage("Success");
          return ResponseEntity.ok(messageResponse);
       }
       catch (RuntimeException e)
       {

           messageResponse.setStatus(false);
           messageResponse.setMessage("Failed to Fetch");
           return ResponseEntity.badRequest().body(messageResponse);

       }

    }

    @PostMapping("/organization/{orgId}/sites")
    public ResponseEntity<MessageResponse<?>> addOrganizationSites(@PathVariable UUID orgId, @RequestBody CreateSiteRequest site)
    {
        MessageResponse<?>  messageResponse=new MessageResponse<>();
        try{
            messageResponse= siteService.addOrganizationSites(orgId,site);
            return ResponseEntity.ok(messageResponse);
        }
        catch (RuntimeException e)
        {
            messageResponse.setMessage(e.getMessage());
            messageResponse.setStatus(false);
            return ResponseEntity.badRequest().body(messageResponse);
        }
    }

}
