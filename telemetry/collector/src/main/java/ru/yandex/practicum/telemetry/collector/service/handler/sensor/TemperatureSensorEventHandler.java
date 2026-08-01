package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

@Component
public class TemperatureSensorEventHandler implements SensorEventHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR;
    }

    @Override
    public Object handlePayload(SensorEventProto event) {
        TemperatureSensorProto temp = event.getTemperatureSensor();

        return TemperatureSensorAvro.newBuilder()
                .setTemperatureC(temp.getTemperatureC())
                .setTemperatureF(temp.getTemperatureF())
                .build();
    }
}