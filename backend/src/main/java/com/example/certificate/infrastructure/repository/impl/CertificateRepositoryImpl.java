package com.example.certificate.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.domain.repository.CertificateRepository;
import com.example.certificate.infrastructure.persistence.entity.CertificateEntity;
import com.example.certificate.infrastructure.persistence.mapper.CertificateMapper;
import com.example.certificate.infrastructure.converter.CertificateConverter;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class CertificateRepositoryImpl implements CertificateRepository {
    
    @Resource
    private CertificateMapper certificateMapper;
    
    @Resource
    private CertificateConverter certificateConverter;
    
    @Override
    public Optional<Certificate> findById(Long id) {
        CertificateEntity entity = certificateMapper.selectById(id);
        return Optional.ofNullable(entity).map(certificateConverter::toDomain);
    }
    
    @Override
    public List<Certificate> findAll() {
        List<CertificateEntity> entities = certificateMapper.selectList(null);
        return entities.stream().map(certificateConverter::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public Certificate save(Certificate certificate) {
        // 在保存前自动更新状态
        certificate.updateStatus();
        
        CertificateEntity entity = certificateConverter.toEntity(certificate);
        if (certificate.getId() == null) {
            entity.setCreatedAt(new Date());
            entity.setUpdatedAt(new Date());
            certificateMapper.insert(entity);
        } else {
            entity.setUpdatedAt(new Date());
            certificateMapper.updateById(entity);
        }
        return certificateConverter.toDomain(entity);
    }
    
    @Override
    public void deleteById(Long id) {
        certificateMapper.deleteById(id);
    }
    
    @Override
    public List<Certificate> findByStatus(CertificateStatus status) {
        QueryWrapper<CertificateEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", status.name());
        List<CertificateEntity> entities = certificateMapper.selectList(wrapper);
        return entities.stream().map(certificateConverter::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<Certificate> findByExpiryDateBefore(Date date) {
        QueryWrapper<CertificateEntity> wrapper = new QueryWrapper<>();
        wrapper.lt("expiry_date", date);
        List<CertificateEntity> entities = certificateMapper.selectList(wrapper);
        return entities.stream().map(certificateConverter::toDomain).collect(Collectors.toList());
    }
}