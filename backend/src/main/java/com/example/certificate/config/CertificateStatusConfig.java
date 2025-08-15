package com.example.certificate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 证书状态配置类
 * 读取和管理证书状态相关的配置参数
 * 
 * @author Auto Generated
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "certificate")
@Validated
public class CertificateStatusConfig {
    
    /**
     * 状态相关配置
     */
    @NotNull
    private Status status = new Status();
    
    /**
     * 调度器相关配置
     */
    @NotNull
    private Scheduler scheduler = new Scheduler();
    
    /**
     * 状态配置内部类
     */
    @Data
    public static class Status {
        
        /**
         * 即将过期阈值天数
         * 证书在到期前多少天被认为是"即将过期"
         */
        @Min(value = 1, message = "即将过期阈值天数必须大于0")
        private int expiringSoonDays = 30;
        
        /**
         * 状态检查间隔（毫秒）
         */
        @Min(value = 1000, message = "检查间隔必须大于1000毫秒")
        private long checkInterval = 86400000L; // 24小时
    }
    
    /**
     * 调度器配置内部类
     */
    @Data
    public static class Scheduler {
        
        /**
         * 是否启用调度器
         */
        private boolean enabled = true;
        
        /**
         * 状态更新任务的 cron 表达式
         */
        private String cron = "0 0 1 * * ?"; // 每天凌晨1点
        
        /**
         * 监控任务的 cron 表达式
         */
        private String monitorCron = "0 0 * * * ?"; // 每小时
        
        /**
         * 最大重试次数
         */
        @Min(value = 1, message = "最大重试次数必须大于0")
        private int maxRetries = 3;
        
        /**
         * 批量处理大小
         */
        @Min(value = 1, message = "批量处理大小必须大于0")
        private int batchSize = 100;
    }
    
    /**
     * 获取即将过期的阈值天数
     */
    public int getExpiringSoonDays() {
        return status.getExpiringSoonDays();
    }
    
    /**
     * 获取状态检查间隔
     */
    public long getCheckInterval() {
        return status.getCheckInterval();
    }
    
    /**
     * 检查调度器是否启用
     */
    public boolean isSchedulerEnabled() {
        return scheduler.isEnabled();
    }
    
    /**
     * 获取状态更新任务的 cron 表达式
     */
    public String getUpdateCron() {
        return scheduler.getCron();
    }
    
    /**
     * 获取监控任务的 cron 表达式
     */
    public String getMonitorCron() {
        return scheduler.getMonitorCron();
    }
    
    /**
     * 获取最大重试次数
     */
    public int getMaxRetries() {
        return scheduler.getMaxRetries();
    }
    
    /**
     * 获取批量处理大小
     */
    public int getBatchSize() {
        return scheduler.getBatchSize();
    }
    
    /**
     * 获取配置摘要信息（用于日志和监控）
     */
    public String getConfigSummary() {
        return String.format(
                "证书状态配置 - 即将过期阈值: %d天, 检查间隔: %d毫秒, 调度器启用: %s, 更新任务: %s, 监控任务: %s, 最大重试: %d次, 批量大小: %d",
                getExpiringSoonDays(),
                getCheckInterval(),
                isSchedulerEnabled() ? "是" : "否",
                getUpdateCron(),
                getMonitorCron(),
                getMaxRetries(),
                getBatchSize()
        );
    }
}