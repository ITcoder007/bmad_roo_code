package com.example.certificate.service.impl;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.domain.model.LogType;
import com.example.certificate.domain.model.MonitoringLog;
import com.example.certificate.domain.repository.MonitoringLogRepository;
import com.example.certificate.service.MonitoringLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 监控日志服务实现
 * 实现监控日志的记录和管理功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringLogServiceImpl implements MonitoringLogService {
    
    private final MonitoringLogRepository monitoringLogRepository;
    
    @Override
    public void logMonitoringResult(Certificate certificate, int daysUntilExpiry) {
        log.debug("记录证书监控结果: {} (距离过期 {} 天)", certificate.getName(), daysUntilExpiry);
        
        String message = String.format("监控证书 %s (域名: %s)，状态: %s，距离过期: %d 天", 
                certificate.getName(), 
                certificate.getDomain(), 
                certificate.getStatus(), 
                daysUntilExpiry);
        
        MonitoringLog monitoringLog = MonitoringLog.builder()
                .certificateId(certificate.getId())
                .logType(LogType.MONITORING)
                .logTime(new Date())
                .message(message)
                .daysUntilExpiry(daysUntilExpiry)
                .createdAt(new Date())
                .build();
        
        monitoringLogRepository.save(monitoringLog);
    }
    
    @Override
    public void logStatusChange(Certificate certificate, CertificateStatus oldStatus, CertificateStatus newStatus) {
        log.info("记录证书状态变更: {} 从 {} 变更为 {}", certificate.getName(), oldStatus, newStatus);
        
        String message = String.format("证书 %s (域名: %s) 状态从 %s 变更为 %s", 
                certificate.getName(), 
                certificate.getDomain(), 
                oldStatus, 
                newStatus);
        
        MonitoringLog monitoringLog = MonitoringLog.builder()
                .certificateId(certificate.getId())
                .logType(LogType.STATUS_CHANGE)
                .logTime(new Date())
                .message(message)
                .createdAt(new Date())
                .build();
        
        monitoringLogRepository.save(monitoringLog);
    }
    
    @Override
    public void logEmailAlert(Certificate certificate, int daysUntilExpiry, String recipient) {
        log.info("记录邮件预警日志: 证书 {} 发送给 {}, 剩余 {} 天", 
                certificate.getName(), recipient, daysUntilExpiry);
        
        String message = String.format("邮件预警 - 证书: %s (域名: %s)，剩余天数: %d，收件人: %s", 
                certificate.getName(), 
                certificate.getDomain(), 
                daysUntilExpiry,
                recipient);
        
        MonitoringLog monitoringLog = MonitoringLog.builder()
                .certificateId(certificate.getId())
                .logType(LogType.ALERT_EMAIL)
                .logTime(new Date())
                .message(message)
                .daysUntilExpiry(daysUntilExpiry)
                .createdAt(new Date())
                .build();
        
        monitoringLogRepository.save(monitoringLog);
    }
    
    @Override
    public void logDailySummary(List<Certificate> expiringSoonCertificates, 
                               List<Certificate> expiredCertificates, 
                               String recipient) {
        log.info("记录每日摘要日志: 即将过期 {} 个，已过期 {} 个，收件人: {}", 
                expiringSoonCertificates.size(), expiredCertificates.size(), recipient);
        
        String message = String.format("每日摘要 - 即将过期证书: %d个，已过期证书: %d个，收件人: %s", 
                expiringSoonCertificates.size(), 
                expiredCertificates.size(),
                recipient);
        
        MonitoringLog monitoringLog = MonitoringLog.builder()
                .certificateId(null) // 摘要日志不关联特定证书
                .logType(LogType.ALERT_EMAIL)
                .logTime(new Date())
                .message(message)
                .createdAt(new Date())
                .build();
        
        monitoringLogRepository.save(monitoringLog);
        
        // 为每个即将过期的证书单独记录日志
        for (Certificate cert : expiringSoonCertificates) {
            logEmailAlert(cert, (int) cert.getDaysUntilExpiry(), recipient);
        }
        
        // 为每个已过期的证书单独记录日志
        for (Certificate cert : expiredCertificates) {
            logEmailAlert(cert, (int) cert.getDaysUntilExpiry(), recipient);
        }
    }
    
    @Override
    public void logSmsAlert(Certificate certificate, int daysUntilExpiry, String recipient) {
        log.info("记录短信预警日志: 证书 {} 发送给 {}, 剩余 {} 天", 
                certificate.getName(), recipient, daysUntilExpiry);
        
        // 对手机号进行脱敏处理
        String maskedPhone = maskPhoneNumber(recipient);
        String message = String.format("短信预警 - 证书: %s (域名: %s)，剩余天数: %d，收件人: %s", 
                certificate.getName(), 
                certificate.getDomain(), 
                daysUntilExpiry,
                maskedPhone);
        
        MonitoringLog monitoringLog = MonitoringLog.builder()
                .certificateId(certificate.getId())
                .logType(LogType.ALERT_SMS)
                .logTime(new Date())
                .message(message)
                .daysUntilExpiry(daysUntilExpiry)
                .createdAt(new Date())
                .build();
        
        monitoringLogRepository.save(monitoringLog);
    }
    
    @Override
    public void logSmsDailySummary(List<Certificate> expiringSoonCertificates, 
                                  List<Certificate> expiredCertificates, 
                                  String recipient) {
        log.info("记录短信每日摘要日志: 即将过期 {} 个，已过期 {} 个，收件人: {}", 
                expiringSoonCertificates.size(), expiredCertificates.size(), recipient);
        
        // 对手机号进行脱敏处理
        String maskedPhone = maskPhoneNumber(recipient);
        String message = String.format("短信每日摘要 - 即将过期证书: %d个，已过期证书: %d个，收件人: %s", 
                expiringSoonCertificates.size(), 
                expiredCertificates.size(),
                maskedPhone);
        
        MonitoringLog monitoringLog = MonitoringLog.builder()
                .certificateId(null) // 摘要日志不关联特定证书
                .logType(LogType.ALERT_SMS)
                .logTime(new Date())
                .message(message)
                .createdAt(new Date())
                .build();
        
        monitoringLogRepository.save(monitoringLog);
        
        // 为每个即将过期的证书单独记录日志
        for (Certificate cert : expiringSoonCertificates) {
            logSmsAlert(cert, (int) cert.getDaysUntilExpiry(), recipient);
        }
        
        // 为每个已过期的证书单独记录日志
        for (Certificate cert : expiredCertificates) {
            logSmsAlert(cert, (int) cert.getDaysUntilExpiry(), recipient);
        }
    }
    
    /**
     * 手机号脱敏处理
     * 将 13812345678 转换为 138****5678
     */
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}