package ru.yandex.practicum.telemetry.collector.dto.hub;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScenarioConditionDto {
    @NotBlank
    private String sensorId;

    @NotNull
    private ConditionType type;

    @NotNull
    private ConditionOperation operation;
    private Integer value;

    public enum ConditionType {
        MOTION, LUMINOSITY, SWITCH, TEMPERATURE, CO2LEVEL, HUMIDITY
    }
    public enum ConditionOperation {
        EQUALS, GREATER_THAN, LOWER_THAN
    }
}