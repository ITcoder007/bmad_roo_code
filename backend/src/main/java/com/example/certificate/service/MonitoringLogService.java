package com.example.certificate.service;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;

/**
 * 监控日志服务接口
 * 负责记录证书监控过程中的各种日志信息
 */
public interface MonitoringLogService {
    
    /**
     * 记录证书监控结果
     * 
     * @param certificate 证书信息
     * @param daysUntilExpiry 距离过期的天数
     */
    void logMonitoringResult(Certificate certificate, int daysUntilExpiry);
    
    /**
     * 记录证书状态变更
     * 
     * @param certificate 证书信息
     * @param oldStatus 原状态
     * @param newStatus 新状态
     */
    void logStatusChange(Certificate certificate, CertificateStatus oldStatus, CertificateStatus newStatus);
}