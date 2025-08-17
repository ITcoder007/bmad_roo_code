package com.example.certificate.service;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.service.dto.EmailResult;

import java.util.List;

/**
 * 邮件服务接口
 * 定义邮件发送相关的功能契约
 */
public interface EmailService {
    
    /**
     * 发送证书过期预警邮件
     * @param certificate 证书信息
     * @param daysUntilExpiry 距离过期的天数
     * @param recipientEmail 收件人邮箱
     * @return 邮件发送结果
     */
    EmailResult sendExpiryAlertEmail(Certificate certificate, int daysUntilExpiry, String recipientEmail);
    
    /**
     * 发送每日证书状态摘要邮件
     * @param expiringSoonCertificates 即将过期的证书列表
     * @param expiredCertificates 已过期的证书列表
     * @param recipientEmail 收件人邮箱
     * @return 邮件发送结果
     */
    EmailResult sendDailySummary(List<Certificate> expiringSoonCertificates, 
                                List<Certificate> expiredCertificates, 
                                String recipientEmail);
    
    /**
     * 批量发送邮件预警
     * @param certificates 需要发送预警的证书列表
     * @param recipientEmail 收件人邮箱
     * @return 批量发送结果
     */
    List<EmailResult> sendBatchAlerts(List<Certificate> certificates, String recipientEmail);
}