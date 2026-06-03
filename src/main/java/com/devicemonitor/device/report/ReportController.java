package com.devicemonitor.device.report;

import com.devicemonitor.device.dtos.ApiResponse;
import com.devicemonitor.device.dtos.DeviceReport;
import com.devicemonitor.device.dtos.SendReportRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse> sendReport(
            @RequestBody SendReportRequest request
    ) {
        String response = reportService.sendReport(request);
        return ResponseEntity.ok(toApiResponse("Success", response));
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<ApiResponse> getReport(
            @PathVariable UUID deviceId,
            @RequestParam(name = "page", defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
        DeviceReport report = reportService.getDeviceReport(deviceId, pageNumber, size);
        return ResponseEntity.ok(toApiResponse("Success", report));
    }

    public ApiResponse toApiResponse(String message, Object data) {
        return new ApiResponse(message, data);
    }
}
