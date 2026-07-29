package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.telemetry.collector.dto.hub.DeviceAddedEvent;
import ru.yandex.practicum.telemetry.collector.dto.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.dto.hub.HubEventType;

@Component
public class DeviceAddedHubEventHandler implements HubEventHandler {

    @Override
    public HubEventType getMessageType() {
        return HubEventType.DEVICE_ADDED;
    }

    @Override
    public Object handlePayload(HubEvent event) {
        DeviceAddedEvent da = (DeviceAddedEvent) event;
        return DeviceAddedEventAvro.newBuilder()
                .setId(da.getId())
                .setType(DeviceTypeAvro.valueOf(da.getDeviceType().name()))
                .build();
    }
}