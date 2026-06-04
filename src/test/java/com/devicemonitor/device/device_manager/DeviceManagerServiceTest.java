package com.devicemonitor.device.device_manager;

import com.devicemonitor.device.dtos.AddDeviceRequest;
import com.devicemonitor.device.dtos.DeviceInfoResponse;
import com.devicemonitor.device.dtos.DeviceStatus;
import com.devicemonitor.device.dtos.UpdateDeviceRequest;
import com.devicemonitor.device.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeviceManagerServiceTest {

    @Mock
    private DeviceManagerRepository repository;
    @Mock
    private DeviceManagerMapper mapper;
    @InjectMocks
    private DeviceManagerService service;

    private DeviceEntity deviceEntity;
    private DeviceInfoResponse deviceInfoResponse;
    private final UUID deviceId = UUID.randomUUID();

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
                1,
                deviceId,
                "Core-Router-01",
                "ROUTER",
                "192.168.1.10",
                "Nairobi",
                DeviceStatus.ONLINE,
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }


    @Test
    void shouldSaveDeviceAndReturnMappedResponse() {
        AddDeviceRequest request = new AddDeviceRequest(
                "Core-Router-01", "ROUTER", "192.168.1.10", "Nairobi", deviceId
        );

        when(mapper.toDeviceEntity(request)).thenReturn(deviceEntity);
        when(mapper.fromEntity(deviceEntity)).thenReturn(deviceInfoResponse);

        DeviceInfoResponse result = service.addDevice(request);

        verify(repository).save(deviceEntity);
        assertThat(result).isEqualTo(deviceInfoResponse);
    }

    @Test
    void shouldReturnMappedListOfAllDevices() {
        when(repository.findAll()).thenReturn(List.of(deviceEntity));
        when(mapper.fromEntity(deviceEntity)).thenReturn(deviceInfoResponse);

        List<DeviceInfoResponse> result = service.getAllDevices();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(deviceInfoResponse);
    }

    @Test
    void shouldReturnEmptyListWhenNoDevicesExist() {
        when(repository.findAll()).thenReturn(List.of());

        List<DeviceInfoResponse> result = service.getAllDevices();

        assertThat(result).isEmpty();
        verify(mapper, never()).fromEntity(any());
    }

    @Test
    void shouldUpdateIpAndLocationWhenBothAreProvided() {
        UpdateDeviceRequest request = new UpdateDeviceRequest(1, "10.0.0.1", "Mombasa");

        when(repository.findById(1)).thenReturn(Optional.of(deviceEntity));
        when(mapper.fromEntity(deviceEntity)).thenReturn(deviceInfoResponse);

        service.updateDevice(request);

        assertThat(deviceEntity.getIpAddress()).isEqualTo("10.0.0.1");
        assertThat(deviceEntity.getLocation()).isEqualTo("Mombasa");
        verify(repository).save(deviceEntity);
    }

    @Test
    void shouldUpdateNotUpdateIpWhenNotProvided() {
        UpdateDeviceRequest request = new UpdateDeviceRequest(1, "", "Mombasa");

        when(repository.findById(1)).thenReturn(Optional.of(deviceEntity));
        when(mapper.fromEntity(deviceEntity)).thenReturn(deviceInfoResponse);

        service.updateDevice(request);

        assertThat(deviceEntity.getIpAddress()).isEqualTo("192.168.1.10"); // unchanged
        assertThat(deviceEntity.getLocation()).isEqualTo("Mombasa");
    }

    @Test
    void shouldNotUpdateLocationWhenNotProvided() {
        UpdateDeviceRequest request = new UpdateDeviceRequest(1, "10.0.0.1", "");

        when(repository.findById(1)).thenReturn(Optional.of(deviceEntity));
        when(mapper.fromEntity(deviceEntity)).thenReturn(deviceInfoResponse);

        service.updateDevice(request);

        assertThat(deviceEntity.getIpAddress()).isEqualTo("10.0.0.1");
        assertThat(deviceEntity.getLocation()).isEqualTo("Nairobi"); // unchanged
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeviceNotFound() {
        UpdateDeviceRequest request = new UpdateDeviceRequest(99, "10.0.0.1", "Mombasa");

        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateDevice(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void shouldUpdateLastReportAndStatusWhenChanged() {
        LocalDateTime reportTime = LocalDateTime.now();
        deviceEntity.setStatus(DeviceStatus.OFFLINE);

        when(repository.findByDeviceId(deviceId)).thenReturn(Optional.of(deviceEntity));

        service.updateLastReportTimeAndStatus(reportTime, DeviceStatus.ONLINE, deviceId);

        assertThat(deviceEntity.getLastReport()).isEqualTo(reportTime);
        assertThat(deviceEntity.getStatus()).isEqualTo(DeviceStatus.ONLINE);
        verify(repository).save(deviceEntity);
    }

    @Test
    void shouldNotUpdateStatusWhenNotChanged() {
        LocalDateTime reportTime = LocalDateTime.now();
        deviceEntity.setStatus(DeviceStatus.ONLINE);

        when(repository.findByDeviceId(deviceId)).thenReturn(Optional.of(deviceEntity));

        service.updateLastReportTimeAndStatus(reportTime, DeviceStatus.ONLINE, deviceId);
        assertThat(deviceEntity.getStatus()).isEqualTo(DeviceStatus.ONLINE);
        verify(repository).save(deviceEntity);
    }

    @Test
    void shouldMarkStaleDevicesAsOfflineAndIsStaleToTrue() {
        deviceEntity.setStatus(DeviceStatus.ONLINE);
        deviceEntity.setIsStale(false);

        when(repository.findByStatusAndLastReportBefore(eq(DeviceStatus.ONLINE), any(LocalDateTime.class)))
                .thenReturn(List.of(deviceEntity));

        service.getStaleDevices();

        assertThat(deviceEntity.getIsStale()).isTrue();
        assertThat(deviceEntity.getStatus()).isEqualTo(DeviceStatus.OFFLINE);
        verify(repository).save(deviceEntity);
    }

    @Test
    void shouldSaveEachStaleDevice() {
        DeviceEntity device2 = DeviceEntity.builder()
                .id(2).deviceId(UUID.randomUUID())
                .name("Switch-01").status(DeviceStatus.ONLINE).isStale(false)
                .lastReport(LocalDateTime.now().minusMinutes(20))
                .build();

        when(repository.findByStatusAndLastReportBefore(eq(DeviceStatus.ONLINE), any(LocalDateTime.class)))
                .thenReturn(List.of(deviceEntity, device2));

        service.getStaleDevices();

        verify(repository, times(2)).save(any(DeviceEntity.class));
    }

    @Test
    void shouldUse15MinutesThresholdWhenQuerying() {
        when(repository.findByStatusAndLastReportBefore(eq(DeviceStatus.ONLINE), any(LocalDateTime.class)))
                .thenReturn(List.of());

        service.getStaleDevices();

        ArgumentCaptor<LocalDateTime> thresholdCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repository).findByStatusAndLastReportBefore(eq(DeviceStatus.ONLINE), thresholdCaptor.capture());

        LocalDateTime captured = thresholdCaptor.getValue();
        LocalDateTime expected = LocalDateTime.now().minusMinutes(15);

        // allow 5 second tolerance for test execution time
        assertThat(captured).isBetween(expected.minusSeconds(5), expected.plusSeconds(5));
    }
}