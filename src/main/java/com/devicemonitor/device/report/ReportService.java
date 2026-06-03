package com.devicemonitor.device.report;

import com.devicemonitor.device.device_manager.DeviceEntity;
import com.devicemonitor.device.device_manager.DeviceManagerMapper;
import com.devicemonitor.device.device_manager.DeviceManagerRepository;
import com.devicemonitor.device.dtos.DeviceReport;
import com.devicemonitor.device.dtos.SendReportRequest;
import com.devicemonitor.device.dtos.ReportEvent;
import com.devicemonitor.device.exception.NotFoundException;
import com.devicemonitor.device.outerbox.ReportOuterBoxRepository;
import com.devicemonitor.device.outerbox.ReportOuterBoxMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportOuterBoxMapper outerBoxMapper;
    private final ReportMapper reportMapper;
    private final DeviceManagerMapper deviceManagerMapper;
    private final ReportRepository repository;
    private final ReportOuterBoxRepository outerBoxRepository;
    private final DeviceManagerRepository deviceRepository;

    public String sendReport(SendReportRequest request) {
        deviceRepository.findByDeviceId(request.deviceId()).orElseThrow(() ->
                new NotFoundException("Device with id " + request.deviceId() + " not found")
        );
        ReportEvent event = new ReportEvent(
                request.deviceId(),
                request.deviceStatus(),
                request.message(),
                LocalDateTime.now().toString()
        );

        outerBoxRepository.save(outerBoxMapper.toReportOuterBox(event));
        return "Report sent";
    }

    public DeviceReport getDeviceReport(UUID deviceId, Integer pageNumber, Integer size) {
        DeviceEntity entity = deviceRepository.findByDeviceId(deviceId).orElseThrow(() ->
                new NotFoundException("Device with id " + deviceId + " not found")
        );

        Pageable pageable = PageRequest.of(pageNumber, size);
        Page<ReportEntity> reports = repository.findAllByDeviceIdOrderByCreatedAtDesc(deviceId, pageable);
        long totalElements = reports.getTotalElements();
        int totalPages = reports.getTotalPages();
        boolean hasNext = reports.hasNext();
        return new DeviceReport(
                deviceManagerMapper.fromEntity(entity),
                reports.getContent(),
                totalElements,
                totalPages,
                hasNext
        );
    }

    public void saveDeviceReport(ReportEvent event) {
        repository.save(reportMapper.toReportEntity(event));
    }
}
