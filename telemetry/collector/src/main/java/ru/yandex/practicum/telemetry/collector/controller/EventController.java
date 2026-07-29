package ru.yandex.practicum.telemetry.collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.telemetry.collector.dto.hub.HubEvent;
import ru.yandex.practicum.telemetry.collector.dto.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.collector.service.HubEventMapper;
import ru.yandex.practicum.telemetry.collector.service.SensorEventMapper;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {
    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;
    private final SensorEventMapper sensorEventMapper;
    private final HubEventMapper hubEventMapper;

    private static final String SENSORS_TOPIC = "telemetry.sensors.v1";
    private static final String HUBS_TOPIC = "telemetry.hubs.v1";

    @PostMapping("/sensors")
    public void collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        var avroEvent = sensorEventMapper.toAvro(event);

        kafkaTemplate.send(SENSORS_TOPIC, avroEvent);

        log.info("Отправили сенсор в Kafka: {}", avroEvent);
    }

    @PostMapping("/hubs")
    public void collectHubEvent(@Valid @RequestBody HubEvent event) {
        var avroEvent = hubEventMapper.toAvro(event);

        kafkaTemplate.send(HUBS_TOPIC, avroEvent);

        log.info("Отправили хаб в Kafka: {}", avroEvent);
    }
}