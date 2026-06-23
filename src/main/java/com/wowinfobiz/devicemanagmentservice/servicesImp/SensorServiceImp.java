package com.wowinfobiz.devicemanagmentservice.servicesImp;

import com.wowinfobiz.devicemanagmentservice.dto.SensorDTO;
import com.wowinfobiz.devicemanagmentservice.models.SensorDocument;
import com.wowinfobiz.devicemanagmentservice.models.SensorsEntity;
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

    public SensorServiceImp(SensorDocumentRepository sensorRepository,
                            DeviceDocumentRepository deviceRepository,
                            JsonDocumentMapper jsonMapper) {
        this.sensorRepository = sensorRepository;
        this.deviceRepository = deviceRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public SensorDTO createSensor(UUID deviceId, SensorDTO request) {
        if (!deviceRepository.existsById(deviceId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found");
        }

        UUID sensorId = request.getSensorId() == null ? UUID.randomUUID() : request.getSensorId();
        request.setSensorId(sensorId);
        request.setDeviceId(deviceId);

        SensorDocument document = new SensorDocument();
        document.setId(sensorId);
        document.setDeviceId(deviceId);
        document.setData(jsonMapper.toJson(request));
        document.setUpdatedAt(Instant.now());
        sensorRepository.save(document);
        return request;
    }

    @Override
    public List<SensorDocument> getAllSensors() {
        return sensorRepository.findAll();
    }

    @Override
    public SensorDTO getSensor(UUID sensorId) {
        return sensorRepository.findById(sensorId)
                .map(doc -> jsonMapper.fromJson(doc.getData(), SensorDTO.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor not found"));
    }

    @Override
    public List<SensorDTO> getSensorsByDevice(UUID deviceId) {
        return sensorRepository.findByDeviceId(deviceId).stream()
                .map(doc -> jsonMapper.fromJson(doc.getData(), SensorDTO.class))
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

        request.setSensorId(sensorId);
        request.setDeviceId(deviceId);
        existing.setDeviceId(deviceId);
        existing.setData(jsonMapper.toJson(request));
        existing.setUpdatedAt(Instant.now());
        sensorRepository.save(existing);
        return request;
    }

    @Override
    public void deleteSensor(UUID sensorId) {
        if (!sensorRepository.existsById(sensorId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor not found");
        }
        sensorRepository.deleteById(sensorId);
    }
}
