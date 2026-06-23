package com.wowinfobiz.organizationservice.controllers;

import com.wowinfobiz.organizationservice.dto.CreateSiteRequest;
import com.wowinfobiz.organizationservice.dto.CreateZoneRequest;
import com.wowinfobiz.organizationservice.dto.MessageResponse;
import com.wowinfobiz.organizationservice.models.SitesEntity;
import com.wowinfobiz.organizationservice.models.ZonesEntity;
import com.wowinfobiz.organizationservice.services.SiteService;
import com.wowinfobiz.organizationservice.services.ZoneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/org/site")
public class SiteController {

    private SiteService siteService;
    private ZoneService zoneService;
    public SiteController(SiteService siteService, ZoneService zoneService){
        this.siteService=siteService;
        this.zoneService = zoneService;
    }

    @GetMapping("/{siteId}")
    public ResponseEntity<?> getSiteDetail(@PathVariable UUID siteId)
    {
        MessageResponse<SitesEntity> messageResponse=new MessageResponse<>();
        try{
            SitesEntity site =siteService.getSite(siteId);
            messageResponse.setBody(site);
            messageResponse.setMessage("Successfully found site");
            messageResponse.setStatus(true);
            return ResponseEntity.ok(messageResponse);
        }
        catch(RuntimeException e)
        {
            messageResponse.setStatus(false);
            messageResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(messageResponse);
        }


    }

    @GetMapping
    public ResponseEntity<?> getSites()
    {
        MessageResponse<List<SitesEntity>> messageResponse = new MessageResponse<>();
        try{
            List<SitesEntity> sites = siteService.getAllSites();
            messageResponse.setBody(sites);
            messageResponse.setMessage("Successfully fetched sites");
            messageResponse.setStatus(true);
            return ResponseEntity.ok(messageResponse);
        } catch (RuntimeException e) {
            messageResponse.setStatus(false);
            messageResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(messageResponse);
        }
    }


    @PutMapping("/{siteId}")
    public ResponseEntity<?> updateSiteDetail(@RequestBody CreateSiteRequest siteRequest, @PathVariable UUID siteId)
    {
        MessageResponse<?> messageResponse=new MessageResponse<>();
        try{
           messageResponse =siteService.updateSiteDetails(siteRequest,siteId);
           return  ResponseEntity.ok(messageResponse);
        } catch (RuntimeException e) {
            messageResponse.setStatus(false);
            messageResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(messageResponse);
        }

    }

    @DeleteMapping("/{siteId}")
    public ResponseEntity<?> deleteSiteDetail(@PathVariable UUID siteId)
    {
        MessageResponse<?> messageResponse=new MessageResponse<>();
        try{
             messageResponse=siteService.deleteSiteDetail(siteId);
            return ResponseEntity.ok(messageResponse);
        }
        catch (RuntimeException e)
        {
            messageResponse.setMessage(e.getMessage());
            messageResponse.setStatus(false);
            return ResponseEntity.badRequest().body(messageResponse);
        }

    }

    @GetMapping("/{siteId}/zones")
    public ResponseEntity<?> getZonesList(@PathVariable UUID siteId){
        MessageResponse<List<ZonesEntity>> messageResponse=new MessageResponse<>();
        try{
            List<ZonesEntity> zones =zoneService.getAllZoneDetails(siteId);
            messageResponse.setBody(zones);
            messageResponse.setMessage("Successfully Fetched Zones");
            messageResponse.setStatus(true);
            return ResponseEntity.ok(messageResponse);
        }
        catch (RuntimeException e)
        {
            messageResponse.setMessage(e.getMessage());
            messageResponse.setStatus(false);
            return ResponseEntity.badRequest().body(messageResponse);
        }

    }

    @PostMapping("/{siteId}/zones")
    public ResponseEntity<?> addZones(@PathVariable UUID siteId, @RequestBody CreateZoneRequest zoneRequest)
    {
        MessageResponse<?>  messageResponse=new MessageResponse<>();
        try{
            messageResponse= zoneService.addZone(siteId, zoneRequest);
            return ResponseEntity.ok(messageResponse);
        }
        catch (RuntimeException e)
        {
            messageResponse.setStatus(false);
            messageResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(messageResponse);
        }


    }

}
