package com.example.certificate.service;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;

/**
 * 证书监控服务接口
 * 定义证书监控的核心业务逻辑方法
 */
public interface MonitoringService {
    
    /**
     * 监控所有证书
     * 查询所有证书并对每个证书执行监控检查
     */
    void monitorAllCertificates();
    
    /**
     * 监控单个证书
     * 检查证书状态并在必要时更新状态
     * @param certificate 要监控的证书
     */
    void monitorCertificate(Certificate certificate);
    
    /**
     * 检查证书状态
     * 根据证书的到期时间计算当前状态
     * @param certificate 要检查的证书
     * @return 计算出的证书状态
     */
    CertificateStatus checkCertificateStatus(Certificate certificate);
    
    /**
     * 计算距离到期的天数
     * @param certificate 要计算的证书
     * @return 距离到期的天数，负数表示已过期
     */
    int calculateDaysUntilExpiry(Certificate certificate);
    
    /**
     * 使用配置化阈值检查证书状态
     * @param certificate 要检查的证书
     * @return 根据配置阈值计算的证书状态
     */
    CertificateStatus checkCertificateStatusWithConfig(Certificate certificate);
}