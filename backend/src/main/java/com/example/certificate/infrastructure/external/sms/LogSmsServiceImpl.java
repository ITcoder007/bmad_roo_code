package com.example.certificate.infrastructure.external.sms;

import com.example.certificate.common.constant.SmsConstants;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.service.MonitoringLogService;
import com.example.certificate.service.SmsService;
import com.example.certificate.service.dto.SmsResult;
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
 * 日志模式短信服务实现
 * MVP阶段通过日志记录来模拟短信发送功能
 * 
 * 设计原则：
 * 1. 单一职责 - 仅负责日志记录，格式化逻辑分离
 * 2. 错误隔离 - 统一的异常处理策略
 * 3. 性能优化 - 避免重复的字符串操作
 * 4. 隐私保护 - 自动对手机号进行脱敏处理
 */
@Service("logSmsService")
@Primary
@ConditionalOnProperty(name = "alert.sms.mode", havingValue = "log", matchIfMissing = true)
public class LogSmsServiceImpl implements SmsService {
    
    private static final Logger log = LoggerFactory.getLogger(LogSmsServiceImpl.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private final MonitoringLogService monitoringLogService;
    private final SmsLogFormatter logFormatter;
    
    public LogSmsServiceImpl(MonitoringLogService monitoringLogService) {
        this.monitoringLogService = monitoringLogService;
        this.logFormatter = new SmsLogFormatter();
    }
    
    @Override
    public SmsResult sendExpiryAlertSms(Certificate certificate, int daysUntilExpiry, String recipientPhone) {
        try {
            return executeWithErrorHandling(() -> {
                // 验证手机号格式
                if (!logFormatter.isPhoneValid(recipientPhone)) {
                    return SmsResult.failure(
                        "手机号格式不正确: " + recipientPhone,
                        SmsConstants.ERROR_CODE_PHONE_INVALID,
                        recipientPhone
                    );
                }
                
                // 格式化并记录预警信息
                String logMessage = logFormatter.formatExpiryAlert(certificate, daysUntilExpiry, recipientPhone);
                log.info("📱 短信预警发送 - {}", logMessage);
                
                // 记录详细信息
                String details = logFormatter.formatCertificateDetails(certificate, daysUntilExpiry, recipientPhone);
                log.info("  {}", details);
                
                // 记录实际短信内容
                String smsContent = logFormatter.formatSmsContent(certificate, daysUntilExpiry);
                if (!logFormatter.isContentValid(smsContent)) {
                    log.warn("短信内容过长，需要截断: {}", smsContent);
                    smsContent = smsContent.substring(0, SmsConstants.MAX_SMS_LENGTH) + "...";
                }
                log.info("  📱 短信内容: {}", smsContent);
                
                // 记录监控日志到数据库
                monitoringLogService.logSmsAlert(certificate, daysUntilExpiry, recipientPhone);
                
                SmsResult result = SmsResult.success(SmsConstants.SMS_SUCCESS_MESSAGE_LOG_MODE, recipientPhone);
                log.debug("短信预警记录成功: {}", result);
                
                return result;
            }, certificate.getName(), recipientPhone, SmsConstants.ERROR_CODE_LOG_SMS_FAILED);
            
        } catch (Exception e) {
            // 这里不应该到达，但保留作为最后的防线
            log.error("意外的异常逃脱了错误处理: {}", e.getMessage(), e);
            return SmsResult.failure(
                "系统内部错误: " + e.getMessage(),
                SmsConstants.ERROR_CODE_LOG_SMS_FAILED,
                recipientPhone
            );
        }
    }
    
    @Override
    public SmsResult sendDailySummary(List<Certificate> expiringSoonCertificates, 
                                     List<Certificate> expiredCertificates, 
                                     String recipientPhone) {
        try {
            // 验证手机号格式
            if (!logFormatter.isPhoneValid(recipientPhone)) {
                return SmsResult.failure(
                    "手机号格式不正确: " + recipientPhone,
                    SmsConstants.ERROR_CODE_PHONE_INVALID,
                    recipientPhone
                );
            }
            
            String summaryMessage = logFormatter.formatDailySummary(
                expiringSoonCertificates, expiredCertificates, recipientPhone);
            log.info("📱 每日摘要短信 - {}", summaryMessage);
            
            // 记录实际短信内容
            String smsContent = logFormatter.formatDailySummarySmsContent(
                expiringSoonCertificates.size(), expiredCertificates.size());
            log.info("  📱 短信内容: {}", smsContent);
            
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
            monitoringLogService.logSmsDailySummary(expiringSoonCertificates, expiredCertificates, recipientPhone);
            
            return SmsResult.success(SmsConstants.SMS_SUCCESS_MESSAGE_SUMMARY_LOG_MODE, recipientPhone);
            
        } catch (Exception e) {
            log.error("记录每日摘要时发生异常 - 收件人: {}, 错误: {}", 
                     recipientPhone, e.getMessage(), e);
            
            return SmsResult.failure(
                "每日摘要记录失败: " + e.getMessage(),
                SmsConstants.ERROR_CODE_LOG_SUMMARY_FAILED,
                recipientPhone
            );
        }
    }
    
    @Override
    public List<SmsResult> sendBatchAlerts(List<Certificate> certificates, String recipientPhone) {
        log.info("📱 批量短信预警开始 - 证书数量: {}, 收件人: {}", certificates.size(), recipientPhone);
        
        // 验证手机号格式
        if (!logFormatter.isPhoneValid(recipientPhone)) {
            List<SmsResult> errorResults = new ArrayList<>();
            for (int i = 0; i < certificates.size(); i++) {
                errorResults.add(SmsResult.failure(
                    "手机号格式不正确: " + recipientPhone,
                    SmsConstants.ERROR_CODE_PHONE_INVALID,
                    recipientPhone
                ));
            }
            return errorResults;
        }
        
        List<SmsResult> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        
        for (Certificate certificate : certificates) {
            try {
                int daysUntilExpiry = (int) certificate.getDaysUntilExpiry();
                SmsResult result = sendExpiryAlertSms(certificate, daysUntilExpiry, recipientPhone);
                results.add(result);
                
                if (result.isSuccess()) {
                    successCount++;
                } else {
                    failureCount++;
                }
                
            } catch (Exception e) {
                log.error("批量预警中处理证书 {} 时发生异常: {}", certificate.getName(), e.getMessage());
                SmsResult failureResult = SmsResult.failure(
                    "批量处理异常: " + e.getMessage(),
                    SmsConstants.ERROR_CODE_BATCH_PROCESSING_ERROR,
                    recipientPhone
                );
                results.add(failureResult);
                failureCount++;
            }
        }
        
        log.info("📱 批量短信预警完成 - 成功: {}, 失败: {}, 总计: {}", 
                successCount, failureCount, certificates.size());
        
        return results;
    }
    
    /**
     * 统一的错误处理模板方法
     * 封装通用的异常处理逻辑，减少代码重复
     */
    private SmsResult executeWithErrorHandling(SmsOperation operation, 
                                              String entityName, 
                                              String recipient, 
                                              String errorCode) {
        try {
            return operation.execute();
        } catch (Exception e) {
            log.error("记录短信操作时发生异常 - 实体: {}, 收件人: {}, 错误: {}", 
                     entityName, recipient, e.getMessage(), e);
            
            return SmsResult.failure(
                "短信操作失败: " + e.getMessage(),
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
     * 函数式接口用于封装短信操作
     */
    @FunctionalInterface
    private interface SmsOperation {
        SmsResult execute() throws Exception;
    }
}