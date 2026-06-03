package com.devicemonitor.device.report;

import com.devicemonitor.device.dtos.ReportEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private static final String TOPIC = "REPORT";
    private final ReportService reportService;

    @KafkaListener(topics = TOPIC, groupId = "reports")
    public void listen(ReportEvent event) {
        reportService.saveDeviceReport(event);
    }
}
