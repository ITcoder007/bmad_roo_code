package com.example.certificate.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * API错误响应类
 * 用于封装API错误信息，提供统一的错误响应格式
 */
public class ApiError {
    
    private boolean success;
    private int code;
    private String message;
    private String error;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;
    
    public ApiError() {
        this.success = false;
        this.timestamp = LocalDateTime.now();
    }
    
    public ApiError(int code, String message) {
        this();
        this.code = code;
        this.message = message;
    }
    
    public ApiError(int code, String message, String error) {
        this(code, message);
        this.error = error;
    }
    
    /**
     * 创建错误响应
     */
    public static ApiError of(int code, String message) {
        return new ApiError(code, message);
    }
    
    /**
     * 创建错误响应，包含详细错误信息
     */
    public static ApiError of(int code, String message, String error) {
        return new ApiError(code, message, error);
    }
    
    /**
     * 创建错误响应，包含异常信息
     */
    public static ApiError of(int code, String message, Throwable throwable) {
        return new ApiError(code, message, throwable.getMessage());
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "ApiError{" +
                "success=" + success +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", error='" + error + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}