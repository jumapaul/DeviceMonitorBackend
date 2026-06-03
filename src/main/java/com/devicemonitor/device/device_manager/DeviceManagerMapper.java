package com.devicemonitor.device.device_manager;

import com.devicemonitor.device.dtos.AddDeviceRequest;
import com.devicemonitor.device.dtos.DeviceInfoResponse;
import com.devicemonitor.device.dtos.DeviceStatus;
import org.springframework.stereotype.Service;

@Service
public class DeviceManagerMapper {

    public DeviceEntity toDeviceEntity(AddDeviceRequest request) {
        return DeviceEntity.builder()
                .deviceId(request.deviceId())
                .name(request.name())
                .type(request.type())
                .ipAddress(request.ipAddress())
                .location(request.location())
                .status(DeviceStatus.ONLINE)
                .isStale(false)
                .build();
    }

    public DeviceInfoResponse fromEntity(DeviceEntity deviceEntity) {
        return new DeviceInfoResponse(
                deviceEntity.getId(),
                deviceEntity.getDeviceId(),
                deviceEntity.getName(),
                deviceEntity.getType(),
                deviceEntity.getIpAddress(),
                deviceEntity.getLocation(),
                deviceEntity.getStatus(),
                deviceEntity.getIsStale(),
                deviceEntity.getCreatedAt(),
                deviceEntity.getLastReport()
        );
    }
}
