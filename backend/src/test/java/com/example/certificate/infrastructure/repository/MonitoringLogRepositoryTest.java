package com.example.certificate.infrastructure.repository;

import com.example.certificate.domain.model.*;
import com.example.certificate.domain.repository.CertificateRepository;
import com.example.certificate.domain.repository.MonitoringLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MonitoringLogRepositoryTest {
    
    @Resource
    private MonitoringLogRepository monitoringLogRepository;
    
    @Resource
    private CertificateRepository certificateRepository;
    
    private Long certificateId;
    
    @BeforeEach
    void setUp() {
        Certificate certificate = Certificate.builder()
                .name("测试证书")
                .domain("test.example.com")
                .issuer("Test CA")
                .issueDate(new Date())
                .expiryDate(new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000))
                .certificateType("SSL")
                .status(CertificateStatus.NORMAL)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        Certificate saved = certificateRepository.save(certificate);
        certificateId = saved.getId();
    }
    
    @Test
    @DisplayName("测试保存监控日志")
    void testSaveMonitoringLog() {
        MonitoringLog log = createTestLog();
        MonitoringLog saved = monitoringLogRepository.save(log);
        
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(log.getMessage(), saved.getMessage());
        assertEquals(log.getLogType(), saved.getLogType());
    }
    
    @Test
    @DisplayName("测试根据ID查找日志")
    void testFindById() {
        MonitoringLog log = createTestLog();
        MonitoringLog saved = monitoringLogRepository.save(log);
        
        Optional<MonitoringLog> found = monitoringLogRepository.findById(saved.getId());
        
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals(saved.getMessage(), found.get().getMessage());
    }
    
    @Test
    @DisplayName("测试根据证书ID查找日志")
    void testFindByCertificateId() {
        MonitoringLog log1 = createTestLog();
        log1.setMessage("日志1");
        MonitoringLog log2 = createTestLog();
        log2.setMessage("日志2");
        
        monitoringLogRepository.save(log1);
        monitoringLogRepository.save(log2);
        
        List<MonitoringLog> logs = monitoringLogRepository.findByCertificateId(certificateId);
        
        assertNotNull(logs);
        assertTrue(logs.size() >= 2);
        assertTrue(logs.stream().anyMatch(l -> "日志1".equals(l.getMessage())));
        assertTrue(logs.stream().anyMatch(l -> "日志2".equals(l.getMessage())));
    }
    
    @Test
    @DisplayName("测试根据日志类型查找")
    void testFindByLogType() {
        MonitoringLog monitoring = createTestLog();
        monitoring.setLogType(LogType.MONITORING);
        monitoring.setMessage("监控日志");
        
        MonitoringLog alert = createTestLog();
        alert.setLogType(LogType.ALERT_EMAIL);
        alert.setMessage("邮件告警");
        
        monitoringLogRepository.save(monitoring);
        monitoringLogRepository.save(alert);
        
        List<MonitoringLog> monitoringLogs = monitoringLogRepository.findByLogType(LogType.MONITORING);
        List<MonitoringLog> alertLogs = monitoringLogRepository.findByLogType(LogType.ALERT_EMAIL);
        
        assertTrue(monitoringLogs.stream().anyMatch(l -> "监控日志".equals(l.getMessage())));
        assertTrue(alertLogs.stream().anyMatch(l -> "邮件告警".equals(l.getMessage())));
    }
    
    @Test
    @DisplayName("测试根据时间范围查找日志")
    void testFindByLogTimeBetween() {
        Date now = new Date();
        Date yesterday = new Date(now.getTime() - 24L * 60 * 60 * 1000);
        Date tomorrow = new Date(now.getTime() + 24L * 60 * 60 * 1000);
        
        MonitoringLog todayLog = createTestLog();
        todayLog.setLogTime(now);
        todayLog.setMessage("今天的日志");
        
        MonitoringLog yesterdayLog = createTestLog();
        yesterdayLog.setLogTime(yesterday);
        yesterdayLog.setMessage("昨天的日志");
        
        monitoringLogRepository.save(todayLog);
        monitoringLogRepository.save(yesterdayLog);
        
        List<MonitoringLog> logs = monitoringLogRepository.findByLogTimeBetween(
            new Date(yesterday.getTime() - 1000), 
            new Date(now.getTime() + 1000)
        );
        
        assertTrue(logs.stream().anyMatch(l -> "今天的日志".equals(l.getMessage())));
        assertTrue(logs.stream().anyMatch(l -> "昨天的日志".equals(l.getMessage())));
    }
    
    @Test
    @DisplayName("测试删除日志")
    void testDeleteById() {
        MonitoringLog log = createTestLog();
        MonitoringLog saved = monitoringLogRepository.save(log);
        Long id = saved.getId();
        
        monitoringLogRepository.deleteById(id);
        
        Optional<MonitoringLog> found = monitoringLogRepository.findById(id);
        assertFalse(found.isPresent());
    }
    
    @Test
    @DisplayName("测试级联删除")
    void testCascadeDelete() {
        MonitoringLog log = createTestLog();
        monitoringLogRepository.save(log);
        
        List<MonitoringLog> logs = monitoringLogRepository.findByCertificateId(certificateId);
        assertFalse(logs.isEmpty());
        
        certificateRepository.deleteById(certificateId);
        
        logs = monitoringLogRepository.findByCertificateId(certificateId);
        assertTrue(logs.isEmpty());
    }
    
    private MonitoringLog createTestLog() {
        return MonitoringLog.builder()
                .certificateId(certificateId)
                .logType(LogType.MONITORING)
                .logTime(new Date())
                .message("测试日志消息")
                .daysUntilExpiry(30)
                .createdAt(new Date())
                .build();
    }
}