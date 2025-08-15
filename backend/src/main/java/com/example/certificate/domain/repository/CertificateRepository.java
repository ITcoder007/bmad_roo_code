package com.example.certificate.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface CertificateRepository {
    
    Optional<Certificate> findById(Long id);
    
    List<Certificate> findAll();
    
    Certificate save(Certificate certificate);
    
    void deleteById(Long id);
    
    List<Certificate> findByStatus(CertificateStatus status);
    
    List<Certificate> findByExpiryDateBefore(Date date);
    
    /**
     * 分页查询证书列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param sortBy 排序字段
     * @param sortOrder 排序方向
     * @return 分页结果
     */
    Page<Certificate> findPage(int pageNum, int pageSize, String sortBy, String sortOrder);
    
    /**
     * 根据条件分页查询证书列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param sortBy 排序字段
     * @param sortOrder 排序方向
     * @param status 状态筛选
     * @param domain 域名筛选
     * @param issuer 颁发机构筛选
     * @return 分页结果
     */
    Page<Certificate> findPageWithFilter(int pageNum, int pageSize, String sortBy, String sortOrder,
                                         String status, String domain, String issuer);
    
    /**
     * 根据域名查找证书
     * @param domain 域名
     * @return 证书信息
     */
    Optional<Certificate> findByDomain(String domain);
    
    /**
     * 根据域名查找证书（排除指定ID）
     * @param domain 域名
     * @param excludeId 排除的证书ID
     * @return 证书信息
     */
    Optional<Certificate> findByDomainExcludeId(String domain, Long excludeId);
}