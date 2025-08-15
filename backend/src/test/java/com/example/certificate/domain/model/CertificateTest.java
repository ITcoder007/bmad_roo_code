package com.example.certificate.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class CertificateTest {
    
    @Test
    @DisplayName("测试Certificate实体创建和属性访问")
    void testCertificateCreation() {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 30L * 24 * 60 * 60 * 1000);
        
        Certificate certificate = Certificate.builder()
                .id(1L)
                .name("测试证书")
                .domain("example.com")
                .issuer("Let's Encrypt")
                .issueDate(now)
                .expiryDate(expiryDate)
                .certificateType("SSL")
                .status(CertificateStatus.NORMAL)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        assertNotNull(certificate);
        assertEquals(1L, certificate.getId());
        assertEquals("测试证书", certificate.getName());
        assertEquals("example.com", certificate.getDomain());
        assertEquals("Let's Encrypt", certificate.getIssuer());
        assertEquals(now, certificate.getIssueDate());
        assertEquals(expiryDate, certificate.getExpiryDate());
        assertEquals("SSL", certificate.getCertificateType());
        assertEquals(CertificateStatus.NORMAL, certificate.getStatus());
        assertEquals(now, certificate.getCreatedAt());
        assertEquals(now, certificate.getUpdatedAt());
    }
    
    @Test
    @DisplayName("测试Certificate的NoArgsConstructor")
    void testNoArgsConstructor() {
        Certificate certificate = new Certificate();
        assertNotNull(certificate);
        assertNull(certificate.getId());
        assertNull(certificate.getName());
    }
    
    @Test
    @DisplayName("测试Certificate的AllArgsConstructor")
    void testAllArgsConstructor() {
        Date now = new Date();
        Certificate certificate = new Certificate(
                1L, "测试证书", "example.com", "Let's Encrypt",
                now, now, "SSL", CertificateStatus.NORMAL,
                now, now
        );
        
        assertNotNull(certificate);
        assertEquals(1L, certificate.getId());
        assertEquals("测试证书", certificate.getName());
    }
    
    @Test
    @DisplayName("测试Certificate的setter方法")
    void testSetters() {
        Certificate certificate = new Certificate();
        certificate.setId(2L);
        certificate.setName("更新的证书");
        certificate.setDomain("newdomain.com");
        
        assertEquals(2L, certificate.getId());
        assertEquals("更新的证书", certificate.getName());
        assertEquals("newdomain.com", certificate.getDomain());
    }
    
    @Test
    @DisplayName("测试CertificateStatus枚举")
    void testCertificateStatus() {
        assertEquals(3, CertificateStatus.values().length);
        assertEquals(CertificateStatus.NORMAL, CertificateStatus.valueOf("NORMAL"));
        assertEquals(CertificateStatus.EXPIRING_SOON, CertificateStatus.valueOf("EXPIRING_SOON"));
        assertEquals(CertificateStatus.EXPIRED, CertificateStatus.valueOf("EXPIRED"));
    }
}