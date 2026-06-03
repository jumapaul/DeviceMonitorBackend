package com.devicemonitor.device.device_manager;

import com.devicemonitor.device.dtos.AddDeviceRequest;
import com.devicemonitor.device.dtos.DeviceInfoResponse;
import com.devicemonitor.device.dtos.DeviceStatus;
import com.devicemonitor.device.dtos.UpdateDeviceRequest;
import com.devicemonitor.device.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceManagerService {

    private final DeviceManagerRepository repository;
    private final DeviceManagerMapper mapper;

    public DeviceInfoResponse addDevice(AddDeviceRequest request) {
        DeviceEntity deviceEntity = mapper.toDeviceEntity(request);

        repository.save(deviceEntity);
        return mapper.fromEntity(deviceEntity);
    }

    public List<DeviceInfoResponse> getAllDevices() {
        return repository.findAll().stream().map(mapper::fromEntity).toList();
    }

    public DeviceInfoResponse updateDevice(UpdateDeviceRequest request) {
        DeviceEntity deviceEntity = repository.findById(request.id()).orElseThrow(() ->
                new NotFoundException("Device with id " + request.id() + " not found")
        );

        if (!request.ipAddress().isBlank()) {
            deviceEntity.setIpAddress(request.ipAddress());
        }
        if (!request.location().isBlank()) {
            deviceEntity.setLocation(request.location());
        }
        repository.save(deviceEntity);

        return mapper.fromEntity(deviceEntity);
    }

    public void updateLastReportTimeAndStatus(LocalDateTime lastReport, DeviceStatus status, UUID deviceId) {
        DeviceEntity entity = repository.findByDeviceId(deviceId).orElseThrow(() ->
                new NotFoundException("Device with id " + deviceId + " not found")
        );

        entity.setLastReport(lastReport);
        if (entity.getStatus() != status) {
            entity.setStatus(status);
        }
        repository.save(entity);
    }

    @Scheduled(fixedRate = 60000)
    public void getStaleDevices() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(15);
        List<DeviceEntity> devices = repository.findByStatusAndLastReportBefore(DeviceStatus.ONLINE, threshold);

        devices.forEach(device -> {
            device.setIsStale(true);
            device.setStatus(DeviceStatus.OFFLINE);
            repository.save(device);
            //Send notification
            log.warn("The device with the name {} and location {} is stale.", device.getName(), device.getLocation());
        });
    }
}
