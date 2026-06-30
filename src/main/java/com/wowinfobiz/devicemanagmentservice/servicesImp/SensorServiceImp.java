package com.wowinfobiz.devicemanagmentservice.servicesImp;

import com.wowinfobiz.devicemanagmentservice.dto.SensorDTO;
import com.wowinfobiz.devicemanagmentservice.models.SensorDocument;
import com.wowinfobiz.devicemanagmentservice.repository.DeviceDocumentRepository;
import com.wowinfobiz.devicemanagmentservice.repository.SensorDocumentRepository;
import com.wowinfobiz.devicemanagmentservice.services.SensorService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


@Service
public class SensorServiceImp implements SensorService {

    private final SensorDocumentRepository sensorRepository;
    private final DeviceDocumentRepository deviceRepository;
    private final JsonDocumentMapper jsonMapper;
    private final SensorEndpointSupport sensorEndpointSupport;

    public SensorServiceImp(SensorDocumentRepository sensorRepository,
                            DeviceDocumentRepository deviceRepository,
                            JsonDocumentMapper jsonMapper,
                            SensorEndpointSupport sensorEndpointSupport) {
        this.sensorRepository = sensorRepository;
        this.deviceRepository = deviceRepository;
        this.jsonMapper = jsonMapper;
        this.sensorEndpointSupport = sensorEndpointSupport;
    }

    @Override
    public SensorDTO createSensor(UUID deviceId, SensorDTO request) {
        if (!deviceRepository.existsById(deviceId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found");
        }

        UUID sensorId = request.getSensorId() == null ? UUID.randomUUID() : request.getSensorId();
        request.setSensorId(sensorId);
        request.setDeviceId(deviceId);
        request.setName(normalizeName(request));
        request.setEndpointUid(normalizeEndpointUid(request.getEndpointUid(), sensorId));
        sensorEndpointSupport.populateEndpoints(request);

        SensorDocument document = new SensorDocument();
        document.setId(sensorId);
        document.setDeviceId(deviceId);
        document.setData(jsonMapper.toJson(request));
        document.setUpdatedAt(Instant.now());
        SensorDocument saved = sensorRepository.save(document);
        return sensorEndpointSupport.mapDocument(saved);
    }

    @Override
    public List<SensorDTO> getAllSensors() {
        return sensorRepository.findAll().stream()
                .map(sensorEndpointSupport::mapDocument)
                .toList();
    }

    @Override
    public SensorDTO getSensor(UUID sensorId) {
        return sensorRepository.findById(sensorId)
                .map(sensorEndpointSupport::mapDocument)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor not found"));
    }

    @Override
    public SensorDTO getSensorByEndpointKey(String endpointKey) {
        return sensorEndpointSupport.resolveByEndpointKey(endpointKey)
                .map(SensorEndpointSupport.ResolvedSensorEndpoint::sensor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor endpoint not found"));
    }

    @Override
    public List<SensorDTO> getSensorsByDevice(UUID deviceId) {
        return sensorRepository.findByDeviceId(deviceId).stream()
                .map(sensorEndpointSupport::mapDocument)
                .toList();
    }

    @Override
    public SensorDTO updateSensor(UUID sensorId, SensorDTO request) {
        SensorDocument existing = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor not found"));

        UUID deviceId = request.getDeviceId() != null ? request.getDeviceId() : existing.getDeviceId();
        if (!deviceRepository.existsById(deviceId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found");
        }

        SensorDTO existingSensor = sensorEndpointSupport.mapDocument(existing);
        request.setSensorId(sensorId);
        request.setDeviceId(deviceId);
        request.setName(normalizeName(request));
        request.setEndpointUid(normalizeEndpointUid(request.getEndpointUid(), sensorId));
        if ((request.getEndpointUid() == null || request.getEndpointUid().isBlank())
                && existingSensor.getEndpointUid() != null
                && !existingSensor.getEndpointUid().isBlank()) {
            request.setEndpointUid(existingSensor.getEndpointUid());
        }
        sensorEndpointSupport.populateEndpoints(request);
        existing.setDeviceId(deviceId);
        existing.setData(jsonMapper.toJson(request));
        existing.setUpdatedAt(Instant.now());
        SensorDocument saved = sensorRepository.save(existing);
        return sensorEndpointSupport.mapDocument(saved);
    }

    @Override
    public void deleteSensor(UUID sensorId) {
        if (!sensorRepository.existsById(sensorId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor not found");
        }
        sensorRepository.deleteById(sensorId);
    }

    private String normalizeName(SensorDTO request) {
        if (request.getName() != null && !request.getName().trim().isBlank()) {
            return request.getName().trim();
        }
        if (request.getSerialNumber() != null && !request.getSerialNumber().trim().isBlank()) {
            return request.getSerialNumber().trim();
        }
        return "Sensor";
    }

    private String normalizeEndpointUid(String endpointUid, UUID sensorId) {
        if (endpointUid != null && endpointUid.matches("\\d{5}")) {
            return endpointUid;
        }
        int value = Math.abs(sensorId.toString().hashCode()) % 100000;
        return String.format("%05d", value);
    }
}
