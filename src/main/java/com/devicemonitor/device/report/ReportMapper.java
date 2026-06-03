package com.devicemonitor.device.report;

import com.devicemonitor.device.outerbox.ReportOuterBox;
import com.devicemonitor.device.dtos.ReportEvent;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReportMapper {

    public ReportEntity toReportEntity(ReportEvent event) {
        LocalDateTime createdAt = LocalDateTime.parse(event.timeStamp());
        return ReportEntity.builder()
                .deviceId(event.deviceId())
                .deviceStatus(event.deviceStatus())
                .message(event.message())
                .createdAt(createdAt)
                .build();
    }
}
