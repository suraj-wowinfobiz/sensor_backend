package com.wowinfobiz.analyticsservice.services.observer;

import java.util.Map;

public interface AnalyticsEventObserver {
    void onAnalyticsEvent(Map<String, Object> event);
}
