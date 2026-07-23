package ru.yandex.practicum.telemetry.collector.dto.hub;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeviceActionDto {
    @NotBlank
    private String sensorId;

    @NotNull
    private ActionType type;
    private Integer value;

    public enum ActionType {
        ACTIVATE, DEACTIVATE, INVERSE, SET_VALUE
    }
}