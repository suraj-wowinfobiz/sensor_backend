package com.wowinfobiz.processingservice.servicesImp;

import com.wowinfobiz.processingservice.dto.ProcessDataResponse;
import com.wowinfobiz.processingservice.dto.SensorRawDataRequest;
import com.wowinfobiz.processingservice.services.sensors.InclinoMeter;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class InclinoMeterImp implements InclinoMeter {
    @Override
    public ProcessDataResponse<?> processSensorData(SensorRawDataRequest<?> sensorRawDataRequest) {
        Map<String, Object> parameters = sensorRawDataRequest.getParameters();
        double x = extractAxis(parameters, "x", "ax", "accelX", "acc_x");
        double y = extractAxis(parameters, "y", "ay", "accelY", "acc_y");
        double z = extractAxis(parameters, "z", "az", "accelZ", "acc_z");

        double inclinationDegrees = Math.toDegrees(Math.atan2(Math.sqrt((x * x) + (y * y)), z));
        double headingDegrees = Math.toDegrees(Math.atan2(y, x));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("inclinationDegrees", inclinationDegrees);
        body.put("headingDegrees", headingDegrees);
        body.put("x", x);
        body.put("y", y);
        body.put("z", z);
        return new ProcessDataResponse<>("Processed successfully", true, body);
    }

    private double extractAxis(Map<String, Object> parameters, String... keys) {
        if (parameters == null || parameters.isEmpty()) {
            return 0d;
        }
        for (String key : keys) {
            Object value = parameters.get(key);
            Double parsed = toDouble(value);
            if (parsed != null) {
                return parsed;
            }
        }
        return 0d;
    }

    private Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.doubleValue();
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
