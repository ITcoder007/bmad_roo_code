package com.example.certificate.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.certificate.common.response.ApiResponse;
import com.example.certificate.domain.enums.CertificateStatus;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.infrastructure.persistence.mapper.CertificateMapper;
import com.example.certificate.service.dto.SystemStatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {
    
    private final CertificateMapper certificateMapper;
    
    /**
     * 获取系统状态
     */
    @GetMapping("/status")
    public ApiResponse<SystemStatusDto> getSystemStatus() {
        log.debug("获取系统状态");
        
        // 统计证书数量
        long total = certificateMapper.selectCount(null);
        long normal = certificateMapper.selectCount(
            new LambdaQueryWrapper<Certificate>()
                .eq(Certificate::getStatus, CertificateStatus.NORMAL)
        );
        long expiringSoon = certificateMapper.selectCount(
            new LambdaQueryWrapper<Certificate>()
                .eq(Certificate::getStatus, CertificateStatus.EXPIRING_SOON)
        );
        long expired = certificateMapper.selectCount(
            new LambdaQueryWrapper<Certificate>()
                .eq(Certificate::getStatus, CertificateStatus.EXPIRED)
        );
        
        SystemStatusDto status = SystemStatusDto.builder()
            .totalCertificates(total)
            .normalCertificates(normal)
            .expiringSoonCertificates(expiringSoon)
            .expiredCertificates(expired)
            .systemVersion("1.0.0")
            .systemStatus("RUNNING")
            .build();
        
        return ApiResponse.success(status);
    }
}