package com.example.certificate.infrastructure.repository;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.domain.repository.CertificateRepository;
import com.example.certificate.infrastructure.persistence.mapper.CertificateMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CertificateRepositoryTest {
    
    @Resource
    private CertificateRepository certificateRepository;
    
    @Resource
    private CertificateMapper certificateMapper;
    
    @Test
    @DisplayName("测试保存证书")
    void testSaveCertificate() {
        Certificate certificate = createTestCertificate();
        Certificate saved = certificateRepository.save(certificate);
        
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(certificate.getName(), saved.getName());
        assertEquals(certificate.getDomain(), saved.getDomain());
    }
    
    @Test
    @DisplayName("测试根据ID查找证书")
    void testFindById() {
        Certificate certificate = createTestCertificate();
        Certificate saved = certificateRepository.save(certificate);
        
        Optional<Certificate> found = certificateRepository.findById(saved.getId());
        
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals(saved.getName(), found.get().getName());
    }
    
    @Test
    @DisplayName("测试查找所有证书")
    void testFindAll() {
        Certificate cert1 = createTestCertificate();
        cert1.setDomain("test1.com");
        Certificate cert2 = createTestCertificate();
        cert2.setDomain("test2.com");
        
        certificateRepository.save(cert1);
        certificateRepository.save(cert2);
        
        List<Certificate> all = certificateRepository.findAll();
        
        assertNotNull(all);
        assertTrue(all.size() >= 2);
    }
    
    @Test
    @DisplayName("测试根据状态查找证书")
    void testFindByStatus() {
        // 创建正常状态的证书（远期过期）
        Certificate normal = createTestCertificate();
        normal.setDomain("normal.com");
        // 正常状态会自动计算为NORMAL（因为是1年后过期）
        
        // 创建已过期的证书
        Certificate expired = createTestCertificate();
        expired.setDomain("expired.com");
        // 设置一个已过期的日期
        Date pastDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L); // 昨天
        expired.setExpiryDate(pastDate);
        expired.updateStatus(); // 根据过期日期更新状态
        
        certificateRepository.save(normal);
        certificateRepository.save(expired);
        
        List<Certificate> normalCerts = certificateRepository.findByStatus(CertificateStatus.NORMAL);
        List<Certificate> expiredCerts = certificateRepository.findByStatus(CertificateStatus.EXPIRED);
        
        assertTrue(normalCerts.stream().anyMatch(c -> "normal.com".equals(c.getDomain())));
        assertTrue(expiredCerts.stream().anyMatch(c -> "expired.com".equals(c.getDomain())));
    }
    
    @Test
    @DisplayName("测试根据过期日期查找证书")
    void testFindByExpiryDateBefore() {
        Date now = new Date();
        Date futureDate = new Date(now.getTime() + 30L * 24 * 60 * 60 * 1000);
        Date pastDate = new Date(now.getTime() - 30L * 24 * 60 * 60 * 1000);
        
        Certificate future = createTestCertificate();
        future.setExpiryDate(futureDate);
        future.setDomain("future.com");
        
        Certificate past = createTestCertificate();
        past.setExpiryDate(pastDate);
        past.setDomain("past.com");
        
        certificateRepository.save(future);
        certificateRepository.save(past);
        
        List<Certificate> expiring = certificateRepository.findByExpiryDateBefore(now);
        
        assertTrue(expiring.stream().anyMatch(c -> "past.com".equals(c.getDomain())));
        assertFalse(expiring.stream().anyMatch(c -> "future.com".equals(c.getDomain())));
    }
    
    @Test
    @DisplayName("测试删除证书")
    void testDeleteById() {
        Certificate certificate = createTestCertificate();
        Certificate saved = certificateRepository.save(certificate);
        Long id = saved.getId();
        
        certificateRepository.deleteById(id);
        
        Optional<Certificate> found = certificateRepository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    @DisplayName("测试更新证书")
    void testUpdateCertificate() {
        Certificate certificate = createTestCertificate();
        Certificate saved = certificateRepository.save(certificate);
        
        // 设置即将到期的日期（15天后）
        Date soonExpiry = new Date(System.currentTimeMillis() + 15 * 24 * 60 * 60 * 1000L);
        saved.setName("更新后的证书");
        saved.setExpiryDate(soonExpiry);
        saved.updateStatus(); // 根据新的到期日期更新状态
        Certificate updated = certificateRepository.save(saved);
        
        assertEquals("更新后的证书", updated.getName());
        assertEquals(CertificateStatus.EXPIRING_SOON, updated.getStatus());
    }
    
    @Test
    @DisplayName("测试域名唯一性约束")
    void testDomainUniqueConstraint() {
        Certificate cert1 = createTestCertificate();
        cert1.setDomain("unique.com");
        certificateRepository.save(cert1);
        
        Certificate cert2 = createTestCertificate();
        cert2.setDomain("unique.com");
        
        assertThrows(Exception.class, () -> {
            certificateRepository.save(cert2);
        });
    }
    
    private Certificate createTestCertificate() {
        Date now = new Date();
        return Certificate.builder()
                .name("测试证书")
                .domain("test.example.com")
                .issuer("Test CA")
                .issueDate(now)
                .expiryDate(new Date(now.getTime() + 365L * 24 * 60 * 60 * 1000))
                .certificateType("SSL")
                .status(CertificateStatus.NORMAL)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}