package com.example.certificate.service.converter;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.service.dto.CertificateCreateDto;
import com.example.certificate.service.dto.CertificateDto;
import com.example.certificate.service.dto.CertificateUpdateDto;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 证书服务层转换器
 * 负责 DTO 和领域模型之间的转换
 */
@Component
public class CertificateServiceConverter {
    
    /**
     * 将创建 DTO 转换为领域模型
     * @param createDto 创建 DTO
     * @return 领域模型
     */
    public Certificate toCreateDomain(CertificateCreateDto createDto) {
        if (createDto == null) {
            return null;
        }
        
        Date now = new Date();
        return Certificate.builder()
                .name(createDto.getName())
                .domain(createDto.getDomain())
                .issuer(createDto.getIssuer())
                .issueDate(createDto.getIssueDate())
                .expiryDate(createDto.getExpiryDate())
                .certificateType(createDto.getCertificateType())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
    
    /**
     * 将领域模型转换为 DTO
     * @param certificate 领域模型
     * @return DTO
     */
    public CertificateDto toDto(Certificate certificate) {
        if (certificate == null) {
            return null;
        }
        
        // 确保状态是最新的
        certificate.updateStatus();
        
        return CertificateDto.builder()
                .id(certificate.getId())
                .name(certificate.getName())
                .domain(certificate.getDomain())
                .issuer(certificate.getIssuer())
                .issueDate(certificate.getIssueDate())
                .expiryDate(certificate.getExpiryDate())
                .certificateType(certificate.getCertificateType())
                .status(certificate.getStatus() != null ? certificate.getStatus().name() : null)
                .daysUntilExpiry(certificate.getDaysUntilExpiry())
                .createdAt(certificate.getCreatedAt())
                .updatedAt(certificate.getUpdatedAt())
                .build();
    }
    
    /**
     * 将领域模型转换为 DTO（使用自定义阈值）
     * @param certificate 领域模型
     * @param expiringSoonThresholdDays 即将过期的阈值天数
     * @return DTO
     */
    public CertificateDto toDto(Certificate certificate, int expiringSoonThresholdDays) {
        if (certificate == null) {
            return null;
        }
        
        // 使用自定义阈值更新状态
        certificate.updateStatus(expiringSoonThresholdDays);
        
        return CertificateDto.builder()
                .id(certificate.getId())
                .name(certificate.getName())
                .domain(certificate.getDomain())
                .issuer(certificate.getIssuer())
                .issueDate(certificate.getIssueDate())
                .expiryDate(certificate.getExpiryDate())
                .certificateType(certificate.getCertificateType())
                .status(certificate.getStatus() != null ? certificate.getStatus().name() : null)
                .daysUntilExpiry(certificate.getDaysUntilExpiry())
                .createdAt(certificate.getCreatedAt())
                .updatedAt(certificate.getUpdatedAt())
                .build();
    }
    
    /**
     * 将更新 DTO 合并到现有的领域模型
     * @param existingCertificate 现有的领域模型
     * @param updateDto 更新 DTO
     * @return 更新后的领域模型
     */
    public Certificate mergeUpdateDto(Certificate existingCertificate, CertificateUpdateDto updateDto) {
        if (existingCertificate == null || updateDto == null) {
            return existingCertificate;
        }
        
        return Certificate.builder()
                .id(existingCertificate.getId())
                .name(updateDto.getName() != null ? updateDto.getName() : existingCertificate.getName())
                .domain(updateDto.getDomain() != null ? updateDto.getDomain() : existingCertificate.getDomain())
                .issuer(updateDto.getIssuer() != null ? updateDto.getIssuer() : existingCertificate.getIssuer())
                .issueDate(updateDto.getIssueDate() != null ? updateDto.getIssueDate() : existingCertificate.getIssueDate())
                .expiryDate(updateDto.getExpiryDate() != null ? updateDto.getExpiryDate() : existingCertificate.getExpiryDate())
                .certificateType(updateDto.getCertificateType() != null ? updateDto.getCertificateType() : existingCertificate.getCertificateType())
                .status(existingCertificate.getStatus())
                .createdAt(existingCertificate.getCreatedAt())
                .updatedAt(new Date())
                .build();
    }
}