package com.example.certificate.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import com.example.certificate.domain.enums.LogType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("monitoring_log")
public class MonitoringLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long certificateId;
    
    private LogType logType;
    
    private LocalDateTime logTime;
    
    private String message;
    
    private Integer daysUntilExpiry;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 创建监控日志
     */
    public static MonitoringLog createLog(Long certificateId, LogType logType, String message, Integer daysUntilExpiry) {
        MonitoringLog log = new MonitoringLog();
        log.setCertificateId(certificateId);
        log.setLogType(logType);
        log.setMessage(message);
        log.setDaysUntilExpiry(daysUntilExpiry);
        log.setLogTime(LocalDateTime.now());
        return log;
    }
}