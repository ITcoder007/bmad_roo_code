package com.example.certificate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 调度器配置类
 * 配置 Spring 调度任务的线程池和相关参数
 * 
 * @author Auto Generated
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
    
    /**
     * 配置任务调度器线程池
     * 用于执行定时任务
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        
        // 设置线程池大小
        taskScheduler.setPoolSize(5);
        
        // 设置线程名称前缀
        taskScheduler.setThreadNamePrefix("certificate-scheduler-");
        
        // 设置线程优先级
        taskScheduler.setThreadPriority(Thread.NORM_PRIORITY);
        
        // 设置是否等待任务完成后再关闭线程池
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        
        // 设置等待时间（秒）
        taskScheduler.setAwaitTerminationSeconds(60);
        
        // 设置拒绝策略：当线程池满时，直接在调用线程中执行任务
        taskScheduler.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        // 初始化线程池
        taskScheduler.initialize();
        
        return taskScheduler;
    }
}