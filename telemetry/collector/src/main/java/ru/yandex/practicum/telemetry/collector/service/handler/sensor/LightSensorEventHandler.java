package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.telemetry.collector.dto.sensor.LightSensorEvent;
import ru.yandex.practicum.telemetry.collector.dto.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.dto.sensor.SensorEventType;

@Component
public class LightSensorEventHandler implements SensorEventHandler {

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }

    @Override
    public Object handlePayload(SensorEvent event) {
        LightSensorEvent le = (LightSensorEvent) event;

        return LightSensorAvro.newBuilder()
                .setLinkQuality(le.getLinkQuality())
                .setLuminosity(le.getLuminosity())
                .build();
    }
}
