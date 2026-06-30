package com.wowinfobiz.devicemanagmentservice.servicesImp;

import com.wowinfobiz.devicemanagmentservice.dto.DeviceDTO;
import com.wowinfobiz.devicemanagmentservice.dto.SensorDTO;
import com.wowinfobiz.devicemanagmentservice.models.DeviceDocument;
import com.wowinfobiz.devicemanagmentservice.models.SensorDocument;
import com.wowinfobiz.devicemanagmentservice.repository.DeviceDocumentRepository;
import com.wowinfobiz.devicemanagmentservice.repository.SensorDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SensorEndpointSupport {

    private static final String API_V1_BASE = "/api/v1";

    private final SensorDocumentRepository sensorRepository;
    private final DeviceDocumentRepository deviceRepository;
    private final JsonDocumentMapper jsonMapper;

    public SensorEndpointSupport(SensorDocumentRepository sensorRepository,
                                 DeviceDocumentRepository deviceRepository,
                                 JsonDocumentMapper jsonMapper) {
        this.sensorRepository = sensorRepository;
        this.deviceRepository = deviceRepository;
        this.jsonMapper = jsonMapper;
    }

    public SensorDTO populateEndpoints(SensorDTO sensor) {
        if (sensor == null || sensor.getDeviceId() == null) {
            return sensor;
        }
        DeviceDTO device = getDevice(sensor.getDeviceId()).orElse(null);
        if (device == null) {
            return sensor;
        }
        applyEndpointMetadata(sensor, device);
        return sensor;
    }

    public Optional<ResolvedSensorEndpoint> resolveByEndpointKey(String endpointKey) {
        String normalizedKey = normalizeSegment(endpointKey);
        if (normalizedKey.isBlank()) {
            return Optional.empty();
        }

        List<SensorDocument> documents = sensorRepository.findAll();
        for (SensorDocument document : documents) {
            SensorDTO sensor = mapDocument(document);
            DeviceDTO device = getDevice(document.getDeviceId()).orElse(null);
            if (device == null) {
                continue;
            }
            applyEndpointMetadata(sensor, device);
            if (normalizedKey.equals(normalizeSegment(sensor.getEndpointKey()))) {
                return Optional.of(new ResolvedSensorEndpoint(sensor, device));
            }
        }
        return Optional.empty();
    }

    public SensorDTO mapDocument(SensorDocument document) {
        SensorDTO sensor = jsonMapper.fromJson(document.getData(), SensorDTO.class);
        sensor.setSensorId(document.getId());
        if (sensor.getDeviceId() == null) {
            sensor.setDeviceId(document.getDeviceId());
        }
        return populateEndpoints(sensor);
    }

    public void applyEndpointMetadata(SensorDTO sensor, DeviceDTO device) {
        if (sensor == null || device == null || sensor.getSensorId() == null) {
            return;
        }

        String endpointUid = normalizeUid(sensor.getEndpointUid(), sensor.getSensorId());
        sensor.setEndpointUid(endpointUid);

        String sensorLabel = firstNonBlank(sensor.getName(), sensor.getSerialNumber(), "sensor");
        String deviceLabel = deviceDisplayName(device);
        String endpointKey = endpointUid + "-" + slugify(sensorLabel) + "-" + slugify(deviceLabel);

        sensor.setDeviceName(deviceLabel);
        sensor.setEndpointKey(endpointKey);
        sensor.setIngestionEndpoint(API_V1_BASE + "/ingestion/" + endpointKey);
        sensor.setIngestionLiveEndpoint(API_V1_BASE + "/ingestion/readings/live/" + endpointKey);
        sensor.setProcessingLiveEndpoint(API_V1_BASE + "/processing/readings/live/" + endpointKey);
        sensor.setAnalyticsLiveEndpoint(API_V1_BASE + "/analytics/events/live/" + endpointKey);
    }

    public String deviceDisplayName(DeviceDTO device) {
        return firstNonBlank(device.getSerialNumber(), "device");
    }

    private Optional<DeviceDTO> getDevice(UUID deviceId) {
        return deviceRepository.findById(deviceId).map(document -> {
            DeviceDTO dto = jsonMapper.fromJson(document.getData(), DeviceDTO.class);
            dto.setDeviceId(document.getId());
            if (dto.getSiteId() == null) {
                dto.setSiteId(document.getSiteId());
            }
            if (dto.getOrganizationId() == null) {
                dto.setOrganizationId(document.getOrganizationId());
            }
            return dto;
        });
    }

    private String normalizeUid(String existingUid, UUID sensorId) {
        if (existingUid != null && existingUid.matches("\\d{5}")) {
            return existingUid;
        }
        int value = Math.abs(sensorId.toString().hashCode()) % 100000;
        return String.format("%05d", value);
    }

    private String slugify(String value) {
        String normalized = normalizeSegment(value);
        return normalized.isBlank() ? "item" : normalized;
    }

    private String normalizeSegment(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "")
                .replaceAll("-{2,}", "-");
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    public record ResolvedSensorEndpoint(SensorDTO sensor, DeviceDTO device) {
        public String sensorId() {
            return sensor.getSensorId() == null ? "" : sensor.getSensorId().toString();
        }

        public String sensorName() {
            return sensor.getName() == null ? "" : sensor.getName().trim();
        }

        public String deviceName() {
            return device == null ? "" : (device.getSerialNumber() == null ? "" : device.getSerialNumber().trim());
        }
    }
}
