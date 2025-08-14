package com.example.certificate.scheduler;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.infrastructure.persistence.mapper.CertificateMapper;
import com.example.certificate.service.AlertService;
import com.example.certificate.service.CertificateService;
import com.example.certificate.service.MonitoringLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateScheduler {
    
    private final CertificateMapper certificateMapper;
    private final CertificateService certificateService;
    private final AlertService alertService;
    private final MonitoringLogService monitoringLogService;
    
    /**
     * 每小时执行一次证书监控任务
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkCertificates() {
        log.info("开始执行证书监控任务");
        
        try {
            // 获取所有证书
            List<Certificate> certificates = certificateMapper.selectList(null);
            
            for (Certificate certificate : certificates) {
                // 更新证书状态
                certificate.updateStatus();
                certificateMapper.updateById(certificate);
                
                // 记录监控日志
                monitoringLogService.logCertificateMonitoring(certificate);
                
                // 检查是否需要发送预警
                long daysUntilExpiry = certificate.getDaysUntilExpiry();
                if (alertService.shouldSendAlert(daysUntilExpiry) && daysUntilExpiry >= 0) {
                    alertService.sendAlert(certificate);
                }
            }
            
            log.info("证书监控任务执行完成，检查了 {} 个证书", certificates.size());
        } catch (Exception e) {
            log.error("证书监控任务执行失败", e);
        }
    }
    
    /**
     * 每天凌晨2点执行状态更新任务
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateCertificateStatus() {
        log.info("开始执行证书状态更新任务");
        
        try {
            certificateService.updateAllCertificateStatus();
            log.info("证书状态更新任务执行完成");
        } catch (Exception e) {
            log.error("证书状态更新任务执行失败", e);
        }
    }
}