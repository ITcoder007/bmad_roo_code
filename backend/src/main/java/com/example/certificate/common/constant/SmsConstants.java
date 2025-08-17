package com.example.certificate.common.constant;

/**
 * 短信相关常量
 * 定义短信服务使用的各种常量值
 */
public final class SmsConstants {
    
    // 短信模式
    public static final String SMS_MODE_LOG = "log";
    public static final String SMS_MODE_REAL = "real";
    
    // 短信类型
    public static final String SMS_TYPE_EXPIRY_ALERT = "expiry_alert";
    public static final String SMS_TYPE_DAILY_SUMMARY = "daily_summary";
    public static final String SMS_TYPE_BATCH_ALERT = "batch_alert";
    
    // 预警类型
    public static final String ALERT_TYPE_EXPIRED = "已过期预警";
    public static final String ALERT_TYPE_1_DAY = "1天预警";
    public static final String ALERT_TYPE_7_DAY = "7天预警";
    public static final String ALERT_TYPE_15_DAY = "15天预警";
    public static final String ALERT_TYPE_30_DAY = "30天预警";
    public static final String ALERT_TYPE_NORMAL = "常规监控";
    
    // 错误代码
    public static final String ERROR_CODE_LOG_SMS_FAILED = "LOG_SMS_FAILED";
    public static final String ERROR_CODE_LOG_SUMMARY_FAILED = "LOG_SUMMARY_FAILED";
    public static final String ERROR_CODE_BATCH_PROCESSING_ERROR = "BATCH_PROCESSING_ERROR";
    public static final String ERROR_CODE_NOT_IMPLEMENTED = "NOT_IMPLEMENTED";
    public static final String ERROR_CODE_PHONE_INVALID = "PHONE_INVALID";
    public static final String ERROR_CODE_CONTENT_TOO_LONG = "CONTENT_TOO_LONG";
    
    // 日志消息模板
    public static final String LOG_MESSAGE_EXPIRY_ALERT = "短信预警 - 证书: %s (域名: %s)，剩余天数: %d，收件人: %s";
    public static final String LOG_MESSAGE_DAILY_SUMMARY = "每日摘要 - 即将过期证书: %d个，已过期证书: %d个，收件人: %s";
    public static final String LOG_MESSAGE_BATCH_ALERT = "批量短信预警 - 证书数量: %d，收件人: %s";
    
    // 日志格式化模板
    public static final String FORMAT_EXPIRY_ALERT = "收件人: %s | 证书: %s | 域名: %s | 到期日期: %s | 剩余天数: %d天 | 预警类型: %s";
    public static final String FORMAT_DAILY_SUMMARY = "收件人: %s | 即将过期证书: %d个 | 已过期证书: %d个 | 发送时间: %s";
    
    // 短信结果消息
    public static final String SMS_SUCCESS_MESSAGE_LOG_MODE = "短信预警已记录 (MVP模式)";
    public static final String SMS_SUCCESS_MESSAGE_SUMMARY_LOG_MODE = "每日摘要已记录 (MVP模式)";
    public static final String SMS_FAILURE_MESSAGE_NOT_IMPLEMENTED = "生产环境短信服务尚未实现短信发送功能";
    
    // 日志前缀
    public static final String LOG_PREFIX_SMS = "📱";
    public static final String LOG_PREFIX_CERTIFICATE = "📋";
    public static final String LOG_PREFIX_DOMAIN = "🌐";
    public static final String LOG_PREFIX_DATE = "📅";
    public static final String LOG_PREFIX_TIME = "⏰";
    public static final String LOG_PREFIX_STATUS = "📊";
    public static final String LOG_PREFIX_DAYS = "⏳";
    public static final String LOG_PREFIX_RECIPIENT = "📱";
    public static final String LOG_PREFIX_ALERT_TYPE = "🏷️";
    public static final String LOG_PREFIX_WARNING = "⚠️";
    
    // 预警天数阈值
    public static final int EXPIRY_THRESHOLD_1_DAY = 1;
    public static final int EXPIRY_THRESHOLD_7_DAY = 7;
    public static final int EXPIRY_THRESHOLD_15_DAY = 15;
    public static final int EXPIRY_THRESHOLD_30_DAY = 30;
    
    // 短信内容限制
    public static final int MAX_SMS_LENGTH = 70; // 单条短信最大长度
    public static final int MAX_SMS_LENGTH_LONG = 500; // 长短信最大长度
    
    // 手机号码格式
    public static final String PHONE_PATTERN = "^1[3-9]\\d{9}$"; // 中国大陆手机号
    public static final String PHONE_MASK_PATTERN = "^(\\d{3})\\d{4}(\\d{4})$"; // 脱敏模式：138****1234
    
    // 短信模板
    public static final String TEMPLATE_EXPIRY_ALERT = "【证书管理】证书%s将在%d天后过期，请及时处理。";
    public static final String TEMPLATE_DAILY_SUMMARY = "【证书管理】今日摘要：即将过期%d个，已过期%d个证书。";
    
    // 私有构造函数，防止实例化
    private SmsConstants() {
        throw new UnsupportedOperationException("这是一个工具类，不能被实例化");
    }
}