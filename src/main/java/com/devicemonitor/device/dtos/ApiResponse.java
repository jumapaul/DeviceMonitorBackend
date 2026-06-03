package com.devicemonitor.device.dtos;

public record ApiResponse(
        String message,
        Object data
) {
}
