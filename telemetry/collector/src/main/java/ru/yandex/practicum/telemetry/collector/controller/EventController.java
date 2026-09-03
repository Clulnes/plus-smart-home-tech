package ru.yandex.practicum.telemetry.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.service.HubEventMapper;
import ru.yandex.practicum.telemetry.collector.service.SensorEventMapper;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {
    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;
    private final SensorEventMapper sensorEventMapper;
    private final HubEventMapper hubEventMapper;

    private static final String SENSORS_TOPIC = "telemetry.sensors.v1";
    private static final String HUBS_TOPIC = "telemetry.hubs.v1";

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            var avroEvent = sensorEventMapper.toAvro(request);

            kafkaTemplate.send(SENSORS_TOPIC, avroEvent);
            log.info("Отправили сенсор в Kafka по gRPC: {}", avroEvent);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Ошибка при обработке gRPC события сенсора", e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL.withDescription(e.getLocalizedMessage()).withCause(e)
            ));
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            var avroEvent = hubEventMapper.toAvro(request);

            kafkaTemplate.send(HUBS_TOPIC, avroEvent);
            log.info("Отправили хаб в Kafka по gRPC: {}", avroEvent);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Ошибка при обработке gRPC события хаба", e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL.withDescription(e.getLocalizedMessage()).withCause(e)
            ));
        }
    }
}