package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import ru.yandex.practicum.telemetry.collector.dto.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.dto.sensor.SensorEventType;

public interface SensorEventHandler {
    SensorEventType getMessageType();

    Object handlePayload(SensorEvent event);
}