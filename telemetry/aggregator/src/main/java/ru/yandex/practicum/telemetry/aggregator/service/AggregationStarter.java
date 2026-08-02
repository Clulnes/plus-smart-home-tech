package ru.yandex.practicum.telemetry.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.aggregator.config.KafkaConfig;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final KafkaProducer<String, SensorsSnapshotAvro> producer;
    private final SnapshotService snapshotService;
    private final KafkaConfig kafkaConfig;

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(List.of(kafkaConfig.getSensorsTopic()));
            log.info("Aggregator успешно подписался на топик: {}", kafkaConfig.getSensorsTopic());

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    SensorEventAvro event = record.value();
                    if (event == null) continue;

                    Optional<SensorsSnapshotAvro> updatedSnapshot = snapshotService.updateState(event);

                    if (updatedSnapshot.isPresent()) {
                        SensorsSnapshotAvro snapshot = updatedSnapshot.get();
                        ProducerRecord<String, SensorsSnapshotAvro> producerRecord =
                                new ProducerRecord<>(kafkaConfig.getSnapshotsTopic(), snapshot.getHubId(), snapshot);

                        producer.send(producerRecord, (metadata, exception) -> {
                            if (exception != null) {
                                log.error("Ошибка при отправке снапшота в Kafka", exception);
                            } else {
                                log.info("Отправлен снапшот для хаба {} в топик {}: offset={}",
                                        snapshot.getHubId(), metadata.topic(), metadata.offset());
                            }
                        });
                    }
                }

                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {
            log.info("Получен сигнал к остановке Aggregator консьюмера");
        } catch (Exception e) {
            log.error("Ошибка во время агрегации событий датчиков", e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync();
            } catch (Exception e) {
                log.error("Ошибка при завершении работы Kafka клиента", e);
            } finally {
                log.info("Закрываем консьюмер и продюсер Aggregator");
                consumer.close();
                producer.close();
            }
        }
    }
}