package com.wowinfobiz.analyticsservice.services.observer;

import com.wowinfobiz.analyticsservice.services.AnalyticsEventStore;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Order(2)
public class DatabaseAnalyticsObserver implements AnalyticsEventObserver {

    private final AnalyticsEventStore analyticsEventStore;

    public DatabaseAnalyticsObserver(AnalyticsEventStore analyticsEventStore) {
        this.analyticsEventStore = analyticsEventStore;
    }

    @Override
    public void onAnalyticsEvent(Map<String, Object> event) {
        analyticsEventStore.addEvent(event);
    }
}
