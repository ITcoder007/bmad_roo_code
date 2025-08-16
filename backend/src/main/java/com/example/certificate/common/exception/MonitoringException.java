package com.example.certificate.common.exception;

/**
 * 监控异常类
 * 用于处理证书监控过程中的各种异常情况
 */
public class MonitoringException extends RuntimeException {
    
    private final String errorCode;
    
    public MonitoringException(String message) {
        super(message);
        this.errorCode = "MONITORING_ERROR";
    }
    
    public MonitoringException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "MONITORING_ERROR";
    }
    
    public MonitoringException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public MonitoringException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}