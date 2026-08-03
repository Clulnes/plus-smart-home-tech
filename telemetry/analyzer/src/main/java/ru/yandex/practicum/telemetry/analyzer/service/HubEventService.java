package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.model.*;
import ru.yandex.practicum.telemetry.analyzer.repository.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class HubEventService {
    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    @Transactional
    public void processHubEvent(HubEventAvro event) {
        String hubId = event.getHubId();
        Object payload = event.getPayload();

        if (payload instanceof DeviceAddedEventAvro da) {
            Sensor sensor = Sensor.builder()
                    .id(da.getId())
                    .hubId(hubId)
                    .build();
            sensorRepository.save(sensor);
            log.info("Сохранено устройство: id={}, hubId={}", da.getId(), hubId);

        } else if (payload instanceof DeviceRemovedEventAvro dr) {
            sensorRepository.deleteById(dr.getId());
            log.info("Удалено устройство: id={}", dr.getId());

        } else if (payload instanceof ScenarioAddedEventAvro sa) {
            scenarioRepository.findByHubIdAndName(hubId, sa.getName())
                    .ifPresent(existing -> {
                        scenarioRepository.delete(existing);
                        scenarioRepository.flush();
                    });

            Scenario scenario = Scenario.builder()
                    .hubId(hubId)
                    .name(sa.getName())
                    .conditions(new HashSet<>())
                    .actions(new HashSet<>())
                    .build();

            Scenario savedScenario = scenarioRepository.save(scenario);

            Set<String> requiredSensorIds = Stream.concat(
                    sa.getConditions().stream().map(ScenarioConditionAvro::getSensorId),
                    sa.getActions().stream().map(DeviceActionAvro::getSensorId)
            ).collect(Collectors.toSet());

            Map<String, Sensor> sensorMap = sensorRepository.findAllById(requiredSensorIds).stream()
                    .collect(Collectors.toMap(Sensor::getId, Function.identity()));

            List<Sensor> missingSensors = requiredSensorIds.stream()
                    .filter(id -> !sensorMap.containsKey(id))
                    .map(id -> Sensor.builder().id(id).hubId(hubId).build())
                    .toList();

            if (!missingSensors.isEmpty()) {
                sensorRepository.saveAll(missingSensors).forEach(s -> sensorMap.put(s.getId(), s));
            }

            List<Condition> conditionsToSave = sa.getConditions().stream().map(c -> {
                Object val = c.getValue();
                Integer intValue = null;
                if (val instanceof Integer i) {
                    intValue = i;
                } else if (val instanceof Boolean b) {
                    intValue = b ? 1 : 0;
                }

                return Condition.builder()
                        .type(ConditionType.valueOf(c.getType().name()))
                        .operation(ConditionOperation.valueOf(c.getOperation().name()))
                        .value(intValue)
                        .build();
            }).toList();

            List<Condition> savedConditions = conditionRepository.saveAll(conditionsToSave);

            for (int i = 0; i < sa.getConditions().size(); i++) {
                ScenarioConditionAvro avroCond = sa.getConditions().get(i);
                Condition savedCond = savedConditions.get(i);
                Sensor sensor = sensorMap.get(avroCond.getSensorId());

                ScenarioCondition scenarioCondition = ScenarioCondition.builder()
                        .id(new ScenarioConditionId(savedScenario.getId(), sensor.getId(), savedCond.getId()))
                        .scenario(savedScenario)
                        .sensor(sensor)
                        .condition(savedCond)
                        .build();

                savedScenario.getConditions().add(scenarioCondition);
            }

            List<Action> actionsToSave = sa.getActions().stream().map(a ->
                    Action.builder()
                            .type(ActionType.valueOf(a.getType().name()))
                            .value(a.getValue())
                            .build()
            ).toList();

            List<Action> savedActions = actionRepository.saveAll(actionsToSave);

            for (int i = 0; i < sa.getActions().size(); i++) {
                DeviceActionAvro avroAction = sa.getActions().get(i);
                Action savedAction = savedActions.get(i);
                Sensor sensor = sensorMap.get(avroAction.getSensorId());

                ScenarioAction scenarioAction = ScenarioAction.builder()
                        .id(new ScenarioActionId(savedScenario.getId(), sensor.getId(), savedAction.getId()))
                        .scenario(savedScenario)
                        .sensor(sensor)
                        .action(savedAction)
                        .build();

                savedScenario.getActions().add(scenarioAction);
            }

            scenarioRepository.save(savedScenario);
            log.info("Сохранен сценарий: name={}, hubId={}, условий={}, действий={}",
                    sa.getName(), hubId, savedScenario.getConditions().size(), savedScenario.getActions().size());

        } else if (payload instanceof ScenarioRemovedEventAvro sr) {
            scenarioRepository.findByHubIdAndName(hubId, sr.getName())
                    .ifPresent(existing -> {
                        scenarioRepository.delete(existing);
                        scenarioRepository.flush();
                    });
            log.info("Удален сценарий: name={}, hubId={}", sr.getName(), hubId);
        }
    }
}