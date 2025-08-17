package com.example.certificate.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public class AlertRule {
    private Long id;
    private String name;
    private AlertRuleType type;
    private Integer daysBefore;
    private AlertTriggerCondition condition;
    private Integer priority;
    private Boolean enabled;
    private List<String> alertChannels;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AlertRule() {
    }

    public AlertRule(String name, AlertRuleType type, Integer daysBefore, Integer priority, Boolean enabled, List<String> alertChannels) {
        this.name = name;
        this.type = type;
        this.daysBefore = daysBefore;
        this.priority = priority;
        this.enabled = enabled;
        this.alertChannels = alertChannels;
        this.condition = new AlertTriggerCondition();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AlertRuleType getType() {
        return type;
    }

    public void setType(AlertRuleType type) {
        this.type = type;
    }

    public Integer getDaysBefore() {
        return daysBefore;
    }

    public void setDaysBefore(Integer daysBefore) {
        this.daysBefore = daysBefore;
    }

    public AlertTriggerCondition getCondition() {
        return condition;
    }

    public void setCondition(AlertTriggerCondition condition) {
        this.condition = condition;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getAlertChannels() {
        return alertChannels;
    }

    public void setAlertChannels(List<String> alertChannels) {
        this.alertChannels = alertChannels;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isTimeBasedRule() {
        return AlertRuleType.TIME_BASED.equals(this.type);
    }

    public boolean shouldTriggerFor(Integer daysUntilExpiry) {
        if (!enabled || !isTimeBasedRule()) {
            return false;
        }
        return daysUntilExpiry != null && daysUntilExpiry <= daysBefore && daysUntilExpiry >= 0;
    }

    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("预警规则名称不能为空");
        }
        
        if (type == null) {
            throw new IllegalArgumentException("预警规则类型不能为空");
        }
        
        if (priority == null || priority < 1) {
            throw new IllegalArgumentException("预警规则优先级必须为正整数");
        }
        
        if (AlertRuleType.TIME_BASED.equals(type)) {
            if (daysBefore == null || daysBefore < 0) {
                throw new IllegalArgumentException("基于时间的预警规则的天数必须为非负整数");
            }
            if (daysBefore > 365) {
                throw new IllegalArgumentException("预警天数不能超过365天");
            }
        }
        
        if (alertChannels == null || alertChannels.isEmpty()) {
            throw new IllegalArgumentException("预警渠道不能为空");
        }
        
        for (String channel : alertChannels) {
            if (!"EMAIL".equals(channel) && !"SMS".equals(channel)) {
                throw new IllegalArgumentException("不支持的预警渠道: " + channel);
            }
        }
    }

    public boolean isValid() {
        try {
            validate();
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean hasHigherPriorityThan(AlertRule other) {
        if (other == null) {
            return true;
        }
        return this.priority != null && other.priority != null && this.priority < other.priority;
    }

    public void updateLastModified() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "AlertRule{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", daysBefore=" + daysBefore +
                ", priority=" + priority +
                ", enabled=" + enabled +
                '}';
    }
}