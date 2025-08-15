package com.example.certificate.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.domain.repository.CertificateRepository;
import com.example.certificate.infrastructure.persistence.entity.CertificateEntity;
import com.example.certificate.infrastructure.persistence.mapper.CertificateMapper;
import org.springframework.beans.BeanUtils;
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
    
    @Override
    public Optional<Certificate> findById(Long id) {
        CertificateEntity entity = certificateMapper.selectById(id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }
    
    @Override
    public List<Certificate> findAll() {
        List<CertificateEntity> entities = certificateMapper.selectList(null);
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public Certificate save(Certificate certificate) {
        CertificateEntity entity = toEntity(certificate);
        if (certificate.getId() == null) {
            certificateMapper.insert(entity);
        } else {
            certificateMapper.updateById(entity);
        }
        return toDomain(entity);
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
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<Certificate> findByExpiryDateBefore(Date date) {
        QueryWrapper<CertificateEntity> wrapper = new QueryWrapper<>();
        wrapper.lt("expiry_date", date);
        List<CertificateEntity> entities = certificateMapper.selectList(wrapper);
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }
    
    private Certificate toDomain(CertificateEntity entity) {
        Certificate certificate = new Certificate();
        BeanUtils.copyProperties(entity, certificate);
        certificate.setStatus(CertificateStatus.valueOf(entity.getStatus()));
        return certificate;
    }
    
    private CertificateEntity toEntity(Certificate certificate) {
        CertificateEntity entity = new CertificateEntity();
        BeanUtils.copyProperties(certificate, entity);
        entity.setStatus(certificate.getStatus().name());
        return entity;
    }
}