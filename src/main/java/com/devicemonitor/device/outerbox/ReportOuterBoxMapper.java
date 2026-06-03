package com.devicemonitor.device.outerbox;

import com.devicemonitor.device.dtos.ReportEvent;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReportOuterBoxMapper {

    public ReportOuterBox toReportOuterBox(ReportEvent event) {
        LocalDateTime createdAt = LocalDateTime.parse(event.timeStamp());
        return ReportOuterBox.builder()
                .deviceId(event.deviceId())
                .deviceStatus(event.deviceStatus())
                .message(event.message())
                .createdAt(createdAt)
                .build();
    }
}
