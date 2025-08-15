package com.example.certificate.service.impl;

import com.example.certificate.config.CertificateStatusConfig;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.domain.repository.CertificateRepository;
import com.example.certificate.service.CertificateStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 证书状态服务实现类
 * 负责证书状态的计算和批量更新
 * 
 * @author Auto Generated
 */
@Service
public class CertificateStatusServiceImpl implements CertificateStatusService {
    
    private static final Logger logger = LoggerFactory.getLogger(CertificateStatusServiceImpl.class);
    
    @Resource
    private CertificateRepository certificateRepository;
    
    @Resource
    private CertificateStatusConfig certificateStatusConfig;
    
    @Override
    public CertificateStatus calculateStatus(Certificate certificate) {
        return calculateStatus(certificate, certificateStatusConfig.getExpiringSoonDays());
    }
    
    @Override
    public CertificateStatus calculateStatus(Certificate certificate, int expiringSoonThresholdDays) {
        if (certificate == null) {
            logger.warn("证书对象为空，无法计算状态");
            return CertificateStatus.NORMAL;
        }
        
        logger.debug("计算证书状态，证书ID: {}, 阈值: {} 天", certificate.getId(), expiringSoonThresholdDays);
        
        CertificateStatus status = certificate.calculateStatus(expiringSoonThresholdDays);
        logger.debug("证书状态计算完成，证书ID: {}, 状态: {}", certificate.getId(), status);
        
        return status;
    }
    
    @Override
    public List<Certificate> bulkCalculateStatus(List<Certificate> certificates) {
        return bulkCalculateStatus(certificates, certificateStatusConfig.getExpiringSoonDays());
    }
    
    @Override
    public List<Certificate> bulkCalculateStatus(List<Certificate> certificates, int expiringSoonThresholdDays) {
        if (certificates == null || certificates.isEmpty()) {
            logger.warn("证书列表为空，无法批量计算状态");
            return certificates;
        }
        
        logger.info("开始批量计算证书状态，证书数量: {}, 阈值: {} 天", certificates.size(), expiringSoonThresholdDays);
        
        List<Certificate> updatedCertificates = certificates.stream()
                .peek(certificate -> {
                    CertificateStatus oldStatus = certificate.getStatus();
                    certificate.updateStatus(expiringSoonThresholdDays);
                    CertificateStatus newStatus = certificate.getStatus();
                    
                    if (oldStatus != newStatus) {
                        logger.debug("证书状态已更新，证书ID: {}, 旧状态: {}, 新状态: {}", 
                                certificate.getId(), oldStatus, newStatus);
                    }
                })
                .collect(Collectors.toList());
        
        logger.info("批量计算证书状态完成，更新证书数量: {}", updatedCertificates.size());
        return updatedCertificates;
    }
    
    @Override
    public long getDaysUntilExpiry(Certificate certificate) {
        if (certificate == null) {
            logger.warn("证书对象为空，无法计算到期天数");
            return Long.MAX_VALUE;
        }
        
        long days = certificate.getDaysUntilExpiry();
        logger.debug("证书到期天数计算完成，证书ID: {}, 到期天数: {}", certificate.getId(), days);
        
        return days;
    }
    
    @Override
    @Transactional
    public int updateAllCertificateStatus() {
        return updateAllCertificateStatus(certificateStatusConfig.getExpiringSoonDays());
    }
    
    @Override
    @Transactional
    public int updateAllCertificateStatus(int expiringSoonThresholdDays) {
        logger.info("开始批量更新所有证书状态，阈值: {} 天", expiringSoonThresholdDays);
        
        // 分页获取所有证书，避免内存溢出
        int pageSize = certificateStatusConfig.getBatchSize();
        int pageNum = 1;
        int totalUpdated = 0;
        
        while (true) {
            List<Certificate> certificates = certificateRepository.selectAllPaged(pageNum, pageSize);
            if (certificates.isEmpty()) {
                break;
            }
            
            logger.debug("处理第 {} 页证书，数量: {}", pageNum, certificates.size());
            
            // 批量计算状态
            List<Certificate> updatedCertificates = bulkCalculateStatus(certificates, expiringSoonThresholdDays);
            
            // 批量更新到数据库
            for (Certificate certificate : updatedCertificates) {
                try {
                    certificateRepository.updateStatus(certificate.getId(), certificate.getStatus());
                    totalUpdated++;
                } catch (Exception e) {
                    logger.error("更新证书状态失败，证书ID: {}, 错误: {}", certificate.getId(), e.getMessage(), e);
                }
            }
            
            pageNum++;
        }
        
        logger.info("批量更新所有证书状态完成，总更新数量: {}", totalUpdated);
        return totalUpdated;
    }
}