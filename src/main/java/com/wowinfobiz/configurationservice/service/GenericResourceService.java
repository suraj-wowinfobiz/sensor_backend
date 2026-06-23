package com.wowinfobiz.configurationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wowinfobiz.configurationservice.model.GenericResourceEntity;
import com.wowinfobiz.configurationservice.repository.GenericResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GenericResourceService {

    private final GenericResourceRepository repository;
    private final JsonMapperService jsonMapperService;

    public GenericResourceService(GenericResourceRepository repository, JsonMapperService jsonMapperService) {
        this.repository = repository;
        this.jsonMapperService = jsonMapperService;
    }

    public List<JsonNode> list(String domain) {
        return repository.findByDomainOrderByCreatedAtDesc(domain).stream()
                .map(this::toResponse)
                .toList();
    }

    public JsonNode get(String domain, UUID id) {
        GenericResourceEntity entity = repository.findByDomainAndId(domain, id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
        return toResponse(entity);
    }

    public JsonNode create(String domain, JsonNode payload) {
        GenericResourceEntity entity = new GenericResourceEntity();
        entity.setDomain(domain);
        entity.setPayloadJson(jsonMapperService.toJson(payload));
        return toResponse(repository.save(entity));
    }

    public JsonNode upsert(String domain, UUID id, JsonNode payload) {
        GenericResourceEntity entity = repository.findByDomainAndId(domain, id)
                .orElseGet(GenericResourceEntity::new);
        entity.setId(id);
        entity.setDomain(domain);
        entity.setPayloadJson(jsonMapperService.toJson(payload));
        return toResponse(repository.save(entity));
    }

    public void delete(String domain, UUID id) {
        GenericResourceEntity entity = repository.findByDomainAndId(domain, id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
        repository.delete(entity);
    }

    public long count(String domain) {
        return repository.countByDomain(domain);
    }

    private JsonNode toResponse(GenericResourceEntity entity) {
        JsonNode parsed = jsonMapperService.fromJson(entity.getPayloadJson());
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        if (parsed.isObject()) {
            payload.setAll((ObjectNode) parsed);
        } else {
            payload.set("data", parsed);
        }
        payload.put("id", entity.getId().toString());
        payload.put("createdAt", entity.getCreatedAt().toString());
        payload.put("updatedAt", entity.getUpdatedAt().toString());
        return payload;
    }
}
