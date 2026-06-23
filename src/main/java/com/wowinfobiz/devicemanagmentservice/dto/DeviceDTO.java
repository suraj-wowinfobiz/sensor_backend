package com.wowinfobiz.devicemanagmentservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceDTO {

    @JsonAlias({"id", "device_id"})
    private UUID deviceId;

    @JsonAlias({"organizationID", "organization_id"})
    private UUID organizationId;

    @JsonAlias({"siteID", "site_id"})
    private UUID siteId;

    @JsonAlias({"zoneID", "zone_id"})
    private UUID zoneId;

    @JsonAlias({"serialNo", "serial_number"})
    private String serialNumber;

    @JsonAlias({"firmware", "firmware_version"})
    private String firmwareVersion;

    @JsonAlias({"mac", "mac_address"})
    private String macAddress;

    @JsonAlias({"ip", "ip_address"})
    private String ipAddress;

    @JsonAlias({"noOfChannels", "number_of_channels"})
    private int numberOfChannels;

    @JsonAlias({"webhookUrl", "web_hook_url"})
    private String webHookUrl;

    @JsonAlias({"latitude"})
    private double lat;

    @JsonAlias({"lng", "lon", "longitude"})
    private double log;

    @JsonAlias({"lastHeartbeat", "last_heartbeat", "last_heart_beat"})
    private Date lastHeartBeat;

    @JsonAlias({"deviceStatus"})
    private String status;

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public UUID getZoneId() {
        return zoneId;
    }

    public void setZoneId(UUID zoneId) {
        this.zoneId = zoneId;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getNumberOfChannels() {
        return numberOfChannels;
    }

    public void setNumberOfChannels(int numberOfChannels) {
        this.numberOfChannels = numberOfChannels;
    }

    public String getWebHookUrl() {
        return webHookUrl;
    }

    public void setWebHookUrl(String webHookUrl) {
        this.webHookUrl = webHookUrl;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLog() {
        return log;
    }

    public void setLog(double log) {
        this.log = log;
    }

    public UUID getSiteId() {
        return siteId;
    }

    public void setSiteId(UUID siteId) {
        this.siteId = siteId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getLastHeartBeat() {
        return lastHeartBeat;
    }

    public void setLastHeartBeat(Date lastHeartBeat) {
        this.lastHeartBeat = lastHeartBeat;
    }
}
