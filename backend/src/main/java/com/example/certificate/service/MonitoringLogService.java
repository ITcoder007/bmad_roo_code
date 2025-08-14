package com.example.certificate.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.service.dto.MonitoringLogDto;

public interface MonitoringLogService {
    
    /**
     * 分页查询监控日志
     */
    IPage<MonitoringLogDto> findPage(int page, int size, Long certificateId);
    
    /**
     * 记录证书创建日志
     */
    void logCertificateCreated(Certificate certificate);
    
    /**
     * 记录证书更新日志
     */
    void logCertificateUpdated(Certificate certificate);
    
    /**
     * 记录证书删除日志
     */
    void logCertificateDeleted(Certificate certificate);
    
    /**
     * 记录证书监控日志
     */
    void logCertificateMonitoring(Certificate certificate);
    
    /**
     * 记录证书预警日志
     */
    void logCertificateAlert(Certificate certificate, String alertType);
}