package com.example.certificate.functional;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.infrastructure.external.email.EmailTemplate;
import com.example.certificate.service.EmailService;
import com.example.certificate.service.MonitoringLogService;
import com.example.certificate.service.dto.EmailResult;
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

/**
 * 邮件功能验证测试
 * 验证邮件系统在真实业务场景下的功能表现
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("邮件功能验证测试")
class EmailFunctionalTest {

    @Autowired
    private EmailService emailService;

    @SpyBean
    private MonitoringLogService monitoringLogService;

    @Autowired
    private EmailTemplate emailTemplate;

    private SimpleDateFormat dateFormat;

    @BeforeEach
    void setUp() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        // 设置监控日志服务的默认行为，避免在功能测试中因数据库问题导致失败
        doNothing().when(monitoringLogService).logEmailAlert(any(Certificate.class), anyInt(), anyString());
        doNothing().when(monitoringLogService).logDailySummary(any(List.class), any(List.class), anyString());
    }

    @Test
    @DisplayName("功能验证：完整的证书过期预警流程")
    void testCompleteExpiryAlertWorkflow() {
        // Given - 模拟真实的证书过期场景
        Certificate sslCertificate = createRealWorldCertificate(
                "生产环境SSL证书",
                "www.mycompany.com",
                "DigiCert Inc",
                7 // 7天后过期
        );
        
        String systemAdmin = "admin@mycompany.com";

        // When - 发送预警邮件
        EmailResult result = emailService.sendExpiryAlertEmail(sslCertificate, 7, systemAdmin);

        // Then - 验证完整流程
        assertTrue(result.isSuccess(), "邮件预警应该成功发送");
        assertEquals("邮件预警已记录 (MVP模式)", result.getMessage());
        assertEquals(systemAdmin, result.getRecipient());
        assertNotNull(result.getSentAt());

        // 验证监控日志记录了正确的信息
        verify(monitoringLogService, times(1))
                .logEmailAlert(eq(sslCertificate), eq(7), eq(systemAdmin));
    }

    @Test
    @DisplayName("功能验证：不同预警级别的邮件内容差异")
    void testDifferentAlertLevelContent() {
        // Given - 不同紧急程度的证书
        Certificate cert = createRealWorldCertificate("测试证书", "test.domain.com", "Test CA", 15);
        
        // 测试不同预警级别
        int[] alertDays = {30, 15, 7, 1, 0, -5};
        String[] expectedAlertTypes = {"30天预警", "15天预警", "7天预警", "1天预警", "已过期预警", "已过期预警"};

        for (int i = 0; i < alertDays.length; i++) {
            // When
            EmailTemplate.EmailContent content = emailTemplate.generateExpiryAlertContent(
                    cert, alertDays[i], null);

            // Then
            assertNotNull(content, "邮件内容不应为空");
            assertTrue(content.getContent().contains(expectedAlertTypes[i]), 
                    "预警类型应正确识别: " + expectedAlertTypes[i]);
            
            // 验证紧急程度在主题中体现
            if (alertDays[i] <= 1) {
                assertTrue(content.getSubject().contains("🚨"), "紧急预警应包含警告图标");
            }
        }
    }

    @Test
    @DisplayName("功能验证：每日摘要邮件的业务价值")
    void testDailySummaryBusinessValue() {
        // Given - 模拟真实的证书组合
        List<Certificate> expiringSoonCerts = Arrays.asList(
                createRealWorldCertificate("主站SSL", "www.company.com", "DigiCert", 15),
                createRealWorldCertificate("API网关", "api.company.com", "Let's Encrypt", 7),
                createRealWorldCertificate("CDN证书", "cdn.company.com", "Cloudflare", 3)
        );

        List<Certificate> expiredCerts = Arrays.asList(
                createRealWorldCertificate("测试环境", "test.company.com", "Self-Signed", -2)
        );

        String opsTeam = "ops-team@company.com";

        // When - 发送每日摘要
        EmailResult result = emailService.sendDailySummary(expiringSoonCerts, expiredCerts, opsTeam);

        // Then - 验证业务价值
        assertTrue(result.isSuccess(), "每日摘要应该成功发送");

        // 验证摘要内容包含管理所需的关键信息
        EmailTemplate.EmailContent content = emailTemplate.generateDailySummaryContent(
                expiringSoonCerts, expiredCerts, null);

        // 检查统计信息
        assertTrue(content.getContent().contains("即将过期证书：3个"), "应显示即将过期证书数量");
        assertTrue(content.getContent().contains("已过期证书：1个"), "应显示已过期证书数量");

        // 检查具体证书信息
        assertTrue(content.getContent().contains("主站SSL"), "应包含主要证书信息");
        assertTrue(content.getContent().contains("www.company.com"), "应包含域名信息");

        // 验证监控日志记录
        verify(monitoringLogService, times(1))
                .logDailySummary(eq(expiringSoonCerts), eq(expiredCerts), eq(opsTeam));
    }

    @Test
    @DisplayName("功能验证：高频批量预警处理性能")
    void testHighVolumeBatchAlertProcessing() {
        // Given - 模拟大量证书预警场景
        List<Certificate> largeBatchCerts = Arrays.asList(
                createRealWorldCertificate("电商主站", "shop.company.com", "DigiCert", 30),
                createRealWorldCertificate("支付网关", "pay.company.com", "DigiCert", 25),
                createRealWorldCertificate("用户中心", "user.company.com", "Let's Encrypt", 20),
                createRealWorldCertificate("订单系统", "order.company.com", "Let's Encrypt", 15),
                createRealWorldCertificate("库存管理", "inventory.company.com", "DigiCert", 10),
                createRealWorldCertificate("客服系统", "support.company.com", "Let's Encrypt", 7),
                createRealWorldCertificate("数据分析", "analytics.company.com", "DigiCert", 5),
                createRealWorldCertificate("监控告警", "monitoring.company.com", "Let's Encrypt", 3)
        );

        String securityTeam = "security@company.com";

        // When - 批量处理预警
        long startTime = System.currentTimeMillis();
        List<EmailResult> results = emailService.sendBatchAlerts(largeBatchCerts, securityTeam);
        long processingTime = System.currentTimeMillis() - startTime;

        // Then - 验证性能和正确性
        assertEquals(8, results.size(), "应处理所有8个证书");
        assertTrue(processingTime < 5000, "批量处理应在5秒内完成"); // 性能要求

        // 验证所有预警都成功
        for (EmailResult result : results) {
            assertTrue(result.isSuccess(), "每个预警都应成功");
            assertEquals(securityTeam, result.getRecipient());
        }

        // 验证监控日志记录次数正确
        verify(monitoringLogService, times(8))
                .logEmailAlert(any(Certificate.class), anyInt(), eq(securityTeam));
    }

    @Test
    @DisplayName("功能验证：业务连续性场景下的错误恢复")
    void testBusinessContinuityErrorRecovery() {
        // Given - 模拟关键业务证书和系统异常
        Certificate criticalCert = createRealWorldCertificate(
                "核心支付证书",
                "payment.critical-system.com",
                "Extended Validation CA",
                1 // 明天过期！
        );

        String emergencyContact = "emergency@company.com";

        // 模拟第一次调用失败（网络问题）
        doThrow(new RuntimeException("网络连接超时"))
                .doNothing() // 第二次调用成功
                .when(monitoringLogService)
                .logEmailAlert(any(Certificate.class), anyInt(), anyString());

        // When - 第一次调用失败
        EmailResult firstAttempt = emailService.sendExpiryAlertEmail(criticalCert, 1, emergencyContact);

        // Then - 验证失败被正确处理
        assertFalse(firstAttempt.isSuccess(), "第一次调用应该失败");
        assertEquals("LOG_EMAIL_FAILED", firstAttempt.getErrorCode());

        // When - 第二次调用成功（模拟重试机制）
        EmailResult secondAttempt = emailService.sendExpiryAlertEmail(criticalCert, 1, emergencyContact);

        // Then - 验证恢复成功
        assertTrue(secondAttempt.isSuccess(), "第二次调用应该成功");
        assertEquals(emergencyContact, secondAttempt.getRecipient());

        // 验证调用次数
        verify(monitoringLogService, times(2))
                .logEmailAlert(eq(criticalCert), eq(1), eq(emergencyContact));
    }

    @Test
    @DisplayName("功能验证：邮件模板变量替换的准确性")
    void testEmailTemplateVariableAccuracy() {
        // Given - 具有特殊字符的真实证书信息
        Certificate complexCert = Certificate.builder()
                .id(999L)
                .name("复杂证书名称 (包含特殊字符)")
                .domain("特殊-域名.example.com")
                .issuer("CA机构 & 认证中心")
                .issueDate(new Date(System.currentTimeMillis() - 86400000L * 90))
                .expiryDate(new Date(System.currentTimeMillis() + 86400000L * 5))
                .certificateType("EV SSL")
                .status(CertificateStatus.EXPIRING_SOON)
                .build();

        // When - 生成邮件内容
        EmailTemplate.EmailContent content = emailTemplate.generateExpiryAlertContent(
                complexCert, 5, null);

        // Then - 验证变量替换准确性
        assertNotNull(content);
        assertTrue(content.getSubject().contains("复杂证书名称"), "证书名称应正确替换");
        assertTrue(content.getContent().contains("特殊-域名.example.com"), "域名应正确替换");
        assertTrue(content.getContent().contains("CA机构 & 认证中心"), "颁发机构应正确替换");
        assertTrue(content.getContent().contains("5天"), "剩余天数应正确替换");
        assertTrue(content.getContent().contains("EV SSL"), "证书类型应正确替换");

        // 验证日期格式正确
        String contentText = content.getContent();
        // 使用Pattern.DOTALL标志来处理多行文本，或者直接使用contains检查
        assertTrue(contentText.matches("(?s).*\\d{4}-\\d{2}-\\d{2}.*"), "应包含正确的日期格式");
    }

    @Test
    @DisplayName("功能验证：多语言和特殊字符处理")
    void testMultiLanguageAndSpecialCharacterHandling() {
        // Given - 包含中文、英文、特殊字符的证书
        Certificate multiLangCert = createRealWorldCertificate(
                "测试证书-Test Certificate (αβγ)",
                "中文域名.example.com",
                "国际认证机构 International CA",
                10
        );

        String recipient = "国际团队@company.com";

        // When
        EmailResult result = emailService.sendExpiryAlertEmail(multiLangCert, 10, recipient);

        // Then
        assertTrue(result.isSuccess(), "多语言证书预警应该成功");
        assertEquals(recipient, result.getRecipient());

        // 验证内容生成正确
        EmailTemplate.EmailContent content = emailTemplate.generateExpiryAlertContent(
                multiLangCert, 10, null);
        
        assertTrue(content.getContent().contains("测试证书-Test Certificate"), "应支持中英文混合");
        assertTrue(content.getContent().contains("中文域名.example.com"), "应支持中文域名");
        assertTrue(content.getContent().contains("国际认证机构"), "应支持中文机构名");
    }

    /**
     * 创建真实场景的证书对象
     */
    private Certificate createRealWorldCertificate(String name, String domain, String issuer, int daysUntilExpiry) {
        long currentTime = System.currentTimeMillis();
        Date issueDate = new Date(currentTime - 86400000L * 30); // 30天前颁发
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
                .id((long) (Math.random() * 1000))
                .name(name)
                .domain(domain)
                .issuer(issuer)
                .issueDate(issueDate)
                .expiryDate(expiryDate)
                .certificateType("SSL")
                .status(status)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }
}