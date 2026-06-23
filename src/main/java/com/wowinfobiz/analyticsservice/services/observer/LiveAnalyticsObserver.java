package com.wowinfobiz.analyticsservice.services.observer;

import org.springframework.core.annotation.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Order(1)
public class LiveAnalyticsObserver implements AnalyticsEventObserver {
    private static final Logger LOG = LoggerFactory.getLogger(LiveAnalyticsObserver.class);

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final AnalyticsEventCache analyticsEventCache;

    public LiveAnalyticsObserver(AnalyticsEventCache analyticsEventCache) {
        this.analyticsEventCache = analyticsEventCache;
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(ex -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("connected").data(Map.of("message", "Analytics live stream connected")));
            Map<String, Object> lastEvent = analyticsEventCache.getLastEvent();
            if (!lastEvent.isEmpty()) {
                emitter.send(toChartPayload(lastEvent));
            }
        } catch (Exception ex) {
            emitters.remove(emitter);
            emitter.completeWithError(ex);
        }
        return emitter;
    }

    @Override
    public void onAnalyticsEvent(Map<String, Object> event) {
        if (emitters.isEmpty()) {
            return;
        }
        Map<String, Object> livePayload = toChartPayload(event);

        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                // Send as default SSE message event for broad client compatibility.
                emitter.send(livePayload);
            } catch (Exception ex) {
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
        if (!dead.isEmpty()) {
            LOG.debug("Removed {} dead analytics live emitters; active={}", dead.size(), emitters.size());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toChartPayload(Map<String, Object> event) {
        Map<String, Object> safeEvent = event == null ? Map.of() : event;

        Object timestamp = safeEvent.getOrDefault("timestamp", Instant.now().toString());
        long epochMillis = parseEpochMillis(timestamp);

        List<Map<String, Object>> series = new ArrayList<>();
        Object evaluationsRaw = safeEvent.get("evaluations");
        if (evaluationsRaw instanceof List<?> evaluations) {
            for (Object item : evaluations) {
                if (!(item instanceof Map<?, ?> evalMap)) {
                    continue;
                }
                Object valueObj = evalMap.get("value");
                if (!(valueObj instanceof Number value)) {
                    continue;
                }
                Map<String, Object> point = new LinkedHashMap<>();
                point.put("name", evalMap.containsKey("parameterName") ? String.valueOf(evalMap.get("parameterName")) : "value");
                point.put("value", value.doubleValue());
                point.put("x", epochMillis);
                point.put("timestamp", timestamp);
                series.add(point);
            }
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventType", "analytics-live");
        payload.put("sensorId", safeEvent.get("sensorId"));
        payload.put("readingId", safeEvent.get("readingId"));
        payload.put("dataType", safeEvent.get("dataType"));
        payload.put("timestamp", timestamp);
        payload.put("x", epochMillis);
        payload.put("alertCount", safeEvent.getOrDefault("alertCount", 0));
        payload.put("series", series);
        payload.put("event", safeEvent);
        return payload;
    }

    private long parseEpochMillis(Object timestamp) {
        if (timestamp instanceof Number number) {
            long value = number.longValue();
            return value > 1_000_000_000_000L ? value : value * 1000L;
        }
        try {
            return Instant.parse(String.valueOf(timestamp)).toEpochMilli();
        } catch (Exception ex) {
            return Instant.now().toEpochMilli();
        }
    }
}
