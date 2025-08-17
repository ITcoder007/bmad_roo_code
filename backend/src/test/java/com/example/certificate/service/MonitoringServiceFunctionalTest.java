package com.example.certificate.service;

import com.example.certificate.config.CertificateStatusConfig;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.domain.repository.CertificateRepository;
import com.example.certificate.infrastructure.scheduler.CertificateScheduler;
import com.example.certificate.service.impl.MonitoringServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * 监控服务功能验证测试
 * 验证整体监控流程的正确性
 */
@ExtendWith(MockitoExtension.class)
class MonitoringServiceFunctionalTest {
    
    @Mock
    private CertificateRepository certificateRepository;
    
    @Mock
    private CertificateService certificateService;
    
    @Mock
    private MonitoringLogService monitoringLogService;
    
    @Mock
    private CertificateStatusConfig certificateStatusConfig;
    
    @Mock
    private AlertRuleEngine alertRuleEngine;
    
    private MonitoringServiceImpl monitoringService;
    private CertificateScheduler certificateScheduler;
    
    @BeforeEach
    void setUp() {
        lenient().when(certificateStatusConfig.getExpiringSoonDays()).thenReturn(30);
        
        monitoringService = new MonitoringServiceImpl(
                certificateRepository, 
                certificateService, 
                certificateStatusConfig,
                monitoringLogService,
                alertRuleEngine
        );
        
        certificateScheduler = new CertificateScheduler(monitoringService);
    }
    
    @Test
    void should_execute_complete_monitoring_workflow_via_scheduler() {
        // Given - 准备测试证书数据
        List<Certificate> certificates = createTestCertificatesWithCorrectStatus();
        when(certificateRepository.findAll()).thenReturn(certificates);
        
        // When - 通过调度器触发监控
        certificateScheduler.monitorCertificates();
        
        // Then - 验证监控流程执行
        verify(certificateRepository).findAll();
        verify(monitoringLogService, times(3)).logMonitoringResult(any(Certificate.class), anyInt());
        
        // 由于证书状态已正确，不应该有状态更新
        verify(certificateService, never()).updateCertificateStatus(any(), any());
        verify(monitoringLogService, never()).logStatusChange(any(), any(), any());
    }
    
    @Test
    void should_handle_scheduler_exception_gracefully() {
        // Given - 模拟监控过程中的异常
        when(certificateRepository.findAll()).thenThrow(new RuntimeException("数据库连接失败"));
        
        // When - 调度器执行（应该不抛异常）
        certificateScheduler.monitorCertificates();
        
        // Then - 验证异常被正确处理
        verify(certificateRepository).findAll();
        // 由于异常，后续操作不会执行
        verify(monitoringLogService, never()).logMonitoringResult(any(), anyInt());
    }
    
    @Test
    void should_verify_monitoring_service_configuration() {
        // Given - 验证服务配置
        Certificate testCert = createCertificateExpiringIn(25);
        
        // When - 检查状态计算
        CertificateStatus status = monitoringService.checkCertificateStatusWithConfig(testCert);
        int daysUntilExpiry = monitoringService.calculateDaysUntilExpiry(testCert);
        
        // Then - 验证配置正确应用
        assert status == CertificateStatus.EXPIRING_SOON; // 25 < 30
        assert daysUntilExpiry >= 24 && daysUntilExpiry <= 26; // 允许时间差异
    }
    
    @Test
    void should_demonstrate_end_to_end_monitoring_capabilities() {
        // Given - 完整的测试场景
        List<Certificate> certificates = Arrays.asList(
                createCertificateWithStatus(45, CertificateStatus.EXPIRING_SOON), // 需要更新为NORMAL
                createCertificateWithStatus(20, CertificateStatus.NORMAL),        // 需要更新为EXPIRING_SOON
                createCertificateWithStatus(-5, CertificateStatus.NORMAL)         // 需要更新为EXPIRED
        );
        when(certificateRepository.findAll()).thenReturn(certificates);
        
        // When - 执行监控
        monitoringService.monitorAllCertificates();
        
        // Then - 验证系统能力
        
        // 1. 监控所有证书
        verify(monitoringLogService, times(3)).logMonitoringResult(any(Certificate.class), anyInt());
        
        // 2. 更新错误的状态
        verify(certificateService, times(3)).updateCertificateStatus(any(), any());
        
        // 3. 记录状态变更
        verify(monitoringLogService, times(3)).logStatusChange(any(), any(), any());
        
        // 4. 系统具备容错能力（通过日志验证）
        // 这在实际执行中会产生相应的日志输出
    }
    
    private List<Certificate> createTestCertificatesWithCorrectStatus() {
        Certificate normalCert = createCertificateExpiringIn(45);
        normalCert.setStatus(CertificateStatus.NORMAL); // 状态正确
        
        Certificate expiringSoonCert = createCertificateExpiringIn(20);
        expiringSoonCert.setStatus(CertificateStatus.EXPIRING_SOON); // 状态正确
        
        Certificate expiredCert = createCertificateExpiringIn(-5);
        expiredCert.setStatus(CertificateStatus.EXPIRED); // 状态正确
        
        return Arrays.asList(normalCert, expiringSoonCert, expiredCert);
    }
    
    private Certificate createCertificateWithStatus(int daysUntilExpiry, CertificateStatus status) {
        Certificate cert = createCertificateExpiringIn(daysUntilExpiry);
        cert.setStatus(status);
        return cert;
    }
    
    private Certificate createCertificateExpiringIn(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        
        return Certificate.builder()
                .id((long) Math.abs(days) == 0 ? 1L : Math.abs(days))
                .name("Test Certificate " + days)
                .domain("test" + Math.abs(days) + ".example.com")
                .issuer("Test CA")
                .expiryDate(calendar.getTime())
                .status(CertificateStatus.NORMAL)
                .build();
    }
}