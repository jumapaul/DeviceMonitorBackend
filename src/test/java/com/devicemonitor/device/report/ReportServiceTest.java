package com.devicemonitor.device.report;

import com.devicemonitor.device.device_manager.DeviceEntity;
import com.devicemonitor.device.device_manager.DeviceManagerMapper;
import com.devicemonitor.device.device_manager.DeviceManagerRepository;
import com.devicemonitor.device.dtos.*;
import com.devicemonitor.device.exception.NotFoundException;
import com.devicemonitor.device.outerbox.ReportOuterBox;
import com.devicemonitor.device.outerbox.ReportOuterBoxMapper;
import com.devicemonitor.device.outerbox.ReportOuterBoxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ReportOuterBoxMapper outerBoxMapper;
    @Mock private DeviceManagerMapper deviceManagerMapper;
    @Mock private ReportRepository repository;
    @Mock private ReportOuterBoxRepository outerBoxRepository;
    @Mock private DeviceManagerRepository deviceRepository;
    @InjectMocks private ReportService service;

    private final UUID deviceId = UUID.randomUUID();
    private DeviceEntity deviceEntity;
    private DeviceInfoResponse deviceInfoResponse;

    @BeforeEach
    void setUp() {
        deviceEntity = DeviceEntity.builder()
                .id(1)
                .deviceId(deviceId)
                .name("Core-Router-01")
                .type("ROUTER")
                .ipAddress("192.168.1.10")
                .location("Nairobi")
                .status(DeviceStatus.ONLINE)
                .isStale(false)
                .createdAt(LocalDateTime.now())
                .lastReport(LocalDateTime.now())
                .build();

        deviceInfoResponse = new DeviceInfoResponse(
                1, deviceId, "Core-Router-01", "ROUTER",
                "192.168.1.10", "Nairobi", DeviceStatus.ONLINE,
                false, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    void shouldSaveReportToOuterBoxAndReturnSuccess() {
        SendReportRequest request = new SendReportRequest(deviceId, DeviceStatus.ONLINE, "All good");
        ReportOuterBox outerBox = new ReportOuterBox();

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(deviceEntity));
        when(outerBoxMapper.toReportOuterBox(any(ReportEvent.class))).thenReturn(outerBox);

        String result = service.sendReport(request);

        assertThat(result).isEqualTo("Report sent");
        verify(outerBoxRepository).save(outerBox);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeviceDoesNotExist() {
        SendReportRequest request = new SendReportRequest(deviceId, DeviceStatus.ONLINE, "All good");

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.sendReport(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(deviceId.toString());

        verify(outerBoxRepository, never()).save(any());
    }

    @Test
    void shouldBuildReportEventWithCorrectDeviceIdAndStatus() {
        SendReportRequest request = new SendReportRequest(deviceId, DeviceStatus.DEGRADED, "High CPU");
        ReportOuterBox outerBox = new ReportOuterBox();

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(deviceEntity));
        when(outerBoxMapper.toReportOuterBox(any(ReportEvent.class))).thenReturn(outerBox);

        service.sendReport(request);

        ArgumentCaptor<ReportEvent> eventCaptor = ArgumentCaptor.forClass(ReportEvent.class);
        verify(outerBoxMapper).toReportOuterBox(eventCaptor.capture());

        ReportEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.deviceId()).isEqualTo(deviceId);
        assertThat(capturedEvent.deviceStatus()).isEqualTo(DeviceStatus.DEGRADED);
        assertThat(capturedEvent.message()).isEqualTo("High CPU");
        assertThat(capturedEvent.timeStamp()).isNotNull();
    }

    @Test
    void shouldReturnDeviceReportWithDeviceInformationAndPaginatedResponse() {
        ReportEntity report1 = ReportEntity.builder()
                .id(1).deviceId(deviceId)
                .deviceStatus(DeviceStatus.ONLINE)
                .createdAt(LocalDateTime.now())
                .build();

        Page<ReportEntity> page = new PageImpl<>(
                List.of(report1), PageRequest.of(0, 20), 1
        );

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(deviceEntity));
        when(repository.findAllByDeviceIdOrderByCreatedAtDesc(eq(deviceId), any(Pageable.class)))
                .thenReturn(page);
        when(deviceManagerMapper.fromEntity(deviceEntity)).thenReturn(deviceInfoResponse);

        DeviceReport result = service.getDeviceReport(deviceId, 0, 20);

        assertThat(result.deviceInfoResponse()).isEqualTo(deviceInfoResponse);
        assertThat(result.reports()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void shouldReturnEmptyReportListWhenNoReportsExists() {
        Page<ReportEntity> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(deviceEntity));
        when(repository.findAllByDeviceIdOrderByCreatedAtDesc(eq(deviceId), any(Pageable.class)))
                .thenReturn(emptyPage);
        when(deviceManagerMapper.fromEntity(deviceEntity)).thenReturn(deviceInfoResponse);

        DeviceReport result = service.getDeviceReport(deviceId, 0, 20);

        assertThat(result.reports()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void shouldCorrectlyReflectHasNextWhenMorePagesExist() {
        List<ReportEntity> reportList = List.of(ReportEntity.builder()
                .id(1).deviceId(deviceId)
                .deviceStatus(DeviceStatus.ONLINE)
                .createdAt(LocalDateTime.now())
                .build());

        Page<ReportEntity> page = new PageImpl<>(reportList, PageRequest.of(0, 20), 25);

        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(deviceEntity));
        when(repository.findAllByDeviceIdOrderByCreatedAtDesc(eq(deviceId), any(Pageable.class)))
                .thenReturn(page);
        when(deviceManagerMapper.fromEntity(deviceEntity)).thenReturn(deviceInfoResponse);

        DeviceReport result = service.getDeviceReport(deviceId, 0, 20);

        assertThat(result.hasNext()).isTrue();
        assertThat(result.totalPages()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(25L);
    }
}