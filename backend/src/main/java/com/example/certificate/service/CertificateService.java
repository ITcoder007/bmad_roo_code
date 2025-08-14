package com.example.certificate.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.certificate.service.dto.CertificateDto;

public interface CertificateService {
    
    /**
     * 分页查询证书列表
     */
    IPage<CertificateDto> findPage(int page, int size, String keyword, String status);
    
    /**
     * 根据ID查询证书
     */
    CertificateDto findById(Long id);
    
    /**
     * 创建证书
     */
    CertificateDto create(CertificateDto dto);
    
    /**
     * 更新证书
     */
    CertificateDto update(Long id, CertificateDto dto);
    
    /**
     * 删除证书
     */
    void delete(Long id);
    
    /**
     * 更新所有证书状态
     */
    void updateAllCertificateStatus();
}