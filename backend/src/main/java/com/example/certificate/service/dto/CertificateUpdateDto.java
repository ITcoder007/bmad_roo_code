package com.example.certificate.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateUpdateDto {
    
    @Size(min = 1, max = 100, message = "证书名称长度必须在1-100个字符之间")
    private String name;
    
    @Size(min = 1, max = 255, message = "域名长度必须在1-255个字符之间")
    private String domain;
    
    @Size(min = 1, max = 100, message = "颁发机构长度必须在1-100个字符之间")
    private String issuer;
    
    private Date issueDate;
    
    private Date expiryDate;
    
    @Size(min = 1, max = 50, message = "证书类型长度必须在1-50个字符之间")
    private String certificateType;
    
    private String status;
}