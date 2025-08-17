package com.example.certificate.service.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 短信发送结果（不可变对象）
 * 用于封装短信发送操作的结果信息
 * 
 * 设计原则：
 * 1. 不可变性 - 一旦创建不可修改，线程安全
 * 2. 值对象语义 - 基于内容的相等性比较
 * 3. 防御性复制 - 避免外部修改内部状态
 */
public final class SmsResult {
    private final boolean success;
    private final String message;
    private final String recipient;
    private final LocalDateTime sentAt;
    private final String errorCode;
    private final String errorMessage;

    private SmsResult(boolean success, String message, String recipient, 
                     LocalDateTime sentAt, String errorCode, String errorMessage) {
        this.success = success;
        this.message = message;
        this.recipient = recipient;
        this.sentAt = sentAt != null ? sentAt : LocalDateTime.now();
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getRecipient() {
        return recipient;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    
    public boolean isFailure() {
        return !success;
    }
    
    public boolean hasError() {
        return errorCode != null || errorMessage != null;
    }

    /**
     * 创建成功结果
     */
    public static SmsResult success(String message, String recipient) {
        return new SmsResult(true, message, recipient, LocalDateTime.now(), null, null);
    }
    
    /**
     * 创建成功结果（指定时间）
     */
    public static SmsResult success(String message, String recipient, LocalDateTime sentAt) {
        return new SmsResult(true, message, recipient, sentAt, null, null);
    }

    /**
     * 创建失败结果
     */
    public static SmsResult failure(String errorMessage, String errorCode, String recipient) {
        return new SmsResult(false, "短信发送失败", recipient, LocalDateTime.now(), errorCode, errorMessage);
    }
    
    /**
     * 创建失败结果（指定时间）
     */
    public static SmsResult failure(String errorMessage, String errorCode, String recipient, LocalDateTime sentAt) {
        return new SmsResult(false, "短信发送失败", recipient, sentAt, errorCode, errorMessage);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SmsResult smsResult = (SmsResult) o;
        return success == smsResult.success &&
                Objects.equals(message, smsResult.message) &&
                Objects.equals(recipient, smsResult.recipient) &&
                Objects.equals(sentAt, smsResult.sentAt) &&
                Objects.equals(errorCode, smsResult.errorCode) &&
                Objects.equals(errorMessage, smsResult.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, message, recipient, sentAt, errorCode, errorMessage);
    }

    @Override
    public String toString() {
        return "SmsResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", recipient='" + recipient + '\'' +
                ", sentAt=" + sentAt +
                ", errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}