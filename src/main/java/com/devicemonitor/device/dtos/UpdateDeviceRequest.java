package com.devicemonitor.device.dtos;

public record UpdateDeviceRequest(
        Integer id,
        String ipAddress,
        String location
) {
}
