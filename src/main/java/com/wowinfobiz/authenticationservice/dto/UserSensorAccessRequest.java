package com.wowinfobiz.authenticationservice.dto;

import java.util.List;

public class UserSensorAccessRequest {

    private List<String> sensorIds;

    public List<String> getSensorIds() {
        return sensorIds;
    }

    public void setSensorIds(List<String> sensorIds) {
        this.sensorIds = sensorIds;
    }
}
