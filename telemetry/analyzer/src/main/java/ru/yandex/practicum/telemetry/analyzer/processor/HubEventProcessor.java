package ru.yandex.practicum.telemetry.analyzer.processor;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.analyzer.service.HubEventService;

import java.time.Duration;
import java.util.List;

@Component
public class HubEventProcessor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(HubEventProcessor.class);

    private final KafkaConsumer<String, HubEventAvro> hubConsumer;
    private final HubEventService hubEventService;

    public HubEventProcessor(KafkaConsumer<String, HubEventAvro> hubConsumer,
                             HubEventService hubEventService) {
        this.hubConsumer = hubConsumer;
        this.hubEventService = hubEventService;
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(hubConsumer::wakeup));

        try {
            hubConsumer.subscribe(List.of("telemetry.hubs.v1"));
            log.info("HubEventProcessor подписался на топик telemetry.hubs.v1");

            while (true) {
                ConsumerRecords<String, HubEventAvro> records = hubConsumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    HubEventAvro event = record.value();
                    if (event != null) {
                        hubEventService.processHubEvent(event);
                    }
                }
                hubConsumer.commitAsync();
            }
        } catch (WakeupException ignored) {
            log.info("Получен сигнал к остановке HubEventProcessor");
        } catch (Exception e) {
            log.error("Ошибка при обработке событий хаба", e);
        } finally {
            try {
                hubConsumer.commitSync();
            } finally {
                hubConsumer.close();
            }
        }
    }
}