package com.wowinfobiz.devicemanagmentservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorDTO {

    @JsonAlias({"sensorId", "id", "sensor_id"})
    private UUID sensorId;
    @JsonAlias({"deviceId", "device_id"})
    private UUID deviceId;
    @JsonAlias({"sensorTypeId", "sensor_type_id"})
    private UUID sensorTypeId;
    @JsonAlias({"sensorParameterId", "sensor_parameter_id"})
    private UUID sensorParameterId;
    private String name;
    @JsonAlias({"serialNumber", "serial_number"})
    private String serialNumber;
    @JsonAlias({"channelNumber", "channel_number"})
    private Integer channelNumber;
    private String status;
    private String unit;
    private Double lat;
    private Double longitude;
    private String endpointUid;
    private String endpointKey;
    private String ingestionEndpoint;
    private String ingestionLiveEndpoint;
    private String processingLiveEndpoint;
    private String analyticsLiveEndpoint;
    private String deviceName;

    public UUID getSensorId() {
        return sensorId;
    }

    public void setSensorId(UUID sensorId) {
        this.sensorId = sensorId;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(UUID deviceId) {
        this.deviceId = deviceId;
    }

    public UUID getSensorTypeId() {
        return sensorTypeId;
    }

    public void setSensorTypeId(UUID sensorTypeId) {
        this.sensorTypeId = sensorTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getSensorParameterId() {
        return sensorParameterId;
    }

    public void setSensorParameterId(UUID sensorParameterId) {
        this.sensorParameterId = sensorParameterId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Integer getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(Integer channelNumber) {
        this.channelNumber = channelNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    @JsonProperty("long")
    public Double getLong() {
        return longitude;
    }

    @JsonProperty("long")
    public void setLong(Double longitudeValue) {
        this.longitude = longitudeValue;
    }

    public String getEndpointUid() {
        return endpointUid;
    }

    public void setEndpointUid(String endpointUid) {
        this.endpointUid = endpointUid;
    }

    public String getEndpointKey() {
        return endpointKey;
    }

    public void setEndpointKey(String endpointKey) {
        this.endpointKey = endpointKey;
    }

    public String getIngestionEndpoint() {
        return ingestionEndpoint;
    }

    public void setIngestionEndpoint(String ingestionEndpoint) {
        this.ingestionEndpoint = ingestionEndpoint;
    }

    public String getIngestionLiveEndpoint() {
        return ingestionLiveEndpoint;
    }

    public void setIngestionLiveEndpoint(String ingestionLiveEndpoint) {
        this.ingestionLiveEndpoint = ingestionLiveEndpoint;
    }

    public String getProcessingLiveEndpoint() {
        return processingLiveEndpoint;
    }

    public void setProcessingLiveEndpoint(String processingLiveEndpoint) {
        this.processingLiveEndpoint = processingLiveEndpoint;
    }

    public String getAnalyticsLiveEndpoint() {
        return analyticsLiveEndpoint;
    }

    public void setAnalyticsLiveEndpoint(String analyticsLiveEndpoint) {
        this.analyticsLiveEndpoint = analyticsLiveEndpoint;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
