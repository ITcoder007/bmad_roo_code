package com.example.certificate.service.impl;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.service.AlertService;
import com.example.certificate.service.MonitoringLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {
    
    private final MonitoringLogService monitoringLogService;
    
    @Override
    public void sendAlert(Certificate certificate) {
        long daysUntilExpiry = certificate.getDaysUntilExpiry();
        
        // MVP阶段：仅记录日志，不实际发送邮件和短信
        log.warn("证书预警: [{}] 将在 {} 天后过期，域名: {}", 
            certificate.getName(), daysUntilExpiry, certificate.getDomain());
        
        // 记录邮件预警日志
        monitoringLogService.logCertificateAlert(certificate, "EMAIL");
        
        // 记录短信预警日志
        monitoringLogService.logCertificateAlert(certificate, "SMS");
    }
    
    @Override
    public boolean shouldSendAlert(long daysUntilExpiry) {
        // 在30天、15天、7天、1天时发送预警
        return daysUntilExpiry == 30 || 
               daysUntilExpiry == 15 || 
               daysUntilExpiry == 7 || 
               daysUntilExpiry == 1;
    }
}