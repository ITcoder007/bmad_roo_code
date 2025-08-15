package com.example.certificate.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class MonitoringLogTest {
    
    @Test
    @DisplayName("测试MonitoringLog实体创建和属性访问")
    void testMonitoringLogCreation() {
        Date now = new Date();
        
        MonitoringLog log = MonitoringLog.builder()
                .id(1L)
                .certificateId(100L)
                .logType(LogType.MONITORING)
                .logTime(now)
                .message("证书检查完成")
                .daysUntilExpiry(30)
                .createdAt(now)
                .build();
        
        assertNotNull(log);
        assertEquals(1L, log.getId());
        assertEquals(100L, log.getCertificateId());
        assertEquals(LogType.MONITORING, log.getLogType());
        assertEquals(now, log.getLogTime());
        assertEquals("证书检查完成", log.getMessage());
        assertEquals(30, log.getDaysUntilExpiry());
        assertEquals(now, log.getCreatedAt());
    }
    
    @Test
    @DisplayName("测试MonitoringLog的NoArgsConstructor")
    void testNoArgsConstructor() {
        MonitoringLog log = new MonitoringLog();
        assertNotNull(log);
        assertNull(log.getId());
        assertNull(log.getCertificateId());
    }
    
    @Test
    @DisplayName("测试MonitoringLog的AllArgsConstructor")
    void testAllArgsConstructor() {
        Date now = new Date();
        MonitoringLog log = new MonitoringLog(
                1L, 100L, LogType.ALERT_EMAIL, now,
                "证书即将过期", 7, now
        );
        
        assertNotNull(log);
        assertEquals(1L, log.getId());
        assertEquals(100L, log.getCertificateId());
        assertEquals(LogType.ALERT_EMAIL, log.getLogType());
    }
    
    @Test
    @DisplayName("测试MonitoringLog的setter方法")
    void testSetters() {
        MonitoringLog log = new MonitoringLog();
        log.setId(2L);
        log.setCertificateId(200L);
        log.setMessage("更新的消息");
        log.setDaysUntilExpiry(15);
        
        assertEquals(2L, log.getId());
        assertEquals(200L, log.getCertificateId());
        assertEquals("更新的消息", log.getMessage());
        assertEquals(15, log.getDaysUntilExpiry());
    }
    
    @Test
    @DisplayName("测试LogType枚举")
    void testLogType() {
        assertEquals(3, LogType.values().length);
        assertEquals(LogType.MONITORING, LogType.valueOf("MONITORING"));
        assertEquals(LogType.ALERT_EMAIL, LogType.valueOf("ALERT_EMAIL"));
        assertEquals(LogType.ALERT_SMS, LogType.valueOf("ALERT_SMS"));
    }
}