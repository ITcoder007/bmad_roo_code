package com.example.certificate.service.impl;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.service.EmailService;
import com.example.certificate.service.dto.EmailResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 邮件服务生产环境实现
 * 用于实际的SMTP邮件发送（当前版本暂未实现具体发送逻辑）
 */
@Service("productionEmailService")
@ConditionalOnProperty(name = "alert.email.mode", havingValue = "real")
public class EmailServiceImpl implements EmailService {
    
    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    
    @Override
    public EmailResult sendExpiryAlertEmail(Certificate certificate, int daysUntilExpiry, String recipientEmail) {
        log.info("生产环境邮件服务 - 发送证书过期预警邮件");
        log.info("收件人: {}, 证书: {}, 域名: {}, 剩余天数: {}天", 
                recipientEmail, certificate.getName(), certificate.getDomain(), daysUntilExpiry);
        
        // TODO: 实现真实的SMTP邮件发送逻辑
        // 当前版本返回未实现的提示
        return EmailResult.failure(
            "生产环境邮件服务尚未实现SMTP发送功能", 
            "NOT_IMPLEMENTED", 
            recipientEmail
        );
    }
    
    @Override
    public EmailResult sendDailySummary(List<Certificate> expiringSoonCertificates, 
                                       List<Certificate> expiredCertificates, 
                                       String recipientEmail) {
        log.info("生产环境邮件服务 - 发送每日摘要邮件");
        log.info("收件人: {}, 即将过期证书: {}个, 已过期证书: {}个", 
                recipientEmail, expiringSoonCertificates.size(), expiredCertificates.size());
        
        // TODO: 实现真实的SMTP邮件发送逻辑
        return EmailResult.failure(
            "生产环境邮件服务尚未实现SMTP发送功能", 
            "NOT_IMPLEMENTED", 
            recipientEmail
        );
    }
    
    @Override
    public List<EmailResult> sendBatchAlerts(List<Certificate> certificates, String recipientEmail) {
        log.info("生产环境邮件服务 - 批量发送邮件预警，共{}个证书", certificates.size());
        
        List<EmailResult> results = new ArrayList<>();
        for (Certificate certificate : certificates) {
            int daysUntilExpiry = (int) certificate.getDaysUntilExpiry();
            EmailResult result = sendExpiryAlertEmail(certificate, daysUntilExpiry, recipientEmail);
            results.add(result);
        }
        
        return results;
    }
}