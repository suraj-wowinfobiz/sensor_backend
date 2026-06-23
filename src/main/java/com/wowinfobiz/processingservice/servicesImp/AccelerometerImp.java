package com.wowinfobiz.processingservice.servicesImp;

import com.wowinfobiz.processingservice.dto.ProcessDataResponse;
import com.wowinfobiz.processingservice.dto.SensorRawDataRequest;
import com.wowinfobiz.processingservice.services.sensors.Accelerometer;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AccelerometerImp implements Accelerometer {


    @Override
    public ProcessDataResponse<?> processSensorData(SensorRawDataRequest<?> sensorRawDataRequest) {
        Map<String, Object> parameters = sensorRawDataRequest.getParameters();
        double x = extractAxis(parameters, "x", "ax", "accelX", "acc_x");
        double y = extractAxis(parameters, "y", "ay", "accelY", "acc_y");
        double z = extractAxis(parameters, "z", "az", "accelZ", "acc_z");

        double accelerationMagnitude = Math.sqrt((x * x) + (y * y) + (z * z));
        double accelerationMs2 = accelerationMagnitude * 9.80665d;
        double horizontalMagnitude = Math.sqrt((x * x) + (y * y));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("x", x);
        response.put("y", y);
        response.put("z", z);
        response.put("accelerationMagnitude", accelerationMagnitude);
        response.put("accelerationMs2", accelerationMs2);
        response.put("horizontalMagnitude", horizontalMagnitude);
        response.put("dominantAxis", dominantAxis(x, y, z));
        response.put("sourceDataType", sensorRawDataRequest.getDataType());
        response.put("timestamp", sensorRawDataRequest.getTimestamp());
        return new ProcessDataResponse<>("Processed successfully", true, response);
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

    private String dominantAxis(double x, double y, double z) {
        double absX = Math.abs(x);
        double absY = Math.abs(y);
        double absZ = Math.abs(z);
        if (absX >= absY && absX >= absZ) {
            return "x";
        }
        if (absY >= absX && absY >= absZ) {
            return "y";
        }
        return "z";
    }
}
