package com.example.certificate.service.dto;

import com.example.certificate.domain.enums.LogType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MonitoringLogDto {
    private Long id;
    private Long certificateId;
    private String certificateName;
    private String domain;
    private LogType logType;
    private LocalDateTime logTime;
    private String message;
    private Integer daysUntilExpiry;
    private LocalDateTime createdAt;
}