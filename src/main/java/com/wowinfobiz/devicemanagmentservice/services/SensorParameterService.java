package com.wowinfobiz.devicemanagmentservice.services;

import com.wowinfobiz.devicemanagmentservice.dto.SensorParameterDTO;

import java.util.List;
import java.util.UUID;

public interface SensorParameterService {
    SensorParameterDTO createParameter(UUID typeId, SensorParameterDTO request);

    List<SensorParameterDTO> getAllParameters();

    List<SensorParameterDTO> getParametersByType(UUID typeId);

    SensorParameterDTO updateParameter(UUID parameterId, SensorParameterDTO request);

    void deleteParameter(UUID parameterId);
}
