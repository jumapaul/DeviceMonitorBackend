package com.devicemonitor.device.dtos;

import java.util.UUID;

public record SendReportRequest(
        UUID deviceId,
        DeviceStatus deviceStatus,
        String message
) {
}
