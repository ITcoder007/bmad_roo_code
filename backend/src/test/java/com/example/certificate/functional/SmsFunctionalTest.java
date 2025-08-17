package com.example.certificate.functional;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.infrastructure.external.sms.SmsLogFormatter;
import com.example.certificate.service.MonitoringLogService;
import com.example.certificate.service.SmsService;
import com.example.certificate.service.dto.SmsResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

/**
 * 短信功能验证测试
 * 验证短信系统在真实业务场景下的功能表现
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("短信功能验证测试")
class SmsFunctionalTest {

    @Autowired
    private SmsService smsService;

    @SpyBean
    private MonitoringLogService monitoringLogService;

    @Autowired
    private SmsLogFormatter smsLogFormatter;

    private SimpleDateFormat dateFormat;
    private String testRecipient;

    @BeforeEach
    void setUp() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        testRecipient = "13800138000";
        
        // 重置监控日志服务的调用记录
        reset(monitoringLogService);
        
        // 设置监控日志服务的默认行为，避免在功能测试中因数据库问题导致失败
        doNothing().when(monitoringLogService).logSmsAlert(any(Certificate.class), anyInt(), anyString());
        doNothing().when(monitoringLogService).logSmsDailySummary(any(List.class), any(List.class), anyString());
    }

    @Test
    @DisplayName("功能测试：证书即将过期预警短信发送")
    void testExpiryAlertSmsFunctional() {
        // Given - 创建一个即将过期的证书
        Certificate expiringSoonCert = createTestCertificate(
                "生产网站证书", 
                "www.production.com", 
                CertificateStatus.EXPIRING_SOON, 
                7  // 7天后过期
        );
        
        // When
        SmsResult result = smsService.sendExpiryAlertSms(expiringSoonCert, 7, testRecipient);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("短信预警已记录 (MVP模式)", result.getMessage());
        assertEquals(testRecipient, result.getRecipient());
        assertNotNull(result.getSentAt());
        assertNull(result.getErrorCode());
        assertNull(result.getErrorMessage());
        
        // 验证监控日志被正确记录
        verify(monitoringLogService, times(1))
                .logSmsAlert(expiringSoonCert, 7, testRecipient);
    }

    @Test
    @DisplayName("功能测试：已过期证书紧急预警短信发送")
    void testExpiredCertificateUrgentSmsFunctional() {
        // Given - 创建一个已过期的证书
        Certificate expiredCert = createTestCertificate(
                "支付系统证书", 
                "pay.production.com", 
                CertificateStatus.EXPIRED, 
                -3  // 已过期3天
        );
        
        // When
        SmsResult result = smsService.sendExpiryAlertSms(expiredCert, -3, testRecipient);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(testRecipient, result.getRecipient());
        
        // 验证紧急预警被记录
        verify(monitoringLogService, times(1))
                .logSmsAlert(expiredCert, -3, testRecipient);
    }

    @Test
    @DisplayName("功能测试：每日证书状态摘要短信发送")
    void testDailySummarySmsFunctional() {
        // Given - 创建测试数据
        List<Certificate> expiringSoonCertificates = Arrays.asList(
                createTestCertificate("API网关证书", "api.production.com", CertificateStatus.EXPIRING_SOON, 5),
                createTestCertificate("CDN证书", "cdn.production.com", CertificateStatus.EXPIRING_SOON, 10)
        );
        
        List<Certificate> expiredCertificates = Arrays.asList(
                createTestCertificate("测试环境证书", "test.production.com", CertificateStatus.EXPIRED, -1)
        );
        
        // When
        SmsResult result = smsService.sendDailySummary(expiringSoonCertificates, expiredCertificates, testRecipient);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("每日摘要已记录 (MVP模式)", result.getMessage());
        assertEquals(testRecipient, result.getRecipient());
        
        // 验证每日摘要被正确记录
        verify(monitoringLogService, times(1))
                .logSmsDailySummary(expiringSoonCertificates, expiredCertificates, testRecipient);
    }

    @Test
    @DisplayName("功能测试：批量短信预警发送")
    void testBatchSmsAlertsFunctional() {
        // Given - 创建多个需要预警的证书
        List<Certificate> criticalCertificates = Arrays.asList(
                createTestCertificate("数据库证书", "db.production.com", CertificateStatus.EXPIRING_SOON, 1),
                createTestCertificate("缓存证书", "cache.production.com", CertificateStatus.EXPIRING_SOON, 2),
                createTestCertificate("监控证书", "monitor.production.com", CertificateStatus.EXPIRING_SOON, 3)
        );
        
        // When
        List<SmsResult> results = smsService.sendBatchAlerts(criticalCertificates, testRecipient);
        
        // Then
        assertNotNull(results);
        assertEquals(3, results.size());
        
        for (SmsResult result : results) {
            assertTrue(result.isSuccess());
            assertEquals(testRecipient, result.getRecipient());
            assertEquals("短信预警已记录 (MVP模式)", result.getMessage());
        }
        
        // 验证所有证书的预警都被记录
        verify(monitoringLogService, times(3))
                .logSmsAlert(any(Certificate.class), anyInt(), eq(testRecipient));
    }

    @Test
    @DisplayName("功能测试：不同紧急程度的预警短信")
    void testDifferentUrgencyLevelsSmsFunctional() {
        // Test certificates with different urgency levels
        Certificate[] certificates = {
                createTestCertificate("低优先级证书", "low.production.com", CertificateStatus.EXPIRING_SOON, 30),    // 30天预警
                createTestCertificate("中优先级证书", "medium.production.com", CertificateStatus.EXPIRING_SOON, 15),  // 15天预警
                createTestCertificate("高优先级证书", "high.production.com", CertificateStatus.EXPIRING_SOON, 7),     // 7天预警
                createTestCertificate("紧急证书", "critical.production.com", CertificateStatus.EXPIRING_SOON, 1),     // 1天预警
                createTestCertificate("已过期证书", "expired.production.com", CertificateStatus.EXPIRED, -1)          // 已过期
        };
        
        int[] daysArray = {30, 15, 7, 1, -1};
        
        for (int i = 0; i < certificates.length; i++) {
            // When
            SmsResult result = smsService.sendExpiryAlertSms(certificates[i], daysArray[i], testRecipient);
            
            // Then
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals(testRecipient, result.getRecipient());
        }
        
        // 验证所有不同紧急程度的预警都被记录
        verify(monitoringLogService, times(5))
                .logSmsAlert(any(Certificate.class), anyInt(), eq(testRecipient));
    }

    @Test
    @DisplayName("功能测试：短信内容格式验证")
    void testSmsContentFormatFunctional() {
        // Given
        Certificate testCert = createTestCertificate(
                "API服务证书", 
                "api.service.com", 
                CertificateStatus.EXPIRING_SOON, 
                5
        );
        
        // When
        SmsResult result = smsService.sendExpiryAlertSms(testCert, 5, testRecipient);
        
        // Then
        assertTrue(result.isSuccess());
        
        // 验证日志格式化器被正确调用
        String expectedLogFormat = smsLogFormatter.formatExpiryAlert(testCert, 5, testRecipient);
        assertNotNull(expectedLogFormat);
        assertTrue(expectedLogFormat.contains("API服务证书"));
        assertTrue(expectedLogFormat.contains("api.service.com"));
        assertTrue(expectedLogFormat.contains("5天"));
        assertTrue(expectedLogFormat.contains("138****8000")); // 脱敏后的手机号
    }

    @Test
    @DisplayName("功能测试：手机号验证功能")
    void testPhoneNumberValidationFunctional() {
        // Given
        Certificate testCert = createTestCertificate(
                "测试证书", 
                "test.example.com", 
                CertificateStatus.EXPIRING_SOON, 
                7
        );
        
        String[] phoneNumbers = {
                "13812345678",    // 有效手机号
                "15987654321",    // 有效手机号
                "18566778899",    // 有效手机号
                "12345678901",    // 无效手机号（不以13-19开头）
                "1381234567",     // 无效手机号（长度不够）
                "138123456789",   // 无效手机号（长度过长）
                "13a12345678",    // 无效手机号（包含字母）
                "",               // 空字符串
                null              // null值
        };
        
        boolean[] expectedResults = {true, true, true, false, false, false, false, false, false};
        
        for (int i = 0; i < phoneNumbers.length; i++) {
            String phone = phoneNumbers[i];
            boolean expectedValid = expectedResults[i];
            
            // When
            SmsResult result = smsService.sendExpiryAlertSms(testCert, 7, phone);
            
            // Then
            if (expectedValid) {
                assertTrue(result.isSuccess(), "Phone " + phone + " should be valid");
                assertEquals(phone, result.getRecipient());
            } else {
                assertFalse(result.isSuccess(), "Phone " + phone + " should be invalid");
                assertEquals("PHONE_INVALID", result.getErrorCode());
                assertEquals(phone, result.getRecipient());
            }
        }
    }

    @Test
    @DisplayName("功能测试：监控日志集成验证")
    void testMonitoringLogIntegrationFunctional() {
        // Given
        Certificate testCert = createTestCertificate(
                "监控测试证书", 
                "monitor.test.com", 
                CertificateStatus.EXPIRING_SOON, 
                3
        );
        
        // When - 发送预警短信
        SmsResult result = smsService.sendExpiryAlertSms(testCert, 3, testRecipient);
        
        // Then
        assertTrue(result.isSuccess());
        
        // 验证监控日志服务被调用，并且参数正确
        verify(monitoringLogService, times(1)).logSmsAlert(
                argThat(cert -> "监控测试证书".equals(cert.getName()) && 
                               "monitor.test.com".equals(cert.getDomain())),
                eq(3),
                eq(testRecipient)
        );
    }

    @Test
    @DisplayName("功能测试：异常场景处理验证")
    void testExceptionHandlingFunctional() {
        // Given
        Certificate testCert = createTestCertificate(
                "异常测试证书", 
                "exception.test.com", 
                CertificateStatus.EXPIRING_SOON, 
                5
        );
        
        // 模拟监控日志服务异常
        doThrow(new RuntimeException("监控日志服务不可用"))
                .when(monitoringLogService).logSmsAlert(any(), anyInt(), anyString());
        
        // When
        SmsResult result = smsService.sendExpiryAlertSms(testCert, 5, testRecipient);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("LOG_SMS_FAILED", result.getErrorCode());
        assertEquals(testRecipient, result.getRecipient());
        assertTrue(result.getErrorMessage().contains("短信操作失败"));
        
        // 验证即使发生异常，也尝试了调用监控日志服务
        verify(monitoringLogService, times(1))
                .logSmsAlert(testCert, 5, testRecipient);
    }

    /**
     * 创建测试证书的辅助方法
     */
    private Certificate createTestCertificate(String name, String domain, 
                                            CertificateStatus status, int daysUntilExpiry) {
        Date expiryDate = new Date(System.currentTimeMillis() + (long) daysUntilExpiry * 86400000L);
        
        return Certificate.builder()
                .id(System.currentTimeMillis()) // 使用时间戳作为唯一ID
                .name(name)
                .domain(domain)
                .issuer("功能测试CA")
                .issueDate(new Date(System.currentTimeMillis() - 86400000L * 365)) // 1年前颁发
                .expiryDate(expiryDate)
                .certificateType("SSL")
                .status(status)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }
}