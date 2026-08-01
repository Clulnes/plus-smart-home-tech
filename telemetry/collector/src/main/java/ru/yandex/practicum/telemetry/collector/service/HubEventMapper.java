package ru.yandex.practicum.telemetry.collector.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.collector.service.handler.hub.HubEventHandler;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class HubEventMapper {

    private final Map<HubEventProto.PayloadCase, HubEventHandler> handlers;

    public HubEventMapper(List<HubEventHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getMessageType,
                        Function.identity()
                ));
    }

    public HubEventAvro toAvro(HubEventProto event) {
        HubEventHandler handler = handlers.get(event.getPayloadCase());

        if (handler == null) {
            throw new IllegalArgumentException("Не найден обработчик для типа события хаба: " + event.getPayloadCase());
        }

        Object payload = handler.handlePayload(event);

        Instant timestamp = Instant.ofEpochSecond(
                event.getTimestamp().getSeconds(),
                event.getTimestamp().getNanos()
        );

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(timestamp)
                .setPayload(payload)
                .build();
    }
}