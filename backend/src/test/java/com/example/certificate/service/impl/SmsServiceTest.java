package com.example.certificate.service.impl;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.service.dto.SmsResult;
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
 * SmsServiceImpl 单元测试
 * 测试生产环境短信服务实现
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("生产环境短信服务测试")
class SmsServiceTest {
    
    @InjectMocks
    private SmsServiceImpl smsService;
    
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
        
        testRecipient = "13800138000";
    }
    
    @Test
    @DisplayName("发送过期预警短信 - 返回未实现错误")
    void testSendExpiryAlertSms_NotImplemented() {
        // Given
        int daysUntilExpiry = 10;
        
        // When
        SmsResult result = smsService.sendExpiryAlertSms(testCertificate, daysUntilExpiry, testRecipient);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("NOT_IMPLEMENTED", result.getErrorCode());
        assertEquals(testRecipient, result.getRecipient());
        assertTrue(result.getErrorMessage().contains("生产环境短信服务尚未实现短信发送功能"));
    }
    
    @Test
    @DisplayName("发送每日摘要短信 - 返回未实现错误")
    void testSendDailySummary_NotImplemented() {
        // Given
        List<Certificate> expiringSoonCertificates = Arrays.asList(testCertificate);
        List<Certificate> expiredCertificates = Arrays.asList();
        
        // When
        SmsResult result = smsService.sendDailySummary(expiringSoonCertificates, expiredCertificates, testRecipient);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("NOT_IMPLEMENTED", result.getErrorCode());
        assertEquals(testRecipient, result.getRecipient());
        assertTrue(result.getErrorMessage().contains("生产环境短信服务尚未实现短信发送功能"));
    }
    
    @Test
    @DisplayName("批量发送短信预警 - 返回未实现错误")
    void testSendBatchAlerts_NotImplemented() {
        // Given
        List<Certificate> certificates = Arrays.asList(testCertificate);
        
        // When
        List<SmsResult> results = smsService.sendBatchAlerts(certificates, testRecipient);
        
        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        
        SmsResult result = results.get(0);
        assertFalse(result.isSuccess());
        assertEquals("NOT_IMPLEMENTED", result.getErrorCode());
        assertEquals(testRecipient, result.getRecipient());
    }
    
    @Test
    @DisplayName("测试短信结果对象的基本属性")
    void testSmsResultProperties() {
        // Given
        int daysUntilExpiry = 5;
        
        // When
        SmsResult result = smsService.sendExpiryAlertSms(testCertificate, daysUntilExpiry, testRecipient);
        
        // Then
        assertNotNull(result.getRecipient());
        assertNotNull(result.getSentAt());
        assertTrue(result.isFailure());
        assertTrue(result.hasError());
        assertEquals("短信发送失败", result.getMessage());
    }
    
    @Test
    @DisplayName("测试证书名称包含特殊字符的处理")
    void testSpecialCharactersInCertificateName() {
        // Given
        Certificate specialCert = Certificate.builder()
                .id(2L)
                .name("测试证书-特殊字符&符号")
                .domain("special.example.com")
                .issuer("Test CA")
                .issueDate(new Date(System.currentTimeMillis() - 86400000L * 30))
                .expiryDate(new Date(System.currentTimeMillis() + 86400000L * 7))
                .certificateType("SSL")
                .status(CertificateStatus.EXPIRING_SOON)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        
        int daysUntilExpiry = 7;
        
        // When
        SmsResult result = smsService.sendExpiryAlertSms(specialCert, daysUntilExpiry, testRecipient);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(testRecipient, result.getRecipient());
    }
    
    @Test
    @DisplayName("测试空证书列表的批量发送")
    void testSendBatchAlertsWithEmptyList() {
        // Given
        List<Certificate> emptyCertificates = Arrays.asList();
        
        // When
        List<SmsResult> results = smsService.sendBatchAlerts(emptyCertificates, testRecipient);
        
        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}