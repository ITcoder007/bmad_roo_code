package com.example.certificate.service;

import com.example.certificate.config.CertificateStatusConfig;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.domain.model.LogType;
import com.example.certificate.domain.model.MonitoringLog;
import com.example.certificate.domain.repository.CertificateRepository;
import com.example.certificate.domain.repository.MonitoringLogRepository;
import com.example.certificate.service.impl.MonitoringServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 监控服务集成测试
 * 验证监控服务与证书服务、日志服务的完整集成
 */
@ExtendWith(MockitoExtension.class)
class MonitoringServiceIntegrationTest {
    
    @Mock
    private CertificateRepository certificateRepository;
    
    @Mock
    private CertificateService certificateService;
    
    @Mock
    private MonitoringLogService monitoringLogService;
    
    @Mock
    private CertificateStatusConfig certificateStatusConfig;
    
    private MonitoringServiceImpl monitoringService;
    
    @BeforeEach
    void setUp() {
        when(certificateStatusConfig.getExpiringSoonDays()).thenReturn(30);
        
        monitoringService = new MonitoringServiceImpl(
                certificateRepository, 
                certificateService, 
                certificateStatusConfig,
                monitoringLogService
        );
    }
    
    @Test
    void should_perform_end_to_end_monitoring_workflow() {
        // Given - 创建测试数据，包含不同状态的证书
        List<Certificate> certificates = createTestCertificates();
        when(certificateRepository.findAll()).thenReturn(certificates);
        
        // When - 执行完整的监控流程
        monitoringService.monitorAllCertificates();
        
        // Then - 验证完整的工作流
        
        // 1. 验证所有证书都被尝试监控
        verify(monitoringLogService, times(3)).logMonitoringResult(any(Certificate.class), anyInt());
        
        // 2. 验证状态变更的证书被正确更新（假设初始状态不正确）
        ArgumentCaptor<CertificateStatus> statusCaptor = ArgumentCaptor.forClass(CertificateStatus.class);
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(certificateService, times(3)).updateCertificateStatus(idCaptor.capture(), statusCaptor.capture());
        
        // 验证状态更新是正确的
        List<CertificateStatus> capturedStatuses = statusCaptor.getAllValues();
        assertThat(capturedStatuses).containsExactlyInAnyOrder(
                CertificateStatus.NORMAL,           // 正常证书
                CertificateStatus.EXPIRING_SOON,    // 即将过期
                CertificateStatus.EXPIRED           // 已过期
        );
        
        // 3. 验证状态变更日志被记录
        verify(monitoringLogService, times(3)).logStatusChange(
                any(Certificate.class), 
                any(CertificateStatus.class), 
                any(CertificateStatus.class)
        );
    }
    
    @Test
    void should_handle_mixed_success_and_failure_scenarios() {
        // Given - 混合成功和失败的场景
        List<Certificate> certificates = createTestCertificates();
        when(certificateRepository.findAll()).thenReturn(certificates);
        
        // 模拟第二个证书的日志记录失败
        doThrow(new RuntimeException("日志服务异常"))
            .when(monitoringLogService)
            .logMonitoringResult(argThat(cert -> cert.getId().equals(2L)), anyInt());
        
        // When - 执行监控（应该不抛异常）
        monitoringService.monitorAllCertificates();
        
        // Then - 验证容错行为
        // 第一个和第三个证书应该正常处理
        verify(certificateService, times(2)).updateCertificateStatus(any(), any());
        
        // 所有证书都尝试了日志记录
        verify(monitoringLogService, times(3)).logMonitoringResult(any(Certificate.class), anyInt());
    }
    
    @Test
    void should_correctly_calculate_and_log_certificate_status_changes() {
        // Given - 单个证书状态变更测试
        Certificate certificate = createCertificateExpiringIn(20);
        certificate.setStatus(CertificateStatus.NORMAL); // 当前状态错误
        
        // When - 监控单个证书
        monitoringService.monitorCertificate(certificate);
        
        // Then - 验证状态计算和更新流程
        
        // 1. 验证监控结果被记录
        verify(monitoringLogService).logMonitoringResult(eq(certificate), eq(20));
        
        // 2. 验证状态被正确更新为 EXPIRING_SOON
        verify(certificateService).updateCertificateStatus(eq(1L), eq(CertificateStatus.EXPIRING_SOON));
        
        // 3. 验证状态变更被记录
        verify(monitoringLogService).logStatusChange(
                eq(certificate), 
                eq(CertificateStatus.NORMAL), 
                eq(CertificateStatus.EXPIRING_SOON)
        );
    }
    
    @Test
    void should_not_update_status_when_certificate_status_is_already_correct() {
        // Given - 证书状态已经正确
        Certificate certificate = createCertificateExpiringIn(20);
        certificate.setStatus(CertificateStatus.EXPIRING_SOON); // 状态已正确
        
        // When - 监控证书
        monitoringService.monitorCertificate(certificate);
        
        // Then - 验证不会进行不必要的更新
        
        // 1. 仍然记录监控结果
        verify(monitoringLogService).logMonitoringResult(eq(certificate), eq(20));
        
        // 2. 不应该更新状态
        verify(certificateService, never()).updateCertificateStatus(any(), any());
        
        // 3. 不应该记录状态变更
        verify(monitoringLogService, never()).logStatusChange(any(), any(), any());
    }
    
    private List<Certificate> createTestCertificates() {
        Certificate normalCert = createCertificateExpiringIn(45);
        normalCert.setStatus(CertificateStatus.EXPIRING_SOON); // 错误状态，应该是NORMAL
        
        Certificate expiringSoonCert = createCertificateExpiringIn(20);
        expiringSoonCert.setStatus(CertificateStatus.NORMAL); // 错误状态，应该是EXPIRING_SOON
        
        Certificate expiredCert = createCertificateExpiringIn(-5);
        expiredCert.setStatus(CertificateStatus.NORMAL); // 错误状态，应该是EXPIRED
        
        return Arrays.asList(normalCert, expiringSoonCert, expiredCert);
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