package com.example.certificate.service.dto;

import com.example.certificate.common.validation.ValidDateRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidDateRange(message = "颁发日期必须早于到期日期")
public class CertificateUpdateDto {
    
    @Size(min = 1, max = 100, message = "证书名称长度必须在1-100个字符之间")
    private String name;
    
    @Size(min = 1, max = 255, message = "域名长度必须在1-255个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?)*$", 
             message = "域名格式无效")
    private String domain;
    
    @Size(min = 1, max = 100, message = "颁发机构长度必须在1-100个字符之间")
    private String issuer;
    
    private Date issueDate;
    
    private Date expiryDate;
    
    @Size(min = 1, max = 50, message = "证书类型长度必须在1-50个字符之间")
    @Pattern(regexp = "^(SSL|TLS|HTTPS|CA|DV|OV|EV)$", 
             message = "证书类型必须是 SSL、TLS、HTTPS、CA、DV、OV 或 EV 之一")
    private String certificateType;
}