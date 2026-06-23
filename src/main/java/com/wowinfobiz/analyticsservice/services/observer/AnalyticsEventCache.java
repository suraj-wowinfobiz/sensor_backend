package com.wowinfobiz.analyticsservice.services.observer;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class AnalyticsEventCache {

    private final AtomicReference<Map<String, Object>> lastEvent = new AtomicReference<>(Map.of());

    public void setLastEvent(Map<String, Object> event) {
        Map<String, Object> safe = event == null ? Map.of() : new LinkedHashMap<>(event);
        lastEvent.set(safe);
    }

    public Map<String, Object> getLastEvent() {
        Map<String, Object> event = lastEvent.get();
        return event == null ? Map.of() : new LinkedHashMap<>(event);
    }
}
