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
}