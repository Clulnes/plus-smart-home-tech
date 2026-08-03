package ru.yandex.practicum.telemetry.analyzer.processor;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequestProto;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.model.ConditionOperation;
import ru.yandex.practicum.telemetry.analyzer.model.ConditionType;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioAction;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioCondition;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class SnapshotProcessor {
    private final KafkaConsumer<String, SensorsSnapshotAvro> snapshotConsumer;
    private final ScenarioRepository scenarioRepository;

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(snapshotConsumer::wakeup));

        try {
            snapshotConsumer.subscribe(List.of("telemetry.snapshots.v1"));
            log.info("SnapshotProcessor подписался на топик telemetry.snapshots.v1");

            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = snapshotConsumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    SensorsSnapshotAvro snapshot = record.value();
                    if (snapshot != null) {
                        evaluateSnapshot(snapshot);
                    }
                }
                snapshotConsumer.commitAsync();
            }
        } catch (WakeupException ignored) {
            log.info("Получен сигнал к остановке SnapshotProcessor");
        } catch (Exception e) {
            log.error("Ошибка при обработке снапшотов", e);
        } finally {
            try {
                snapshotConsumer.commitSync();
            } finally {
                snapshotConsumer.close();
            }
        }
    }

    private void evaluateSnapshot(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        Map<String, SensorStateAvro> sensorStates = snapshot.getSensorsState();

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        log.info("Проверка снапшота хаба {}. Найдено сценариев в БД: {}", hubId, scenarios.size());

        for (Scenario scenario : scenarios) {
            boolean allConditionsMet = true;

            for (ScenarioCondition sc : scenario.getConditions()) {
                String sensorId = sc.getSensor().getId();
                SensorStateAvro state = sensorStates.get(sensorId);

                if (state == null || !checkCondition(sc, state.getData())) {
                    allConditionsMet = false;
                    break;
                }
            }

            if (allConditionsMet) {
                log.info("Сценарий [{}] выполнен для хаба [{}]! Выполняем действия...", scenario.getName(), hubId);
                executeActions(scenario, snapshot.getTimestamp());
            }
        }
    }

    private boolean checkCondition(ScenarioCondition sc, Object sensorData) {
        Integer sensorValue = extractValue(sc.getCondition().getType(), sensorData);
        if (sensorValue == null) return false;

        ConditionOperation op = sc.getCondition().getOperation();
        Integer targetValue = sc.getCondition().getValue();
        if (targetValue == null) return false;

        return switch (op) {
            case EQUALS -> sensorValue.equals(targetValue);
            case GREATER_THAN -> sensorValue > targetValue;
            case LOWER_THAN -> sensorValue < targetValue;
        };
    }

    private Integer extractValue(ConditionType conditionType, Object sensorData) {
        if (sensorData instanceof ClimateSensorAvro c) {
            return switch (conditionType) {
                case TEMPERATURE -> c.getTemperatureC();
                case HUMIDITY -> c.getHumidity();
                case CO2LEVEL -> c.getCo2Level();
                default -> null;
            };
        } else if (sensorData instanceof LightSensorAvro l) {
            return conditionType == ConditionType.LUMINOSITY ? l.getLuminosity() : null;
        } else if (sensorData instanceof MotionSensorAvro m) {
            return conditionType == ConditionType.MOTION ? (m.getMotion() ? 1 : 0) : null;
        } else if (sensorData instanceof SwitchSensorAvro s) {
            return conditionType == ConditionType.SWITCH ? (s.getState() ? 1 : 0) : null;
        } else if (sensorData instanceof TemperatureSensorAvro t) {
            return conditionType == ConditionType.TEMPERATURE ? t.getTemperatureC() : null;
        }
        return null;
    }

    private void executeActions(Scenario scenario, Instant timestamp) {
        for (ScenarioAction sa : scenario.getActions()) {
            try {
                DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                        .setSensorId(sa.getSensor().getId())
                        .setType(ActionTypeProto.valueOf(sa.getAction().getType().name()));

                if (sa.getAction().getValue() != null) {
                    actionBuilder.setValue(sa.getAction().getValue());
                }

                DeviceActionProto actionProto = actionBuilder.build();

                Timestamp tsProto = Timestamp.newBuilder()
                        .setSeconds(timestamp.getEpochSecond())
                        .setNanos(timestamp.getNano())
                        .build();

                DeviceActionRequestProto request = DeviceActionRequestProto.newBuilder()
                        .setHubId(scenario.getHubId())
                        .setScenarioName(scenario.getName())
                        .setAction(actionProto)
                        .setTimestamp(tsProto)
                        .build();

                hubRouterClient.handleDeviceAction(request);
                log.info("Отправлено gRPC действие: scenario={}, sensorId={}, type={}, value={}",
                        scenario.getName(), sa.getSensor().getId(), sa.getAction().getType(), sa.getAction().getValue());
            } catch (Exception e) {
                log.error("Ошибка при отправке gRPC действия для сценария {}", scenario.getName(), e);
            }
        }
    }
}