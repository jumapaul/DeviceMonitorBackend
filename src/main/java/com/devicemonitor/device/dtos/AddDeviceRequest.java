package com.devicemonitor.device.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddDeviceRequest(
        @NotBlank(message = "Device name is required")
        String name,
        @NotBlank(message = "Device type is required")
        String type,
        @NotBlank(message = "Device ip address is required")
        String ipAddress,
        @NotBlank(message = "Device location is required")
        String location,
        @NotNull(message = "Device id is required")
        UUID deviceId
) {
}
