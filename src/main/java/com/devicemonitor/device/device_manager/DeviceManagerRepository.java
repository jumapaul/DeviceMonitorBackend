package com.devicemonitor.device.device_manager;

import com.devicemonitor.device.dtos.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceManagerRepository extends JpaRepository<DeviceEntity, Integer> {
    Optional<DeviceEntity> findByDeviceId(UUID deviceId);

    List<DeviceEntity> findByStatusAndLastReportBefore(DeviceStatus status, LocalDateTime threshold);
}
