package com.example.certificate.service;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.service.dto.SmsResult;

import java.util.List;

/**
 * 短信服务接口
 * 定义短信发送相关的功能契约
 */
public interface SmsService {
    
    /**
     * 发送证书过期预警短信
     * @param certificate 证书信息
     * @param daysUntilExpiry 距离过期的天数
     * @param recipientPhone 收件人手机号
     * @return 短信发送结果
     */
    SmsResult sendExpiryAlertSms(Certificate certificate, int daysUntilExpiry, String recipientPhone);
    
    /**
     * 发送每日证书状态摘要短信
     * @param expiringSoonCertificates 即将过期的证书列表
     * @param expiredCertificates 已过期的证书列表
     * @param recipientPhone 收件人手机号
     * @return 短信发送结果
     */
    SmsResult sendDailySummary(List<Certificate> expiringSoonCertificates, 
                              List<Certificate> expiredCertificates, 
                              String recipientPhone);
    
    /**
     * 批量发送短信预警
     * @param certificates 需要发送预警的证书列表
     * @param recipientPhone 收件人手机号
     * @return 批量发送结果
     */
    List<SmsResult> sendBatchAlerts(List<Certificate> certificates, String recipientPhone);
}