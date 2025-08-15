package com.example.certificate.service;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;

import java.util.List;

/**
 * 证书状态服务接口
 * 
 * @author Auto Generated
 */
public interface CertificateStatusService {
    
    /**
     * 计算单个证书的状态
     * 
     * @param certificate 证书对象
     * @return 计算后的证书状态
     */
    CertificateStatus calculateStatus(Certificate certificate);
    
    /**
     * 计算单个证书的状态（使用自定义阈值）
     * 
     * @param certificate 证书对象
     * @param expiringSoonThresholdDays 即将过期的阈值天数
     * @return 计算后的证书状态
     */
    CertificateStatus calculateStatus(Certificate certificate, int expiringSoonThresholdDays);
    
    /**
     * 批量计算证书状态
     * 
     * @param certificates 证书列表
     * @return 更新状态后的证书列表
     */
    List<Certificate> bulkCalculateStatus(List<Certificate> certificates);
    
    /**
     * 批量计算证书状态（使用自定义阈值）
     * 
     * @param certificates 证书列表
     * @param expiringSoonThresholdDays 即将过期的阈值天数
     * @return 更新状态后的证书列表
     */
    List<Certificate> bulkCalculateStatus(List<Certificate> certificates, int expiringSoonThresholdDays);
    
    /**
     * 获取证书距离到期的天数
     * 
     * @param certificate 证书对象
     * @return 距离到期的天数，负数表示已过期
     */
    long getDaysUntilExpiry(Certificate certificate);
    
    /**
     * 批量更新所有证书的状态
     * 
     * @return 更新的证书数量
     */
    int updateAllCertificateStatus();
    
    /**
     * 批量更新所有证书的状态（使用自定义阈值）
     * 
     * @param expiringSoonThresholdDays 即将过期的阈值天数
     * @return 更新的证书数量
     */
    int updateAllCertificateStatus(int expiringSoonThresholdDays);
}