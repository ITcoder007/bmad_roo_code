package com.example.certificate.common.exception;

/**
 * 错误代码枚举类
 * 定义系统中所有可能的错误代码和相应的错误消息
 */
public enum ErrorCode {
    
    // 通用错误代码 (1000-1999)
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "访问被拒绝"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    
    // 证书相关错误代码 (10001-10999)
    CERTIFICATE_NOT_FOUND(10001, "证书不存在"),
    CERTIFICATE_VALIDATION_FAILED(10002, "证书验证失败"),
    CERTIFICATE_DOMAIN_EXISTS(10003, "域名已存在"),
    CERTIFICATE_EXPIRED(10004, "证书已过期"),
    CERTIFICATE_INVALID_DATE(10005, "证书日期无效"),
    CERTIFICATE_INVALID_FORMAT(10006, "证书格式无效"),
    CERTIFICATE_SAVE_FAILED(10007, "证书保存失败"),
    CERTIFICATE_DELETE_FAILED(10008, "证书删除失败"),
    CERTIFICATE_UPDATE_FAILED(10009, "证书更新失败"),
    
    // 监控相关错误代码 (11001-11999)
    MONITORING_LOG_NOT_FOUND(11001, "监控日志不存在"),
    MONITORING_SERVICE_UNAVAILABLE(11002, "监控服务不可用"),
    MONITORING_CONFIG_ERROR(11003, "监控配置错误"),
    
    // 用户相关错误代码 (12001-12999)
    USER_NOT_FOUND(12001, "用户不存在"),
    USER_UNAUTHORIZED(12002, "用户未授权"),
    USER_FORBIDDEN(12003, "用户权限不足"),
    
    // 系统相关错误代码 (13001-13999)
    SYSTEM_BUSY(13001, "系统繁忙，请稍后重试"),
    SYSTEM_MAINTENANCE(13002, "系统维护中"),
    DATABASE_ERROR(13003, "数据库操作失败"),
    CACHE_ERROR(13004, "缓存操作失败"),
    
    // 验证相关错误代码 (14001-14999)
    VALIDATION_FAILED(14001, "数据验证失败"),
    PARAMETER_MISSING(14002, "缺少必要参数"),
    PARAMETER_INVALID(14003, "参数格式无效"),
    DATE_RANGE_INVALID(14004, "日期范围无效"),
    DOMAIN_FORMAT_INVALID(14005, "域名格式无效"),
    PAGE_PARAMETER_INVALID(14006, "分页参数无效");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    /**
     * 根据错误代码获取错误枚举
     */
    public static ErrorCode getByCode(int code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return INTERNAL_SERVER_ERROR;
    }
    
    @Override
    public String toString() {
        return "ErrorCode{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}