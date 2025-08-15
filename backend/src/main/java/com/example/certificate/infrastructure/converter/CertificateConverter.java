package com.example.certificate.infrastructure.converter;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.infrastructure.persistence.entity.CertificateEntity;
import org.springframework.stereotype.Component;

/**
 * 证书对象转换器
 * 负责领域模型和数据库实体之间的转换
 */
@Component
public class CertificateConverter {
    
    /**
     * 将数据库实体转换为领域模型
     * @param entity 数据库实体
     * @return 领域模型
     */
    public Certificate toDomain(CertificateEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return Certificate.builder()
                .id(entity.getId())
                .name(entity.getName())
                .domain(entity.getDomain())
                .issuer(entity.getIssuer())
                .issueDate(entity.getIssueDate())
                .expiryDate(entity.getExpiryDate())
                .certificateType(entity.getCertificateType())
                .status(CertificateStatus.valueOf(entity.getStatus()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    /**
     * 将领域模型转换为数据库实体
     * @param certificate 领域模型
     * @return 数据库实体
     */
    public CertificateEntity toEntity(Certificate certificate) {
        if (certificate == null) {
            return null;
        }
        
        return CertificateEntity.builder()
                .id(certificate.getId())
                .name(certificate.getName())
                .domain(certificate.getDomain())
                .issuer(certificate.getIssuer())
                .issueDate(certificate.getIssueDate())
                .expiryDate(certificate.getExpiryDate())
                .certificateType(certificate.getCertificateType())
                .status(certificate.getStatus().name())
                .createdAt(certificate.getCreatedAt())
                .updatedAt(certificate.getUpdatedAt())
                .build();
    }
}