package ru.yandex.practicum.telemetry.collector.service.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.MotionSensorProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;

@Component
public class MotionSensorEventHandler implements SensorEventHandler {

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR;
    }

    @Override
    public Object handlePayload(SensorEventProto event) {
        MotionSensorProto motion = event.getMotionSensor();

        return MotionSensorAvro.newBuilder()
                .setLinkQuality(motion.getLinkQuality())
                .setMotion(motion.getMotion())
                .setVoltage(motion.getVoltage())
                .build();
    }
}