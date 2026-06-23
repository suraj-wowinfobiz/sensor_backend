package com.wowinfobiz.ingestionservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class BatchSensorReadingRequest {

    @Valid
    @NotEmpty
    private List<SensorReadingRequest> readings;

    public List<SensorReadingRequest> getReadings() {
        return readings;
    }

    public void setReadings(List<SensorReadingRequest> readings) {
        this.readings = readings;
    }
}
