package com.example.certificate.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.certificate.controller.request.CertificateQueryRequest;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.domain.repository.CertificateRepository;
import com.example.certificate.infrastructure.persistence.entity.CertificateEntity;
import com.example.certificate.infrastructure.persistence.mapper.CertificateMapper;
import com.example.certificate.infrastructure.converter.CertificateConverter;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
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
    
    @Override
    public Page<Certificate> findPage(int pageNum, int pageSize, String sortBy, String sortOrder) {
        Page<CertificateEntity> page = new Page<>(pageNum, pageSize);
        
        // 设置排序
        if (StringUtils.hasText(sortBy)) {
            OrderItem orderItem = new OrderItem();
            orderItem.setColumn(convertSortField(sortBy));
            orderItem.setAsc(!"DESC".equalsIgnoreCase(sortOrder));
            page.addOrder(orderItem);
        }
        
        Page<CertificateEntity> entityPage = certificateMapper.selectPage(page, null);
        
        // 转换为 Domain 对象的分页结果
        Page<Certificate> domainPage = new Page<>(pageNum, pageSize);
        domainPage.setTotal(entityPage.getTotal());
        domainPage.setRecords(entityPage.getRecords().stream()
                .map(certificateConverter::toDomain)
                .collect(Collectors.toList()));
        
        return domainPage;
    }
    
    @Override
    public Page<Certificate> findPageWithFilter(int pageNum, int pageSize, String sortBy, String sortOrder,
                                                String status, String domain, String issuer) {
        Page<CertificateEntity> page = new Page<>(pageNum, pageSize);
        
        // 设置排序
        if (StringUtils.hasText(sortBy)) {
            OrderItem orderItem = new OrderItem();
            orderItem.setColumn(convertSortField(sortBy));
            orderItem.setAsc(!"DESC".equalsIgnoreCase(sortOrder));
            page.addOrder(orderItem);
        }
        
        // 构建查询条件
        QueryWrapper<CertificateEntity> wrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }
        
        if (StringUtils.hasText(domain)) {
            wrapper.like("domain", domain);
        }
        
        if (StringUtils.hasText(issuer)) {
            wrapper.like("issuer", issuer);
        }
        
        Page<CertificateEntity> entityPage = certificateMapper.selectPage(page, wrapper);
        
        // 转换为 Domain 对象的分页结果
        Page<Certificate> domainPage = new Page<>(pageNum, pageSize);
        domainPage.setTotal(entityPage.getTotal());
        domainPage.setRecords(entityPage.getRecords().stream()
                .map(certificateConverter::toDomain)
                .collect(Collectors.toList()));
        
        return domainPage;
    }
    
    @Override
    public Optional<Certificate> findByDomain(String domain) {
        QueryWrapper<CertificateEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("domain", domain);
        CertificateEntity entity = certificateMapper.selectOne(wrapper);
        return Optional.ofNullable(entity).map(certificateConverter::toDomain);
    }
    
    @Override
    public Optional<Certificate> findByDomainExcludeId(String domain, Long excludeId) {
        QueryWrapper<CertificateEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("domain", domain)
               .ne("id", excludeId);
        CertificateEntity entity = certificateMapper.selectOne(wrapper);
        return Optional.ofNullable(entity).map(certificateConverter::toDomain);
    }
    
    @Override
    public Page<Certificate> findPageWithQuery(CertificateQueryRequest queryRequest) {
        Page<CertificateEntity> page = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize());
        
        // 设置排序
        if (StringUtils.hasText(queryRequest.getSortBy())) {
            OrderItem orderItem = new OrderItem();
            orderItem.setColumn(convertSortField(queryRequest.getSortBy()));
            orderItem.setAsc(!"DESC".equalsIgnoreCase(queryRequest.getSortOrder()));
            page.addOrder(orderItem);
        }
        
        // 构建查询条件
        QueryWrapper<CertificateEntity> wrapper = new QueryWrapper<>();
        
        // 状态筛选（支持多状态）
        List<String> effectiveStatusList = queryRequest.getEffectiveStatusList();
        if (effectiveStatusList != null && !effectiveStatusList.isEmpty()) {
            wrapper.in("status", effectiveStatusList);
        }
        
        // 域名筛选
        if (StringUtils.hasText(queryRequest.getDomain())) {
            wrapper.like("domain", queryRequest.getDomain());
        }
        
        // 颁发机构筛选
        if (StringUtils.hasText(queryRequest.getIssuer())) {
            wrapper.like("issuer", queryRequest.getIssuer());
        }
        
        // 到期日期范围筛选
        if (queryRequest.getExpiryDateFrom() != null) {
            wrapper.ge("expiry_date", queryRequest.getExpiryDateFrom());
        }
        if (queryRequest.getExpiryDateTo() != null) {
            wrapper.le("expiry_date", queryRequest.getExpiryDateTo());
        }
        
        Page<CertificateEntity> entityPage = certificateMapper.selectPage(page, wrapper);
        
        // 转换为 Domain 对象的分页结果
        Page<Certificate> domainPage = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize());
        domainPage.setTotal(entityPage.getTotal());
        domainPage.setRecords(entityPage.getRecords().stream()
                .map(certificateConverter::toDomain)
                .collect(Collectors.toList()));
        
        return domainPage;
    }
    
    @Override
    public List<Certificate> findByStatusIn(List<CertificateStatus> statusList) {
        if (statusList == null || statusList.isEmpty()) {
            return Collections.emptyList();
        }
        
        QueryWrapper<CertificateEntity> wrapper = new QueryWrapper<>();
        List<String> statusNames = statusList.stream()
                .map(CertificateStatus::name)
                .collect(Collectors.toList());
        wrapper.in("status", statusNames);
        
        List<CertificateEntity> entities = certificateMapper.selectList(wrapper);
        return entities.stream().map(certificateConverter::toDomain).collect(Collectors.toList());
    }
    
    @Override
    public List<Certificate> selectAllPaged(int pageNum, int pageSize) {
        Page<CertificateEntity> page = new Page<>(pageNum, pageSize);
        Page<CertificateEntity> entityPage = certificateMapper.selectPage(page, null);
        return entityPage.getRecords().stream()
                .map(certificateConverter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public int updateStatus(Long id, CertificateStatus status) {
        UpdateWrapper<CertificateEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                     .set("status", status.name())
                     .set("updated_at", new Date());
        return certificateMapper.update(null, updateWrapper);
    }
    
    /**
     * 转换排序字段名
     */
    private String convertSortField(String sortBy) {
        switch (sortBy) {
            case "name":
                return "name";
            case "domain":
                return "domain";
            case "expiryDate":
                return "expiry_date";
            case "createdAt":
                return "created_at";
            default:
                return "created_at";
        }
    }
}