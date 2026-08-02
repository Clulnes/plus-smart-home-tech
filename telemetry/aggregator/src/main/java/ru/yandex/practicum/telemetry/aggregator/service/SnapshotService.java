package ru.yandex.practicum.telemetry.aggregator.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SnapshotService {
    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();

        SensorsSnapshotAvro snapshot = snapshots.get(hubId);

        if (snapshot == null) {
            Map<String, SensorStateAvro> sensorsState = new HashMap<>();
            SensorStateAvro newState = SensorStateAvro.newBuilder()
                    .setTimestamp(event.getTimestamp())
                    .setData(event.getPayload())
                    .build();

            sensorsState.put(sensorId, newState);

            snapshot = SensorsSnapshotAvro.newBuilder()
                    .setHubId(hubId)
                    .setTimestamp(event.getTimestamp())
                    .setSensorsState(sensorsState)
                    .build();

            snapshots.put(hubId, snapshot);
            return Optional.of(snapshot);
        }

        SensorStateAvro oldState = snapshot.getSensorsState().get(sensorId);

        if (oldState != null) {
            if (oldState.getTimestamp().isAfter(event.getTimestamp())
                    || oldState.getData().equals(event.getPayload())) {
                return Optional.empty();
            }
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();

        snapshot.getSensorsState().put(sensorId, newState);
        snapshot.setTimestamp(event.getTimestamp());

        return Optional.of(snapshot);
    }
}