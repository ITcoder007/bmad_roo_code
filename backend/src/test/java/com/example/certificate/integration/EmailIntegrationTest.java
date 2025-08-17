package com.example.certificate.integration;

import com.example.certificate.config.EmailConfig;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.domain.repository.CertificateRepository;
import com.example.certificate.service.DailySummaryService;
import com.example.certificate.service.EmailService;
import com.example.certificate.service.MonitoringLogService;
import com.example.certificate.service.MonitoringService;
import com.example.certificate.service.dto.EmailResult;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 邮件服务集成测试
 * 验证邮件预警功能与其他系统组件的集成
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("邮件服务集成测试")
class EmailIntegrationTest {

    @Autowired
    private EmailService emailService;

    @Autowired
    private MonitoringService monitoringService;

    @Autowired
    private DailySummaryService dailySummaryService;

    @Autowired
    private EmailConfig emailConfig;

    @MockBean
    private CertificateRepository certificateRepository;

    @MockBean
    private MonitoringLogService monitoringLogService;

    private List<Certificate> testCertificates;

    @BeforeEach
    void setUp() {
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
    @DisplayName("集成测试：邮件服务基本功能验证")
    void testEmailServiceBasicFunctionality() {
        // Given
        Certificate expiringSoonCert = testCertificates.get(2); // 15天预警证书
        String recipient = "test@example.com";
        
        // When - 直接测试邮件服务
        EmailResult result = emailService.sendExpiryAlertEmail(expiringSoonCert, 15, recipient);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("邮件预警已记录 (MVP模式)", result.getMessage());
        assertEquals(recipient, result.getRecipient());
        
        // 验证监控日志服务被调用
        verify(monitoringLogService, times(1))
                .logEmailAlert(eq(expiringSoonCert), eq(15), eq(recipient));
    }

    @Test
    @DisplayName("集成测试：每日摘要服务完整流程")
    void testDailySummaryServiceIntegration() {
        // Given
        String recipient = "summary@example.com";

        // When
        dailySummaryService.generateAndSendDailySummary(recipient);

        // Then
        // 验证证书库被查询
        verify(certificateRepository, times(1)).findAll();

        // 验证每日摘要被记录
        verify(monitoringLogService, times(1))
                .logDailySummary(any(), any(), eq(recipient));
    }

    @Test
    @DisplayName("集成测试：邮件配置正确加载")
    void testEmailConfigurationLoading() {
        // Then
        assertNotNull(emailConfig);
        assertTrue(emailConfig.isEnabled());
        assertEquals("log", emailConfig.getMode());
        assertNotNull(emailConfig.getDefaultRecipient());
    }

    @Test
    @DisplayName("集成测试：LogEmailService 功能验证")
    void testLogEmailServiceFunctionality() {
        // Given
        Certificate testCert = testCertificates.get(1); // 30天预警证书
        String recipient = "test@example.com";

        // When
        EmailResult result = emailService.sendExpiryAlertEmail(testCert, 30, recipient);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("邮件预警已记录 (MVP模式)", result.getMessage());
        assertEquals(recipient, result.getRecipient());

        // 验证监控日志服务被调用
        verify(monitoringLogService, times(1))
                .logEmailAlert(testCert, 30, recipient);
    }

    @Test
    @DisplayName("集成测试：批量邮件预警处理")
    void testBatchEmailAlerts() {
        // Given
        List<Certificate> expiringSoonCerts = Arrays.asList(
                testCertificates.get(2), // 15天预警
                testCertificates.get(3), // 7天预警
                testCertificates.get(4)  // 1天预警
        );
        String recipient = "batch@example.com";

        // When
        List<EmailResult> results = emailService.sendBatchAlerts(expiringSoonCerts, recipient);

        // Then
        assertNotNull(results);
        assertEquals(3, results.size());

        // 验证所有邮件都成功处理
        for (EmailResult result : results) {
            assertTrue(result.isSuccess());
            assertEquals(recipient, result.getRecipient());
        }

        // 验证监控日志服务被调用了3次
        verify(monitoringLogService, times(3))
                .logEmailAlert(any(Certificate.class), anyInt(), eq(recipient));
    }

    @Test
    @DisplayName("集成测试：邮件模板系统")
    void testEmailTemplateSystem() {
        // Given
        Certificate testCert = testCertificates.get(3); // 7天预警证书
        List<Certificate> expiredCerts = Arrays.asList(testCertificates.get(5)); // 已过期证书

        // When
        EmailResult alertResult = emailService.sendExpiryAlertEmail(testCert, 7, "template-test@example.com");
        EmailResult summaryResult = emailService.sendDailySummary(
                Arrays.asList(testCert), expiredCerts, "summary-test@example.com");

        // Then
        assertTrue(alertResult.isSuccess());
        assertTrue(summaryResult.isSuccess());

        // 验证两种类型的邮件都被正确处理
        verify(monitoringLogService, times(1))
                .logEmailAlert(any(Certificate.class), anyInt(), anyString());
        verify(monitoringLogService, times(1))
                .logDailySummary(any(), any(), anyString());
    }

    @Test
    @DisplayName("集成测试：邮件功能禁用场景")
    void testEmailDisabledScenario() {
        // Given
        EmailConfig disabledConfig = mock(EmailConfig.class);
        when(disabledConfig.isEnabled()).thenReturn(false);

        DailySummaryService summaryServiceWithDisabledEmail = 
            new com.example.certificate.service.impl.DailySummaryServiceImpl(
                certificateRepository, emailService, disabledConfig);

        // When
        summaryServiceWithDisabledEmail.generateAndSendDailySummary("disabled-test@example.com");

        // Then
        // 当邮件功能禁用时，不应查询证书库或发送邮件
        verify(certificateRepository, never()).findAll();
        verify(monitoringLogService, never()).logDailySummary(any(), any(), anyString());
    }

    @Test
    @DisplayName("集成测试：错误处理和恢复机制")
    void testErrorHandlingAndRecovery() {
        // Given
        Certificate testCert = testCertificates.get(4); // 1天预警证书
        String recipient = "error-test@example.com";

        // 模拟监控日志服务异常
        doThrow(new RuntimeException("数据库连接异常"))
                .when(monitoringLogService)
                .logEmailAlert(any(Certificate.class), anyInt(), anyString());

        // When
        EmailResult result = emailService.sendExpiryAlertEmail(testCert, 1, recipient);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("LOG_EMAIL_FAILED", result.getErrorCode());
        assertTrue(result.getErrorMessage().contains("邮件预警记录失败"));

        // 验证异常被正确处理，不会中断服务
        verify(monitoringLogService, times(1))
                .logEmailAlert(testCert, 1, recipient);
    }

    @Test
    @DisplayName("集成测试：证书状态分类准确性")
    void testCertificateStatusClassification() {
        // Given
        String recipient = "classification-test@example.com";

        // When
        dailySummaryService.generateAndSendDailySummary(recipient);

        // Then
        // 验证证书按状态正确分类
        verify(monitoringLogService, times(1)).logDailySummary(
            argThat(expiringSoon -> {
                // 应该有4个即将过期的证书（30天、15天、7天、1天）
                return expiringSoon.size() == 4;
            }),
            argThat(expired -> {
                // 应该有1个已过期的证书
                return expired.size() == 1;
            }),
            eq(recipient)
        );
    }

    /**
     * 创建测试证书对象
     */
    private Certificate createCertificate(Long id, String name, String domain, 
                                        CertificateStatus status, int daysUntilExpiry) {
        long currentTime = System.currentTimeMillis();
        Date expiryDate = new Date(currentTime + daysUntilExpiry * 86400000L);

        return Certificate.builder()
                .id(id)
                .name(name)
                .domain(domain)
                .issuer("Integration Test CA")
                .issueDate(new Date(currentTime - 86400000L * 30))
                .expiryDate(expiryDate)
                .certificateType("SSL")
                .status(status)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }
}