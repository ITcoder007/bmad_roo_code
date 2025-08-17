package com.example.certificate.service;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;

import java.util.List;

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
    
    /**
     * 记录邮件预警日志
     * 
     * @param certificate 证书信息
     * @param daysUntilExpiry 距离过期的天数
     * @param recipient 收件人邮箱
     */
    void logEmailAlert(Certificate certificate, int daysUntilExpiry, String recipient);
    
    /**
     * 记录每日摘要日志
     * 
     * @param expiringSoonCertificates 即将过期的证书列表
     * @param expiredCertificates 已过期的证书列表
     * @param recipient 收件人邮箱
     */
    void logDailySummary(List<Certificate> expiringSoonCertificates, 
                        List<Certificate> expiredCertificates, 
                        String recipient);
    
    /**
     * 记录短信预警日志
     * 
     * @param certificate 证书信息
     * @param daysUntilExpiry 距离过期的天数
     * @param recipient 收件人手机号
     */
    void logSmsAlert(Certificate certificate, int daysUntilExpiry, String recipient);
    
    /**
     * 记录短信每日摘要日志
     * 
     * @param expiringSoonCertificates 即将过期的证书列表
     * @param expiredCertificates 已过期的证书列表
     * @param recipient 收件人手机号
     */
    void logSmsDailySummary(List<Certificate> expiringSoonCertificates, 
                           List<Certificate> expiredCertificates, 
                           String recipient);
}