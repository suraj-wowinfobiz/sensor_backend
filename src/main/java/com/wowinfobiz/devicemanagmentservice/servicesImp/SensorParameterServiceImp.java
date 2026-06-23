package com.wowinfobiz.devicemanagmentservice.servicesImp;

import com.wowinfobiz.devicemanagmentservice.dto.SensorParameterDTO;
import com.wowinfobiz.devicemanagmentservice.models.SensorParameterDocument;
import com.wowinfobiz.devicemanagmentservice.repository.SensorParameterDocumentRepository;
import com.wowinfobiz.devicemanagmentservice.repository.SensorTypeDocumentRepository;
import com.wowinfobiz.devicemanagmentservice.services.SensorParameterService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SensorParameterServiceImp implements SensorParameterService {

    private final SensorParameterDocumentRepository parameterRepository;
    private final SensorTypeDocumentRepository sensorTypeRepository;
    private final JsonDocumentMapper jsonMapper;

    public SensorParameterServiceImp(SensorParameterDocumentRepository parameterRepository,
                                     SensorTypeDocumentRepository sensorTypeRepository,
                                     JsonDocumentMapper jsonMapper) {
        this.parameterRepository = parameterRepository;
        this.sensorTypeRepository = sensorTypeRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public SensorParameterDTO createParameter(UUID typeId, SensorParameterDTO request) {
        if (!sensorTypeRepository.existsById(typeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor type not found");
        }

        UUID parameterId = request.getSensorParameterId() == null ? UUID.randomUUID() : request.getSensorParameterId();
        request.setSensorParameterId(parameterId);
        request.setSensorTypeId(typeId);

        SensorParameterDocument document = new SensorParameterDocument();
        document.setId(parameterId);
        document.setTypeId(typeId);
        document.setData(jsonMapper.toJson(request));
        document.setUpdatedAt(Instant.now());
        parameterRepository.save(document);

        return request;
    }

    @Override
    public List<SensorParameterDTO> getAllParameters() {
        return parameterRepository.findAll().stream()
                .map(doc -> jsonMapper.fromJson(doc.getData(), SensorParameterDTO.class))
                .toList();
    }

    @Override
    public List<SensorParameterDTO> getParametersByType(UUID typeId) {
        return parameterRepository.findByTypeId(typeId).stream()
                .map(doc -> jsonMapper.fromJson(doc.getData(), SensorParameterDTO.class))
                .toList();
    }

    @Override
    public SensorParameterDTO updateParameter(UUID parameterId, SensorParameterDTO request) {
        SensorParameterDocument existing = parameterRepository.findById(parameterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parameter not found"));

        UUID typeId = request.getSensorTypeId() != null ? request.getSensorTypeId() : existing.getTypeId();
        if (!sensorTypeRepository.existsById(typeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor type not found");
        }

        request.setSensorParameterId(parameterId);
        request.setSensorTypeId(typeId);
        existing.setTypeId(typeId);
        existing.setData(jsonMapper.toJson(request));
        existing.setUpdatedAt(Instant.now());
        parameterRepository.save(existing);

        return request;
    }

    @Override
    public void deleteParameter(UUID parameterId) {
        if (!parameterRepository.existsById(parameterId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Parameter not found");
        }
        parameterRepository.deleteById(parameterId);
    }
}
