package com.wowinfobiz.configurationservice.thresholdalert.models;

import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "alerts")
public class AlertEntity {

    @Id
    @Column(name = "alert_id")
    private UUID alertId;

    @Column(name = "sensor_id")
    private UUID sensorId;

    @Column(name = "sensor_parameter_id")
    private UUID sensorParameterId;

    @Column(name = "alert_level")
    private String alertLevel;

    @Column(name = "message")
    private String message;

    @Column(name = "triggered_at")
    private Date triggeredAt;

    @Column(name = "resolved_at")
    private Date resolvedAt;

    @Column(name = "acknowledged_at")
    private Date acknowledgedAt;

    @Column(name = "assigned_to")
    private String assignedTo;

    @ManyToOne
    @JoinColumn(name = "threshold_profile_id")
    private  ThresholdProfileEntity thresholdProfile;

    public ThresholdProfileEntity getThresholdProfile() {
        return thresholdProfile;
    }

    public void setThresholdProfile(ThresholdProfileEntity thresholdProfile) {
        this.thresholdProfile = thresholdProfile;
    }

    @Column(name = "status")
    private String status;

    public UUID getAlertId() {
        return alertId;
    }

    public void setAlertId(UUID alertId) {
        this.alertId = alertId;
    }

    public UUID getSensorId() {
        return sensorId;
    }

    public void setSensorId(UUID sensorId) {
        this.sensorId = sensorId;
    }

    public UUID getSensorParameterId() {
        return sensorParameterId;
    }

    public void setSensorParameterId(UUID sensorParameterId) {
        this.sensorParameterId = sensorParameterId;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(Date triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public Date getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Date resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Date getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public void setAcknowledgedAt(Date acknowledgedAt) {
        this.acknowledgedAt = acknowledgedAt;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AlertEntity() {
        super();
    }
}
