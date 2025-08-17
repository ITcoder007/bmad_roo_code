package com.example.certificate.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.certificate.controller.request.CertificateQueryRequest;
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
    
    /**
     * 根据查询请求分页查询证书列表（支持多状态筛选）
     * @param queryRequest 查询请求对象
     * @return 分页结果
     */
    Page<Certificate> findPageWithQuery(CertificateQueryRequest queryRequest);
    
    /**
     * 根据多个状态查找证书
     * @param statusList 状态列表
     * @return 证书列表
     */
    List<Certificate> findByStatusIn(List<CertificateStatus> statusList);
    
    /**
     * 分页获取所有证书（用于批量处理）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 证书列表
     */
    List<Certificate> selectAllPaged(int pageNum, int pageSize);
    
    /**
     * 更新证书状态
     * @param id 证书ID
     * @param status 新状态
     * @return 更新成功的行数
     */
    int updateStatus(Long id, CertificateStatus status);
    
    /**
     * 统计证书总数
     * @return 证书总数
     */
    long count();
    
    /**
     * 根据状态统计证书数量
     * @param status 证书状态
     * @return 指定状态的证书数量
     */
    long countByStatus(CertificateStatus status);
    
    /**
     * 查找即将过期的证书
     * @param thresholdDate 阈值日期
     * @return 即将过期的证书列表
     */
    List<Certificate> findExpiringCertificates(Date thresholdDate);
    
    /**
     * 查找最近添加的证书
     * @param limit 数量限制
     * @return 最近添加的证书列表
     */
    List<Certificate> findRecentCertificates(int limit);
}