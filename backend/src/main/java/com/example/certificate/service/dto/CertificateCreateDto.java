package com.example.certificate.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.Future;
import javax.validation.constraints.PastOrPresent;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateCreateDto {
    
    @NotNull(message = "证书名称不能为空")
    @Size(min = 1, max = 100, message = "证书名称长度必须在1-100个字符之间")
    private String name;
    
    @NotNull(message = "域名不能为空")
    @Size(min = 1, max = 255, message = "域名长度必须在1-255个字符之间")
    private String domain;
    
    @NotNull(message = "颁发机构不能为空")
    @Size(min = 1, max = 100, message = "颁发机构长度必须在1-100个字符之间")
    private String issuer;
    
    @NotNull(message = "颁发日期不能为空")
    @PastOrPresent(message = "颁发日期不能是未来日期")
    private Date issueDate;
    
    @NotNull(message = "到期日期不能为空")
    @Future(message = "到期日期必须是未来日期")
    private Date expiryDate;
    
    @NotNull(message = "证书类型不能为空")
    @Size(min = 1, max = 50, message = "证书类型长度必须在1-50个字符之间")
    private String certificateType;
}