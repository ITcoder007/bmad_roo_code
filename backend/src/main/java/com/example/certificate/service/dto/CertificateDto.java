package com.example.certificate.service.dto;

import com.example.certificate.domain.enums.CertificateStatus;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CertificateDto {
    private Long id;
    
    @NotBlank(message = "证书名称不能为空")
    private String name;
    
    @NotBlank(message = "域名不能为空")
    private String domain;
    
    private String issuer;
    
    private LocalDate issueDate;
    
    @NotNull(message = "到期日期不能为空")
    private LocalDate expiryDate;
    
    private String certificateType;
    
    private CertificateStatus status;
    
    private Long daysUntilExpiry;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}