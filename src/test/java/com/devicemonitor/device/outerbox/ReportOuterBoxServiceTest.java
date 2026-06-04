package com.devicemonitor.device.outerbox;

import com.devicemonitor.device.device_manager.DeviceManagerService;
import com.devicemonitor.device.device_manager.KafkaProducer;
import com.devicemonitor.device.dtos.DeviceStatus;
import com.devicemonitor.device.dtos.ReportEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.SendResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportOuterBoxServiceTest {

    @Mock private KafkaProducer kafkaProducer;
    @Mock private ReportOuterBoxRepository outerBoxRepository;
    @Mock private DeviceManagerService deviceManagerService;
    @InjectMocks private ReportOuterBoxService service;

    private final UUID deviceId = UUID.randomUUID();
    private ReportOuterBox reportOuterBox;
    private final LocalDateTime createdAt = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        reportOuterBox = ReportOuterBox.builder()
                .id(1)
                .deviceId(deviceId)
                .deviceStatus(DeviceStatus.ONLINE)
                .message("All good")
                .createdAt(createdAt)
                .build();
    }

    @Test
    void shouldPublishEventToKafka() {
        when(outerBoxRepository.findAll()).thenReturn(List.of(reportOuterBox));
        when(kafkaProducer.publish(any(ReportEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        service.pullAndPublishEventToKafka();

        verify(kafkaProducer).publish(any(ReportEvent.class));
    }

    @Test
    void shouldUpdateDeviceStatusAndDeleteEventOnKafkaAckSuccess() throws Exception {
        when(outerBoxRepository.findAll()).thenReturn(List.of(reportOuterBox));
        when(kafkaProducer.publish(any(ReportEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        service.pullAndPublishEventToKafka();

        Thread.sleep(100);

        verify(deviceManagerService).updateLastReportTimeAndStatus(
                createdAt, DeviceStatus.ONLINE, deviceId
        );
        verify(outerBoxRepository).delete(reportOuterBox);
    }

    @Test
    void shouldNotUpdateOrDeleteEventWhenKafkaPublishFails() throws Exception {
        CompletableFuture<SendResult<String, ReportEvent>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka broker unavailable"));

        when(outerBoxRepository.findAll()).thenReturn(List.of(reportOuterBox));
        when(kafkaProducer.publish(any(ReportEvent.class))).thenReturn(failedFuture);

        service.pullAndPublishEventToKafka();

        Thread.sleep(100);

        verify(deviceManagerService, never()).updateLastReportTimeAndStatus(any(), any(), any());
        verify(outerBoxRepository, never()).delete(any());
    }

    @Test
    void shouldPublishMultipleEventsWhenMultipleExistInOuterBox() throws Exception {
        ReportOuterBox event2 = ReportOuterBox.builder()
                .id(2)
                .deviceId(UUID.randomUUID())
                .deviceStatus(DeviceStatus.DEGRADED)
                .message("High CPU")
                .createdAt(LocalDateTime.now())
                .build();

        when(outerBoxRepository.findAll()).thenReturn(List.of(reportOuterBox, event2));
        when(kafkaProducer.publish(any(ReportEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        service.pullAndPublishEventToKafka();

        Thread.sleep(100);

        verify(kafkaProducer, times(2)).publish(any(ReportEvent.class));
        verify(outerBoxRepository, times(2)).delete(any(ReportOuterBox.class));
        verify(deviceManagerService, times(2))
                .updateLastReportTimeAndStatus(any(), any(), any());
    }
}