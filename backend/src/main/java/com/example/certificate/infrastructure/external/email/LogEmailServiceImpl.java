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
 * 
 * 设计原则：
 * 1. 单一职责 - 仅负责日志记录，格式化逻辑分离
 * 2. 错误隔离 - 统一的异常处理策略
 * 3. 性能优化 - 避免重复的字符串操作
 */
@Service("logEmailService")
@Primary
@ConditionalOnProperty(name = "alert.email.mode", havingValue = "log", matchIfMissing = true)
public class LogEmailServiceImpl implements EmailService {
    
    private static final Logger log = LoggerFactory.getLogger(LogEmailServiceImpl.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private final MonitoringLogService monitoringLogService;
    private final EmailLogFormatter logFormatter;
    
    public LogEmailServiceImpl(MonitoringLogService monitoringLogService) {
        this.monitoringLogService = monitoringLogService;
        this.logFormatter = new EmailLogFormatter();
    }
    
    @Override
    public EmailResult sendExpiryAlertEmail(Certificate certificate, int daysUntilExpiry, String recipientEmail) {
        try {
            return executeWithErrorHandling(() -> {
                // 格式化并记录预警信息
                String logMessage = logFormatter.formatExpiryAlert(certificate, daysUntilExpiry, recipientEmail);
                log.info("📧 邮件预警发送 - {}", logMessage);
                
                // 记录详细信息
                String details = logFormatter.formatCertificateDetails(certificate, daysUntilExpiry, recipientEmail);
                log.info("  {}", details);
                
                // 记录监控日志到数据库
                monitoringLogService.logEmailAlert(certificate, daysUntilExpiry, recipientEmail);
                
                EmailResult result = EmailResult.success(EmailConstants.EMAIL_SUCCESS_MESSAGE_LOG_MODE, recipientEmail);
                log.debug("邮件预警记录成功: {}", result);
                
                return result;
            }, certificate.getName(), recipientEmail, EmailConstants.ERROR_CODE_LOG_EMAIL_FAILED);
            
        } catch (Exception e) {
            // 这里不应该到达，但保留作为最后的防线
            log.error("意外的异常逃脱了错误处理: {}", e.getMessage(), e);
            return EmailResult.failure(
                "系统内部错误: " + e.getMessage(),
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
                String summaryMessage = logFormatter.formatDailySummary(
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
     * 统一的错误处理模板方法
     * 封装通用的异常处理逻辑，减少代码重复
     */
    private EmailResult executeWithErrorHandling(EmailOperation operation, 
                                                String entityName, 
                                                String recipient, 
                                                String errorCode) {
        try {
            return operation.execute();
        } catch (Exception e) {
            log.error("记录邮件操作时发生异常 - 实体: {}, 收件人: {}, 错误: {}", 
                     entityName, recipient, e.getMessage(), e);
            
            return EmailResult.failure(
                "邮件操作失败: " + e.getMessage(),
                errorCode,
                recipient
            );
        }
    }
    
    /**
     * 记录证书列表的通用方法
     */
    private void logCertificateList(String title, List<Certificate> certificates, boolean isExpired) {
        if (certificates.isEmpty()) {
            return;
        }
        
        if (isExpired) {
            log.warn(title + ":");
            certificates.forEach(cert -> {
                synchronized (DATE_FORMAT) {
                    log.warn("  ⚠️ 证书: {}, 域名: {}, 到期日期: {}, 已过期: {}天", 
                            cert.getName(), cert.getDomain(), 
                            DATE_FORMAT.format(cert.getExpiryDate()),
                            Math.abs(cert.getDaysUntilExpiry()));
                }
            });
        } else {
            log.info(title + ":");
            certificates.forEach(cert -> {
                synchronized (DATE_FORMAT) {
                    log.info("  → 证书: {}, 域名: {}, 到期日期: {}, 剩余天数: {}天", 
                            cert.getName(), cert.getDomain(), 
                            DATE_FORMAT.format(cert.getExpiryDate()),
                            cert.getDaysUntilExpiry());
                }
            });
        }
    }
    
    /**
     * 函数式接口用于封装邮件操作
     */
    @FunctionalInterface
    private interface EmailOperation {
        EmailResult execute() throws Exception;
    }
}