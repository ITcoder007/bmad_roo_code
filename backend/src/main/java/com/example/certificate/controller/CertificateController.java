package com.example.certificate.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.certificate.common.response.ApiResponse;
import com.example.certificate.service.CertificateService;
import com.example.certificate.service.dto.CertificateCreateDto;
import com.example.certificate.service.dto.CertificateDto;
import com.example.certificate.service.dto.CertificateUpdateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 证书管理控制器
 * 提供证书管理的 REST API 接口
 */
@RestController
@RequestMapping("/v1/certificates")
public class CertificateController {
    
    private static final Logger logger = LoggerFactory.getLogger(CertificateController.class);
    
    @Resource
    private CertificateService certificateService;
    
    /**
     * 创建新证书
     * @param createDto 证书创建数据
     * @return 创建后的证书信息
     */
    @PostMapping
    public ApiResponse<CertificateDto> createCertificate(@Valid @RequestBody CertificateCreateDto createDto) {
        logger.info("接收证书创建请求，域名: {}", createDto.getDomain());
        CertificateDto certificate = certificateService.createCertificate(createDto);
        return ApiResponse.success("证书创建成功", certificate);
    }
    
    /**
     * 获取仪表板统计信息
     * @return 统计信息
     */
    @GetMapping("/dashboard/stats")
    public ApiResponse<Map<String, Object>> getDashboardStats() {
        logger.debug("接收仪表板统计查询请求");
        try {
            Map<String, Object> stats = certificateService.getDashboardStatistics();
            return ApiResponse.success(stats);
        } catch (Exception e) {
            logger.error("获取仪表板统计失败", e);
            return ApiResponse.error("获取统计信息失败");
        }
    }
    
    /**
     * 获取即将过期的证书
     * @param days 天数阈值，默认7天
     * @return 即将过期的证书列表
     */
    @GetMapping("/expiring")
    public ApiResponse<List<CertificateDto>> getExpiringCertificates(
            @RequestParam(defaultValue = "7") int days) {
        logger.debug("接收即将过期证书查询请求，天数阈值: {}", days);
        try {
            List<CertificateDto> expiringCertificates = certificateService.getExpiringCertificates(days);
            return ApiResponse.success(expiringCertificates);
        } catch (Exception e) {
            logger.error("获取即将过期证书失败", e);
            return ApiResponse.error("获取即将过期证书失败");
        }
    }
    
    /**
     * 获取最近添加的证书
     * @param limit 数量限制，默认5个
     * @return 最近添加的证书列表
     */
    @GetMapping("/recent")
    public ApiResponse<List<CertificateDto>> getRecentCertificates(
            @RequestParam(defaultValue = "5") int limit) {
        logger.debug("接收最近证书查询请求，数量限制: {}", limit);
        try {
            List<CertificateDto> recentCertificates = certificateService.getRecentCertificates(limit);
            return ApiResponse.success(recentCertificates);
        } catch (Exception e) {
            logger.error("获取最近证书失败", e);
            return ApiResponse.error("获取最近证书失败");
        }
    }
    
    /**
     * 根据条件获取证书列表（分页）
     * @param pageNum 页码，默认1
     * @param pageSize 每页大小，默认10
     * @param sortBy 排序字段，默认创建时间
     * @param sortOrder 排序方向，默认降序
     * @param status 证书状态筛选
     * @param domain 域名筛选（模糊搜索）
     * @param issuer 颁发机构筛选
     * @return 分页的证书列表
     */
    @GetMapping("/search")
    public ApiResponse<Page<CertificateDto>> searchCertificates(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortOrder,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String issuer) {
        
        logger.debug("接收证书搜索请求，页码: {}, 每页大小: {}, 状态: {}, 域名: {}, 颁发机构: {}",
                pageNum, pageSize, status, domain, issuer);
        
        Page<CertificateDto> certificatePage = certificateService.getCertificateListWithFilter(
                pageNum, pageSize, sortBy, sortOrder, status, domain, issuer);
        return ApiResponse.success(certificatePage);
    }
    
    /**
     * 获取证书详情
     * @param id 证书ID
     * @return 证书详情信息
     */
    @GetMapping("/{id}")
    public ApiResponse<CertificateDto> getCertificate(@PathVariable Long id) {
        logger.debug("接收证书查询请求，ID: {}", id);
        CertificateDto certificate = certificateService.getCertificateById(id);
        return ApiResponse.success(certificate);
    }
    
    /**
     * 获取证书列表（分页）
     * @param pageNum 页码，默认1
     * @param pageSize 每页大小，默认10
     * @param sortBy 排序字段，默认创建时间
     * @param sortOrder 排序方向，默认降序
     * @return 分页的证书列表
     */
    @GetMapping
    public ApiResponse<Page<CertificateDto>> getCertificateList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortOrder) {
        
        logger.debug("接收证书列表查询请求，页码: {}, 每页大小: {}", pageNum, pageSize);
        Page<CertificateDto> certificatePage = certificateService.getCertificateList(pageNum, pageSize, sortBy, sortOrder);
        return ApiResponse.success(certificatePage);
    }
    
    /**
     * 更新证书信息
     * @param id 证书ID
     * @param updateDto 证书更新数据
     * @return 更新后的证书信息
     */
    @PutMapping("/{id}")
    public ApiResponse<CertificateDto> updateCertificate(@PathVariable Long id, 
                                                         @Valid @RequestBody CertificateUpdateDto updateDto) {
        logger.info("接收证书更新请求，ID: {}", id);
        CertificateDto certificate = certificateService.updateCertificate(id, updateDto);
        return ApiResponse.success("证书更新成功", certificate);
    }
    
    /**
     * 删除证书
     * @param id 证书ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCertificate(@PathVariable Long id) {
        logger.info("接收证书删除请求，ID: {}", id);
        certificateService.deleteCertificate(id);
        return ApiResponse.success("证书删除成功");
    }
    
}