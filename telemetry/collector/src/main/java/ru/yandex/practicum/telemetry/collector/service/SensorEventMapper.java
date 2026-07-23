package ru.yandex.practicum.telemetry.collector.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.telemetry.collector.dto.sensor.ClimateSensorEvent;
import ru.yandex.practicum.telemetry.collector.dto.sensor.LightSensorEvent;
import ru.yandex.practicum.telemetry.collector.dto.sensor.MotionSensorEvent;
import ru.yandex.practicum.telemetry.collector.dto.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.dto.sensor.SwitchSensorEvent;
import ru.yandex.practicum.telemetry.collector.dto.sensor.TemperatureSensorEvent;

@Service
public class SensorEventMapper {

    public SensorEventAvro toAvro(SensorEvent event) {
        Object payload;

        if (event instanceof ClimateSensorEvent ce) {
            payload = ClimateSensorAvro.newBuilder()
                    .setTemperatureC(ce.getTemperatureC())
                    .setHumidity(ce.getHumidity())
                    .setCo2Level(ce.getCo2Level())
                    .build();
        } else if (event instanceof LightSensorEvent le) {
            payload = LightSensorAvro.newBuilder()
                    .setLinkQuality(le.getLinkQuality())
                    .setLuminosity(le.getLuminosity())
                    .build();
        } else if (event instanceof MotionSensorEvent me) {
            payload = MotionSensorAvro.newBuilder()
                    .setLinkQuality(me.getLinkQuality())
                    .setMotion(me.isMotion())
                    .setVoltage(me.getVoltage())
                    .build();
        } else if (event instanceof SwitchSensorEvent se) {
            payload = SwitchSensorAvro.newBuilder()
                    .setState(se.isState())
                    .build();
        } else if (event instanceof TemperatureSensorEvent te) {
            payload = TemperatureSensorAvro.newBuilder()
                    .setTemperatureC(te.getTemperatureC())
                    .setTemperatureF(te.getTemperatureF())
                    .build();
        } else {
            throw new IllegalArgumentException("Неизвестный тип события датчика: " + event.getClass());
        }

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }
}