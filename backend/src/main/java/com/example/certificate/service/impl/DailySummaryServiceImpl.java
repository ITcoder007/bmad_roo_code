package com.example.certificate.service.impl;

import com.example.certificate.config.EmailConfig;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.domain.repository.CertificateRepository;
import com.example.certificate.service.DailySummaryService;
import com.example.certificate.service.EmailService;
import com.example.certificate.service.dto.EmailResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 每日摘要服务实现
 * 实现证书状态的每日摘要报告功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailySummaryServiceImpl implements DailySummaryService {
    
    private final CertificateRepository certificateRepository;
    private final EmailService emailService;
    private final EmailConfig emailConfig;
    
    @Override
    public void generateAndSendDailySummary() {
        String defaultRecipient = emailConfig.getDefaultRecipient();
        generateAndSendDailySummary(defaultRecipient);
    }
    
    @Override
    public void generateAndSendDailySummary(String recipientEmail) {
        log.info("开始生成每日摘要报告，收件人: {}", recipientEmail);
        
        try {
            // 检查邮件功能是否启用
            if (!emailConfig.isEnabled()) {
                log.info("邮件功能未启用，跳过每日摘要发送");
                return;
            }
            
            // 获取所有证书
            List<Certificate> allCertificates = certificateRepository.findAll();
            log.debug("共找到 {} 个证书需要分析", allCertificates.size());
            
            // 分类证书状态
            List<Certificate> expiringSoonCertificates = allCertificates.stream()
                    .filter(cert -> cert.getStatus() == CertificateStatus.EXPIRING_SOON)
                    .collect(Collectors.toList());
            
            List<Certificate> expiredCertificates = allCertificates.stream()
                    .filter(cert -> cert.getStatus() == CertificateStatus.EXPIRED)
                    .collect(Collectors.toList());
            
            List<Certificate> normalCertificates = allCertificates.stream()
                    .filter(cert -> cert.getStatus() == CertificateStatus.NORMAL)
                    .collect(Collectors.toList());
            
            log.info("证书状态统计 - 正常: {}, 即将过期: {}, 已过期: {}", 
                    normalCertificates.size(), expiringSoonCertificates.size(), expiredCertificates.size());
            
            // 发送每日摘要邮件
            EmailResult result = emailService.sendDailySummary(
                    expiringSoonCertificates, 
                    expiredCertificates, 
                    recipientEmail
            );
            
            if (result.isSuccess()) {
                log.info("✅ 每日摘要邮件发送成功 - 收件人: {}, 模式: {}", 
                        recipientEmail, emailConfig.getMode());
                        
                // 记录摘要统计信息
                logSummaryStatistics(allCertificates.size(), expiringSoonCertificates.size(), 
                                   expiredCertificates.size(), normalCertificates.size());
                                   
            } else {
                log.warn("⚠️ 每日摘要邮件发送失败 - 收件人: {}, 错误: {}", 
                        recipientEmail, result.getErrorMessage());
            }
            
        } catch (Exception e) {
            log.error("生成每日摘要时发生异常 - 收件人: {}, 错误: {}", 
                     recipientEmail, e.getMessage(), e);
        }
    }
    
    /**
     * 记录摘要统计信息
     */
    private void logSummaryStatistics(int totalCount, int expiringSoonCount, 
                                    int expiredCount, int normalCount) {
        log.info("📊 每日摘要统计信息:");
        log.info("  📋 证书总数: {}", totalCount);
        log.info("  ✅ 正常状态: {} ({}%)", normalCount, 
                totalCount > 0 ? String.format("%.1f", (normalCount * 100.0 / totalCount)) : "0");
        log.info("  ⚠️ 即将过期: {} ({}%)", expiringSoonCount, 
                totalCount > 0 ? String.format("%.1f", (expiringSoonCount * 100.0 / totalCount)) : "0");
        log.info("  🚨 已过期: {} ({}%)", expiredCount, 
                totalCount > 0 ? String.format("%.1f", (expiredCount * 100.0 / totalCount)) : "0");
        
        // 如果有需要关注的证书，提供更多详细信息
        if (expiringSoonCount > 0 || expiredCount > 0) {
            log.info("  🔍 需要关注的证书总数: {}", expiringSoonCount + expiredCount);
            
            if (expiredCount > 0) {
                log.warn("  ⚠️ 注意：有 {} 个证书已过期，需要立即处理！", expiredCount);
            }
            
            if (expiringSoonCount > 0) {
                log.warn("  ⏰ 提醒：有 {} 个证书即将过期，请及时续期！", expiringSoonCount);
            }
        } else {
            log.info("  🎉 恭喜：所有证书状态正常，无需特别关注！");
        }
    }
}