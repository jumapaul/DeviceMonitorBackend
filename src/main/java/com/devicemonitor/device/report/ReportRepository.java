package com.devicemonitor.device.report;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportRepository extends JpaRepository<ReportEntity, Integer> {

    Page<ReportEntity> findAllByDeviceIdOrderByCreatedAtDesc(UUID deviceId, Pageable pageable);
}
