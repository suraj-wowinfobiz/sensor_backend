package com.wowinfobiz.devicemanagmentservice.servicesImp;

import com.wowinfobiz.devicemanagmentservice.dto.DeviceDTO;
import com.wowinfobiz.devicemanagmentservice.models.DeviceDocument;
import com.wowinfobiz.devicemanagmentservice.repository.DeviceDocumentRepository;
import com.wowinfobiz.devicemanagmentservice.services.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DeviceServiceImp implements DeviceService {

    private final DeviceDocumentRepository deviceRepository;
    private final JsonDocumentMapper jsonMapper;

    public DeviceServiceImp(DeviceDocumentRepository deviceRepository, JsonDocumentMapper jsonMapper) {
        this.deviceRepository = deviceRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public DeviceDTO createDevice(DeviceDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        if (request.getSiteId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "siteId is required (accepted keys: siteId, siteID, site_id)");
        }

        UUID id = UUID.randomUUID();
        request.setDeviceId(id);
        DeviceDocument document = new DeviceDocument();
        document.setId(id);
        document.setOrganizationId(request.getOrganizationId());
        document.setSiteId(request.getSiteId());
        document.setData(jsonMapper.toJson(request));
        document.setUpdatedAt(Instant.now());

        DeviceDocument saved = deviceRepository.saveAndFlush(document);
        if (!deviceRepository.existsById(saved.getId())) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Device was not persisted");
        }
        return mapDocumentToDto(saved);
    }

    @Override
    public List<DeviceDocument> getAllDevice() {
        return deviceRepository.findAll();
    }

    @Override
    public DeviceDTO getDevice(UUID deviceId) {
        return deviceRepository.findById(deviceId)
                .map(this::mapDocumentToDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));
    }



    @Override
    public List<DeviceDTO> getDevicesBySite(UUID siteId) {
        return deviceRepository.findBySiteId(siteId).stream()
                .map(this::mapDocumentToDto)
                .toList();
    }

    @Override
    public DeviceDTO updateDevice(UUID deviceId, DeviceDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        DeviceDocument existing = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        UUID siteId = request.getSiteId() != null ? request.getSiteId() : existing.getSiteId();
        UUID organizationId = request.getOrganizationId() != null ? request.getOrganizationId() : existing.getOrganizationId();

        request.setSiteId(siteId);
        if (request.getOrganizationId() == null) {
            request.setOrganizationId(organizationId);
        }
        request.setDeviceId(deviceId);

        existing.setSiteId(siteId);
        existing.setOrganizationId(organizationId);
        existing.setData(jsonMapper.toJson(request));
        existing.setUpdatedAt(Instant.now());

        DeviceDocument saved = deviceRepository.saveAndFlush(existing);
        return mapDocumentToDto(saved);
    }

    @Override
    public void deleteDevice(UUID deviceId) {
        if (!deviceRepository.existsById(deviceId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found");
        }
        deviceRepository.deleteById(deviceId);
    }

    private DeviceDTO mapDocumentToDto(DeviceDocument doc) {
        DeviceDTO dto = jsonMapper.fromJson(doc.getData(), DeviceDTO.class);
        dto.setDeviceId(doc.getId());
        if (dto.getSiteId() == null) {
            dto.setSiteId(doc.getSiteId());
        }
        if (dto.getOrganizationId() == null) {
            dto.setOrganizationId(doc.getOrganizationId());
        }
        return dto;
    }
}
