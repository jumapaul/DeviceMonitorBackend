package com.devicemonitor.device.device_manager;

import com.devicemonitor.device.dtos.AddDeviceRequest;
import com.devicemonitor.device.dtos.ApiResponse;
import com.devicemonitor.device.dtos.DeviceInfoResponse;
import com.devicemonitor.device.dtos.UpdateDeviceRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/device")
@RequiredArgsConstructor
public class DeviceManagerController {

    private final DeviceManagerService service;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> addDevice(@RequestBody @Valid AddDeviceRequest request) {
        DeviceInfoResponse response = service.addDevice(request);
        return ResponseEntity.ok(toApiResponse("Device added successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllDevices() {
        List<DeviceInfoResponse> response = service.getAllDevices();

        return ResponseEntity.ok(toApiResponse("Devices retrieved successfully", response));
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateDevice(@RequestBody UpdateDeviceRequest request) {
        DeviceInfoResponse response = service.updateDevice(request);
        return ResponseEntity.ok(toApiResponse("Device updated successfully", response));
    }

    public ApiResponse toApiResponse(String message, Object data) {
        return new ApiResponse(message, data);
    }
}
