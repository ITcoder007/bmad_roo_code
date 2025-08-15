package com.example.certificate.common.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * API统一响应封装类
 * 用于封装所有API的响应数据，确保响应格式的一致性
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private boolean success;
    private int code;
    private String message;
    private T data;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;
    
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ApiResponse(boolean success, int code, String message) {
        this();
        this.success = success;
        this.code = code;
        this.message = message;
    }
    
    public ApiResponse(boolean success, int code, String message, T data) {
        this(success, code, message);
        this.data = data;
    }
    
    /**
     * 创建成功响应
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, 200, "操作成功");
    }
    
    /**
     * 创建成功响应，包含数据
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, 200, "操作成功", data);
    }
    
    /**
     * 创建成功响应，自定义消息
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, 200, message);
    }
    
    /**
     * 创建成功响应，自定义消息和数据
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, 200, message, data);
    }
    
    /**
     * 创建失败响应
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(false, code, message);
    }
    
    /**
     * 创建失败响应，默认500错误
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, 500, message);
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
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}