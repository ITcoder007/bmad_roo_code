package com.example.certificate.domain.model;

public enum AlertRuleType {
    /**
     * 基于时间的规则 - 在证书到期前指定天数触发预警
     */
    TIME_BASED("TIME_BASED", "基于时间的规则"),
    
    /**
     * 基于状态变更的规则 - 当证书状态发生变化时触发预警
     */
    STATUS_CHANGE("STATUS_CHANGE", "基于状态变更的规则"),
    
    /**
     * 自定义规则 - 基于自定义条件的预警规则
     */
    CUSTOM("CUSTOM", "自定义规则");

    private final String code;
    private final String description;

    AlertRuleType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static AlertRuleType fromCode(String code) {
        for (AlertRuleType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown alert rule type code: " + code);
    }

    @Override
    public String toString() {
        return code;
    }
}