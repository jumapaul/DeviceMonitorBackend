package com.devicemonitor.device.dtos;

import com.devicemonitor.device.report.ReportEntity;

import java.util.List;

public record DeviceReport(
        DeviceInfoResponse deviceInfoResponse,
        List<ReportEntity> reports,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
}
