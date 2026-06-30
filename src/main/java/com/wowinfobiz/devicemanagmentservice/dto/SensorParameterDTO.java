package com.wowinfobiz.devicemanagmentservice.dto;

import java.util.UUID;

public class SensorParameterDTO {

    UUID sensorParameterId;
    UUID sensorTypeId;
    String name;
    String unit;
    double minValue;
    double maxValue;
    String calculationName;
    String formulaType;
    String graphType;
    String useFor;

    public UUID getSensorParameterId() {
        return sensorParameterId;
    }

    public void setSensorParameterId(UUID sensorParameterId) {
        this.sensorParameterId = sensorParameterId;
    }

    public UUID getSensorTypeId() {
        return sensorTypeId;
    }

    public void setSensorTypeId(UUID sensorTypeId) {
        this.sensorTypeId = sensorTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public String getCalculationName() {
        return calculationName;
    }

    public void setCalculationName(String calculationName) {
        this.calculationName = calculationName;
    }

    public String getFormulaType() {
        return formulaType;
    }

    public void setFormulaType(String formulaType) {
        this.formulaType = formulaType;
    }

    public String getGraphType() {
        return graphType;
    }

    public void setGraphType(String graphType) {
        this.graphType = graphType;
    }

    public String getUseFor() {
        return useFor;
    }

    public void setUseFor(String useFor) {
        this.useFor = useFor;
    }
}
