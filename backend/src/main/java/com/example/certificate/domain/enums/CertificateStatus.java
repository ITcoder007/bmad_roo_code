package com.example.certificate.domain.enums;

public enum CertificateStatus {
    NORMAL("正常"),
    EXPIRING_SOON("即将过期"),
    EXPIRED("已过期");
    
    private final String description;
    
    CertificateStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}