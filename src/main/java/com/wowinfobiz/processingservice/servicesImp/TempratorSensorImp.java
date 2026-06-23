package com.wowinfobiz.processingservice.servicesImp;

import com.wowinfobiz.processingservice.dto.ProcessDataResponse;
import com.wowinfobiz.processingservice.dto.SensorRawDataRequest;
import com.wowinfobiz.processingservice.services.sensors.TempratorSensor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TempratorSensorImp implements TempratorSensor {
    @Override
    public ProcessDataResponse<?> processSensorData(SensorRawDataRequest<?> sensorRawDataRequest) {
        Map<String, Object> parameters = sensorRawDataRequest.getParameters();

        double temperatureC = readDouble(parameters, Double.NaN, "temperatureC", "temperature_c", "temperature", "temp", "value");
        double temperatureF = readDouble(parameters, Double.NaN, "temperatureF", "temperature_f", "tempF");

        if (Double.isNaN(temperatureC) && !Double.isNaN(temperatureF)) {
            temperatureC = (temperatureF - 32d) * (5d / 9d);
        } else if (!Double.isNaN(temperatureC) && Double.isNaN(temperatureF)) {
            temperatureF = (temperatureC * (9d / 5d)) + 32d;
        } else if (Double.isNaN(temperatureC)) {
            temperatureC = 0d;
            temperatureF = 32d;
        }

        double temperatureK = temperatureC + 273.15d;
        double humidity = readDouble(parameters, 0d, "humidity", "humidityPercent", "rh");
        if (humidity < 0d) {
            humidity = 0d;
        }
        if (humidity > 100d) {
            humidity = 100d;
        }

        double dewPointC = calculateDewPoint(temperatureC, humidity);
        double heatIndexC = calculateHeatIndexC(temperatureC, humidity);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("temperatureC", temperatureC);
        body.put("temperatureF", temperatureF);
        body.put("temperatureK", temperatureK);
        body.put("humidity", humidity);
        body.put("dewPointC", dewPointC);
        body.put("heatIndexC", heatIndexC);
        body.put("sensorId", sensorRawDataRequest.getSensorId());
        body.put("readingId", sensorRawDataRequest.getReadingId());
        body.put("timestamp", sensorRawDataRequest.getTimestamp());

        return new ProcessDataResponse<>("Processed successfully", true, body);
    }

    private double calculateDewPoint(double temperatureC, double humidityPercent) {
        if (humidityPercent <= 0d) {
            return temperatureC;
        }
        double a = 17.27d;
        double b = 237.7d;
        double gamma = ((a * temperatureC) / (b + temperatureC)) + Math.log(humidityPercent / 100d);
        return (b * gamma) / (a - gamma);
    }

    private double calculateHeatIndexC(double temperatureC, double humidityPercent) {
        double temperatureF = (temperatureC * (9d / 5d)) + 32d;
        double hiF = -42.379d
                + (2.04901523d * temperatureF)
                + (10.14333127d * humidityPercent)
                - (0.22475541d * temperatureF * humidityPercent)
                - (0.00683783d * temperatureF * temperatureF)
                - (0.05481717d * humidityPercent * humidityPercent)
                + (0.00122874d * temperatureF * temperatureF * humidityPercent)
                + (0.00085282d * temperatureF * humidityPercent * humidityPercent)
                - (0.00000199d * temperatureF * temperatureF * humidityPercent * humidityPercent);
        return (hiF - 32d) * (5d / 9d);
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
