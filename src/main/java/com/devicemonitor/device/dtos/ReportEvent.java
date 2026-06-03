package com.devicemonitor.device.dtos;

import java.util.UUID;

public record ReportEvent(
        UUID deviceId,
        DeviceStatus deviceStatus,
        String message,
        String timeStamp
) {
}