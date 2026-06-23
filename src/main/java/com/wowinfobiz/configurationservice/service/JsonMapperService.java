package com.wowinfobiz.configurationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class JsonMapperService {

    private final ObjectMapper objectMapper;

    public JsonMapperService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(JsonNode payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON payload", e);
        }
    }

    public JsonNode fromJson(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Stored JSON is invalid", e);
        }
    }
}
