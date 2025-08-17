package com.example.certificate.service.impl;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.service.dto.EmailResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EmailServiceImpl 单元测试
 * 测试生产环境邮件服务实现
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("生产环境邮件服务测试")
class EmailServiceTest {
    
    @InjectMocks
    private EmailServiceImpl emailService;
    
    private Certificate testCertificate;
    private String testRecipient;
    
    @BeforeEach
    void setUp() {
        // 创建测试用证书
        testCertificate = Certificate.builder()
                .id(1L)
                .name("生产证书")
                .domain("production.example.com")
                .issuer("Production CA")
                .issueDate(new Date(System.currentTimeMillis() - 86400000L * 30)) // 30天前颁发
                .expiryDate(new Date(System.currentTimeMillis() + 86400000L * 10)) // 10天后过期
                .certificateType("SSL")
                .status(CertificateStatus.EXPIRING_SOON)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        
        testRecipient = "production@example.com";
    }
    
    @Test
    @DisplayName("发送过期预警邮件 - 返回未实现错误")
    void testSendExpiryAlertEmail_NotImplemented() {
        // Given
        int daysUntilExpiry = 10;
        
        // When
        EmailResult result = emailService.sendExpiryAlertEmail(testCertificate, daysUntilExpiry, testRecipient);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("NOT_IMPLEMENTED", result.getErrorCode());
        assertEquals(testRecipient, result.getRecipient());
        assertTrue(result.getErrorMessage().contains("生产环境邮件服务尚未实现SMTP发送功能"));
    }
    
    @Test
    @DisplayName("发送每日摘要邮件 - 返回未实现错误")
    void testSendDailySummary_NotImplemented() {
        // Given
        List<Certificate> expiringSoonCertificates = Arrays.asList(testCertificate);
        List<Certificate> expiredCertificates = Arrays.asList();
        
        // When
        EmailResult result = emailService.sendDailySummary(expiringSoonCertificates, expiredCertificates, testRecipient);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("NOT_IMPLEMENTED", result.getErrorCode());
        assertEquals(testRecipient, result.getRecipient());
        assertTrue(result.getErrorMessage().contains("生产环境邮件服务尚未实现SMTP发送功能"));
    }
    
    @Test
    @DisplayName("批量发送邮件预警 - 所有都返回未实现错误")
    void testSendBatchAlerts_AllNotImplemented() {
        // Given
        List<Certificate> certificates = Arrays.asList(
                createCertificate(1L, "证书1", "site1.example.com", 15),
                createCertificate(2L, "证书2", "site2.example.com", 7),
                createCertificate(3L, "证书3", "site3.example.com", 1)
        );
        
        // When
        List<EmailResult> results = emailService.sendBatchAlerts(certificates, testRecipient);
        
        // Then
        assertNotNull(results);
        assertEquals(3, results.size());
        
        for (EmailResult result : results) {
            assertFalse(result.isSuccess());
            assertEquals("NOT_IMPLEMENTED", result.getErrorCode());
            assertEquals(testRecipient, result.getRecipient());
        }
    }
    
    @Test
    @DisplayName("批量发送邮件预警 - 空列表")
    void testSendBatchAlerts_EmptyList() {
        // Given
        List<Certificate> certificates = Arrays.asList();
        
        // When
        List<EmailResult> results = emailService.sendBatchAlerts(certificates, testRecipient);
        
        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
    
    @Test
    @DisplayName("测试不同剩余天数的证书处理")
    void testDifferentDaysUntilExpiry() {
        // Given
        int[] testDays = {30, 15, 7, 1, 0, -5};
        
        for (int days : testDays) {
            // When
            EmailResult result = emailService.sendExpiryAlertEmail(testCertificate, days, testRecipient);
            
            // Then
            assertNotNull(result, "结果不应为null，测试天数: " + days);
            assertFalse(result.isSuccess(), "生产环境服务应返回失败，测试天数: " + days);
            assertEquals("NOT_IMPLEMENTED", result.getErrorCode(), "错误代码应为NOT_IMPLEMENTED，测试天数: " + days);
        }
    }
    
    /**
     * 创建测试证书对象的辅助方法
     */
    private Certificate createCertificate(Long id, String name, String domain, int daysUntilExpiry) {
        long currentTime = System.currentTimeMillis();
        Date expiryDate = new Date(currentTime + daysUntilExpiry * 86400000L);
        
        CertificateStatus status;
        if (daysUntilExpiry <= 0) {
            status = CertificateStatus.EXPIRED;
        } else if (daysUntilExpiry <= 30) {
            status = CertificateStatus.EXPIRING_SOON;
        } else {
            status = CertificateStatus.NORMAL;
        }
        
        return Certificate.builder()
                .id(id)
                .name(name)
                .domain(domain)
                .issuer("Test CA")
                .issueDate(new Date(currentTime - 86400000L * 30))
                .expiryDate(expiryDate)
                .certificateType("SSL")
                .status(status)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }
}