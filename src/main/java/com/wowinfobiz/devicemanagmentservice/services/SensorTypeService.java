package com.wowinfobiz.devicemanagmentservice.services;

import com.wowinfobiz.devicemanagmentservice.dto.SensorTypeDTO;

import java.util.List;
import java.util.UUID;

public interface SensorTypeService {
    SensorTypeDTO createType(SensorTypeDTO request);

    List<SensorTypeDTO> getAllTypes();

    List<SensorTypeDTO> getTypes();

    SensorTypeDTO getType(UUID typeId);

    SensorTypeDTO updateType(UUID typeId, SensorTypeDTO request);

    void deleteType(UUID typeId);
}
