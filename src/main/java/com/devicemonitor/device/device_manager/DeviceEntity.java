package com.devicemonitor.device.device_manager;

import com.devicemonitor.device.dtos.DeviceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class DeviceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true)
    private UUID deviceId;
    private String name;
    private String type;
    private String ipAddress;
    private String location;
    @Enumerated(EnumType.STRING)
    private DeviceStatus status;
    private Boolean isStale;
    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime lastReport;
}
