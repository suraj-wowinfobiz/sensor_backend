package com.wowinfobiz.processingservice.servicesImp;

import com.wowinfobiz.processingservice.dto.ProcessDataResponse;
import com.wowinfobiz.processingservice.dto.SensorRawDataRequest;
import com.wowinfobiz.processingservice.services.sensors.PiezoMeter;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PiezoMeterImp implements PiezoMeter {
    @Override
    public ProcessDataResponse<?> processSensorData(SensorRawDataRequest<?> sensorRawDataRequest) {
        Map<String, Object> parameters = sensorRawDataRequest.getParameters();

        double pressureKpa = readDouble(parameters, 0d, "pressureKpa", "pressure_kpa", "pressure", "porePressureKpa");
        double waterHeadM = readDouble(parameters, Double.NaN, "waterHeadM", "water_head_m", "headM", "head");

        if (Double.isNaN(waterHeadM)) {
            waterHeadM = pressureKpa / 9.80665d;
        } else if (pressureKpa == 0d) {
            pressureKpa = waterHeadM * 9.80665d;
        }

        double pressurePa = pressureKpa * 1000d;
        double porePressureRatio = readDouble(parameters, 0d, "porePressureRatio", "ru", "ratio");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pressureKpa", pressureKpa);
        body.put("pressurePa", pressurePa);
        body.put("waterHeadM", waterHeadM);
        body.put("porePressureRatio", porePressureRatio);
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
