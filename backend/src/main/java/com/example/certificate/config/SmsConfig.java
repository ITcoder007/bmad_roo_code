package com.example.certificate.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;

/**
 * 短信配置类
 * 管理短信发送相关的所有配置参数
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "alert.sms")
@Validated
public class SmsConfig {
    
    /**
     * 是否启用短信功能
     */
    private boolean enabled = true;
    
    /**
     * 短信发送模式：log（日志模式）或 real（真实发送模式）
     */
    @Pattern(regexp = "log|real", message = "短信模式只能是 'log' 或 'real'")
    private String mode = "log";
    
    /**
     * 默认收件人手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "默认收件人手机号格式不正确")
    private String defaultRecipient = "13800138000";
    
    /**
     * 短信服务提供商配置
     */
    @Valid
    @NotNull
    private ProviderConfig provider = new ProviderConfig();
    
    /**
     * 短信模板配置
     */
    @Valid
    private Map<String, SmsTemplateConfig> templates = new HashMap<>();
    
    /**
     * 发送限制配置
     */
    @Valid
    @NotNull
    private LimitConfig limits = new LimitConfig();
    
    /**
     * 短信服务提供商配置
     */
    @Data
    public static class ProviderConfig {
        /**
         * 服务提供商名称：aliyun（阿里云）、tencent（腾讯云）、custom（自定义）
         */
        @Pattern(regexp = "aliyun|tencent|custom", message = "短信服务提供商只能是 'aliyun'、'tencent' 或 'custom'")
        private String name = "aliyun";
        
        /**
         * API访问密钥ID
         */
        private String accessKey;
        
        /**
         * API访问密钥Secret
         */
        private String secretKey;
        
        /**
         * 服务端点地址
         */
        private String endpoint;
        
        /**
         * 短信签名
         */
        @NotBlank(message = "短信签名不能为空")
        private String signature = "证书管理系统";
        
        /**
         * 连接超时时间（毫秒）
         */
        private int connectionTimeout = 30000;
        
        /**
         * 读取超时时间（毫秒）
         */
        private int readTimeout = 30000;
        
        /**
         * 重试次数
         */
        private int retryCount = 3;
    }
    
    /**
     * 短信模板配置
     */
    @Data
    public static class SmsTemplateConfig {
        /**
         * 模板代码（服务商提供的模板ID）
         */
        @NotBlank(message = "模板代码不能为空")
        private String templateCode;
        
        /**
         * 短信签名
         */
        private String signature;
        
        /**
         * 模板内容（用于日志模式或自定义模板）
         */
        private String content;
        
        /**
         * 是否启用此模板
         */
        private boolean enabled = true;
        
        /**
         * 模板变量映射
         */
        private Map<String, String> variables = new HashMap<>();
    }
    
    /**
     * 发送限制配置
     */
    @Data
    public static class LimitConfig {
        /**
         * 单次批量发送最大数量
         */
        private int maxBatchSize = 50;
        
        /**
         * 每小时发送限制
         */
        private int hourlyLimit = 500;
        
        /**
         * 每日发送限制
         */
        private int dailyLimit = 2000;
        
        /**
         * 发送间隔（毫秒）
         */
        private long sendInterval = 2000;
        
        /**
         * 是否启用限流
         */
        private boolean rateLimitEnabled = true;
        
        /**
         * 单条短信最大长度
         */
        private int maxSmsLength = 70;
        
        /**
         * 长短信最大长度
         */
        private int maxLongSmsLength = 500;
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
     * 获取指定类型的短信模板配置
     */
    public SmsTemplateConfig getTemplateConfig(String templateType) {
        return templates.get(templateType);
    }
    
    /**
     * 验证短信配置的完整性
     */
    public boolean isValidConfiguration() {
        if (!enabled) {
            return true; // 如果短信功能未启用，则配置始终有效
        }
        
        if (isRealMode()) {
            // 真实发送模式需要完整的服务商配置
            return provider != null && 
                   provider.getAccessKey() != null && !provider.getAccessKey().trim().isEmpty() &&
                   provider.getSecretKey() != null && !provider.getSecretKey().trim().isEmpty() &&
                   provider.getSignature() != null && !provider.getSignature().trim().isEmpty();
        }
        
        // 日志模式只需要基本配置
        return defaultRecipient != null && !defaultRecipient.trim().isEmpty();
    }
    
    /**
     * 验证手机号格式
     */
    public boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return phone.matches("^1[3-9]\\d{9}$");
    }
    
    /**
     * 手机号脱敏处理
     */
    public String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
    
    /**
     * 获取配置摘要信息（用于日志记录，不包含敏感信息）
     */
    public String getConfigSummary() {
        return String.format(
            "SmsConfig{enabled=%s, mode='%s', defaultRecipient='%s', provider='%s', signature='%s', templatesCount=%d}",
            enabled, mode, maskPhoneNumber(defaultRecipient), 
            provider != null ? provider.getName() : "null",
            provider != null ? provider.getSignature() : "null",
            templates.size()
        );
    }
    
    /**
     * 检查是否为阿里云短信服务
     */
    public boolean isAliyunProvider() {
        return provider != null && "aliyun".equalsIgnoreCase(provider.getName());
    }
    
    /**
     * 检查是否为腾讯云短信服务
     */
    public boolean isTencentProvider() {
        return provider != null && "tencent".equalsIgnoreCase(provider.getName());
    }
    
    /**
     * 检查是否为自定义短信服务
     */
    public boolean isCustomProvider() {
        return provider != null && "custom".equalsIgnoreCase(provider.getName());
    }
}