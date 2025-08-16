package com.example.certificate.service.impl;

import com.example.certificate.config.CertificateStatusConfig;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.domain.repository.CertificateRepository;
import com.example.certificate.common.exception.MonitoringException;
import com.example.certificate.service.CertificateService;
import com.example.certificate.service.MonitoringService;
import com.example.certificate.service.MonitoringLogService;
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
}