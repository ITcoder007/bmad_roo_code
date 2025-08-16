package com.example.certificate.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.certificate.controller.request.CertificateQueryRequest;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.service.dto.CertificateCreateDto;
import com.example.certificate.service.dto.CertificateDto;
import com.example.certificate.service.dto.CertificateUpdateDto;

/**
 * 证书服务接口
 * 定义证书管理的核心业务逻辑方法
 */
public interface CertificateService {
    
    /**
     * 创建证书
     * @param createDto 证书创建数据传输对象
     * @return 创建后的证书信息
     */
    CertificateDto createCertificate(CertificateCreateDto createDto);
    
    /**
     * 根据ID获取证书详情
     * @param id 证书ID
     * @return 证书详情信息
     */
    CertificateDto getCertificateById(Long id);
    
    /**
     * 更新证书信息
     * @param id 证书ID
     * @param updateDto 证书更新数据传输对象
     * @return 更新后的证书信息
     */
    CertificateDto updateCertificate(Long id, CertificateUpdateDto updateDto);
    
    /**
     * 删除证书
     * @param id 证书ID
     */
    void deleteCertificate(Long id);
    
    /**
     * 获取证书列表（分页）
     * @param pageNum 页码，从1开始
     * @param pageSize 每页大小
     * @param sortBy 排序字段
     * @param sortOrder 排序方向（ASC/DESC）
     * @return 分页的证书列表
     */
    Page<CertificateDto> getCertificateList(int pageNum, int pageSize, String sortBy, String sortOrder);
    
    /**
     * 根据条件查询证书列表（分页）
     * @param pageNum 页码，从1开始
     * @param pageSize 每页大小
     * @param sortBy 排序字段
     * @param sortOrder 排序方向（ASC/DESC）
     * @param status 证书状态筛选
     * @param domain 域名筛选（模糊搜索）
     * @param issuer 颁发机构筛选
     * @return 分页的证书列表
     */
    Page<CertificateDto> getCertificateListWithFilter(int pageNum, int pageSize, String sortBy, String sortOrder,
                                                      String status, String domain, String issuer);
    
    /**
     * 检查域名是否已存在
     * @param domain 域名
     * @return 是否存在
     */
    boolean isDomainExists(String domain);
    
    /**
     * 检查域名是否已存在（排除指定ID的证书）
     * @param domain 域名
     * @param excludeId 排除的证书ID
     * @return 是否存在
     */
    boolean isDomainExists(String domain, Long excludeId);
    
    /**
     * 根据查询请求查询证书列表（支持多状态筛选）
     * @param queryRequest 查询请求对象
     * @return 分页的证书列表
     */
    Page<CertificateDto> getCertificateListWithQuery(CertificateQueryRequest queryRequest);
    
    /**
     * 更新证书状态
     * @param id 证书ID
     * @param status 新的证书状态
     */
    void updateCertificateStatus(Long id, CertificateStatus status);
}