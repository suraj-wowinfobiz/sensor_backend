package com.wowinfobiz.devicemanagmentservice.servicesImp;

import com.wowinfobiz.devicemanagmentservice.dto.SensorReadingDTO;
import com.wowinfobiz.devicemanagmentservice.models.ReadingDocument;
import com.wowinfobiz.devicemanagmentservice.repository.ReadingDocumentRepository;
import com.wowinfobiz.devicemanagmentservice.repository.SensorDocumentRepository;
import com.wowinfobiz.devicemanagmentservice.services.IngestionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class IngestionServiceImp implements IngestionService {

    private final ReadingDocumentRepository readingRepository;
    private final SensorDocumentRepository sensorRepository;
    private final JsonDocumentMapper jsonMapper;

    public IngestionServiceImp(ReadingDocumentRepository readingRepository,
                               SensorDocumentRepository sensorRepository,
                               JsonDocumentMapper jsonMapper) {
        this.readingRepository = readingRepository;
        this.sensorRepository = sensorRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public SensorReadingDTO createReading(SensorReadingDTO request) {
        if (request.getSensorId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sensorId is required");
        }
        if (!sensorRepository.existsById(request.getSensorId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor not found");
        }

        UUID readingId = request.getReadingId() == null ? UUID.randomUUID() : request.getReadingId();
        request.setReadingId(readingId);
        if (request.getIngestionTime() == null) {
            request.setIngestionTime(Instant.now());
        }

        ReadingDocument document = new ReadingDocument();
        document.setId(readingId);
        document.setSensorId(request.getSensorId());
        document.setReadingTime(request.getTimestamp());
        document.setData(jsonMapper.toJson(request));
        document.setUpdatedAt(Instant.now());
        readingRepository.save(document);
        return request;
    }

    @Override
    public List<SensorReadingDTO> createBatch(List<SensorReadingDTO> requests) {
        return requests.stream().map(this::createReading).toList();
    }

    @Override
    public List<SensorReadingDTO> getAllReadings() {
        return readingRepository.findAll().stream()
                .map(doc -> jsonMapper.fromJson(doc.getData(), SensorReadingDTO.class))
                .toList();
    }

    @Override
    public List<SensorReadingDTO> getReadings(UUID sensorId, Instant from, Instant to) {
        List<ReadingDocument> readings;
        if (from != null && to != null) {
            readings = readingRepository.findBySensorIdAndReadingTimeBetween(sensorId, from, to);
        } else {
            readings = readingRepository.findBySensorId(sensorId);
        }
        return readings.stream()
                .map(doc -> jsonMapper.fromJson(doc.getData(), SensorReadingDTO.class))
                .toList();
    }

    @Override
    public SensorReadingDTO getReading(UUID readingId) {
        return readingRepository.findById(readingId)
                .map(doc -> jsonMapper.fromJson(doc.getData(), SensorReadingDTO.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reading not found"));
    }

    @Override
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "ingestion",
                "timestamp", Instant.now().toString()
        );
    }
}
