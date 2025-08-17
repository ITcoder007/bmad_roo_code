package com.example.certificate.service.impl;

import com.example.certificate.config.EmailConfig;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.domain.repository.CertificateRepository;
import com.example.certificate.service.EmailService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * DailySummaryServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("每日摘要服务测试")
class DailySummaryServiceTest {
    
    @Mock
    private CertificateRepository certificateRepository;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private EmailConfig emailConfig;
    
    @InjectMocks
    private DailySummaryServiceImpl dailySummaryService;
    
    private List<Certificate> testCertificates;
    private String testRecipient;
    
    @BeforeEach
    void setUp() {
        testCertificates = Arrays.asList(
                createCertificate(1L, "正常证书1", "normal1.example.com", CertificateStatus.NORMAL, 60),
                createCertificate(2L, "正常证书2", "normal2.example.com", CertificateStatus.NORMAL, 45),
                createCertificate(3L, "即将过期证书1", "expiring1.example.com", CertificateStatus.EXPIRING_SOON, 15),
                createCertificate(4L, "即将过期证书2", "expiring2.example.com", CertificateStatus.EXPIRING_SOON, 7),
                createCertificate(5L, "已过期证书", "expired.example.com", CertificateStatus.EXPIRED, -5)
        );
        
        testRecipient = "summary@example.com";
    }
    
    @Test
    @DisplayName("生成每日摘要 - 使用默认收件人")
    void testGenerateAndSendDailySummary_DefaultRecipient() {
        // Given
        when(emailConfig.isEnabled()).thenReturn(true);
        when(emailConfig.getDefaultRecipient()).thenReturn(testRecipient);
        when(certificateRepository.findAll()).thenReturn(testCertificates);
        when(emailService.sendDailySummary(any(), any(), anyString())).thenReturn(EmailResult.success("摘要发送成功", testRecipient));
        
        // When
        dailySummaryService.generateAndSendDailySummary();
        
        // Then
        verify(emailConfig, times(1)).isEnabled();
        verify(emailConfig, times(1)).getDefaultRecipient();
        verify(certificateRepository, times(1)).findAll();
        verify(emailService, times(1)).sendDailySummary(any(), any(), eq(testRecipient));
    }
    
    @Test
    @DisplayName("生成每日摘要 - 指定收件人")
    void testGenerateAndSendDailySummary_SpecificRecipient() {
        // Given
        String specificRecipient = "specific@example.com";
        when(emailConfig.isEnabled()).thenReturn(true);
        when(emailConfig.getMode()).thenReturn("log");
        when(certificateRepository.findAll()).thenReturn(testCertificates);
        when(emailService.sendDailySummary(any(), any(), anyString())).thenReturn(EmailResult.success("摘要发送成功", specificRecipient));
        
        // When
        dailySummaryService.generateAndSendDailySummary(specificRecipient);
        
        // Then
        verify(emailConfig, times(1)).isEnabled();
        verify(certificateRepository, times(1)).findAll();
        verify(emailService, times(1)).sendDailySummary(any(), any(), eq(specificRecipient));
    }
    
    @Test
    @DisplayName("生成每日摘要 - 邮件功能未启用")
    void testGenerateAndSendDailySummary_EmailDisabled() {
        // Given
        when(emailConfig.isEnabled()).thenReturn(false);
        
        // When
        dailySummaryService.generateAndSendDailySummary(testRecipient);
        
        // Then
        verify(emailConfig, times(1)).isEnabled();
        verify(certificateRepository, never()).findAll();
        verify(emailService, never()).sendDailySummary(any(), any(), anyString());
    }
    
    @Test
    @DisplayName("生成每日摘要 - 证书状态分类正确")
    void testGenerateAndSendDailySummary_CorrectClassification() {
        // Given
        when(emailConfig.isEnabled()).thenReturn(true);
        when(emailConfig.getMode()).thenReturn("log");
        when(certificateRepository.findAll()).thenReturn(testCertificates);
        when(emailService.sendDailySummary(any(), any(), anyString())).thenReturn(EmailResult.success("摘要发送成功", testRecipient));
        
        // When
        dailySummaryService.generateAndSendDailySummary(testRecipient);
        
        // Then
        verify(emailService, times(1)).sendDailySummary(argThat(expiringSoon -> {
            // 验证即将过期的证书数量（应该有2个）
            return expiringSoon.size() == 2;
        }), argThat(expired -> {
            // 验证已过期的证书数量（应该有1个）
            return expired.size() == 1;
        }), eq(testRecipient));
    }
    
    @Test
    @DisplayName("生成每日摘要 - 邮件发送失败")
    void testGenerateAndSendDailySummary_EmailSendFailure() {
        // Given
        when(emailConfig.isEnabled()).thenReturn(true);
        when(certificateRepository.findAll()).thenReturn(testCertificates);
        when(emailService.sendDailySummary(any(), any(), anyString()))
                .thenReturn(EmailResult.failure("发送失败", "SEND_ERROR", testRecipient));
        
        // When
        dailySummaryService.generateAndSendDailySummary(testRecipient);
        
        // Then
        verify(emailConfig, times(1)).isEnabled();
        verify(certificateRepository, times(1)).findAll();
        verify(emailService, times(1)).sendDailySummary(any(), any(), eq(testRecipient));
    }
    
    @Test
    @DisplayName("生成每日摘要 - 数据库查询异常")
    void testGenerateAndSendDailySummary_DatabaseException() {
        // Given
        when(emailConfig.isEnabled()).thenReturn(true);
        when(certificateRepository.findAll()).thenThrow(new RuntimeException("数据库连接异常"));
        
        // When
        dailySummaryService.generateAndSendDailySummary(testRecipient);
        
        // Then
        verify(emailConfig, times(1)).isEnabled();
        verify(certificateRepository, times(1)).findAll();
        verify(emailService, never()).sendDailySummary(any(), any(), anyString());
    }
    
    @Test
    @DisplayName("生成每日摘要 - 没有证书")
    void testGenerateAndSendDailySummary_NoCertificates() {
        // Given
        when(emailConfig.isEnabled()).thenReturn(true);
        when(emailConfig.getMode()).thenReturn("log");
        when(certificateRepository.findAll()).thenReturn(Arrays.asList());
        when(emailService.sendDailySummary(any(), any(), anyString())).thenReturn(EmailResult.success("摘要发送成功", testRecipient));
        
        // When
        dailySummaryService.generateAndSendDailySummary(testRecipient);
        
        // Then
        verify(emailService, times(1)).sendDailySummary(argThat(expiringSoon -> {
            return expiringSoon.isEmpty();
        }), argThat(expired -> {
            return expired.isEmpty();
        }), eq(testRecipient));
    }
    
    @Test
    @DisplayName("生成每日摘要 - 只有正常状态证书")
    void testGenerateAndSendDailySummary_OnlyNormalCertificates() {
        // Given
        List<Certificate> normalCertificates = Arrays.asList(
                createCertificate(1L, "正常证书1", "normal1.example.com", CertificateStatus.NORMAL, 60),
                createCertificate(2L, "正常证书2", "normal2.example.com", CertificateStatus.NORMAL, 90)
        );
        
        when(emailConfig.isEnabled()).thenReturn(true);
        when(emailConfig.getMode()).thenReturn("log");
        when(certificateRepository.findAll()).thenReturn(normalCertificates);
        when(emailService.sendDailySummary(any(), any(), anyString())).thenReturn(EmailResult.success("摘要发送成功", testRecipient));
        
        // When
        dailySummaryService.generateAndSendDailySummary(testRecipient);
        
        // Then
        verify(emailService, times(1)).sendDailySummary(argThat(expiringSoon -> {
            return expiringSoon.isEmpty();
        }), argThat(expired -> {
            return expired.isEmpty();
        }), eq(testRecipient));
    }
    
    /**
     * 创建测试证书对象的辅助方法
     */
    private Certificate createCertificate(Long id, String name, String domain, CertificateStatus status, int daysUntilExpiry) {
        long currentTime = System.currentTimeMillis();
        Date expiryDate = new Date(currentTime + daysUntilExpiry * 86400000L);
        
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