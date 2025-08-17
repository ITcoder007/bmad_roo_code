package com.example.certificate.infrastructure.external.sms;

import com.example.certificate.common.constant.SmsConstants;
import com.example.certificate.domain.model.Certificate;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 短信日志格式化器
 * 专门负责短信相关的日志格式化逻辑
 * 
 * 设计原则：
 * 1. 单一职责 - 只负责格式化，不涉及业务逻辑
 * 2. 无状态 - 线程安全的设计
 * 3. 高性能 - 避免重复的字符串操作
 * 4. 隐私保护 - 自动对手机号进行脱敏处理
 */
@Component
public class SmsLogFormatter {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Pattern PHONE_PATTERN = Pattern.compile(SmsConstants.PHONE_PATTERN);
    
    /**
     * 格式化证书过期预警消息
     */
    public String formatExpiryAlert(Certificate certificate, int daysUntilExpiry, String recipient) {
        String alertType = determineAlertType(daysUntilExpiry);
        String maskedPhone = maskPhoneNumber(recipient);
        return String.format(
            SmsConstants.FORMAT_EXPIRY_ALERT,
            maskedPhone,
            certificate.getName(),
            certificate.getDomain(),
            formatDate(certificate.getExpiryDate()),
            daysUntilExpiry,
            alertType
        );
    }
    
    /**
     * 格式化每日摘要消息
     */
    public String formatDailySummary(List<Certificate> expiringSoon, List<Certificate> expired, String recipient) {
        String maskedPhone = maskPhoneNumber(recipient);
        return String.format(
            SmsConstants.FORMAT_DAILY_SUMMARY,
            maskedPhone,
            expiringSoon.size(),
            expired.size(),
            formatDate(new Date())
        );
    }
    
    /**
     * 格式化证书详细信息为结构化日志
     */
    public String formatCertificateDetails(Certificate certificate, int daysUntilExpiry, String recipient) {
        String maskedPhone = maskPhoneNumber(recipient);
        StringBuilder details = new StringBuilder();
        details.append("证书详情:\n");
        details.append(String.format("    %s 证书名称: %s\n", SmsConstants.LOG_PREFIX_CERTIFICATE, certificate.getName()));
        details.append(String.format("    %s 域名: %s\n", SmsConstants.LOG_PREFIX_DOMAIN, certificate.getDomain()));
        details.append(String.format("    %s 颁发日期: %s\n", SmsConstants.LOG_PREFIX_DATE, formatDate(certificate.getIssueDate())));
        details.append(String.format("    %s 到期日期: %s\n", SmsConstants.LOG_PREFIX_TIME, formatDate(certificate.getExpiryDate())));
        details.append(String.format("    %s 证书状态: %s\n", SmsConstants.LOG_PREFIX_STATUS, certificate.getStatus()));
        details.append(String.format("    %s 剩余天数: %d天\n", SmsConstants.LOG_PREFIX_DAYS, daysUntilExpiry));
        details.append(String.format("    %s 收件人: %s\n", SmsConstants.LOG_PREFIX_RECIPIENT, maskedPhone));
        details.append(String.format("    %s 预警类型: %s", SmsConstants.LOG_PREFIX_ALERT_TYPE, determineAlertType(daysUntilExpiry)));
        return details.toString();
    }
    
    /**
     * 格式化批量统计信息
     */
    public String formatBatchStatistics(int total, int success, int failure) {
        return String.format("批量短信预警完成 - 成功: %d, 失败: %d, 总计: %d", success, failure, total);
    }
    
    /**
     * 格式化短信内容模板（用于实际发送）
     */
    public String formatSmsContent(Certificate certificate, int daysUntilExpiry) {
        if (daysUntilExpiry <= 0) {
            return String.format("【证书管理】证书%s已过期%d天，请立即处理！", 
                certificate.getName(), Math.abs(daysUntilExpiry));
        } else {
            return String.format(SmsConstants.TEMPLATE_EXPIRY_ALERT, 
                certificate.getName(), daysUntilExpiry);
        }
    }
    
    /**
     * 格式化每日摘要短信内容
     */
    public String formatDailySummarySmsContent(int expiringSoonCount, int expiredCount) {
        return String.format(SmsConstants.TEMPLATE_DAILY_SUMMARY, expiringSoonCount, expiredCount);
    }
    
    /**
     * 验证短信内容长度
     */
    public boolean isContentValid(String content) {
        return content != null && content.length() <= SmsConstants.MAX_SMS_LENGTH_LONG;
    }
    
    /**
     * 验证手机号格式
     */
    public boolean isPhoneValid(String phone) {
        return phone != null && Pattern.matches(SmsConstants.PHONE_PATTERN, phone);
    }
    
    /**
     * 根据剩余天数确定预警类型
     */
    private String determineAlertType(int daysUntilExpiry) {
        if (daysUntilExpiry <= 0) {
            return SmsConstants.ALERT_TYPE_EXPIRED;
        } else if (daysUntilExpiry <= SmsConstants.EXPIRY_THRESHOLD_1_DAY) {
            return SmsConstants.ALERT_TYPE_1_DAY;
        } else if (daysUntilExpiry <= SmsConstants.EXPIRY_THRESHOLD_7_DAY) {
            return SmsConstants.ALERT_TYPE_7_DAY;
        } else if (daysUntilExpiry <= SmsConstants.EXPIRY_THRESHOLD_15_DAY) {
            return SmsConstants.ALERT_TYPE_15_DAY;
        } else if (daysUntilExpiry <= SmsConstants.EXPIRY_THRESHOLD_30_DAY) {
            return SmsConstants.ALERT_TYPE_30_DAY;
        } else {
            return SmsConstants.ALERT_TYPE_NORMAL;
        }
    }
    
    /**
     * 统一的日期格式化方法
     */
    private String formatDate(Date date) {
        synchronized (DATE_FORMAT) {
            return DATE_FORMAT.format(date);
        }
    }
    
    /**
     * 手机号脱敏处理
     * 将 13812345678 转换为 138****5678
     */
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}