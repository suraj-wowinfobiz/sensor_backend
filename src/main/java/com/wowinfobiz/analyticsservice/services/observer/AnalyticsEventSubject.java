package com.wowinfobiz.analyticsservice.services.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsEventSubject {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsEventSubject.class);

    private final List<AnalyticsEventObserver> observers;
    private final AnalyticsEventCache analyticsEventCache;

    public AnalyticsEventSubject(List<AnalyticsEventObserver> observers, AnalyticsEventCache analyticsEventCache) {
        this.observers = observers;
        this.analyticsEventCache = analyticsEventCache;
    }

    public void publish(Map<String, Object> event) {
        Map<String, Object> safeEvent = event == null ? Map.of() : new LinkedHashMap<>(event);
        analyticsEventCache.setLastEvent(safeEvent);
        for (AnalyticsEventObserver observer : observers) {
            try {
                observer.onAnalyticsEvent(safeEvent);
            } catch (Exception ex) {
                LOG.error("Analytics observer {} failed to process event", observer.getClass().getSimpleName(), ex);
            }
        }
    }
}
