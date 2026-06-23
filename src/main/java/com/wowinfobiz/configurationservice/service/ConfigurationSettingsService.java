package com.wowinfobiz.configurationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ConfigurationSettingsService {

    private static final List<String> CONFIG_KEYS = List.of(
            "config",
            "configsystem",
            "confignotifications",
            "configthresholds",
            "configalerts",
            "configemail"
    );

    private final StringRedisTemplate redisTemplate;
    private final JsonMapperService jsonMapperService;

    public ConfigurationSettingsService(StringRedisTemplate redisTemplate, JsonMapperService jsonMapperService) {
        this.redisTemplate = redisTemplate;
        this.jsonMapperService = jsonMapperService;
    }

    public JsonNode getConfig(String key) {
        String redisKey = "cfg:" + key;
        String json = redisTemplate.opsForValue().get(redisKey);
        if (json == null) {
            return jsonMapperService.fromJson("{}");
        }
        return jsonMapperService.fromJson(json);
    }

    public JsonNode saveConfig(String key, JsonNode payload) {
        String redisKey = "cfg:" + key;
        redisTemplate.opsForValue().set(redisKey, jsonMapperService.toJson(payload));
        return getConfig(key);
    }

    public JsonNode testEmail(JsonNode payload) {
        redisTemplate.opsForValue().set("cfg:test-email:last", jsonMapperService.toJson(payload), 1, TimeUnit.DAYS);
        return jsonMapperService.fromJson("{\"status\":\"queued\"}");
    }

    public long configKeyCount() {
        return CONFIG_KEYS.stream().count();
    }
}
