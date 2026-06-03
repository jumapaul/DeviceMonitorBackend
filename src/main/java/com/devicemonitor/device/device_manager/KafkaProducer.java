package com.devicemonitor.device.device_manager;

import com.devicemonitor.device.dtos.ReportEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class KafkaProducer {
    private static final String TOPIC = "REPORT";
    private final KafkaTemplate<String, ReportEvent> kafkaTemplate;

    public CompletableFuture<SendResult<String, ReportEvent>> publish(ReportEvent event) {
        String key = event.deviceId().toString();
        return kafkaTemplate.send(TOPIC, key, event);
    }
}
