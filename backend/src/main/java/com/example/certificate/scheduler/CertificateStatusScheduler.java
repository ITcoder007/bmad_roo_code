package com.example.certificate.scheduler;

import com.example.certificate.config.CertificateStatusConfig;
import com.example.certificate.service.CertificateStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 证书状态定时任务调度器
 * 负责定时更新所有证书的状态
 * 
 * @author Auto Generated
 */
@Component
public class CertificateStatusScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(CertificateStatusScheduler.class);
    
    @Resource
    private CertificateStatusService certificateStatusService;
    
    @Resource
    private CertificateStatusConfig certificateStatusConfig;
    
    /**
     * 每天凌晨1点执行证书状态更新任务
     * cron 表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "${certificate.scheduler.cron:0 0 1 * * ?}")
    public void updateAllCertificateStatus() {
        if (!certificateStatusConfig.isSchedulerEnabled()) {
            logger.debug("证书状态定时任务已禁用，跳过执行");
            return;
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logger.info("开始执行证书状态定时更新任务，时间: {}", timestamp);
        
        int retryCount = 0;
        boolean success = false;
        int maxRetries = certificateStatusConfig.getMaxRetries();
        
        while (retryCount < maxRetries && !success) {
            try {
                int updatedCount = certificateStatusService.updateAllCertificateStatus(
                        certificateStatusConfig.getExpiringSoonDays());
                logger.info("证书状态定时更新任务执行成功，更新证书数量: {}, 执行时间: {}", updatedCount, timestamp);
                success = true;
            } catch (Exception e) {
                retryCount++;
                logger.error("证书状态定时更新任务执行失败，重试次数: {}/{}, 错误信息: {}", 
                        retryCount, maxRetries, e.getMessage(), e);
                
                if (retryCount < maxRetries) {
                    try {
                        // 重试前等待一定时间
                        Thread.sleep(5000L * retryCount); // 递增等待时间
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.error("定时任务重试等待被中断", ie);
                        break;
                    }
                }
            }
        }
        
        if (!success) {
            logger.error("证书状态定时更新任务最终执行失败，已达到最大重试次数: {}", maxRetries);
        }
    }
    
    /**
     * 每小时执行一次快速状态检查（仅记录统计信息）
     * 用于监控和告警，不更新数据库
     */
    @Scheduled(cron = "${certificate.scheduler.monitor-cron:0 0 * * * ?}")
    public void monitorCertificateStatus() {
        if (!certificateStatusConfig.isSchedulerEnabled()) {
            logger.debug("证书状态监控任务已禁用，跳过执行");
            return;
        }
        
        try {
            logger.debug("执行证书状态监控检查");
            // 这里可以添加监控逻辑，比如统计即将过期的证书数量
            // 为了避免频繁更新数据库，这个任务只做监控和日志记录
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            logger.debug("证书状态监控检查完成，时间: {}", timestamp);
        } catch (Exception e) {
            logger.error("证书状态监控检查失败", e);
        }
    }
    
    /**
     * 手动触发证书状态更新（用于测试或应急情况）
     */
    public void manualUpdateCertificateStatus() {
        logger.info("手动触发证书状态更新任务");
        updateAllCertificateStatus();
    }
    
    /**
     * 获取任务执行状态信息
     */
    public String getSchedulerStatus() {
        return certificateStatusConfig.getConfigSummary();
    }
}