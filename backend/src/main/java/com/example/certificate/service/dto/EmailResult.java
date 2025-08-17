package com.example.certificate.service.dto;

import java.util.Date;

/**
 * 邮件发送结果
 * 用于封装邮件发送操作的结果信息
 */
public class EmailResult {
    private boolean success;
    private String message;
    private String recipient;
    private Date sentAt;
    private String errorCode;
    private String errorMessage;

    public EmailResult() {
    }

    public EmailResult(boolean success, String message, String recipient) {
        this.success = success;
        this.message = message;
        this.recipient = recipient;
        this.sentAt = new Date();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * 创建成功结果
     */
    public static EmailResult success(String message, String recipient) {
        return new EmailResult(true, message, recipient);
    }

    /**
     * 创建失败结果
     */
    public static EmailResult failure(String errorMessage, String errorCode, String recipient) {
        EmailResult result = new EmailResult(false, "邮件发送失败", recipient);
        result.setErrorMessage(errorMessage);
        result.setErrorCode(errorCode);
        return result;
    }

    @Override
    public String toString() {
        return "EmailResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", recipient='" + recipient + '\'' +
                ", sentAt=" + sentAt +
                ", errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}