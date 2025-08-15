package com.example.certificate.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {
    private Long id;
    private String name;
    private String domain;
    private String issuer;
    private Date issueDate;
    private Date expiryDate;
    private String certificateType;
    private CertificateStatus status;
    private Date createdAt;
    private Date updatedAt;
}