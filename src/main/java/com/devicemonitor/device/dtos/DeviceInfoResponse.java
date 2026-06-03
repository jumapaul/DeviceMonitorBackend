package com.devicemonitor.device.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeviceInfoResponse(
        Integer id,
        UUID deviceId,
        String name,
        String type,
        String ipAddress,
        String location,
        DeviceStatus deviceStatus,
        Boolean isStale,
        LocalDateTime createdAt,
        LocalDateTime lastReport
) {
}
