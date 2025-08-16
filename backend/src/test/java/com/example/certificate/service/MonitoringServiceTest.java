package com.example.certificate.service;

import com.example.certificate.config.CertificateStatusConfig;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.domain.repository.CertificateRepository;
import com.example.certificate.service.impl.MonitoringServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

/**
 * 监控服务测试
 * 使用TDD方法验证监控服务的核心功能
 */
@ExtendWith(MockitoExtension.class)
class MonitoringServiceTest {

    @Mock
    private CertificateRepository certificateRepository;
    
    @Mock
    private CertificateService certificateService;
    
    @Mock
    private CertificateStatusConfig certificateStatusConfig;
    
    @InjectMocks
    private MonitoringServiceImpl monitoringService;

    private Certificate normalCertificate;
    private Certificate expiringSoonCertificate;
    private Certificate expiredCertificate;

    @BeforeEach
    void setUp() {
        // 使用lenient()处理可能不被调用的Mock
        lenient().when(certificateStatusConfig.getExpiringSoonDays()).thenReturn(30);
        
        // 创建测试数据
        normalCertificate = createCertificateExpiringIn(45); // 45天后到期 - NORMAL
        expiringSoonCertificate = createCertificateExpiringIn(20); // 20天后到期 - EXPIRING_SOON  
        expiredCertificate = createCertificateExpiringIn(-5); // 5天前已过期 - EXPIRED
    }

    @Test
    void should_monitor_all_certificates_and_update_status_when_called() {
        // Given - 准备测试数据，设置错误的初始状态以触发更新
        normalCertificate.setStatus(CertificateStatus.EXPIRING_SOON); // 错误状态，应该是NORMAL
        expiringSoonCertificate.setStatus(CertificateStatus.NORMAL); // 错误状态，应该是EXPIRING_SOON
        expiredCertificate.setStatus(CertificateStatus.NORMAL); // 错误状态，应该是EXPIRED
        
        List<Certificate> certificates = Arrays.asList(
            normalCertificate, 
            expiringSoonCertificate, 
            expiredCertificate
        );
        
        when(certificateRepository.findAll()).thenReturn(certificates);
        
        // When - 执行监控方法
        monitoringService.monitorAllCertificates();
        
        // Then - 验证结果
        verify(certificateRepository).findAll();
        verify(certificateService, times(3)).updateCertificateStatus(any(Long.class), any(CertificateStatus.class));
    }

    @Test
    void should_monitor_single_certificate_and_update_status_when_status_changed() {
        // Given - 证书状态需要更新
        Certificate certificate = createCertificateExpiringIn(20);
        certificate.setStatus(CertificateStatus.NORMAL); // 当前状态为NORMAL，但应该是EXPIRING_SOON
        
        // When - 监控单个证书
        monitoringService.monitorCertificate(certificate);
        
        // Then - 验证状态更新被调用
        verify(certificateService).updateCertificateStatus(certificate.getId(), CertificateStatus.EXPIRING_SOON);
    }

    @Test
    void should_not_update_status_when_certificate_status_unchanged() {
        // Given - 证书状态无需更新
        Certificate certificate = createCertificateExpiringIn(20);
        certificate.setStatus(CertificateStatus.EXPIRING_SOON); // 状态已正确
        
        // When - 监控单个证书
        monitoringService.monitorCertificate(certificate);
        
        // Then - 验证状态更新未被调用
        verify(certificateService, never()).updateCertificateStatus(any(), any());
    }

    @Test
    void should_calculate_correct_days_until_expiry() {
        // Given - 20天后到期的证书
        Certificate certificate = createCertificateExpiringIn(20);
        
        // When - 计算到期天数
        int days = monitoringService.calculateDaysUntilExpiry(certificate);
        
        // Then - 验证计算结果正确
        assert days == 20;
    }

    @Test
    void should_return_correct_certificate_status() {
        // When & Then - 验证不同情况下的状态计算
        CertificateStatus status1 = monitoringService.checkCertificateStatus(normalCertificate);
        assert status1 == CertificateStatus.NORMAL;
        
        CertificateStatus status2 = monitoringService.checkCertificateStatus(expiringSoonCertificate);
        assert status2 == CertificateStatus.EXPIRING_SOON;
        
        CertificateStatus status3 = monitoringService.checkCertificateStatus(expiredCertificate);
        assert status3 == CertificateStatus.EXPIRED;
    }
    
    @Test
    void should_use_configurable_threshold_for_status_calculation() {
        // Given - 设置不同的阈值
        when(certificateStatusConfig.getExpiringSoonDays()).thenReturn(25);
        Certificate certificate = createCertificateExpiringIn(28); // 28天后到期
        
        // When - 使用新阈值检查状态
        CertificateStatus status = monitoringService.checkCertificateStatusWithConfig(certificate);
        
        // Then - 应该返回NORMAL（因为28 > 25）
        assert status == CertificateStatus.NORMAL;
    }
    
    @Test
    void should_handle_edge_case_expiring_soon_threshold() {
        // Given - 边界值测试：恰好30天
        when(certificateStatusConfig.getExpiringSoonDays()).thenReturn(30);
        Certificate certificate = createCertificateExpiringIn(30); // 恰好30天后到期
        
        // When - 检查状态
        CertificateStatus status = monitoringService.checkCertificateStatusWithConfig(certificate);
        
        // Then - 应该返回EXPIRING_SOON（因为30 <= 30）
        assert status == CertificateStatus.EXPIRING_SOON;
    }

    private Certificate createCertificateExpiringIn(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        
        return Certificate.builder()
            .id((long) Math.abs(days)) // 使用天数作为ID避免冲突
            .name("Test Certificate " + days)
            .domain("test" + Math.abs(days) + ".example.com")
            .issuer("Test CA")
            .expiryDate(calendar.getTime())
            .status(CertificateStatus.NORMAL) // 默认状态
            .build();
    }
}