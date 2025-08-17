package com.example.certificate.infrastructure.external.email;

import com.example.certificate.common.constant.EmailConstants;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.service.EmailService;
import com.example.certificate.service.MonitoringLogService;
import com.example.certificate.service.dto.EmailResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 日志模式邮件服务实现
 * MVP阶段通过日志记录来模拟邮件发送功能
 */
@Service("logEmailService")
@Primary
@ConditionalOnProperty(name = "alert.email.mode", havingValue = "log", matchIfMissing = true)
public class LogEmailServiceImpl implements EmailService {
    
    private static final Logger log = LoggerFactory.getLogger(LogEmailServiceImpl.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private final MonitoringLogService monitoringLogService;
    
    public LogEmailServiceImpl(MonitoringLogService monitoringLogService) {
        this.monitoringLogService = monitoringLogService;
    }
    
    @Override
    public EmailResult sendExpiryAlertEmail(Certificate certificate, int daysUntilExpiry, String recipientEmail) {
        try {
            // 格式化邮件预警日志信息
            String logMessage = formatExpiryAlertMessage(certificate, daysUntilExpiry, recipientEmail);
            log.info("📧 邮件预警发送 - {}", logMessage);
            
            // 记录详细的预警信息到控制台
            logCertificateDetails(certificate, daysUntilExpiry, recipientEmail);
            
            // 记录监控日志到数据库
            monitoringLogService.logEmailAlert(certificate, daysUntilExpiry, recipientEmail);
            
            // 返回成功结果
            EmailResult result = EmailResult.success(EmailConstants.EMAIL_SUCCESS_MESSAGE_LOG_MODE, recipientEmail);
            log.debug("邮件预警记录成功: {}", result);
            
            return result;
            
        } catch (Exception e) {
            log.error("记录邮件预警时发生异常 - 证书: {}, 收件人: {}, 错误: {}", 
                     certificate.getName(), recipientEmail, e.getMessage(), e);
            
            return EmailResult.failure(
                "邮件预警记录失败: " + e.getMessage(),
                EmailConstants.ERROR_CODE_LOG_EMAIL_FAILED,
                recipientEmail
            );
        }
    }
    
    @Override
    public EmailResult sendDailySummary(List<Certificate> expiringSoonCertificates, 
                                       List<Certificate> expiredCertificates, 
                                       String recipientEmail) {
        try {
            String summaryMessage = formatDailySummaryMessage(
                expiringSoonCertificates, expiredCertificates, recipientEmail);
            log.info("📧 每日摘要邮件 - {}", summaryMessage);
            
            // 记录即将过期的证书详情
            if (!expiringSoonCertificates.isEmpty()) {
                log.info("即将过期的证书清单:");
                for (Certificate cert : expiringSoonCertificates) {
                    log.info("  → 证书: {}, 域名: {}, 到期日期: {}, 剩余天数: {}天", 
                            cert.getName(), cert.getDomain(), 
                            DATE_FORMAT.format(cert.getExpiryDate()),
                            cert.getDaysUntilExpiry());
                }
            }
            
            // 记录已过期的证书详情
            if (!expiredCertificates.isEmpty()) {
                log.warn("已过期的证书清单:");
                for (Certificate cert : expiredCertificates) {
                    log.warn("  ⚠️ 证书: {}, 域名: {}, 到期日期: {}, 已过期: {}天", 
                            cert.getName(), cert.getDomain(), 
                            DATE_FORMAT.format(cert.getExpiryDate()),
                            Math.abs(cert.getDaysUntilExpiry()));
                }
            }
            
            // 记录监控日志到数据库
            monitoringLogService.logDailySummary(expiringSoonCertificates, expiredCertificates, recipientEmail);
            
            return EmailResult.success(EmailConstants.EMAIL_SUCCESS_MESSAGE_SUMMARY_LOG_MODE, recipientEmail);
            
        } catch (Exception e) {
            log.error("记录每日摘要时发生异常 - 收件人: {}, 错误: {}", 
                     recipientEmail, e.getMessage(), e);
            
            return EmailResult.failure(
                "每日摘要记录失败: " + e.getMessage(),
                EmailConstants.ERROR_CODE_LOG_SUMMARY_FAILED,
                recipientEmail
            );
        }
    }
    
    @Override
    public List<EmailResult> sendBatchAlerts(List<Certificate> certificates, String recipientEmail) {
        log.info("📧 批量邮件预警开始 - 证书数量: {}, 收件人: {}", certificates.size(), recipientEmail);
        
        List<EmailResult> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        
        for (Certificate certificate : certificates) {
            try {
                int daysUntilExpiry = (int) certificate.getDaysUntilExpiry();
                EmailResult result = sendExpiryAlertEmail(certificate, daysUntilExpiry, recipientEmail);
                results.add(result);
                
                if (result.isSuccess()) {
                    successCount++;
                } else {
                    failureCount++;
                }
                
            } catch (Exception e) {
                log.error("批量预警中处理证书 {} 时发生异常: {}", certificate.getName(), e.getMessage());
                EmailResult failureResult = EmailResult.failure(
                    "批量处理异常: " + e.getMessage(),
                    EmailConstants.ERROR_CODE_BATCH_PROCESSING_ERROR,
                    recipientEmail
                );
                results.add(failureResult);
                failureCount++;
            }
        }
        
        log.info("📧 批量邮件预警完成 - 成功: {}, 失败: {}, 总计: {}", 
                successCount, failureCount, certificates.size());
        
        return results;
    }
    
    /**
     * 格式化证书过期预警消息
     */
    private String formatExpiryAlertMessage(Certificate certificate, int daysUntilExpiry, String recipient) {
        String alertType = getAlertTypeByDays(daysUntilExpiry);
        return String.format(
            EmailConstants.FORMAT_EXPIRY_ALERT,
            recipient,
            certificate.getName(),
            certificate.getDomain(),
            DATE_FORMAT.format(certificate.getExpiryDate()),
            daysUntilExpiry,
            alertType
        );
    }
    
    /**
     * 格式化每日摘要消息
     */
    private String formatDailySummaryMessage(List<Certificate> expiringSoon, 
                                           List<Certificate> expired, 
                                           String recipient) {
        return String.format(
            EmailConstants.FORMAT_DAILY_SUMMARY,
            recipient,
            expiringSoon.size(),
            expired.size(),
            DATE_FORMAT.format(new Date())
        );
    }
    
    /**
     * 记录证书详细信息
     */
    private void logCertificateDetails(Certificate certificate, int daysUntilExpiry, String recipient) {
        log.info("  证书详情:");
        log.info("    {} 证书名称: {}", EmailConstants.LOG_PREFIX_CERTIFICATE, certificate.getName());
        log.info("    {} 域名: {}", EmailConstants.LOG_PREFIX_DOMAIN, certificate.getDomain());
        log.info("    {} 颁发日期: {}", EmailConstants.LOG_PREFIX_DATE, DATE_FORMAT.format(certificate.getIssueDate()));
        log.info("    {} 到期日期: {}", EmailConstants.LOG_PREFIX_TIME, DATE_FORMAT.format(certificate.getExpiryDate()));
        log.info("    {} 证书状态: {}", EmailConstants.LOG_PREFIX_STATUS, certificate.getStatus());
        log.info("    {} 剩余天数: {}天", EmailConstants.LOG_PREFIX_DAYS, daysUntilExpiry);
        log.info("    {} 收件人: {}", EmailConstants.LOG_PREFIX_RECIPIENT, recipient);
        log.info("    {} 预警类型: {}", EmailConstants.LOG_PREFIX_ALERT_TYPE, getAlertTypeByDays(daysUntilExpiry));
    }
    
    /**
     * 根据剩余天数确定预警类型
     */
    private String getAlertTypeByDays(int daysUntilExpiry) {
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
}