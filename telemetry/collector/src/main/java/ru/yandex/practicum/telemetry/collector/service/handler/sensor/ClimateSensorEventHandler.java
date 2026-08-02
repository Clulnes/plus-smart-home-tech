package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ClimateSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;

@Component
public class ClimateSensorEventHandler implements SensorEventHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR;
    }

    @Override
    public Object handlePayload(SensorEventProto event) {
        ClimateSensorProto climate = event.getClimateSensor();

        return ClimateSensorAvro.newBuilder()
                .setTemperatureC(climate.getTemperatureC())
                .setHumidity(climate.getHumidity())
                .setCo2Level(climate.getCo2Level())
                .build();
    }
}
