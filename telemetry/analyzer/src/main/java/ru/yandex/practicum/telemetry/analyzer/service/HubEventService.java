package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.telemetry.analyzer.model.Action;
import ru.yandex.practicum.telemetry.analyzer.model.Condition;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioAction;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioCondition;
import ru.yandex.practicum.telemetry.analyzer.model.Sensor;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HubEventService {
    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;

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
                    .ifPresent(scenarioRepository::delete);

            Scenario scenario = Scenario.builder()
                    .hubId(hubId)
                    .name(sa.getName())
                    .build();

            List<ScenarioCondition> conditions = sa.getConditions().stream().map(c -> {
                Sensor sensor = sensorRepository.findById(c.getSensorId())
                        .orElseGet(() -> sensorRepository.save(Sensor.builder().id(c.getSensorId()).hubId(hubId)
                                .build()));

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

                return ScenarioCondition.builder()
                        .scenario(scenario)
                        .sensor(sensor)
                        .condition(condition)
                        .build();
            }).toList();

            List<ScenarioAction> actions = sa.getActions().stream().map(a -> {
                Sensor sensor = sensorRepository.findById(a.getSensorId())
                        .orElseGet(() -> sensorRepository.save(Sensor.builder().id(a.getSensorId()).hubId(hubId).build()));

                Action action = Action.builder()
                        .type(a.getType().name())
                        .value(a.getValue())
                        .build();

                return ScenarioAction.builder()
                        .scenario(scenario)
                        .sensor(sensor)
                        .action(action)
                        .build();
            }).toList();

            scenario.setConditions(conditions);
            scenario.setActions(actions);
            scenarioRepository.save(scenario);
            log.info("Сохранен сценарий: name={}, hubId={}", sa.getName(), hubId);

        } else if (payload instanceof ScenarioRemovedEventAvro sr) {
            scenarioRepository.findByHubIdAndName(hubId, sr.getName())
                    .ifPresent(scenarioRepository::delete);
            log.info("Удален сценарий: name={}, hubId={}", sr.getName(), hubId);
        }
    }
}