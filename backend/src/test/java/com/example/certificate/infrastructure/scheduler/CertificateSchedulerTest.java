package com.example.certificate.infrastructure.scheduler;

import com.example.certificate.service.MonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * CertificateScheduler 测试类
 * 测试定时任务的执行逻辑
 */
@ExtendWith(MockitoExtension.class)
class CertificateSchedulerTest {
    
    @Mock
    private MonitoringService monitoringService;
    
    @InjectMocks
    private CertificateScheduler certificateScheduler;
    
    @BeforeEach
    void setUp() {
        // Mock 设置在这里
    }
    
    @Test
    void should_call_monitoring_service_when_scheduled_task_runs() {
        // Given - MonitoringService 已经通过 @Mock 注入
        
        // When
        certificateScheduler.monitorCertificates();
        
        // Then
        verify(monitoringService, times(1)).monitorAllCertificates();
    }
    
    @Test
    void should_handle_exception_gracefully_when_monitoring_service_fails() {
        // Given
        doThrow(new RuntimeException("监控服务异常")).when(monitoringService).monitorAllCertificates();
        
        // When & Then - 应该不抛出异常
        certificateScheduler.monitorCertificates();
        
        // Verify 仍然会调用监控服务
        verify(monitoringService, times(1)).monitorAllCertificates();
    }
}