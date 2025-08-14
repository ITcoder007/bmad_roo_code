package com.example.certificate.domain.enums;

public enum LogType {
    MONITORING("监控检查"),
    ALERT_EMAIL("邮件预警"),
    ALERT_SMS("短信预警");
    
    private final String description;
    
    LogType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}