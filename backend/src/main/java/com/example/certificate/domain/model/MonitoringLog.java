package com.example.certificate.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringLog {
    private Long id;
    private Long certificateId;
    private LogType logType;
    private Date logTime;
    private String message;
    private Integer daysUntilExpiry;
    private Date createdAt;
}