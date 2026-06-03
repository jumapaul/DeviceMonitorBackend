package com.devicemonitor.device.report;

import com.devicemonitor.device.dtos.DeviceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private UUID deviceId;
    @Enumerated(EnumType.STRING)
    private DeviceStatus deviceStatus;
    private String message;
    private LocalDateTime createdAt;
}
