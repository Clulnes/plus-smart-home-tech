package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.telemetry.collector.dto.sensor.MotionSensorEvent;
import ru.yandex.practicum.telemetry.collector.dto.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.dto.sensor.SensorEventType;

@Component
public class MotionSensorEventHandler implements SensorEventHandler {

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }

    @Override
    public Object handlePayload(SensorEvent event) {
        MotionSensorEvent me = (MotionSensorEvent) event;

        return MotionSensorAvro.newBuilder()
                .setLinkQuality(me.getLinkQuality())
                .setMotion(me.isMotion())
                .setVoltage(me.getVoltage())
                .build();
    }
}
