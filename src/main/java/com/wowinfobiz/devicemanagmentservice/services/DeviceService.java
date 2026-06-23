package com.wowinfobiz.devicemanagmentservice.services;

import com.wowinfobiz.devicemanagmentservice.dto.DeviceDTO;
import com.wowinfobiz.devicemanagmentservice.models.DeviceDocument;

import java.util.List;
import java.util.UUID;

public interface DeviceService {
    DeviceDTO createDevice(DeviceDTO request);
    List<DeviceDocument> getAllDevice();
    DeviceDTO getDevice(UUID deviceId);

    List<DeviceDTO> getDevicesBySite(UUID siteId);

    DeviceDTO updateDevice(UUID deviceId, DeviceDTO request);

    void deleteDevice(UUID deviceId);
}
