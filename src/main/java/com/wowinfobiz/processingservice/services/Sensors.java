package com.wowinfobiz.processingservice.services;

import com.wowinfobiz.processingservice.dto.ProcessDataResponse;
import com.wowinfobiz.processingservice.dto.SensorRawDataRequest;

public interface Sensors {
    ProcessDataResponse<?> processSensorData(SensorRawDataRequest<?> sensorRawDataRequest);
}
