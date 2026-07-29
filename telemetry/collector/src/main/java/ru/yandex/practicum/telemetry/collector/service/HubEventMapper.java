package ru.yandex.practicum.telemetry.collector.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.collector.dto.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.dto.hub.HubEventType;
import ru.yandex.practicum.telemetry.collector.service.handler.hub.HubEventHandler;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class HubEventMapper {

    private final Map<HubEventType, HubEventHandler> handlers;

    public HubEventMapper(List<HubEventHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getMessageType,
                        Function.identity()
                ));
    }

    public HubEventAvro toAvro(HubEvent event) {
        HubEventHandler handler = handlers.get(event.getType());

        if (handler == null) {
            throw new IllegalArgumentException("Не найден обработчик для типа события хаба: " + event.getType());
        }

        Object payload = handler.handlePayload(event);

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }
}