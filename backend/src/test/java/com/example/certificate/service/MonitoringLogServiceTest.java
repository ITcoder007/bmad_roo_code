package com.example.certificate.service;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.domain.model.LogType;
import com.example.certificate.domain.model.MonitoringLog;
import com.example.certificate.domain.repository.MonitoringLogRepository;
import com.example.certificate.service.impl.MonitoringLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * MonitoringLogService 测试类
 * 测试监控日志服务的业务逻辑
 */
@ExtendWith(MockitoExtension.class)
class MonitoringLogServiceTest {
    
    @Mock
    private MonitoringLogRepository monitoringLogRepository;
    
    @InjectMocks
    private MonitoringLogServiceImpl monitoringLogService;
    
    private Certificate testCertificate;
    
    @BeforeEach
    void setUp() {
        testCertificate = Certificate.builder()
                .id(1L)
                .name("test-cert")
                .domain("example.com")
                .status(CertificateStatus.NORMAL)
                .build();
    }
    
    @Test
    void should_log_certificate_monitoring_result() {
        // Given
        int daysUntilExpiry = 45;
        MonitoringLog savedLog = MonitoringLog.builder()
                .id(1L)
                .certificateId(1L)
                .logType(LogType.MONITORING)
                .build();
        when(monitoringLogRepository.save(any(MonitoringLog.class))).thenReturn(savedLog);
        
        // When
        monitoringLogService.logMonitoringResult(testCertificate, daysUntilExpiry);
        
        // Then
        ArgumentCaptor<MonitoringLog> logCaptor = ArgumentCaptor.forClass(MonitoringLog.class);
        verify(monitoringLogRepository, times(1)).save(logCaptor.capture());
        
        MonitoringLog capturedLog = logCaptor.getValue();
        assertThat(capturedLog.getCertificateId()).isEqualTo(1L);
        assertThat(capturedLog.getLogType()).isEqualTo(LogType.MONITORING);
        assertThat(capturedLog.getDaysUntilExpiry()).isEqualTo(45);
        assertThat(capturedLog.getMessage()).isNotNull();
        assertThat(capturedLog.getLogTime()).isNotNull();
    }
    
    @Test
    void should_log_certificate_status_change() {
        // Given
        CertificateStatus oldStatus = CertificateStatus.NORMAL;
        CertificateStatus newStatus = CertificateStatus.EXPIRING_SOON;
        MonitoringLog savedLog = MonitoringLog.builder()
                .id(1L)
                .certificateId(1L)
                .logType(LogType.STATUS_CHANGE)
                .build();
        when(monitoringLogRepository.save(any(MonitoringLog.class))).thenReturn(savedLog);
        
        // When
        monitoringLogService.logStatusChange(testCertificate, oldStatus, newStatus);
        
        // Then
        ArgumentCaptor<MonitoringLog> logCaptor = ArgumentCaptor.forClass(MonitoringLog.class);
        verify(monitoringLogRepository, times(1)).save(logCaptor.capture());
        
        MonitoringLog capturedLog = logCaptor.getValue();
        assertThat(capturedLog.getCertificateId()).isEqualTo(1L);
        assertThat(capturedLog.getLogType()).isEqualTo(LogType.STATUS_CHANGE);
        assertThat(capturedLog.getMessage()).contains("NORMAL", "EXPIRING_SOON");
        assertThat(capturedLog.getLogTime()).isNotNull();
    }
}