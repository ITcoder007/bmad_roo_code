package com.example.certificate.infrastructure.external.email;

import com.example.certificate.common.constant.EmailConstants;
import com.example.certificate.domain.model.Certificate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 邮件日志格式化器
 * 专门负责邮件相关的日志格式化逻辑
 * 
 * 设计原则：
 * 1. 单一职责 - 只负责格式化，不涉及业务逻辑
 * 2. 无状态 - 线程安全的设计
 * 3. 高性能 - 避免重复的字符串操作
 */
public class EmailLogFormatter {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 格式化证书过期预警消息
     */
    public String formatExpiryAlert(Certificate certificate, int daysUntilExpiry, String recipient) {
        String alertType = determineAlertType(daysUntilExpiry);
        return String.format(
            EmailConstants.FORMAT_EXPIRY_ALERT,
            recipient,
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
        return String.format(
            EmailConstants.FORMAT_DAILY_SUMMARY,
            recipient,
            expiringSoon.size(),
            expired.size(),
            formatDate(new Date())
        );
    }
    
    /**
     * 格式化证书详细信息为结构化日志
     */
    public String formatCertificateDetails(Certificate certificate, int daysUntilExpiry, String recipient) {
        StringBuilder details = new StringBuilder();
        details.append("证书详情:\n");
        details.append(String.format("    %s 证书名称: %s\n", EmailConstants.LOG_PREFIX_CERTIFICATE, certificate.getName()));
        details.append(String.format("    %s 域名: %s\n", EmailConstants.LOG_PREFIX_DOMAIN, certificate.getDomain()));
        details.append(String.format("    %s 颁发日期: %s\n", EmailConstants.LOG_PREFIX_DATE, formatDate(certificate.getIssueDate())));
        details.append(String.format("    %s 到期日期: %s\n", EmailConstants.LOG_PREFIX_TIME, formatDate(certificate.getExpiryDate())));
        details.append(String.format("    %s 证书状态: %s\n", EmailConstants.LOG_PREFIX_STATUS, certificate.getStatus()));
        details.append(String.format("    %s 剩余天数: %d天\n", EmailConstants.LOG_PREFIX_DAYS, daysUntilExpiry));
        details.append(String.format("    %s 收件人: %s\n", EmailConstants.LOG_PREFIX_RECIPIENT, recipient));
        details.append(String.format("    %s 预警类型: %s", EmailConstants.LOG_PREFIX_ALERT_TYPE, determineAlertType(daysUntilExpiry)));
        return details.toString();
    }
    
    /**
     * 格式化批量统计信息
     */
    public String formatBatchStatistics(int total, int success, int failure) {
        return String.format("批量邮件预警完成 - 成功: %d, 失败: %d, 总计: %d", success, failure, total);
    }
    
    /**
     * 根据剩余天数确定预警类型
     */
    private String determineAlertType(int daysUntilExpiry) {
        if (daysUntilExpiry <= 0) {
            return EmailConstants.ALERT_TYPE_EXPIRED;
        } else if (daysUntilExpiry <= EmailConstants.EXPIRY_THRESHOLD_1_DAY) {
            return EmailConstants.ALERT_TYPE_1_DAY;
        } else if (daysUntilExpiry <= EmailConstants.EXPIRY_THRESHOLD_7_DAY) {
            return EmailConstants.ALERT_TYPE_7_DAY;
        } else if (daysUntilExpiry <= EmailConstants.EXPIRY_THRESHOLD_15_DAY) {
            return EmailConstants.ALERT_TYPE_15_DAY;
        } else if (daysUntilExpiry <= EmailConstants.EXPIRY_THRESHOLD_30_DAY) {
            return EmailConstants.ALERT_TYPE_30_DAY;
        } else {
            return EmailConstants.ALERT_TYPE_NORMAL;
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
}