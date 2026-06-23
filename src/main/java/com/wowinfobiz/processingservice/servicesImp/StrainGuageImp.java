package com.wowinfobiz.processingservice.servicesImp;

import com.wowinfobiz.processingservice.dto.ProcessDataResponse;
import com.wowinfobiz.processingservice.dto.SensorRawDataRequest;
import com.wowinfobiz.processingservice.services.sensors.StrainGuage;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class StrainGuageImp implements StrainGuage {
    @Override
    public ProcessDataResponse<?> processSensorData(SensorRawDataRequest<?> sensorRawDataRequest) {
        Map<String, Object> parameters = sensorRawDataRequest.getParameters();

        double microStrain = readDouble(parameters, Double.NaN, "microStrain", "micro_strain", "strain", "value");
        double deltaLengthMm = readDouble(parameters, 0d, "deltaLengthMm", "delta_length_mm", "deltaLength");
        double gaugeLengthMm = readDouble(parameters, 0d, "gaugeLengthMm", "gauge_length_mm", "gaugeLength");

        if (Double.isNaN(microStrain) && gaugeLengthMm > 0d) {
            microStrain = (deltaLengthMm / gaugeLengthMm) * 1_000_000d;
        } else if (!Double.isNaN(microStrain) && gaugeLengthMm > 0d && deltaLengthMm == 0d) {
            deltaLengthMm = (microStrain / 1_000_000d) * gaugeLengthMm;
        } else if (Double.isNaN(microStrain)) {
            microStrain = 0d;
        }

        double strain = microStrain / 1_000_000d;
        double strainPercent = strain * 100d;

        double youngModulusGpa = readDouble(parameters, 0d, "youngModulusGpa", "young_modulus_gpa", "elasticModulusGpa");
        double stressMpa = youngModulusGpa > 0d ? strain * youngModulusGpa * 1000d : 0d;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("microStrain", microStrain);
        body.put("strain", strain);
        body.put("strainPercent", strainPercent);
        body.put("deltaLengthMm", deltaLengthMm);
        body.put("gaugeLengthMm", gaugeLengthMm);
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
