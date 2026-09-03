package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.stream.Collectors;

@Component
public class ScenarioAddedHubEventHandler implements HubEventHandler {

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    public Object handlePayload(HubEventProto event) {
        ScenarioAddedEventProto sa = event.getScenarioAdded();

        var conditions = sa.getConditionList().stream()
                .map(c -> {
                    int conditionValue = 0;
                    if (c.getValueCase() == ScenarioConditionProto.ValueCase.INT_VALUE) {
                        conditionValue = c.getIntValue();
                    } else if (c.getValueCase() == ScenarioConditionProto.ValueCase.BOOL_VALUE) {
                        conditionValue = c.getBoolValue() ? 1 : 0;
                    }

                    return ScenarioConditionAvro.newBuilder()
                            .setSensorId(c.getSensorId())
                            .setType(ConditionTypeAvro.valueOf(c.getType().name()))
                            .setOperation(ConditionOperationAvro.valueOf(c.getOperation().name()))
                            .setValue(conditionValue)
                            .build();
                })
                .collect(Collectors.toList());

        var actions = sa.getActionList().stream()
                .map(a -> DeviceActionAvro.newBuilder()
                        .setSensorId(a.getSensorId())
                        .setType(ActionTypeAvro.valueOf(a.getType().name()))
                        .setValue(a.getValue())
                        .build())
                .collect(Collectors.toList());

        return ScenarioAddedEventAvro.newBuilder()
                .setName(sa.getName())
                .setConditions(conditions)
                .setActions(actions)
                .build();
    }
}