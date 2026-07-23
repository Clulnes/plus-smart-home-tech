package ru.yandex.practicum.telemetry.collector.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.telemetry.collector.dto.hub.DeviceAddedEvent;
import ru.yandex.practicum.telemetry.collector.dto.hub.DeviceRemovedEvent;
import ru.yandex.practicum.telemetry.collector.dto.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.dto.hub.ScenarioAddedEvent;
import ru.yandex.practicum.telemetry.collector.dto.hub.ScenarioRemovedEvent;

import java.util.stream.Collectors;

@Service
public class HubEventMapper {

    public HubEventAvro toAvro(HubEvent event) {
        Object payload;

        if (event instanceof DeviceAddedEvent da) {
            payload = DeviceAddedEventAvro.newBuilder()
                    .setId(da.getId())
                    .setType(DeviceTypeAvro.valueOf(da.getDeviceType().name()))
                    .build();
        } else if (event instanceof DeviceRemovedEvent dr) {
            payload = DeviceRemovedEventAvro.newBuilder()
                    .setId(dr.getId())
                    .build();
        } else if (event instanceof ScenarioAddedEvent sa) {
            payload = ScenarioAddedEventAvro.newBuilder()
                    .setName(sa.getName())
                    .setConditions(sa.getConditions().stream().map(c -> ScenarioConditionAvro.newBuilder()
                            .setSensorId(c.getSensorId())
                            .setType(ConditionTypeAvro.valueOf(c.getType().name()))
                            .setOperation(ConditionOperationAvro.valueOf(c.getOperation().name()))
                            .setValue(c.getValue())
                            .build()).collect(Collectors.toList()))
                    .setActions(sa.getActions().stream().map(a -> DeviceActionAvro.newBuilder()
                            .setSensorId(a.getSensorId())
                            .setType(ActionTypeAvro.valueOf(a.getType().name()))
                            .setValue(a.getValue())
                            .build()).collect(Collectors.toList()))
                    .build();
        } else if (event instanceof ScenarioRemovedEvent sr) {
            payload = ScenarioRemovedEventAvro.newBuilder()
                    .setName(sr.getName())
                    .build();
        } else {
            throw new IllegalArgumentException("Неизвестный тип события хаба: " + event.getClass());
        }

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }
}