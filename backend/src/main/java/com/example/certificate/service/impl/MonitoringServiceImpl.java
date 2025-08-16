package com.example.certificate.service.impl;

import com.example.certificate.config.CertificateStatusConfig;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.domain.repository.CertificateRepository;
import com.example.certificate.service.CertificateService;
import com.example.certificate.service.MonitoringService;
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
    
    @Override
    public void monitorAllCertificates() {
        log.info("开始监控所有证书");
        
        List<Certificate> certificates = certificateRepository.findAll();
        
        for (Certificate certificate : certificates) {
            monitorCertificate(certificate);
        }
        
        log.info("完成监控所有证书，共监控 {} 个证书", certificates.size());
    }
    
    @Override
    public void monitorCertificate(Certificate certificate) {
        log.debug("监控证书: {}", certificate.getName());
        
        // 使用配置化阈值计算当前状态
        CertificateStatus currentStatus = checkCertificateStatusWithConfig(certificate);
        
        // 如果状态发生变化，更新证书状态
        if (currentStatus != certificate.getStatus()) {
            log.info("证书 {} 状态从 {} 变更为 {}", 
                certificate.getName(), certificate.getStatus(), currentStatus);
            certificateService.updateCertificateStatus(certificate.getId(), currentStatus);
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