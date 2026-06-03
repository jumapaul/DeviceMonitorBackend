package com.devicemonitor.device.outerbox;

import com.devicemonitor.device.device_manager.DeviceManagerService;
import com.devicemonitor.device.device_manager.KafkaProducer;
import com.devicemonitor.device.dtos.ReportEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportOuterBoxService {
    private final KafkaProducer kafkaProducer;
    private final ReportOuterBoxRepository outerBoxRepository;
    private final DeviceManagerService deviceManagerService;

    @Scheduled(fixedRate = 10000)
    public void pullAndPublishEventToKafka() {
        List<ReportOuterBox> events = outerBoxRepository.findAll();

        events.forEach(event ->
                publishEvent(event).whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Kafka ack received for event {}", event);
                        deviceManagerService.updateLastReportTimeAndStatus(event.getCreatedAt(),
                                event.getDeviceStatus(), event.getDeviceId());
                        outerBoxRepository.delete(event);

                    } else {
                        log.error("Kafka send failed for event: {}", ex.getMessage());
                    }
                }));
    }

    private CompletableFuture<SendResult<String, ReportEvent>> publishEvent(
            ReportOuterBox reportOuterBox
    ) {
        ReportEvent event = new ReportEvent(
                reportOuterBox.getDeviceId(),
                reportOuterBox.getDeviceStatus(),
                reportOuterBox.getMessage(),
                reportOuterBox.getCreatedAt().toString()
        );
        return kafkaProducer.publish(event);
    }
}
