package com.wowinfobiz.ingestionservice.controller;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wowinfobiz.authenticationservice.security.LiveEndpointAccessService;
import com.wowinfobiz.devicemanagmentservice.servicesImp.SensorEndpointSupport;
import com.wowinfobiz.ingestionservice.dto.SensorReadingResponse;
import com.wowinfobiz.ingestionservice.dto.SensorReadingView;
import com.wowinfobiz.ingestionservice.service.IngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ingestion")
public class IngestionController {
    private static final Logger LOG = LoggerFactory.getLogger(IngestionController.class);

    private final IngestionService ingestionService;
    private final SensorEndpointSupport sensorEndpointSupport;
    private final LiveEndpointAccessService liveEndpointAccessService;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public IngestionController(IngestionService ingestionService,
                               SensorEndpointSupport sensorEndpointSupport,
                               LiveEndpointAccessService liveEndpointAccessService) {
        this.ingestionService = ingestionService;
        this.sensorEndpointSupport = sensorEndpointSupport;
        this.liveEndpointAccessService = liveEndpointAccessService;
    }



    @PostMapping(
            value = {"", "/", "/stream", "/esp32", "/readings", "/sensor-data"},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.ALL_VALUE}
    )
    public ResponseEntity<SensorReadingResponse> ingestReadingAlias(@RequestBody(required = false) String rawBody,
                                                                    @RequestParam MultiValueMap<String, String> queryParams,
                                                                    @RequestParam String userId,
                                                                    @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType) {
        liveEndpointAccessService.requireUser(userId);
        LOG.info("Ingestion POST received: contentType={}, rawBodyLength={}, queryParamKeys={}",
                contentType,
                rawBody == null ? 0 : rawBody.length(),
                queryParams == null ? List.of() : queryParams.keySet());
        Map<String, Object> payload = parseInboundPayload(rawBody, queryParams);
        if (!payload.isEmpty()) {
            payload.remove("userId");
        }
        if (payload.isEmpty()) {
            LOG.warn("Ingestion POST rejected: unable to parse payload");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unable to parse ingestion payload. Provide JSON object, form data, query params, or key=value body");
        }
        LOG.info("Ingestion payload accepted: keys={}", payload.keySet());
        return ResponseEntity.status(HttpStatus.CREATED).body(ingestionService.saveRawReading(payload));
    }

    @PostMapping(
            value = "/{endpointKey}",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.ALL_VALUE}
    )
    public ResponseEntity<SensorReadingResponse> ingestReadingForSensorEndpoint(@PathVariable String endpointKey,
                                                                                @RequestBody(required = false) String rawBody,
                                                                                @RequestParam MultiValueMap<String, String> queryParams,
                                                                                @RequestParam String userId,
                                                                                @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType) {
        SensorEndpointSupport.ResolvedSensorEndpoint resolved = sensorEndpointSupport.resolveByEndpointKey(endpointKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor endpoint not found"));
        liveEndpointAccessService.requireUserForSensor(
                userId,
                resolved.sensorId(),
                resolved.device().getSiteId(),
                resolved.device().getZoneId()
        );

        Map<String, Object> payload = parseInboundPayload(rawBody, queryParams);
        if (!payload.isEmpty()) {
            payload.remove("userId");
        }
        if (payload.isEmpty()) {
            LOG.warn("Sensor endpoint ingestion rejected: endpointKey={}, unable to parse payload", endpointKey);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unable to parse ingestion payload. Provide JSON object, form data, query params, or key=value body");
        }

        Map<String, Object> enriched = new LinkedHashMap<>(payload);
        enriched.put("sensorId", resolved.sensorId());
        enriched.put("sensorName", resolved.sensorName());
        enriched.put("deviceName", resolved.deviceName());
        if (resolved.sensor().getSensorTypeId() != null) {
            enriched.put("sensorTypeId", resolved.sensor().getSensorTypeId().toString());
        }
        if (StringUtils.hasText(resolved.sensorName())) {
            enriched.putIfAbsent("dataType", resolved.sensorName());
        }

        LOG.info("Ingestion sensor endpoint POST received: endpointKey={}, sensorId={}, contentType={}, keys={}",
                endpointKey,
                resolved.sensorId(),
                contentType,
                enriched.keySet());
        return ResponseEntity.status(HttpStatus.CREATED).body(ingestionService.saveRawReading(enriched));
    }

    @PostMapping(
            value = "/{endpointKey}/{userId}",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.ALL_VALUE}
    )
    public ResponseEntity<SensorReadingResponse> ingestReadingForSensorEndpointWithUser(@PathVariable String endpointKey,
                                                                                        @PathVariable String userId,
                                                                                        @RequestBody(required = false) String rawBody,
                                                                                        @RequestParam MultiValueMap<String, String> queryParams,
                                                                                        @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType) {
        SensorEndpointSupport.ResolvedSensorEndpoint resolved = sensorEndpointSupport.resolveByEndpointKey(endpointKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor endpoint not found"));
        liveEndpointAccessService.requireUserForSensor(
                userId,
                resolved.sensorId(),
                resolved.device().getSiteId(),
                resolved.device().getZoneId()
        );

        Map<String, Object> payload = parseInboundPayload(rawBody, queryParams);
        if (!payload.isEmpty()) {
            payload.remove("userId");
        }
        if (payload.isEmpty()) {
            LOG.warn("Sensor endpoint ingestion rejected: endpointKey={}, unable to parse payload", endpointKey);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unable to parse ingestion payload. Provide JSON object, form data, query params, or key=value body");
        }

        Map<String, Object> enriched = new LinkedHashMap<>(payload);
        enriched.put("sensorId", resolved.sensorId());
        enriched.put("sensorName", resolved.sensorName());
        enriched.put("deviceName", resolved.deviceName());
        if (resolved.sensor().getSensorTypeId() != null) {
            enriched.put("sensorTypeId", resolved.sensor().getSensorTypeId().toString());
        }
        if (StringUtils.hasText(resolved.sensorName())) {
            enriched.putIfAbsent("dataType", resolved.sensorName());
        }

        LOG.info("Ingestion sensor endpoint POST received: endpointKey={}, userId={}, sensorId={}, contentType={}, keys={}",
                endpointKey,
                userId,
                resolved.sensorId(),
                contentType,
                enriched.keySet());
        return ResponseEntity.status(HttpStatus.CREATED).body(ingestionService.saveRawReading(enriched));
    }


    @GetMapping("/readings")
    public ResponseEntity<List<SensorReadingView>> getReadings(
            @RequestParam(required = false) String sensorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return ResponseEntity.ok(ingestionService.getReadings(sensorId, from, to));
    }

    @GetMapping("/readings/getall")
    public ResponseEntity<List<SensorReadingView>> getAllReadings() {
        return ResponseEntity.ok(ingestionService.getAllReadings());
    }

    @GetMapping(value = "/readings/live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAllReadings(@RequestParam String userId) {
        liveEndpointAccessService.requireUser(userId);
        return ingestionService.subscribeAllReadings();
    }

    @GetMapping(value = "/readings/live/{endpointKey}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSensorReadings(@PathVariable String endpointKey,
                                           @RequestParam String userId) {
        SensorEndpointSupport.ResolvedSensorEndpoint resolved = sensorEndpointSupport.resolveByEndpointKey(endpointKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor endpoint not found"));
        liveEndpointAccessService.requireUserForSensor(
                userId,
                resolved.sensorId(),
                resolved.device().getSiteId(),
                resolved.device().getZoneId()
        );
        return ingestionService.subscribeReadings(resolved.sensorId());
    }

    @GetMapping(value = "/readings/live/{endpointKey}/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSensorReadingsWithUserPath(@PathVariable String endpointKey,
                                                       @PathVariable String userId) {
        SensorEndpointSupport.ResolvedSensorEndpoint resolved = sensorEndpointSupport.resolveByEndpointKey(endpointKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor endpoint not found"));
        liveEndpointAccessService.requireUserForSensor(
                userId,
                resolved.sensorId(),
                resolved.device().getSiteId(),
                resolved.device().getZoneId()
        );
        return ingestionService.subscribeReadings(resolved.sensorId());
    }

    @GetMapping("/readings/{readingId}")
    public ResponseEntity<SensorReadingView> getReadingById(@PathVariable UUID readingId) {
        SensorReadingView reading = ingestionService.getReadingById(readingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reading not found"));
        return ResponseEntity.ok(reading);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("storedReadings", ingestionService.getReadingCount());
        return ResponseEntity.ok(response);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractBatchPayload(Object payload) {
        List<Map<String, Object>> items = new ArrayList<>();
        if (payload instanceof List<?> payloadList) {
            for (Object item : payloadList) {
                if (item instanceof Map<?, ?> map) {
                    items.add((Map<String, Object>) map);
                }
            }
            return items;
        }

        if (payload instanceof Map<?, ?> mapPayload) {
            Object readingsNode = mapPayload.get("readings");
            if (readingsNode instanceof List<?> readingsList) {
                for (Object item : readingsList) {
                    if (item instanceof Map<?, ?> map) {
                        items.add((Map<String, Object>) map);
                    }
                }
                return items;
            }
            return Collections.singletonList((Map<String, Object>) mapPayload);
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Batch payload must be an array, single object, or object with 'readings' array");
    }

    private Map<String, Object> parseInboundPayload(String rawBody, MultiValueMap<String, String> queryParams) {
        if (StringUtils.hasText(rawBody)) {
            Map<String, Object> parsedAsJson = parseJsonObject(rawBody);
            if (!parsedAsJson.isEmpty()) {
                return parsedAsJson;
            }

            Map<String, Object> parsedAsForm = parseFormBody(rawBody);
            if (!parsedAsForm.isEmpty()) {
                return parsedAsForm;
            }

            Map<String, Object> fallback = new HashMap<>();
            fallback.put("value", rawBody.trim());
            return fallback;
        }

        if (queryParams != null && !queryParams.isEmpty()) {
            Map<String, Object> map = new HashMap<>();
            queryParams.forEach((key, values) -> {
                if (values == null || values.isEmpty()) {
                    map.put(key, "");
                } else if (values.size() == 1) {
                    map.put(key, values.get(0));
                } else {
                    map.put(key, values);
                }
            });
            return map;
        }

        return Map.of();
    }

    private Map<String, Object> parseJsonObject(String rawBody) {
        try {
            return objectMapper.readValue(rawBody, new TypeReference<>() {
            });
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    private Map<String, Object> parseFormBody(String rawBody) {
        if (!rawBody.contains("=")) {
            return Map.of();
        }

        Map<String, Object> values = new HashMap<>();
        String[] pairs = rawBody.split("&");
        for (String pair : pairs) {
            if (!StringUtils.hasText(pair)) {
                continue;
            }

            String[] kv = pair.split("=", 2);
            String key = decodeUrlComponent(kv[0]);
            String value = kv.length > 1 ? decodeUrlComponent(kv[1]) : "";
            if (StringUtils.hasText(key)) {
                values.put(key, value);
            }
        }
        return values;
    }

    private String decodeUrlComponent(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
