package com.example.certificate.service.impl;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.service.SmsService;
import com.example.certificate.service.dto.SmsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 短信服务生产环境实现
 * 用于实际的短信发送（当前版本暂未实现具体发送逻辑）
 */
@Service("productionSmsService")
@ConditionalOnProperty(name = "alert.sms.mode", havingValue = "real")
public class SmsServiceImpl implements SmsService {
    
    private static final Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);
    
    @Override
    public SmsResult sendExpiryAlertSms(Certificate certificate, int daysUntilExpiry, String recipientPhone) {
        log.info("生产环境短信服务 - 发送证书过期预警短信");
        log.info("收件人: {}, 证书: {}, 域名: {}, 剩余天数: {}天", 
                recipientPhone, certificate.getName(), certificate.getDomain(), daysUntilExpiry);
        
        // TODO: 实现真实的短信发送逻辑（阿里云、腾讯云等）
        // 当前版本返回未实现的提示
        return SmsResult.failure(
            "生产环境短信服务尚未实现短信发送功能", 
            "NOT_IMPLEMENTED", 
            recipientPhone
        );
    }
    
    @Override
    public SmsResult sendDailySummary(List<Certificate> expiringSoonCertificates, 
                                     List<Certificate> expiredCertificates, 
                                     String recipientPhone) {
        log.info("生产环境短信服务 - 发送每日摘要短信");
        log.info("收件人: {}, 即将过期证书: {}个, 已过期证书: {}个", 
                recipientPhone, expiringSoonCertificates.size(), expiredCertificates.size());
        
        // TODO: 实现真实的短信发送逻辑
        return SmsResult.failure(
            "生产环境短信服务尚未实现短信发送功能", 
            "NOT_IMPLEMENTED", 
            recipientPhone
        );
    }
    
    @Override
    public List<SmsResult> sendBatchAlerts(List<Certificate> certificates, String recipientPhone) {
        log.info("生产环境短信服务 - 批量发送短信预警，共{}个证书", certificates.size());
        
        List<SmsResult> results = new ArrayList<>();
        for (Certificate certificate : certificates) {
            int daysUntilExpiry = (int) certificate.getDaysUntilExpiry();
            SmsResult result = sendExpiryAlertSms(certificate, daysUntilExpiry, recipientPhone);
            results.add(result);
        }
        
        return results;
    }
}