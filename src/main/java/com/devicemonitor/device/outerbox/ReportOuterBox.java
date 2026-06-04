package com.devicemonitor.device.outerbox;

import com.devicemonitor.device.dtos.DeviceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "report_outer_box")
public class ReportOuterBox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private UUID deviceId;
    @Enumerated(EnumType.STRING)
    private DeviceStatus deviceStatus;
    private String message;
    private LocalDateTime createdAt;
}
