package com.wowinfobiz.processingservice.servicesImp;

import com.wowinfobiz.processingservice.dto.ProcessDataResponse;
import com.wowinfobiz.processingservice.dto.SensorRawDataRequest;
import com.wowinfobiz.processingservice.services.sensors.LoadCellSensor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class LoadCellSensorImp implements LoadCellSensor {
    @Override
    public ProcessDataResponse<?> processSensorData(SensorRawDataRequest<?> sensorRawDataRequest) {
        Map<String, Object> parameters = sensorRawDataRequest.getParameters();

        double loadKn = readDouble(parameters, 0d, "loadKn", "load_kN", "load", "forceKn");
        double forceN = readDouble(parameters, Double.NaN, "forceN", "force_n", "newton");
        if (Double.isNaN(forceN)) {
            forceN = loadKn * 1000d;
        } else if (loadKn == 0d) {
            loadKn = forceN / 1000d;
        }

        double areaM2 = readDouble(parameters, 0d, "areaM2", "area_m2", "area");
        double stressPa = areaM2 > 0d ? forceN / areaM2 : 0d;
        double stressMpa = stressPa / 1_000_000d;

        double zeroOffset = readDouble(parameters, 0d, "zeroOffset", "offset", "tare");
        double calibratedLoadKn = loadKn - zeroOffset;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("loadKn", loadKn);
        body.put("calibratedLoadKn", calibratedLoadKn);
        body.put("forceN", forceN);
        body.put("areaM2", areaM2);
        body.put("stressPa", stressPa);
        body.put("stressMpa", stressMpa);
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
