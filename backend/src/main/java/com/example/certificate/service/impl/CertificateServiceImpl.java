package com.example.certificate.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.certificate.common.exception.BusinessException;
import com.example.certificate.common.exception.ErrorCode;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.repository.CertificateRepository;
import com.example.certificate.service.CertificateService;
import com.example.certificate.service.converter.CertificateServiceConverter;
import com.example.certificate.service.dto.CertificateCreateDto;
import com.example.certificate.service.dto.CertificateDto;
import com.example.certificate.service.dto.CertificateUpdateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 证书服务实现类
 * 实现证书管理的核心业务逻辑
 */
@Service
@Transactional
public class CertificateServiceImpl implements CertificateService {
    
    private static final Logger logger = LoggerFactory.getLogger(CertificateServiceImpl.class);
    
    @Resource
    private CertificateRepository certificateRepository;
    
    @Resource
    private CertificateServiceConverter serviceConverter;
    
    @Override
    public CertificateDto createCertificate(CertificateCreateDto createDto) {
        logger.info("开始创建证书，域名: {}", createDto.getDomain());
        
        // 业务验证：检查域名是否已存在
        if (isDomainExists(createDto.getDomain())) {
            logger.warn("域名已存在: {}", createDto.getDomain());
            throw BusinessException.of(ErrorCode.CERTIFICATE_DOMAIN_EXISTS);
        }
        
        // 业务验证：检查日期逻辑
        if (createDto.getIssueDate().after(createDto.getExpiryDate())) {
            logger.warn("证书日期无效，颁发日期晚于到期日期: {} -> {}", 
                    createDto.getIssueDate(), createDto.getExpiryDate());
            throw BusinessException.of(ErrorCode.CERTIFICATE_INVALID_DATE, 
                    "颁发日期不能晚于到期日期");
        }
        
        try {
            // 转换并保存
            Certificate certificate = serviceConverter.toCreateDomain(createDto);
            Certificate savedCertificate = certificateRepository.save(certificate);
            
            logger.info("证书创建成功，ID: {}", savedCertificate.getId());
            return serviceConverter.toDto(savedCertificate);
        } catch (Exception e) {
            logger.error("证书保存失败", e);
            throw BusinessException.of(ErrorCode.CERTIFICATE_SAVE_FAILED, e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public CertificateDto getCertificateById(Long id) {
        logger.debug("查询证书详情，ID: {}", id);
        
        Optional<Certificate> certificateOpt = certificateRepository.findById(id);
        if (!certificateOpt.isPresent()) {
            logger.warn("证书不存在，ID: {}", id);
            throw BusinessException.of(ErrorCode.CERTIFICATE_NOT_FOUND);
        }
        
        return serviceConverter.toDto(certificateOpt.get());
    }
    
    @Override
    public CertificateDto updateCertificate(Long id, CertificateUpdateDto updateDto) {
        logger.info("开始更新证书，ID: {}", id);
        
        // 获取现有证书
        Optional<Certificate> existingOpt = certificateRepository.findById(id);
        if (!existingOpt.isPresent()) {
            logger.warn("证书不存在，ID: {}", id);
            throw BusinessException.of(ErrorCode.CERTIFICATE_NOT_FOUND);
        }
        
        Certificate existingCertificate = existingOpt.get();
        
        // 业务验证：如果更新域名，检查新域名是否已存在
        if (StringUtils.hasText(updateDto.getDomain()) && 
            !updateDto.getDomain().equals(existingCertificate.getDomain())) {
            if (isDomainExists(updateDto.getDomain(), id)) {
                logger.warn("域名已存在: {}", updateDto.getDomain());
                throw BusinessException.of(ErrorCode.CERTIFICATE_DOMAIN_EXISTS);
            }
        }
        
        // 业务验证：检查日期逻辑
        Date issueDate = updateDto.getIssueDate() != null ? 
                updateDto.getIssueDate() : existingCertificate.getIssueDate();
        Date expiryDate = updateDto.getExpiryDate() != null ? 
                updateDto.getExpiryDate() : existingCertificate.getExpiryDate();
        
        if (issueDate.after(expiryDate)) {
            logger.warn("证书日期无效，颁发日期晚于到期日期: {} -> {}", issueDate, expiryDate);
            throw BusinessException.of(ErrorCode.CERTIFICATE_INVALID_DATE, 
                    "颁发日期不能晚于到期日期");
        }
        
        try {
            // 合并更新并保存
            Certificate updatedCertificate = serviceConverter.mergeUpdateDto(existingCertificate, updateDto);
            Certificate savedCertificate = certificateRepository.save(updatedCertificate);
            
            logger.info("证书更新成功，ID: {}", savedCertificate.getId());
            return serviceConverter.toDto(savedCertificate);
        } catch (Exception e) {
            logger.error("证书更新失败", e);
            throw BusinessException.of(ErrorCode.CERTIFICATE_UPDATE_FAILED, e.getMessage());
        }
    }
    
    @Override
    public void deleteCertificate(Long id) {
        logger.info("开始删除证书，ID: {}", id);
        
        // 检查证书是否存在
        if (!certificateRepository.findById(id).isPresent()) {
            logger.warn("证书不存在，ID: {}", id);
            throw BusinessException.of(ErrorCode.CERTIFICATE_NOT_FOUND);
        }
        
        try {
            certificateRepository.deleteById(id);
            logger.info("证书删除成功，ID: {}", id);
        } catch (Exception e) {
            logger.error("证书删除失败", e);
            throw BusinessException.of(ErrorCode.CERTIFICATE_DELETE_FAILED, e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CertificateDto> getCertificateList(int pageNum, int pageSize, String sortBy, String sortOrder) {
        logger.debug("查询证书列表，页码: {}, 每页大小: {}", pageNum, pageSize);
        
        // 参数验证
        validatePageParameters(pageNum, pageSize);
        
        Page<Certificate> certificatePage = certificateRepository.findPage(pageNum, pageSize, sortBy, sortOrder);
        
        // 转换为 DTO 分页结果
        Page<CertificateDto> dtoPage = new Page<>(pageNum, pageSize);
        dtoPage.setTotal(certificatePage.getTotal());
        dtoPage.setRecords(certificatePage.getRecords().stream()
                .map(serviceConverter::toDto)
                .collect(Collectors.toList()));
        
        return dtoPage;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CertificateDto> getCertificateListWithFilter(int pageNum, int pageSize, String sortBy, String sortOrder,
                                                             String status, String domain, String issuer) {
        logger.debug("查询证书列表（带筛选），页码: {}, 每页大小: {}, 状态: {}, 域名: {}, 颁发机构: {}",
                pageNum, pageSize, status, domain, issuer);
        
        // 参数验证
        validatePageParameters(pageNum, pageSize);
        
        Page<Certificate> certificatePage = certificateRepository.findPageWithFilter(
                pageNum, pageSize, sortBy, sortOrder, status, domain, issuer);
        
        // 转换为 DTO 分页结果
        Page<CertificateDto> dtoPage = new Page<>(pageNum, pageSize);
        dtoPage.setTotal(certificatePage.getTotal());
        dtoPage.setRecords(certificatePage.getRecords().stream()
                .map(serviceConverter::toDto)
                .collect(Collectors.toList()));
        
        return dtoPage;
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isDomainExists(String domain) {
        return certificateRepository.findByDomain(domain).isPresent();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isDomainExists(String domain, Long excludeId) {
        return certificateRepository.findByDomainExcludeId(domain, excludeId).isPresent();
    }
    
    /**
     * 验证分页参数
     */
    private void validatePageParameters(int pageNum, int pageSize) {
        if (pageNum < 1) {
            throw BusinessException.of(ErrorCode.PAGE_PARAMETER_INVALID, "页码必须大于0");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw BusinessException.of(ErrorCode.PAGE_PARAMETER_INVALID, "每页大小必须在1-100之间");
        }
    }
}