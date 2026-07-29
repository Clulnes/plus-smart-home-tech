package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import ru.yandex.practicum.telemetry.collector.dto.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.dto.hub.HubEventType;

public interface HubEventHandler {
    HubEventType getMessageType();

    Object handlePayload(HubEvent event);
}