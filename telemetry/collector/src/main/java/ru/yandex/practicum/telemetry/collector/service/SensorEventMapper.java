package ru.yandex.practicum.telemetry.collector.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.service.handler.sensor.SensorEventHandler;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SensorEventMapper {
    private final Map<SensorEventProto.PayloadCase, SensorEventHandler> handlers;

    public SensorEventMapper(List<SensorEventHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(
                        SensorEventHandler::getMessageType,
                        Function.identity()
                ));
    }

    public SensorEventAvro toAvro(SensorEventProto event) {
        SensorEventHandler handler = handlers.get(event.getPayloadCase());

        if (handler == null) {
            throw new IllegalArgumentException("Не найден обработчик для типа события датчика: " + event.getPayloadCase());
        }

        Object payload = handler.handlePayload(event);

        Instant timestamp = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        );

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(timestamp)
                .setPayload(payload)
                .build();
    }
}