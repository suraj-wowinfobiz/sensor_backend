package com.wowinfobiz.processingservice.services;

import com.wowinfobiz.processingservice.models.ProcessedSensorReadingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class ProcessedReadingCsvStoreService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessedReadingCsvStoreService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final String CSV_HEADER = "readingId,sensorId,dataType,timestamp,processedSuccess,message,rawPayload,processedPayload";

    @Value("${app.processed.csv.enabled:true}")
    private boolean csvEnabled;

    @Value("${app.processed.csv.dir:data/processed-readings}")
    private String csvDirectory;

    private final Object csvWriteLock = new Object();

    public void append(ProcessedSensorReadingEntity entity) {
        if (!csvEnabled || entity == null || entity.getTimestamp() == null) {
            return;
        }

        String datePart = DATE_FORMATTER.format(LocalDate.now(ZoneId.systemDefault()));
        Path directoryPath = Paths.get(csvDirectory);
        Path filePath = directoryPath.resolve("processed-readings-" + datePart + ".csv");

        String csvRow = String.join(",",
                csvValue(String.valueOf(entity.getReadingId())),
                csvValue(String.valueOf(entity.getSensorId())),
                csvValue(entity.getDataType()),
                csvValue(String.valueOf(entity.getTimestamp())),
                csvValue(String.valueOf(entity.isProcessedSuccess())),
                csvValue(entity.getMessage()),
                csvValue(entity.getRawPayload()),
                csvValue(entity.getProcessedPayload())
        ) + System.lineSeparator();

        synchronized (csvWriteLock) {
            try {
                Files.createDirectories(directoryPath);
                if (Files.notExists(filePath)) {
                    Files.writeString(
                            filePath,
                            CSV_HEADER + System.lineSeparator(),
                            StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.APPEND
                    );
                }
                Files.writeString(
                        filePath,
                        csvRow,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );
            } catch (IOException ex) {
                LOG.error("Failed to append processed reading {} to CSV file {}",
                        entity.getReadingId(), filePath, ex);
            }
        }
    }

    public Optional<ProcessedSensorReadingEntity> findByReadingId(UUID readingId) {
        if (readingId == null) {
            return Optional.empty();
        }
        Map<UUID, ProcessedSensorReadingEntity> records = loadRecords(null, null, null);
        return Optional.ofNullable(records.get(readingId));
    }

    public List<ProcessedSensorReadingEntity> findAll(@Nullable UUID sensorId, @Nullable Instant from, @Nullable Instant to) {
        Map<UUID, ProcessedSensorReadingEntity> records = loadRecords(sensorId, from, to);
        return records.values().stream()
                .sorted(Comparator.comparing(ProcessedSensorReadingEntity::getTimestamp).reversed())
                .toList();
    }

    private Map<UUID, ProcessedSensorReadingEntity> loadRecords(@Nullable UUID sensorId, @Nullable Instant from, @Nullable Instant to) {
        Path directoryPath = Paths.get(csvDirectory);
        if (!Files.exists(directoryPath)) {
            return Map.of();
        }

        Map<UUID, ProcessedSensorReadingEntity> records = new LinkedHashMap<>();
        try (Stream<Path> paths = Files.list(directoryPath)) {
            List<Path> csvFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith("processed-readings-"))
                    .filter(path -> path.getFileName().toString().endsWith(".csv"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();

            for (Path csvFile : csvFiles) {
                for (ProcessedSensorReadingEntity entity : readFile(csvFile)) {
                    if (entity.getReadingId() == null || entity.getTimestamp() == null) {
                        continue;
                    }
                    if (sensorId != null && !sensorId.equals(entity.getSensorId())) {
                        continue;
                    }
                    if (from != null && entity.getTimestamp().isBefore(from)) {
                        continue;
                    }
                    if (to != null && entity.getTimestamp().isAfter(to)) {
                        continue;
                    }
                    records.put(entity.getReadingId(), entity);
                }
            }
        } catch (IOException ex) {
            LOG.error("Failed to load processed readings from CSV directory {}", directoryPath, ex);
        }
        return records;
    }

    private List<ProcessedSensorReadingEntity> readFile(Path csvFile) {
        try {
            List<String> lines = Files.readAllLines(csvFile, StandardCharsets.UTF_8);
            List<ProcessedSensorReadingEntity> entities = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line == null || line.isBlank()) {
                    continue;
                }
                ProcessedSensorReadingEntity entity = parseRow(line, csvFile, i + 1);
                if (entity != null) {
                    entities.add(entity);
                }
            }
            return entities;
        } catch (IOException ex) {
            LOG.error("Failed to read processed readings CSV {}", csvFile, ex);
            return List.of();
        }
    }

    @Nullable
    private ProcessedSensorReadingEntity parseRow(String row, Path csvFile, int lineNumber) {
        List<String> values = splitCsvRow(row);
        if (values.size() != 8) {
            LOG.warn("Skipping malformed CSV row in {} at line {}. Expected 8 columns but found {}", csvFile, lineNumber, values.size());
            return null;
        }

        try {
            ProcessedSensorReadingEntity entity = new ProcessedSensorReadingEntity();
            entity.setReadingId(parseUuid(values.get(0)));
            entity.setSensorId(parseUuid(values.get(1)));
            entity.setDataType(values.get(2));
            entity.setTimestamp(parseInstant(values.get(3)));
            entity.setProcessedSuccess(Boolean.parseBoolean(values.get(4)));
            entity.setMessage(emptyToNull(values.get(5)));
            entity.setRawPayload(emptyToNull(values.get(6)));
            entity.setProcessedPayload(emptyToNull(values.get(7)));
            return entity;
        } catch (Exception ex) {
            LOG.warn("Skipping malformed CSV row in {} at line {}", csvFile, lineNumber, ex);
            return null;
        }
    }

    private List<String> splitCsvRow(String row) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < row.length(); i++) {
            char ch = row.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < row.length() && row.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }
            if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(ch);
        }
        values.add(current.toString());
        return values;
    }

    private UUID parseUuid(String value) {
        return value == null || value.isBlank() ? null : UUID.fromString(value.trim());
    }

    private Instant parseInstant(String value) {
        return value == null || value.isBlank() ? null : Instant.parse(value.trim());
    }

    @Nullable
    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String csvValue(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
