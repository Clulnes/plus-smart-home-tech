package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.model.*;
import ru.yandex.practicum.telemetry.analyzer.repository.*;

import java.util.HashSet;

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
            // Удаляем старый сценарий с таким же именем и ПРИНУДИТЕЛЬНО очищаем контекст (flush)
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

            for (ScenarioConditionAvro c : sa.getConditions()) {
                Sensor sensor = sensorRepository.findById(c.getSensorId())
                        .orElseGet(() -> sensorRepository.save(Sensor.builder().id(c.getSensorId()).hubId(hubId).build()));

                Object val = c.getValue();
                Integer intValue = null;
                if (val instanceof Integer i) {
                    intValue = i;
                } else if (val instanceof Boolean b) {
                    intValue = b ? 1 : 0;
                }

                Condition condition = Condition.builder()
                        .type(c.getType().name())
                        .operation(c.getOperation().name())
                        .value(intValue)
                        .build();

                Condition savedCondition = conditionRepository.save(condition);

                ScenarioCondition scenarioCondition = ScenarioCondition.builder()
                        .id(new ScenarioConditionId(savedScenario.getId(), sensor.getId(), savedCondition.getId()))
                        .scenario(savedScenario)
                        .sensor(sensor)
                        .condition(savedCondition)
                        .build();

                savedScenario.getConditions().add(scenarioCondition);
            }

            for (DeviceActionAvro a : sa.getActions()) {
                Sensor sensor = sensorRepository.findById(a.getSensorId())
                        .orElseGet(() -> sensorRepository.save(Sensor.builder().id(a.getSensorId()).hubId(hubId).build()));

                Action action = Action.builder()
                        .type(a.getType().name())
                        .value(a.getValue())
                        .build();

                Action savedAction = actionRepository.save(action);

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