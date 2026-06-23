package com.wowinfobiz.devicemanagmentservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "devices")
public class DeviceEntity {

    @Id
    @Column(name = "device_id")
    private UUID deviceId;

    @Column(name="organization_id")
    private UUID organizationId;

    @Column(name = "serial_number")
    private String serialNumber;
    @Column(name = "firmware_version")
    private String firmwareVersion;

    @Column(name = "mac_address")
    private String macAddress;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "number_of_channel")
    private int numberOfChannels;

    @Column(name = "web_hook_url")
    private String webHookUrl;

    @Column(name = "lat")
    private double lat;

    @Column(name = "log")
    private double log;

    @Column(name = "last_heartBeat")
    private Date lastHeartBeat;


    private String status;

    @Column(name = "site_id")
    UUID siteId;

    @Column(name = "zone_id")
    UUID zoneId;

    @Column(name = "device_code")
    String deviceCode;



    @Column(name = "installed_at")
    Date installedAt;

    public DeviceEntity() {
    }

    public DeviceEntity(UUID deviceId, UUID siteId, UUID zoneId, String deviceCode, String status, Date installedAt) {
        this.deviceId = deviceId;
        this.siteId = siteId;
        this.zoneId = zoneId;
        this.deviceCode = deviceCode;
        this.status = status;
        this.installedAt = installedAt;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
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

    public Date getLastHeartBeat() {
        return lastHeartBeat;
    }

    public void setLastHeartBeat(Date lastHeartBeat) {
        this.lastHeartBeat = lastHeartBeat;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public UUID getSiteId() {
        return siteId;
    }

    public void setSiteId(UUID siteId) {
        this.siteId = siteId;
    }

    public UUID getZoneId() {
        return zoneId;
    }

    public void setZoneId(UUID zoneId) {
        this.zoneId = zoneId;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getInstalledAt() {
        return installedAt;
    }

    public void setInstalledAt(Date installedAt) {
        this.installedAt = installedAt;
    }
}
