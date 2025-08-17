package com.example.certificate.common.constant;

/**
 * 邮件相关常量
 * 定义邮件服务使用的各种常量值
 */
public final class EmailConstants {
    
    // 邮件模式
    public static final String EMAIL_MODE_LOG = "log";
    public static final String EMAIL_MODE_REAL = "real";
    
    // 邮件类型
    public static final String EMAIL_TYPE_EXPIRY_ALERT = "expiry_alert";
    public static final String EMAIL_TYPE_DAILY_SUMMARY = "daily_summary";
    public static final String EMAIL_TYPE_BATCH_ALERT = "batch_alert";
    
    // 预警类型
    public static final String ALERT_TYPE_EXPIRED = "已过期预警";
    public static final String ALERT_TYPE_1_DAY = "1天预警";
    public static final String ALERT_TYPE_7_DAY = "7天预警";
    public static final String ALERT_TYPE_15_DAY = "15天预警";
    public static final String ALERT_TYPE_30_DAY = "30天预警";
    public static final String ALERT_TYPE_NORMAL = "常规监控";
    
    // 错误代码
    public static final String ERROR_CODE_LOG_EMAIL_FAILED = "LOG_EMAIL_FAILED";
    public static final String ERROR_CODE_LOG_SUMMARY_FAILED = "LOG_SUMMARY_FAILED";
    public static final String ERROR_CODE_BATCH_PROCESSING_ERROR = "BATCH_PROCESSING_ERROR";
    public static final String ERROR_CODE_NOT_IMPLEMENTED = "NOT_IMPLEMENTED";
    
    // 日志消息模板
    public static final String LOG_MESSAGE_EXPIRY_ALERT = "邮件预警 - 证书: %s (域名: %s)，剩余天数: %d，收件人: %s";
    public static final String LOG_MESSAGE_DAILY_SUMMARY = "每日摘要 - 即将过期证书: %d个，已过期证书: %d个，收件人: %s";
    public static final String LOG_MESSAGE_BATCH_ALERT = "批量邮件预警 - 证书数量: %d，收件人: %s";
    
    // 日志格式化模板
    public static final String FORMAT_EXPIRY_ALERT = "收件人: %s | 证书: %s | 域名: %s | 到期日期: %s | 剩余天数: %d天 | 预警类型: %s";
    public static final String FORMAT_DAILY_SUMMARY = "收件人: %s | 即将过期证书: %d个 | 已过期证书: %d个 | 发送时间: %s";
    
    // 邮件结果消息
    public static final String EMAIL_SUCCESS_MESSAGE_LOG_MODE = "邮件预警已记录 (MVP模式)";
    public static final String EMAIL_SUCCESS_MESSAGE_SUMMARY_LOG_MODE = "每日摘要已记录 (MVP模式)";
    public static final String EMAIL_FAILURE_MESSAGE_NOT_IMPLEMENTED = "生产环境邮件服务尚未实现SMTP发送功能";
    
    // 日志前缀
    public static final String LOG_PREFIX_EMAIL = "📧";
    public static final String LOG_PREFIX_CERTIFICATE = "📋";
    public static final String LOG_PREFIX_DOMAIN = "🌐";
    public static final String LOG_PREFIX_DATE = "📅";
    public static final String LOG_PREFIX_TIME = "⏰";
    public static final String LOG_PREFIX_STATUS = "📊";
    public static final String LOG_PREFIX_DAYS = "⏳";
    public static final String LOG_PREFIX_RECIPIENT = "📧";
    public static final String LOG_PREFIX_ALERT_TYPE = "🏷️";
    public static final String LOG_PREFIX_WARNING = "⚠️";
    
    // 预警天数阈值
    public static final int EXPIRY_THRESHOLD_1_DAY = 1;
    public static final int EXPIRY_THRESHOLD_7_DAY = 7;
    public static final int EXPIRY_THRESHOLD_15_DAY = 15;
    public static final int EXPIRY_THRESHOLD_30_DAY = 30;
    
    // 私有构造函数，防止实例化
    private EmailConstants() {
        throw new UnsupportedOperationException("这是一个工具类，不能被实例化");
    }
}