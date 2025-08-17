package com.example.certificate.integration;

import com.example.certificate.config.SmsConfig;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.domain.repository.CertificateRepository;
import com.example.certificate.service.MonitoringLogService;
import com.example.certificate.service.MonitoringService;
import com.example.certificate.service.SmsService;
import com.example.certificate.service.dto.SmsResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 短信服务集成测试
 * 验证短信预警功能与其他系统组件的集成
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("短信服务集成测试")
class SmsIntegrationTest {

    @Autowired
    private SmsService smsService;

    @Autowired
    private MonitoringService monitoringService;

    @Autowired
    private SmsConfig smsConfig;

    @MockBean
    private CertificateRepository certificateRepository;

    @MockBean
    private MonitoringLogService monitoringLogService;

    private List<Certificate> testCertificates;
    private String testRecipient;

    @BeforeEach
    void setUp() {
        testRecipient = "13800138000";
        
        // 创建测试证书数据
        testCertificates = Arrays.asList(
                createCertificate(1L, "正常证书", "normal.example.com", CertificateStatus.NORMAL, 60),
                createCertificate(2L, "30天预警证书", "warning30.example.com", CertificateStatus.EXPIRING_SOON, 30),
                createCertificate(3L, "15天预警证书", "warning15.example.com", CertificateStatus.EXPIRING_SOON, 15),
                createCertificate(4L, "7天预警证书", "warning7.example.com", CertificateStatus.EXPIRING_SOON, 7),
                createCertificate(5L, "1天预警证书", "warning1.example.com", CertificateStatus.EXPIRING_SOON, 1),
                createCertificate(6L, "已过期证书", "expired.example.com", CertificateStatus.EXPIRED, -5)
        );

        // 模拟证书库查询
        when(certificateRepository.findAll()).thenReturn(testCertificates);
    }

    @Test
    @DisplayName("集成测试：短信服务基本功能验证")
    void testSmsServiceBasicIntegration() {
        // Given
        Certificate testCert = testCertificates.get(3); // 7天预警证书
        int daysUntilExpiry = 7;
        
        // When
        SmsResult result = smsService.sendExpiryAlertSms(testCert, daysUntilExpiry, testRecipient);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("短信预警已记录 (MVP模式)", result.getMessage());
        assertEquals(testRecipient, result.getRecipient());
        assertNotNull(result.getSentAt());
        
        // 验证监控日志服务被调用
        verify(monitoringLogService, times(1)).logSmsAlert(testCert, daysUntilExpiry, testRecipient);
    }

    @Test
    @DisplayName("集成测试：短信每日摘要功能验证")
    void testSmsDailySummaryIntegration() {
        // Given
        List<Certificate> expiringSoon = Arrays.asList(
                testCertificates.get(2), // 15天预警证书
                testCertificates.get(3)  // 7天预警证书
        );
        List<Certificate> expired = Arrays.asList(
                testCertificates.get(5)  // 已过期证书
        );
        
        // When
        SmsResult result = smsService.sendDailySummary(expiringSoon, expired, testRecipient);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("每日摘要已记录 (MVP模式)", result.getMessage());
        assertEquals(testRecipient, result.getRecipient());
        
        // 验证监控日志服务被调用
        verify(monitoringLogService, times(1)).logSmsDailySummary(expiringSoon, expired, testRecipient);
    }

    @Test
    @DisplayName("集成测试：批量短信预警功能验证")
    void testSmsBatchAlertsIntegration() {
        // Given
        List<Certificate> alertCertificates = Arrays.asList(
                testCertificates.get(3), // 7天预警证书
                testCertificates.get(4)  // 1天预警证书
        );
        
        // When
        List<SmsResult> results = smsService.sendBatchAlerts(alertCertificates, testRecipient);
        
        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        
        for (SmsResult result : results) {
            assertTrue(result.isSuccess());
            assertEquals(testRecipient, result.getRecipient());
            assertEquals("短信预警已记录 (MVP模式)", result.getMessage());
        }
        
        // 验证监控日志服务被调用了两次
        verify(monitoringLogService, times(2)).logSmsAlert(any(Certificate.class), anyInt(), eq(testRecipient));
    }

    @Test
    @DisplayName("集成测试：短信配置验证")
    void testSmsConfigIntegration() {
        // When & Then
        assertNotNull(smsConfig);
        assertTrue(smsConfig.isEnabled());
        assertTrue(smsConfig.isLogMode());
        assertFalse(smsConfig.isRealMode());
        assertNotNull(smsConfig.getDefaultRecipient());
        assertTrue(smsConfig.isValidConfiguration());
    }

    @Test
    @DisplayName("集成测试：手机号验证功能")
    void testPhoneValidationIntegration() {
        // Given
        String validPhone = "13812345678";
        String invalidPhone = "12345";
        Certificate testCert = testCertificates.get(3);
        
        // When - 有效手机号
        SmsResult validResult = smsService.sendExpiryAlertSms(testCert, 7, validPhone);
        
        // When - 无效手机号
        SmsResult invalidResult = smsService.sendExpiryAlertSms(testCert, 7, invalidPhone);
        
        // Then
        assertTrue(validResult.isSuccess());
        assertEquals(validPhone, validResult.getRecipient());
        
        assertFalse(invalidResult.isSuccess());
        assertEquals("PHONE_INVALID", invalidResult.getErrorCode());
        assertEquals(invalidPhone, invalidResult.getRecipient());
        
        // 验证有效手机号的监控日志被记录，无效手机号的未被记录
        verify(monitoringLogService, times(1)).logSmsAlert(testCert, 7, validPhone);
        verify(monitoringLogService, never()).logSmsAlert(eq(testCert), eq(7), eq(invalidPhone));
    }

    @Test
    @DisplayName("集成测试：短信内容长度验证")
    void testSmsContentLengthIntegration() {
        // Given
        Certificate longNameCert = Certificate.builder()
                .id(999L)
                .name("这是一个非常长的证书名称用于测试短信内容长度验证功能")
                .domain("verylongdomainname.example.com")
                .issuer("测试CA")
                .issueDate(new Date(System.currentTimeMillis() - 86400000L * 30))
                .expiryDate(new Date(System.currentTimeMillis() + 86400000L * 7))
                .certificateType("SSL")
                .status(CertificateStatus.EXPIRING_SOON)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        
        // When
        SmsResult result = smsService.sendExpiryAlertSms(longNameCert, 7, testRecipient);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess()); // 即使内容长，也应该成功（在日志模式下会被截断）
        assertEquals(testRecipient, result.getRecipient());
        
        verify(monitoringLogService, times(1)).logSmsAlert(longNameCert, 7, testRecipient);
    }

    @Test
    @DisplayName("集成测试：异常处理验证")
    void testSmsServiceExceptionHandlingIntegration() {
        // Given
        Certificate testCert = testCertificates.get(3);
        doThrow(new RuntimeException("数据库连接失败")).when(monitoringLogService)
                .logSmsAlert(any(Certificate.class), anyInt(), anyString());
        
        // When
        SmsResult result = smsService.sendExpiryAlertSms(testCert, 7, testRecipient);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("LOG_SMS_FAILED", result.getErrorCode());
        assertEquals(testRecipient, result.getRecipient());
        assertTrue(result.getErrorMessage().contains("短信操作失败"));
        
        // 验证监控日志服务被调用了
        verify(monitoringLogService, times(1)).logSmsAlert(testCert, 7, testRecipient);
    }

    @Test
    @DisplayName("集成测试：不同预警类型验证")
    void testDifferentAlertTypesIntegration() {
        // Test different alert scenarios
        Certificate[] certs = {
                testCertificates.get(1), // 30天预警
                testCertificates.get(2), // 15天预警
                testCertificates.get(3), // 7天预警
                testCertificates.get(4), // 1天预警
                testCertificates.get(5)  // 已过期
        };
        
        int[] daysArray = {30, 15, 7, 1, -5};
        
        for (int i = 0; i < certs.length; i++) {
            // When
            SmsResult result = smsService.sendExpiryAlertSms(certs[i], daysArray[i], testRecipient);
            
            // Then
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(testRecipient, result.getRecipient());
        }
        
        // 验证所有预警都被记录
        verify(monitoringLogService, times(5)).logSmsAlert(any(Certificate.class), anyInt(), eq(testRecipient));
    }

    /**
     * 创建测试证书的辅助方法
     */
    private Certificate createCertificate(Long id, String name, String domain, 
                                        CertificateStatus status, int daysUntilExpiry) {
        Date expiryDate = new Date(System.currentTimeMillis() + (long) daysUntilExpiry * 86400000L);
        
        return Certificate.builder()
                .id(id)
                .name(name)
                .domain(domain)
                .issuer("测试CA")
                .issueDate(new Date(System.currentTimeMillis() - 86400000L * 30))
                .expiryDate(expiryDate)
                .certificateType("SSL")
                .status(status)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }
}