package com.example.certificate.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("monitoring_log")
public class MonitoringLogEntity {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("certificate_id")
    private Long certificateId;
    
    @TableField("log_type")
    private String logType;
    
    @TableField("log_time")
    private Date logTime;
    
    @TableField("message")
    private String message;
    
    @TableField("days_until_expiry")
    private Integer daysUntilExpiry;
    
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Date createdAt;
}