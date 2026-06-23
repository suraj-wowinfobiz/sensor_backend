package com.wowinfobiz.processingservice.servicesImp;

import com.wowinfobiz.processingservice.dto.ProcessDataResponse;
import com.wowinfobiz.processingservice.dto.SensorRawDataRequest;
import com.wowinfobiz.processingservice.services.sensors.CrackMeter;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CrackMeterImp implements CrackMeter {
    @Override
    public ProcessDataResponse<?> processSensorData(SensorRawDataRequest<?> sensorRawDataRequest) {
        Map<String, Object> parameters = sensorRawDataRequest.getParameters();

        double crackWidthMm = readDouble(parameters, 0d, "crackWidth", "crack_width", "width", "value");
        double baselineMm = readDouble(parameters, crackWidthMm, "baselineWidth", "baseline", "initialWidth", "referenceWidth");
        double previousMm = readDouble(parameters, crackWidthMm, "previousWidth", "lastWidth", "prev", "previous");
        double alertThresholdMm = readDouble(parameters, Double.MAX_VALUE, "threshold", "alertThreshold", "maxWidth");

        double changeFromBaselineMm = crackWidthMm - baselineMm;
        double changeFromPreviousMm = crackWidthMm - previousMm;
        double changePercentFromBaseline = baselineMm == 0d ? 0d : (changeFromBaselineMm / baselineMm) * 100d;
        boolean thresholdExceeded = crackWidthMm >= alertThresholdMm;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("crackWidthMm", crackWidthMm);
        body.put("baselineWidthMm", baselineMm);
        body.put("previousWidthMm", previousMm);
        body.put("changeFromBaselineMm", changeFromBaselineMm);
        body.put("changeFromPreviousMm", changeFromPreviousMm);
        body.put("changePercentFromBaseline", changePercentFromBaseline);
        body.put("thresholdExceeded", thresholdExceeded);
        body.put("sensorId", sensorRawDataRequest.getSensorId());
        body.put("readingId", sensorRawDataRequest.getReadingId());
        body.put("timestamp", sensorRawDataRequest.getTimestamp());

        return new ProcessDataResponse<>("Processed successfully", true, body);
    }

    private double readDouble(Map<String, Object> parameters, double defaultValue, String... keys) {
        if (parameters == null || parameters.isEmpty()) {
            return defaultValue;
        }
        for (String key : keys) {
            Double parsed = toDouble(parameters.get(key));
            if (parsed != null) {
                return parsed;
            }
        }
        return defaultValue;
    }

    private Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            String raw = String.valueOf(value).trim();
            if (raw.isEmpty()) {
                return null;
            }
            return Double.parseDouble(raw);
        } catch (Exception ignored) {
            return null;
        }
    }
}
