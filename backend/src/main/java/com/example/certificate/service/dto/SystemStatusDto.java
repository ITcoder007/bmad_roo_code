package com.example.certificate.service.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class SystemStatusDto {
    private Long totalCertificates;
    private Long normalCertificates;
    private Long expiringSoonCertificates;
    private Long expiredCertificates;
    private String systemVersion;
    private String systemStatus;
}