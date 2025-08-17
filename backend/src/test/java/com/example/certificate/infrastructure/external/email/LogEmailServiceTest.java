package com.example.certificate.infrastructure.external.email;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.service.MonitoringLogService;
import com.example.certificate.service.dto.EmailResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * LogEmailServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("日志邮件服务测试")
class LogEmailServiceTest {
    
    @Mock
    private MonitoringLogService monitoringLogService;
    
    @InjectMocks
    private LogEmailServiceImpl logEmailService;
    
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
        
        testRecipient = "admin@example.com";
    }
    
    @Test
    @DisplayName("发送过期预警邮件 - 成功场景")
    void testSendExpiryAlertEmail_Success() {
        // Given
        int daysUntilExpiry = 15;
        
        // When
        EmailResult result = logEmailService.sendExpiryAlertEmail(testCertificate, daysUntilExpiry, testRecipient);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("邮件预警已记录 (MVP模式)", result.getMessage());
        assertEquals(testRecipient, result.getRecipient());
        assertNotNull(result.getSentAt());
        
        // 验证监控日志服务被调用
        verify(monitoringLogService, times(1)).logEmailAlert(testCertificate, daysUntilExpiry, testRecipient);
    }
    
    @Test
    @DisplayName("发送过期预警邮件 - 监控日志异常场景")
    void testSendExpiryAlertEmail_MonitoringLogException() {
        // Given
        int daysUntilExpiry = 7;
        doThrow(new RuntimeException("数据库连接异常")).when(monitoringLogService)
                .logEmailAlert(any(Certificate.class), anyInt(), anyString());
        
        // When
        EmailResult result = logEmailService.sendExpiryAlertEmail(testCertificate, daysUntilExpiry, testRecipient);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("LOG_EMAIL_FAILED", result.getErrorCode());
        assertEquals(testRecipient, result.getRecipient());
        assertTrue(result.getErrorMessage().contains("邮件操作失败"));
        
        // 验证监控日志服务被调用
        verify(monitoringLogService, times(1)).logEmailAlert(testCertificate, daysUntilExpiry, testRecipient);
    }
    
    @Test
    @DisplayName("发送每日摘要邮件 - 成功场景")
    void testSendDailySummary_Success() {
        // Given
        List<Certificate> expiringSoonCertificates = Arrays.asList(
                createCertificate(1L, "证书1", "site1.example.com", 10),
                createCertificate(2L, "证书2", "site2.example.com", 5)
        );
        
        List<Certificate> expiredCertificates = Arrays.asList(
                createCertificate(3L, "过期证书", "old.example.com", -5)
        );
        
        // When
        EmailResult result = logEmailService.sendDailySummary(expiringSoonCertificates, expiredCertificates, testRecipient);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("每日摘要已记录 (MVP模式)", result.getMessage());
        assertEquals(testRecipient, result.getRecipient());
        assertNotNull(result.getSentAt());
        
        // 验证监控日志服务被调用
        verify(monitoringLogService, times(1)).logDailySummary(expiringSoonCertificates, expiredCertificates, testRecipient);
    }
    
    @Test
    @DisplayName("发送每日摘要邮件 - 空列表场景")
    void testSendDailySummary_EmptyLists() {
        // Given
        List<Certificate> expiringSoonCertificates = Arrays.asList();
        List<Certificate> expiredCertificates = Arrays.asList();
        
        // When
        EmailResult result = logEmailService.sendDailySummary(expiringSoonCertificates, expiredCertificates, testRecipient);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("每日摘要已记录 (MVP模式)", result.getMessage());
        assertEquals(testRecipient, result.getRecipient());
        
        // 验证监控日志服务被调用
        verify(monitoringLogService, times(1)).logDailySummary(expiringSoonCertificates, expiredCertificates, testRecipient);
    }
    
    @Test
    @DisplayName("发送每日摘要邮件 - 异常场景")
    void testSendDailySummary_Exception() {
        // Given
        List<Certificate> expiringSoonCertificates = Arrays.asList(testCertificate);
        List<Certificate> expiredCertificates = Arrays.asList();
        
        doThrow(new RuntimeException("网络异常")).when(monitoringLogService)
                .logDailySummary(any(), any(), anyString());
        
        // When
        EmailResult result = logEmailService.sendDailySummary(expiringSoonCertificates, expiredCertificates, testRecipient);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("LOG_SUMMARY_FAILED", result.getErrorCode());
        assertTrue(result.getErrorMessage().contains("网络异常"));
        
        // 验证监控日志服务被调用
        verify(monitoringLogService, times(1)).logDailySummary(expiringSoonCertificates, expiredCertificates, testRecipient);
    }
    
    @Test
    @DisplayName("批量发送邮件预警 - 成功场景")
    void testSendBatchAlerts_Success() {
        // Given
        List<Certificate> certificates = Arrays.asList(
                createCertificate(1L, "证书1", "site1.example.com", 20),
                createCertificate(2L, "证书2", "site2.example.com", 10),
                createCertificate(3L, "证书3", "site3.example.com", 5)
        );
        
        // When
        List<EmailResult> results = logEmailService.sendBatchAlerts(certificates, testRecipient);
        
        // Then
        assertNotNull(results);
        assertEquals(3, results.size());
        
        for (EmailResult result : results) {
            assertTrue(result.isSuccess());
            assertEquals(testRecipient, result.getRecipient());
        }
        
        // 验证监控日志服务被调用了3次
        verify(monitoringLogService, times(3)).logEmailAlert(any(Certificate.class), anyInt(), eq(testRecipient));
    }
    
    @Test
    @DisplayName("批量发送邮件预警 - 部分失败场景")
    void testSendBatchAlerts_PartialFailure() {
        // Given
        List<Certificate> certificates = Arrays.asList(
                createCertificate(1L, "证书1", "site1.example.com", 20),
                createCertificate(2L, "证书2", "site2.example.com", 10)
        );
        
        // 模拟第二次调用失败
        doNothing().doThrow(new RuntimeException("系统异常")).when(monitoringLogService)
                .logEmailAlert(any(Certificate.class), anyInt(), anyString());
        
        // When
        List<EmailResult> results = logEmailService.sendBatchAlerts(certificates, testRecipient);
        
        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        
        // 第一个成功
        assertTrue(results.get(0).isSuccess());
        
        // 第二个失败
        assertFalse(results.get(1).isSuccess());
        assertEquals("LOG_EMAIL_FAILED", results.get(1).getErrorCode());
    }
    
    @Test
    @DisplayName("批量发送邮件预警 - 空列表场景")
    void testSendBatchAlerts_EmptyList() {
        // Given
        List<Certificate> certificates = Arrays.asList();
        
        // When
        List<EmailResult> results = logEmailService.sendBatchAlerts(certificates, testRecipient);
        
        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
        
        // 验证监控日志服务未被调用
        verify(monitoringLogService, never()).logEmailAlert(any(Certificate.class), anyInt(), anyString());
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
                .issuer("测试CA")
                .issueDate(new Date(currentTime - 86400000L * 30))
                .expiryDate(expiryDate)
                .certificateType("SSL")
                .status(status)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }
}