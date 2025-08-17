package com.example.certificate.service.impl;

import com.example.certificate.config.CertificateStatusConfig;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.domain.repository.CertificateRepository;
import com.example.certificate.common.exception.MonitoringException;
import com.example.certificate.service.CertificateService;
import com.example.certificate.service.MonitoringService;
import com.example.certificate.service.MonitoringLogService;
import com.example.certificate.service.AlertRuleEngine;
import com.example.certificate.service.EmailService;
import com.example.certificate.service.SmsService;
import com.example.certificate.service.dto.EmailResult;
import com.example.certificate.service.dto.SmsResult;
import com.example.certificate.config.EmailConfig;
import com.example.certificate.config.SmsConfig;
import com.example.certificate.domain.model.AlertRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 证书监控服务实现
 * 实现证书监控的核心业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringServiceImpl implements MonitoringService {
    
    private final CertificateRepository certificateRepository;
    private final CertificateService certificateService;
    private final CertificateStatusConfig certificateStatusConfig;
    private final MonitoringLogService monitoringLogService;
    private final AlertRuleEngine alertRuleEngine;
    private final EmailService emailService;
    private final SmsService smsService;
    private final EmailConfig emailConfig;
    private final SmsConfig smsConfig;
    
    @Override
    public void monitorAllCertificates() {
        log.info("开始监控所有证书");
        
        List<Certificate> certificates = certificateRepository.findAll();
        int successCount = 0;
        int failureCount = 0;
        
        for (Certificate certificate : certificates) {
            try {
                monitorCertificate(certificate);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("监控证书 {} 失败: {}", certificate.getName(), e.getMessage(), e);
                // 继续处理其他证书，不中断整个监控流程
            }
        }
        
        log.info("完成监控所有证书，共 {} 个证书，成功 {} 个，失败 {} 个", 
                certificates.size(), successCount, failureCount);
    }
    
    @Override
    public void monitorCertificate(Certificate certificate) {
        log.debug("监控证书: {}", certificate.getName());
        
        try {
            // 计算到期天数并记录监控结果
            int daysUntilExpiry = calculateDaysUntilExpiry(certificate);
            monitoringLogService.logMonitoringResult(certificate, daysUntilExpiry);
            
            // 使用配置化阈值计算当前状态
            CertificateStatus currentStatus = checkCertificateStatusWithConfig(certificate);
            
            // 如果状态发生变化，更新证书状态并记录状态变更
            if (currentStatus != certificate.getStatus()) {
                CertificateStatus oldStatus = certificate.getStatus();
                log.info("证书 {} 状态从 {} 变更为 {}", 
                    certificate.getName(), oldStatus, currentStatus);
                
                // 记录状态变更日志
                monitoringLogService.logStatusChange(certificate, oldStatus, currentStatus);
                
                // 更新证书状态
                certificateService.updateCertificateStatus(certificate.getId(), currentStatus);
            }

            // 评估预警规则并发送预警
            evaluateAndSendAlerts(certificate);

        } catch (Exception e) {
            String errorMessage = String.format("监控证书 %s (ID: %s) 时发生异常", 
                    certificate.getName(), certificate.getId());
            log.error(errorMessage, e);
            throw new MonitoringException("CERTIFICATE_MONITORING_FAILED", errorMessage, e);
        }
    }
    
    @Override
    public CertificateStatus checkCertificateStatus(Certificate certificate) {
        return certificate.calculateStatus();
    }
    
    @Override
    public int calculateDaysUntilExpiry(Certificate certificate) {
        return (int) certificate.getDaysUntilExpiry();
    }
    
    @Override
    public CertificateStatus checkCertificateStatusWithConfig(Certificate certificate) {
        int thresholdDays = certificateStatusConfig.getExpiringSoonDays();
        return certificate.calculateStatus(thresholdDays);
    }

    /**
     * 评估预警规则并发送预警
     *
     * @param certificate 证书信息
     */
    private void evaluateAndSendAlerts(Certificate certificate) {
        try {
            // 评估是否触发预警规则
            if (alertRuleEngine.evaluateRules(certificate)) {
                List<AlertRule> triggeredRules = alertRuleEngine.getTriggeredRules(certificate);
                
                log.info("证书 {} 触发了 {} 个预警规则", certificate.getName(), triggeredRules.size());
                
                for (AlertRule rule : triggeredRules) {
                    sendAlert(certificate, rule);
                }
            } else {
                log.debug("证书 {} 未触发任何预警规则", certificate.getName());
            }
        } catch (Exception e) {
            log.error("评估证书 {} 的预警规则时发生错误: {}", certificate.getName(), e.getMessage(), e);
        }
    }

    /**
     * 发送预警
     *
     * @param certificate 证书信息
     * @param rule        触发的预警规则
     */
    private void sendAlert(Certificate certificate, AlertRule rule) {
        try {
            int daysUntilExpiry = alertRuleEngine.calculateDaysUntilExpiry(certificate);
            
            log.info("🚨 预警触发 - 证书: {}, 规则: {}, 剩余天数: {}, 预警渠道: {}", 
                    certificate.getName(), 
                    rule.getName(), 
                    daysUntilExpiry, 
                    rule.getAlertChannels());
            
            // 根据预警规则的渠道配置发送不同类型的预警
            List<String> alertChannels = rule.getAlertChannels();
            
            if (alertChannels.contains("EMAIL")) {
                sendEmailAlert(certificate, daysUntilExpiry, rule);
            }
            
            if (alertChannels.contains("SMS")) {
                sendSmsAlert(certificate, daysUntilExpiry, rule);
            }
                    
            // 记录预警日志到监控日志服务
            logAlert(certificate, rule, daysUntilExpiry);
            
        } catch (Exception e) {
            log.error("发送证书 {} 的预警时发生错误，规则: {}, 错误: {}", 
                     certificate.getName(), rule.getName(), e.getMessage(), e);
        }
    }

    /**
     * 发送邮件预警
     *
     * @param certificate     证书信息
     * @param daysUntilExpiry 距离到期天数
     * @param rule            触发的预警规则
     */
    private void sendEmailAlert(Certificate certificate, int daysUntilExpiry, AlertRule rule) {
        try {
            // 检查邮件功能是否启用
            if (!emailConfig.isEnabled()) {
                log.debug("邮件功能未启用，跳过邮件预警");
                return;
            }
            
            String recipient = emailConfig.getDefaultRecipient();
            
            // 发送邮件预警
            EmailResult result = emailService.sendExpiryAlertEmail(certificate, daysUntilExpiry, recipient);
            
            if (result.isSuccess()) {
                log.info("✅ 邮件预警发送成功 - 证书: {}, 收件人: {}, 模式: {}", 
                        certificate.getName(), recipient, emailConfig.getMode());
            } else {
                log.warn("⚠️ 邮件预警发送失败 - 证书: {}, 错误: {}", 
                        certificate.getName(), result.getErrorMessage());
            }
            
        } catch (Exception e) {
            log.error("发送邮件预警时发生异常 - 证书: {}, 错误: {}", 
                     certificate.getName(), e.getMessage(), e);
        }
    }
    
    /**
     * 发送短信预警
     *
     * @param certificate     证书信息
     * @param daysUntilExpiry 距离到期天数
     * @param rule            触发的预警规则
     */
    private void sendSmsAlert(Certificate certificate, int daysUntilExpiry, AlertRule rule) {
        try {
            // 检查短信功能是否启用
            if (!smsConfig.isEnabled()) {
                log.debug("短信功能未启用，跳过短信预警");
                return;
            }
            
            String recipient = smsConfig.getDefaultRecipient();
            
            // 发送短信预警
            SmsResult result = smsService.sendExpiryAlertSms(certificate, daysUntilExpiry, recipient);
            
            if (result.isSuccess()) {
                log.info("✅ 短信预警发送成功 - 证书: {}, 收件人: {}, 模式: {}", 
                        certificate.getName(), smsConfig.maskPhoneNumber(recipient), smsConfig.getMode());
            } else {
                log.warn("⚠️ 短信预警发送失败 - 证书: {}, 错误: {}", 
                        certificate.getName(), result.getErrorMessage());
            }
            
        } catch (Exception e) {
            log.error("发送短信预警时发生异常 - 证书: {}, 错误: {}", 
                     certificate.getName(), e.getMessage(), e);
        }
    }

    /**
     * 记录预警日志
     *
     * @param certificate      证书信息
     * @param rule             触发的规则
     * @param daysUntilExpiry  距离到期天数
     */
    private void logAlert(Certificate certificate, AlertRule rule, int daysUntilExpiry) {
        try {
            // 使用现有的监控日志服务记录预警信息
            String alertMessage = String.format("预警规则 '%s' 触发，剩余天数: %d，预警渠道: %s", 
                    rule.getName(), daysUntilExpiry, rule.getAlertChannels());
            
            // 这里可以扩展，记录到专门的预警日志表中
            // 当前使用监控日志服务记录
            log.debug("记录预警日志: {}", alertMessage);
            
        } catch (Exception e) {
            log.error("记录预警日志时发生错误: {}", e.getMessage(), e);
        }
    }
}