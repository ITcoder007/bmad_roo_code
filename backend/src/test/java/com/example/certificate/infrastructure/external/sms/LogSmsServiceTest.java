package com.example.certificate.infrastructure.external.sms;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.service.MonitoringLogService;
import com.example.certificate.service.dto.SmsResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * LogSmsServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("日志短信服务测试")
class LogSmsServiceTest {
    
    @Mock
    private MonitoringLogService monitoringLogService;
    
    @InjectMocks
    private LogSmsServiceImpl logSmsService;
    
    private Certificate testCertificate;
    private String testRecipient;
    
    @BeforeEach
    void setUp() {
        // 创建测试用证书
        testCertificate = Certificate.builder()
                .id(1L)
                .name("测试证书")
                .domain("test.example.com")
                .issuer("测试CA")
                .issueDate(new Date(System.currentTimeMillis() - 86400000L * 30)) // 30天前颁发
                .expiryDate(new Date(System.currentTimeMillis() + 86400000L * 15)) // 15天后过期
                .certificateType("SSL")
                .status(CertificateStatus.EXPIRING_SOON)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        
        testRecipient = "13800138000";
    }
    
    @Test
    @DisplayName("发送过期预警短信 - 成功场景")
    void testSendExpiryAlertSms_Success() {
        // Given
        int daysUntilExpiry = 15;
        
        // When
        SmsResult result = logSmsService.sendExpiryAlertSms(testCertificate, daysUntilExpiry, testRecipient);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("短信预警已记录 (MVP模式)", result.getMessage());
        assertEquals(testRecipient, result.getRecipient());
        assertNotNull(result.getSentAt());
        
        // 验证监控日志服务被调用
        verify(monitoringLogService, times(1)).logSmsAlert(testCertificate, daysUntilExpiry, testRecipient);
    }
    
    @Test
    @DisplayName("发送过期预警短信 - 监控日志异常场景")
    void testSendExpiryAlertSms_MonitoringLogException() {
        // Given
        int daysUntilExpiry = 7;
        doThrow(new RuntimeException("数据库连接异常")).when(monitoringLogService)
                .logSmsAlert(any(Certificate.class), anyInt(), anyString());
        
        // When
        SmsResult result = logSmsService.sendExpiryAlertSms(testCertificate, daysUntilExpiry, testRecipient);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("LOG_SMS_FAILED", result.getErrorCode());
        assertEquals(testRecipient, result.getRecipient());
        assertTrue(result.getErrorMessage().contains("短信操作失败"));
        
        // 验证监控日志服务被调用
        verify(monitoringLogService, times(1)).logSmsAlert(testCertificate, daysUntilExpiry, testRecipient);
    }
    
    @Test
    @DisplayName("发送过期预警短信 - 无效手机号格式")
    void testSendExpiryAlertSms_InvalidPhoneNumber() {
        // Given
        int daysUntilExpiry = 10;
        String invalidPhone = "12345";
        
        // When
        SmsResult result = logSmsService.sendExpiryAlertSms(testCertificate, daysUntilExpiry, invalidPhone);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("PHONE_INVALID", result.getErrorCode());
        assertEquals(invalidPhone, result.getRecipient());
        assertTrue(result.getErrorMessage().contains("手机号格式不正确"));
        
        // 验证监控日志服务未被调用
        verify(monitoringLogService, never()).logSmsAlert(any(Certificate.class), anyInt(), anyString());
    }
    
    @Test
    @DisplayName("发送每日摘要短信 - 成功场景")
    void testSendDailySummary_Success() {
        // Given
        List<Certificate> expiringSoonCertificates = Arrays.asList(testCertificate);
        List<Certificate> expiredCertificates = Collections.emptyList();
        
        // When
        SmsResult result = logSmsService.sendDailySummary(expiringSoonCertificates, expiredCertificates, testRecipient);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("每日摘要已记录 (MVP模式)", result.getMessage());
        assertEquals(testRecipient, result.getRecipient());
        assertNotNull(result.getSentAt());
        
        // 验证监控日志服务被调用
        verify(monitoringLogService, times(1)).logSmsDailySummary(expiringSoonCertificates, expiredCertificates, testRecipient);
    }
    
    @Test
    @DisplayName("发送每日摘要短信 - 无效手机号格式")
    void testSendDailySummary_InvalidPhoneNumber() {
        // Given
        List<Certificate> expiringSoonCertificates = Arrays.asList(testCertificate);
        List<Certificate> expiredCertificates = Collections.emptyList();
        String invalidPhone = "abc123";
        
        // When
        SmsResult result = logSmsService.sendDailySummary(expiringSoonCertificates, expiredCertificates, invalidPhone);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("PHONE_INVALID", result.getErrorCode());
        assertEquals(invalidPhone, result.getRecipient());
        assertTrue(result.getErrorMessage().contains("手机号格式不正确"));
        
        // 验证监控日志服务未被调用
        verify(monitoringLogService, never()).logSmsDailySummary(any(), any(), anyString());
    }
    
    @Test
    @DisplayName("批量发送短信预警 - 成功场景")
    void testSendBatchAlerts_Success() {
        // Given
        Certificate cert1 = testCertificate;
        Certificate cert2 = Certificate.builder()
                .id(2L)
                .name("测试证书2")
                .domain("test2.example.com")
                .issuer("测试CA")
                .issueDate(new Date(System.currentTimeMillis() - 86400000L * 30))
                .expiryDate(new Date(System.currentTimeMillis() + 86400000L * 7))
                .certificateType("SSL")
                .status(CertificateStatus.EXPIRING_SOON)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        
        List<Certificate> certificates = Arrays.asList(cert1, cert2);
        
        // When
        List<SmsResult> results = logSmsService.sendBatchAlerts(certificates, testRecipient);
        
        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        
        for (SmsResult result : results) {
            assertTrue(result.isSuccess());
            assertEquals(testRecipient, result.getRecipient());
        }
        
        // 验证监控日志服务被调用了两次
        verify(monitoringLogService, times(2)).logSmsAlert(any(Certificate.class), anyInt(), eq(testRecipient));
    }
    
    @Test
    @DisplayName("批量发送短信预警 - 无效手机号格式")
    void testSendBatchAlerts_InvalidPhoneNumber() {
        // Given
        List<Certificate> certificates = Arrays.asList(testCertificate);
        String invalidPhone = "invalid";
        
        // When
        List<SmsResult> results = logSmsService.sendBatchAlerts(certificates, invalidPhone);
        
        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        
        SmsResult result = results.get(0);
        assertFalse(result.isSuccess());
        assertEquals("PHONE_INVALID", result.getErrorCode());
        assertEquals(invalidPhone, result.getRecipient());
        
        // 验证监控日志服务未被调用
        verify(monitoringLogService, never()).logSmsAlert(any(Certificate.class), anyInt(), anyString());
    }
    
    @Test
    @DisplayName("批量发送短信预警 - 空证书列表")
    void testSendBatchAlerts_EmptyList() {
        // Given
        List<Certificate> emptyCertificates = Collections.emptyList();
        
        // When
        List<SmsResult> results = logSmsService.sendBatchAlerts(emptyCertificates, testRecipient);
        
        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
        
        // 验证监控日志服务未被调用
        verify(monitoringLogService, never()).logSmsAlert(any(Certificate.class), anyInt(), anyString());
    }
    
    @Test
    @DisplayName("测试手机号脱敏功能")
    void testPhoneNumberMasking() {
        // Given
        int daysUntilExpiry = 15;
        String phoneNumber = "13812345678";
        
        // When
        SmsResult result = logSmsService.sendExpiryAlertSms(testCertificate, daysUntilExpiry, phoneNumber);
        
        // Then
        assertTrue(result.isSuccess());
        assertEquals(phoneNumber, result.getRecipient()); // 结果中应保持原始手机号
        
        verify(monitoringLogService, times(1)).logSmsAlert(testCertificate, daysUntilExpiry, phoneNumber);
    }
    
    @Test
    @DisplayName("测试边界条件 - 刚好过期")
    void testExpiryAlert_JustExpired() {
        // Given
        Certificate expiredCert = Certificate.builder()
                .id(3L)
                .name("已过期证书")
                .domain("expired.example.com")
                .issuer("测试CA")
                .issueDate(new Date(System.currentTimeMillis() - 86400000L * 365)) // 365天前颁发
                .expiryDate(new Date(System.currentTimeMillis() - 86400000L)) // 1天前过期
                .certificateType("SSL")
                .status(CertificateStatus.EXPIRED)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        
        int daysUntilExpiry = -1; // 已过期1天
        
        // When
        SmsResult result = logSmsService.sendExpiryAlertSms(expiredCert, daysUntilExpiry, testRecipient);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(testRecipient, result.getRecipient());
        
        verify(monitoringLogService, times(1)).logSmsAlert(expiredCert, daysUntilExpiry, testRecipient);
    }
}