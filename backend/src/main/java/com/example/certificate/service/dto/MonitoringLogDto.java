package com.example.certificate.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringLogDto {
    
    private Long id;
    
    @NotNull(message = "证书ID不能为空")
    private Long certificateId;
    
    @NotNull(message = "日志类型不能为空")
    private String logType;
    
    private Date logTime;
    
    @NotNull(message = "日志消息不能为空")
    @Size(min = 1, max = 500, message = "日志消息长度必须在1-500个字符之间")
    private String message;
    
    private Integer daysUntilExpiry;
    
    private Date createdAt;
}