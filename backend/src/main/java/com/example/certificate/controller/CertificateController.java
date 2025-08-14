package com.example.certificate.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.certificate.common.response.ApiResponse;
import com.example.certificate.common.response.PageResult;
import com.example.certificate.service.CertificateService;
import com.example.certificate.service.dto.CertificateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/certificates")
@RequiredArgsConstructor
public class CertificateController {
    
    private final CertificateService certificateService;
    
    /**
     * 分页查询证书列表
     */
    @GetMapping
    public ApiResponse<PageResult<CertificateDto>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        
        log.debug("查询证书列表: page={}, size={}, keyword={}, status={}", page, size, keyword, status);
        IPage<CertificateDto> result = certificateService.findPage(page, size, keyword, status);
        return ApiResponse.success(PageResult.of(result));
    }
    
    /**
     * 根据ID查询证书详情
     */
    @GetMapping("/{id}")
    public ApiResponse<CertificateDto> getById(@PathVariable Long id) {
        log.debug("查询证书详情: id={}", id);
        CertificateDto certificate = certificateService.findById(id);
        return ApiResponse.success(certificate);
    }
    
    /**
     * 创建证书
     */
    @PostMapping
    public ApiResponse<CertificateDto> create(@RequestBody @Validated CertificateDto dto) {
        log.info("创建证书: {}", dto.getName());
        CertificateDto certificate = certificateService.create(dto);
        return ApiResponse.success("证书创建成功", certificate);
    }
    
    /**
     * 更新证书
     */
    @PutMapping("/{id}")
    public ApiResponse<CertificateDto> update(@PathVariable Long id, 
                                              @RequestBody @Validated CertificateDto dto) {
        log.info("更新证书: id={}, name={}", id, dto.getName());
        CertificateDto certificate = certificateService.update(id, dto);
        return ApiResponse.success("证书更新成功", certificate);
    }
    
    /**
     * 删除证书
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        log.info("删除证书: id={}", id);
        certificateService.delete(id);
        return ApiResponse.success("证书删除成功", null);
    }
}