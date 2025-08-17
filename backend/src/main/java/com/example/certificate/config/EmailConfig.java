package com.example.certificate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;

/**
 * 邮件配置类
 * 管理邮件发送相关的所有配置参数
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "alert.email")
@Validated
public class EmailConfig {
    
    /**
     * 是否启用邮件功能
     */
    private boolean enabled = true;
    
    /**
     * 邮件发送模式：log（日志模式）或 real（真实发送模式）
     */
    @Pattern(regexp = "log|real", message = "邮件模式只能是 'log' 或 'real'")
    private String mode = "log";
    
    /**
     * 默认收件人邮箱
     */
    @Email(message = "默认收件人邮箱格式不正确")
    private String defaultRecipient = "admin@example.com";
    
    /**
     * SMTP 配置
     */
    @Valid
    @NotNull
    private SmtpConfig smtp = new SmtpConfig();
    
    /**
     * 邮件模板配置
     */
    @Valid
    private Map<String, EmailTemplateConfig> templates = new HashMap<>();
    
    /**
     * 发送限制配置
     */
    @Valid
    @NotNull
    private LimitConfig limits = new LimitConfig();
    
    /**
     * SMTP 服务器配置
     */
    @Data
    public static class SmtpConfig {
        /**
         * SMTP 服务器地址
         */
        @NotBlank(message = "SMTP 服务器地址不能为空")
        private String host = "smtp.example.com";
        
        /**
         * SMTP 服务器端口
         */
        private int port = 587;
        
        /**
         * SMTP 用户名
         */
        private String username = "noreply@example.com";
        
        /**
         * SMTP 密码（建议使用环境变量）
         */
        private String password;
        
        /**
         * 是否启用TLS加密
         */
        private boolean tlsEnabled = true;
        
        /**
         * 连接超时时间（毫秒）
         */
        private int connectionTimeout = 30000;
        
        /**
         * 读取超时时间（毫秒）
         */
        private int readTimeout = 30000;
    }
    
    /**
     * 邮件模板配置
     */
    @Data
    public static class EmailTemplateConfig {
        /**
         * 邮件主题模板
         */
        @NotBlank(message = "邮件主题不能为空")
        private String subject;
        
        /**
         * 邮件模板文件路径
         */
        private String templatePath;
        
        /**
         * 邮件内容模板（简单文本模板）
         */
        private String contentTemplate;
        
        /**
         * 是否启用HTML格式
         */
        private boolean htmlEnabled = false;
    }
    
    /**
     * 发送限制配置
     */
    @Data
    public static class LimitConfig {
        /**
         * 单次批量发送最大数量
         */
        private int maxBatchSize = 100;
        
        /**
         * 每小时发送限制
         */
        private int hourlyLimit = 1000;
        
        /**
         * 每日发送限制
         */
        private int dailyLimit = 10000;
        
        /**
         * 发送间隔（毫秒）
         */
        private long sendInterval = 1000;
        
        /**
         * 是否启用限流
         */
        private boolean rateLimitEnabled = true;
    }
    
    /**
     * 检查配置是否为日志模式
     */
    public boolean isLogMode() {
        return "log".equalsIgnoreCase(mode);
    }
    
    /**
     * 检查配置是否为真实发送模式
     */
    public boolean isRealMode() {
        return "real".equalsIgnoreCase(mode);
    }
    
    /**
     * 获取指定类型的邮件模板配置
     */
    public EmailTemplateConfig getTemplateConfig(String templateType) {
        return templates.get(templateType);
    }
    
    /**
     * 验证邮件配置的完整性
     */
    public boolean isValidConfiguration() {
        if (!enabled) {
            return true; // 如果邮件功能未启用，则配置始终有效
        }
        
        if (isRealMode()) {
            // 真实发送模式需要完整的SMTP配置
            return smtp != null && 
                   smtp.getHost() != null && !smtp.getHost().trim().isEmpty() &&
                   smtp.getUsername() != null && !smtp.getUsername().trim().isEmpty();
        }
        
        // 日志模式只需要基本配置
        return defaultRecipient != null && !defaultRecipient.trim().isEmpty();
    }
    
    /**
     * 获取配置摘要信息（用于日志记录，不包含敏感信息）
     */
    public String getConfigSummary() {
        return String.format(
            "EmailConfig{enabled=%s, mode='%s', defaultRecipient='%s', smtpHost='%s', smtpPort=%d, templatesCount=%d}",
            enabled, mode, defaultRecipient, 
            smtp != null ? smtp.getHost() : "null",
            smtp != null ? smtp.getPort() : 0,
            templates.size()
        );
    }
}