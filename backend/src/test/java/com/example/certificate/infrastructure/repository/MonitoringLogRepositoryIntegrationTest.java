package com.example.certificate.infrastructure.repository;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.domain.model.LogType;
import com.example.certificate.domain.model.MonitoringLog;
import com.example.certificate.domain.repository.CertificateRepository;
import com.example.certificate.domain.repository.MonitoringLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MonitoringLogRepositoryIntegrationTest {
    
    @Autowired
    private MonitoringLogRepository monitoringLogRepository;
    
    @Autowired
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
                .build();
        Certificate saved = certificateRepository.save(certificate);
        certificateId = saved.getId();
    }
    
    @Test
    @DisplayName("测试保存监控日志")
    void testSaveMonitoringLog() {
        MonitoringLog log = MonitoringLog.builder()
                .certificateId(certificateId)
                .logType(LogType.MONITORING)
                .logTime(new Date())
                .message("证书检查完成")
                .daysUntilExpiry(365)
                .build();
        
        MonitoringLog saved = monitoringLogRepository.save(log);
        
        assertNotNull(saved.getId());
        assertEquals(certificateId, saved.getCertificateId());
        assertEquals(LogType.MONITORING, saved.getLogType());
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
        monitoringLogRepository.save(createTestLog());
        monitoringLogRepository.save(createTestLog());
        
        List<MonitoringLog> logs = monitoringLogRepository.findByCertificateId(certificateId);
        
        assertTrue(logs.size() >= 2);
        logs.forEach(log -> assertEquals(certificateId, log.getCertificateId()));
    }
    
    @Test
    @DisplayName("测试根据日志类型查找")
    void testFindByLogType() {
        MonitoringLog monitoring = createTestLog();
        monitoring.setLogType(LogType.MONITORING);
        monitoringLogRepository.save(monitoring);
        
        MonitoringLog alert = createTestLog();
        alert.setLogType(LogType.ALERT_EMAIL);
        monitoringLogRepository.save(alert);
        
        List<MonitoringLog> monitoringLogs = monitoringLogRepository.findByLogType(LogType.MONITORING);
        List<MonitoringLog> alertLogs = monitoringLogRepository.findByLogType(LogType.ALERT_EMAIL);
        
        assertTrue(monitoringLogs.size() >= 1);
        assertTrue(alertLogs.size() >= 1);
    }
    
    @Test
    @DisplayName("测试根据时间范围查找日志")
    void testFindByLogTimeBetween() {
        Date now = new Date();
        Date startTime = new Date(now.getTime() - 24 * 60 * 60 * 1000);
        Date endTime = new Date(now.getTime() + 24 * 60 * 60 * 1000);
        
        MonitoringLog log = createTestLog();
        log.setLogTime(now);
        monitoringLogRepository.save(log);
        
        List<MonitoringLog> logs = monitoringLogRepository.findByLogTimeBetween(startTime, endTime);
        
        assertTrue(logs.size() >= 1);
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
        
        certificateRepository.deleteById(certificateId);
        
        List<MonitoringLog> logs = monitoringLogRepository.findByCertificateId(certificateId);
        assertTrue(logs.isEmpty());
    }
    
    private MonitoringLog createTestLog() {
        return MonitoringLog.builder()
                .certificateId(certificateId)
                .logType(LogType.MONITORING)
                .logTime(new Date())
                .message("测试日志消息")
                .daysUntilExpiry(365)
                .build();
    }
}