package com.example.certificate.domain.model;

import com.baomidou.mybatisplus.annotation.*;
import com.example.certificate.domain.enums.CertificateStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("certificate")
public class Certificate {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String domain;
    
    private String issuer;
    
    private LocalDate issueDate;
    
    private LocalDate expiryDate;
    
    private String certificateType;
    
    private CertificateStatus status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    /**
     * 更新证书状态
     */
    public void updateStatus() {
        if (expiryDate == null) {
            this.status = CertificateStatus.NORMAL;
            return;
        }
        
        LocalDate now = LocalDate.now();
        long daysUntilExpiry = ChronoUnit.DAYS.between(now, expiryDate);
        
        if (daysUntilExpiry < 0) {
            this.status = CertificateStatus.EXPIRED;
        } else if (daysUntilExpiry <= 30) {
            this.status = CertificateStatus.EXPIRING_SOON;
        } else {
            this.status = CertificateStatus.NORMAL;
        }
    }
    
    /**
     * 获取距离过期的天数
     */
    public long getDaysUntilExpiry() {
        if (expiryDate == null) {
            return Long.MAX_VALUE;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }
}