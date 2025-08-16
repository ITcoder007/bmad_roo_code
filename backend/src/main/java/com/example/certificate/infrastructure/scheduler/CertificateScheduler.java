package com.example.certificate.infrastructure.scheduler;

import com.example.certificate.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 证书监控定时任务调度器
 * 负责定时调用监控服务，检查证书状态
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateScheduler {
    
    private final MonitoringService monitoringService;
    
    /**
     * 每小时执行证书监控任务
     * cron 表达式: 每小时的整点执行
     */
    @Scheduled(cron = "0 0 * * * *")
    public void monitorCertificates() {
        log.info("Starting certificate monitoring task");
        
        try {
            monitoringService.monitorAllCertificates();
            log.info("Certificate monitoring task completed successfully");
        } catch (Exception e) {
            log.error("Certificate monitoring task failed", e);
        }
    }
}