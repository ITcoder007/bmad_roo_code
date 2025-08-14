package com.example.certificate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.certificate.common.exception.BusinessException;
import com.example.certificate.domain.enums.CertificateStatus;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.infrastructure.persistence.mapper.CertificateMapper;
import com.example.certificate.service.CertificateService;
import com.example.certificate.service.MonitoringLogService;
import com.example.certificate.service.dto.CertificateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {
    
    private final CertificateMapper certificateMapper;
    private final MonitoringLogService monitoringLogService;
    
    @Override
    public IPage<CertificateDto> findPage(int page, int size, String keyword, String status) {
        Page<Certificate> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Certificate::getName, keyword)
                    .or().like(Certificate::getDomain, keyword));
        }
        
        if (StringUtils.hasText(status)) {
            try {
                CertificateStatus certificateStatus = CertificateStatus.valueOf(status);
                wrapper.eq(Certificate::getStatus, certificateStatus);
            } catch (IllegalArgumentException e) {
                log.warn("无效的状态参数: {}", status);
            }
        }
        
        wrapper.orderByAsc(Certificate::getExpiryDate);
        
        IPage<Certificate> result = certificateMapper.selectPage(pageParam, wrapper);
        
        return result.convert(this::toDto);
    }
    
    @Override
    public CertificateDto findById(Long id) {
        Certificate certificate = certificateMapper.selectById(id);
        if (certificate == null) {
            throw new BusinessException("证书不存在");
        }
        return toDto(certificate);
    }
    
    @Override
    @Transactional
    public CertificateDto create(CertificateDto dto) {
        Certificate certificate = new Certificate();
        BeanUtils.copyProperties(dto, certificate, "id", "createdAt", "updatedAt");
        certificate.updateStatus();
        
        certificateMapper.insert(certificate);
        
        monitoringLogService.logCertificateCreated(certificate);
        
        return toDto(certificate);
    }
    
    @Override
    @Transactional
    public CertificateDto update(Long id, CertificateDto dto) {
        Certificate certificate = certificateMapper.selectById(id);
        if (certificate == null) {
            throw new BusinessException("证书不存在");
        }
        
        BeanUtils.copyProperties(dto, certificate, "id", "createdAt", "updatedAt");
        certificate.updateStatus();
        
        certificateMapper.updateById(certificate);
        
        monitoringLogService.logCertificateUpdated(certificate);
        
        return toDto(certificate);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        Certificate certificate = certificateMapper.selectById(id);
        if (certificate == null) {
            throw new BusinessException("证书不存在");
        }
        
        certificateMapper.deleteById(id);
        
        monitoringLogService.logCertificateDeleted(certificate);
    }
    
    @Override
    @Transactional
    public void updateAllCertificateStatus() {
        List<Certificate> certificates = certificateMapper.selectList(null);
        
        for (Certificate certificate : certificates) {
            CertificateStatus oldStatus = certificate.getStatus();
            certificate.updateStatus();
            
            if (oldStatus != certificate.getStatus()) {
                certificateMapper.updateById(certificate);
                log.info("证书 {} 状态从 {} 更新为 {}", 
                    certificate.getName(), oldStatus, certificate.getStatus());
            }
        }
    }
    
    private CertificateDto toDto(Certificate certificate) {
        CertificateDto dto = new CertificateDto();
        BeanUtils.copyProperties(certificate, dto);
        dto.setDaysUntilExpiry(certificate.getDaysUntilExpiry());
        return dto;
    }
}