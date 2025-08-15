package com.example.certificate.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDto {
    private Long id;
    private String name;
    private String domain;
    private String issuer;
    private Date issueDate;
    private Date expiryDate;
    private String certificateType;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}