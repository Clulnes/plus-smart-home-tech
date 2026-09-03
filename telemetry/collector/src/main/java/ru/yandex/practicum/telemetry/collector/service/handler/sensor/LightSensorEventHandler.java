package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.LightSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;

@Component
public class LightSensorEventHandler implements SensorEventHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR;
    }

    @Override
    public Object handlePayload(SensorEventProto event) {
        LightSensorProto light = event.getLightSensor();

        return LightSensorAvro.newBuilder()
                .setLinkQuality(light.getLinkQuality())
                .setLuminosity(light.getLuminosity())
                .build();
    }
}
