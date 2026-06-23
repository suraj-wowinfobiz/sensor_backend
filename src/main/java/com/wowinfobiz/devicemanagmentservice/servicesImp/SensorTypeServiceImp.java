package com.wowinfobiz.devicemanagmentservice.servicesImp;

import com.wowinfobiz.devicemanagmentservice.dto.SensorTypeDTO;
import com.wowinfobiz.devicemanagmentservice.models.SensorTypeDocument;
import com.wowinfobiz.devicemanagmentservice.repository.SensorTypeDocumentRepository;
import com.wowinfobiz.devicemanagmentservice.services.SensorTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SensorTypeServiceImp implements SensorTypeService {

    private final SensorTypeDocumentRepository sensorTypeRepository;
    private final JsonDocumentMapper jsonMapper;

    public SensorTypeServiceImp(SensorTypeDocumentRepository sensorTypeRepository, JsonDocumentMapper jsonMapper) {
        this.sensorTypeRepository = sensorTypeRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public SensorTypeDTO createType(SensorTypeDTO request) {
        UUID typeId = request.getSensorTypeId() == null ? UUID.randomUUID() : request.getSensorTypeId();
        request.setSensorTypeId(typeId);

        SensorTypeDocument document = new SensorTypeDocument();
        document.setId(typeId);
        document.setData(jsonMapper.toJson(request));
        document.setUpdatedAt(Instant.now());
        sensorTypeRepository.save(document);
        return request;
    }

    @Override
    public List<SensorTypeDTO> getAllTypes() {
        return getTypes();
    }

    @Override
    public List<SensorTypeDTO> getTypes() {
        return sensorTypeRepository.findAll().stream()
                .map(doc -> jsonMapper.fromJson(doc.getData(), SensorTypeDTO.class))
                .toList();
    }

    @Override
    public SensorTypeDTO getType(UUID typeId) {
        return sensorTypeRepository.findById(typeId)
                .map(doc -> jsonMapper.fromJson(doc.getData(), SensorTypeDTO.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor type not found"));
    }

    @Override
    public SensorTypeDTO updateType(UUID typeId, SensorTypeDTO request) {
        SensorTypeDocument existing = sensorTypeRepository.findById(typeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor type not found"));

        request.setSensorTypeId(typeId);
        existing.setData(jsonMapper.toJson(request));
        existing.setUpdatedAt(Instant.now());
        sensorTypeRepository.save(existing);
        return request;
    }

    @Override
    public void deleteType(UUID typeId) {
        if (!sensorTypeRepository.existsById(typeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor type not found");
        }
        sensorTypeRepository.deleteById(typeId);
    }
}
