package com.example.certificate.infrastructure.external.email;

import com.example.certificate.config.EmailConfig;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
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
 * EmailTemplate 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("邮件模板测试")
class EmailTemplateTest {
    
    @InjectMocks
    private EmailTemplate emailTemplate;
    
    private Certificate testCertificate;
    
    @BeforeEach
    void setUp() {
        testCertificate = Certificate.builder()
                .id(1L)
                .name("测试证书")
                .domain("test.example.com")
                .issuer("Test CA")
                .issueDate(new Date(System.currentTimeMillis() - 86400000L * 30)) // 30天前颁发
                .expiryDate(new Date(System.currentTimeMillis() + 86400000L * 15)) // 15天后过期
                .certificateType("SSL")
                .status(CertificateStatus.EXPIRING_SOON)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }
    
    @Test
    @DisplayName("生成过期预警邮件内容 - 使用默认模板")
    void testGenerateExpiryAlertContent_DefaultTemplate() {
        // Given
        int daysUntilExpiry = 15;
        
        // When
        EmailTemplate.EmailContent content = emailTemplate.generateExpiryAlertContent(
                testCertificate, daysUntilExpiry, null);
        
        // Then
        assertNotNull(content);
        assertNotNull(content.getSubject());
        assertNotNull(content.getContent());
        assertFalse(content.isHtmlEnabled());
        
        // 验证主题包含证书名称
        assertTrue(content.getSubject().contains("测试证书"));
        assertTrue(content.getSubject().contains("🚨"));
        
        // 验证内容包含关键信息
        assertTrue(content.getContent().contains("测试证书"));
        assertTrue(content.getContent().contains("test.example.com"));
        assertTrue(content.getContent().contains("15天"));
        assertTrue(content.getContent().contains("15天预警"));
    }
    
    @Test
    @DisplayName("生成过期预警邮件内容 - 使用自定义模板")
    void testGenerateExpiryAlertContent_CustomTemplate() {
        // Given
        int daysUntilExpiry = 7;
        EmailConfig.EmailTemplateConfig templateConfig = new EmailConfig.EmailTemplateConfig();
        templateConfig.setSubject("自定义主题 - {certificateName} 还有 {daysUntilExpiry} 天");
        templateConfig.setContentTemplate("证书 {certificateName} 在域名 {domain} 上还有 {daysUntilExpiry} 天过期。");
        templateConfig.setHtmlEnabled(true);
        
        // When
        EmailTemplate.EmailContent content = emailTemplate.generateExpiryAlertContent(
                testCertificate, daysUntilExpiry, templateConfig);
        
        // Then
        assertNotNull(content);
        assertTrue(content.isHtmlEnabled());
        
        // 验证模板变量替换
        assertEquals("自定义主题 - 测试证书 还有 7 天", content.getSubject());
        assertEquals("证书 测试证书 在域名 test.example.com 上还有 7 天过期。", content.getContent());
    }
    
    @Test
    @DisplayName("生成每日摘要邮件内容 - 使用默认模板")
    void testGenerateDailySummaryContent_DefaultTemplate() {
        // Given
        List<Certificate> expiringSoonCertificates = Arrays.asList(
                createCertificate(1L, "证书1", "site1.example.com", 10),
                createCertificate(2L, "证书2", "site2.example.com", 5)
        );
        
        List<Certificate> expiredCertificates = Arrays.asList(
                createCertificate(3L, "过期证书", "old.example.com", -3)
        );
        
        // When
        EmailTemplate.EmailContent content = emailTemplate.generateDailySummaryContent(
                expiringSoonCertificates, expiredCertificates, null);
        
        // Then
        assertNotNull(content);
        assertNotNull(content.getSubject());
        assertNotNull(content.getContent());
        assertFalse(content.isHtmlEnabled());
        
        // 验证主题包含日期
        assertTrue(content.getSubject().contains("📊"));
        assertTrue(content.getSubject().contains("每日摘要"));
        
        // 验证内容包含统计信息
        assertTrue(content.getContent().contains("即将过期证书：2个"));
        assertTrue(content.getContent().contains("已过期证书：1个"));
        
        // 验证包含证书详情
        assertTrue(content.getContent().contains("证书1"));
        assertTrue(content.getContent().contains("证书2"));
        assertTrue(content.getContent().contains("过期证书"));
    }
    
    @Test
    @DisplayName("生成每日摘要邮件内容 - 使用自定义模板")
    void testGenerateDailySummaryContent_CustomTemplate() {
        // Given
        List<Certificate> expiringSoonCertificates = Arrays.asList(testCertificate);
        List<Certificate> expiredCertificates = Arrays.asList();
        
        EmailConfig.EmailTemplateConfig templateConfig = new EmailConfig.EmailTemplateConfig();
        templateConfig.setSubject("摘要 {currentDate} - 即将过期 {expiringSoonCount} 个");
        templateConfig.setContentTemplate("今日报告：即将过期 {expiringSoonCount} 个，已过期 {expiredCount} 个。");
        templateConfig.setHtmlEnabled(false);
        
        // When
        EmailTemplate.EmailContent content = emailTemplate.generateDailySummaryContent(
                expiringSoonCertificates, expiredCertificates, templateConfig);
        
        // Then
        assertNotNull(content);
        assertFalse(content.isHtmlEnabled());
        
        // 验证模板变量替换
        assertTrue(content.getSubject().contains("即将过期 1 个"));
        assertEquals("今日报告：即将过期 1 个，已过期 0 个。", content.getContent());
    }
    
    @Test
    @DisplayName("生成每日摘要邮件内容 - 空列表")
    void testGenerateDailySummaryContent_EmptyLists() {
        // Given
        List<Certificate> expiringSoonCertificates = Arrays.asList();
        List<Certificate> expiredCertificates = Arrays.asList();
        
        // When
        EmailTemplate.EmailContent content = emailTemplate.generateDailySummaryContent(
                expiringSoonCertificates, expiredCertificates, null);
        
        // Then
        assertNotNull(content);
        assertTrue(content.getContent().contains("即将过期证书：0个"));
        assertTrue(content.getContent().contains("已过期证书：0个"));
    }
    
    @Test
    @DisplayName("测试不同预警类型的识别")
    void testDifferentAlertTypes() {
        // 测试不同剩余天数对应的预警类型
        int[] testDays = {-5, 0, 1, 7, 15, 30, 60};
        String[] expectedTypes = {"已过期预警", "已过期预警", "1天预警", "7天预警", "15天预警", "30天预警", "常规监控"};
        
        for (int i = 0; i < testDays.length; i++) {
            // When
            EmailTemplate.EmailContent content = emailTemplate.generateExpiryAlertContent(
                    testCertificate, testDays[i], null);
            
            // Then
            assertTrue(content.getContent().contains(expectedTypes[i]), 
                    "测试天数 " + testDays[i] + " 应该对应预警类型 " + expectedTypes[i]);
        }
    }
    
    @Test
    @DisplayName("测试模板变量替换的边界情况")
    void testTemplateVariableReplacementEdgeCases() {
        // Given
        EmailConfig.EmailTemplateConfig templateConfig = new EmailConfig.EmailTemplateConfig();
        templateConfig.setSubject("测试 {nonExistentVariable} 和 {certificateName}");
        templateConfig.setContentTemplate("内容: {certificateName} - {anotherNonExistent}");
        
        // When
        EmailTemplate.EmailContent content = emailTemplate.generateExpiryAlertContent(
                testCertificate, 10, templateConfig);
        
        // Then
        // 不存在的变量应该被替换为空字符串
        assertEquals("测试  和 测试证书", content.getSubject());
        assertEquals("内容: 测试证书 - ", content.getContent());
    }
    
    @Test
    @DisplayName("测试null值处理")
    void testNullValueHandling() {
        // Given
        Certificate nullFieldCertificate = Certificate.builder()
                .id(1L)
                .name(null) // null名称
                .domain("test.domain.com")
                .build();
        
        // When
        EmailTemplate.EmailContent content = emailTemplate.generateExpiryAlertContent(
                nullFieldCertificate, 5, null);
        
        // Then
        assertNotNull(content);
        assertNotNull(content.getSubject());
        assertNotNull(content.getContent());
        // 应该能够处理null值而不抛出异常
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