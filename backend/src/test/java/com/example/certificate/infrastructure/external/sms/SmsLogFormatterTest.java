package com.example.certificate.infrastructure.external.sms;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SmsLogFormatter 单元测试
 */
@DisplayName("短信日志格式化器测试")
class SmsLogFormatterTest {
    
    private SmsLogFormatter formatter;
    private Certificate testCertificate;
    private String testRecipient;
    
    @BeforeEach
    void setUp() {
        formatter = new SmsLogFormatter();
        
        testCertificate = Certificate.builder()
                .id(1L)
                .name("测试证书")
                .domain("test.example.com")
                .issuer("测试CA")
                .issueDate(new Date(System.currentTimeMillis() - 86400000L * 30))
                .expiryDate(new Date(System.currentTimeMillis() + 86400000L * 15))
                .certificateType("SSL")
                .status(CertificateStatus.EXPIRING_SOON)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        
        testRecipient = "13812345678";
    }
    
    @Test
    @DisplayName("格式化过期预警消息")
    void testFormatExpiryAlert() {
        // Given
        int daysUntilExpiry = 15;
        
        // When
        String result = formatter.formatExpiryAlert(testCertificate, daysUntilExpiry, testRecipient);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("138****5678")); // 脱敏后的手机号
        assertTrue(result.contains("测试证书"));
        assertTrue(result.contains("test.example.com"));
        assertTrue(result.contains("15天"));
        assertTrue(result.contains("15天预警"));
    }
    
    @Test
    @DisplayName("格式化每日摘要消息")
    void testFormatDailySummary() {
        // Given
        List<Certificate> expiringSoon = Arrays.asList(testCertificate);
        List<Certificate> expired = Collections.emptyList();
        
        // When
        String result = formatter.formatDailySummary(expiringSoon, expired, testRecipient);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("138****5678")); // 脱敏后的手机号
        assertTrue(result.contains("1个")); // 即将过期证书数量
        assertTrue(result.contains("0个")); // 已过期证书数量
    }
    
    @Test
    @DisplayName("格式化证书详细信息")
    void testFormatCertificateDetails() {
        // Given
        int daysUntilExpiry = 7;
        
        // When
        String result = formatter.formatCertificateDetails(testCertificate, daysUntilExpiry, testRecipient);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("证书详情"));
        assertTrue(result.contains("测试证书"));
        assertTrue(result.contains("test.example.com"));
        assertTrue(result.contains("138****5678"));
        assertTrue(result.contains("7天"));
        assertTrue(result.contains("7天预警"));
    }
    
    @Test
    @DisplayName("格式化批量统计信息")
    void testFormatBatchStatistics() {
        // Given
        int total = 10;
        int success = 8;
        int failure = 2;
        
        // When
        String result = formatter.formatBatchStatistics(total, success, failure);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("成功: 8"));
        assertTrue(result.contains("失败: 2"));
        assertTrue(result.contains("总计: 10"));
    }
    
    @Test
    @DisplayName("格式化短信内容模板 - 即将过期")
    void testFormatSmsContent_ExpiringSoon() {
        // Given
        int daysUntilExpiry = 7;
        
        // When
        String result = formatter.formatSmsContent(testCertificate, daysUntilExpiry);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("【证书管理】"));
        assertTrue(result.contains("测试证书"));
        assertTrue(result.contains("7天"));
        assertTrue(result.contains("请及时处理"));
    }
    
    @Test
    @DisplayName("格式化短信内容模板 - 已过期")
    void testFormatSmsContent_Expired() {
        // Given
        int daysUntilExpiry = -3; // 已过期3天
        
        // When
        String result = formatter.formatSmsContent(testCertificate, daysUntilExpiry);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("【证书管理】"));
        assertTrue(result.contains("测试证书"));
        assertTrue(result.contains("已过期3天"));
        assertTrue(result.contains("请立即处理"));
    }
    
    @Test
    @DisplayName("格式化每日摘要短信内容")
    void testFormatDailySummarySmsContent() {
        // Given
        int expiringSoonCount = 5;
        int expiredCount = 2;
        
        // When
        String result = formatter.formatDailySummarySmsContent(expiringSoonCount, expiredCount);
        
        // Then
        assertNotNull(result);
        assertTrue(result.contains("【证书管理】"));
        assertTrue(result.contains("即将过期5个"));
        assertTrue(result.contains("已过期2个"));
    }
    
    @Test
    @DisplayName("验证短信内容长度")
    void testIsContentValid() {
        // Given
        String shortContent = "短内容";
        StringBuilder longContentBuilder = new StringBuilder();
        for (int i = 0; i < 200; i++) { // 增加到200次，确保超过500字符
            longContentBuilder.append("很长的内容");
        }
        String longContent = longContentBuilder.toString(); // 会超过长度限制
        String nullContent = null;
        
        // When & Then
        assertTrue(formatter.isContentValid(shortContent));
        assertFalse(formatter.isContentValid(longContent));
        assertFalse(formatter.isContentValid(nullContent));
    }
    
    @Test
    @DisplayName("验证手机号格式")
    void testIsPhoneValid() {
        // Given
        String validPhone = "13812345678";
        String invalidPhone1 = "12345678901"; // 不以1开头
        String invalidPhone2 = "1381234567"; // 长度不够
        String invalidPhone3 = "138123456789"; // 长度过长
        String invalidPhone4 = "13a12345678"; // 包含非数字
        String nullPhone = null;
        
        // When & Then
        assertTrue(formatter.isPhoneValid(validPhone));
        assertFalse(formatter.isPhoneValid(invalidPhone1));
        assertFalse(formatter.isPhoneValid(invalidPhone2));
        assertFalse(formatter.isPhoneValid(invalidPhone3));
        assertFalse(formatter.isPhoneValid(invalidPhone4));
        assertFalse(formatter.isPhoneValid(nullPhone));
    }
    
    @Test
    @DisplayName("测试不同预警类型的格式化")
    void testDifferentAlertTypes() {
        // Test 30天预警
        String result30 = formatter.formatExpiryAlert(testCertificate, 30, testRecipient);
        assertTrue(result30.contains("30天预警"));
        
        // Test 15天预警
        String result15 = formatter.formatExpiryAlert(testCertificate, 15, testRecipient);
        assertTrue(result15.contains("15天预警"));
        
        // Test 7天预警
        String result7 = formatter.formatExpiryAlert(testCertificate, 7, testRecipient);
        assertTrue(result7.contains("7天预警"));
        
        // Test 1天预警
        String result1 = formatter.formatExpiryAlert(testCertificate, 1, testRecipient);
        assertTrue(result1.contains("1天预警"));
        
        // Test 已过期
        String resultExpired = formatter.formatExpiryAlert(testCertificate, -1, testRecipient);
        assertTrue(resultExpired.contains("已过期预警"));
        
        // Test 常规监控
        String resultNormal = formatter.formatExpiryAlert(testCertificate, 60, testRecipient);
        assertTrue(resultNormal.contains("常规监控"));
    }
    
    @Test
    @DisplayName("测试手机号脱敏边界情况")
    void testPhoneNumberMaskingEdgeCases() {
        // Given
        Certificate cert = testCertificate;
        int days = 15;
        
        // Test 正确格式手机号
        String validPhone = "13812345678";
        String result1 = formatter.formatExpiryAlert(cert, days, validPhone);
        assertTrue(result1.contains("138****5678"));
        
        // Test 空手机号
        String result2 = formatter.formatExpiryAlert(cert, days, null);
        assertTrue(result2.contains("null")); // 应该显示原值
        
        // Test 格式不正确的手机号
        String invalidPhone = "12345";
        String result3 = formatter.formatExpiryAlert(cert, days, invalidPhone);
        assertTrue(result3.contains("12345")); // 应该显示原值，不脱敏
    }
}