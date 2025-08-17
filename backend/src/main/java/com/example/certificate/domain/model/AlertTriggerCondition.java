package com.example.certificate.domain.model;

import java.time.LocalDateTime;
import java.util.Map;

public class AlertTriggerCondition {
    private String conditionExpression;
    private Map<String, Object> parameters;
    private Boolean requireStatusChange;
    private Boolean onlyBusinessHours;
    private Integer maxFrequencyPerDay;
    private LocalDateTime lastTriggeredAt;

    public AlertTriggerCondition() {
        this.requireStatusChange = false;
        this.onlyBusinessHours = false;
        this.maxFrequencyPerDay = null;
    }

    public AlertTriggerCondition(String conditionExpression) {
        this();
        this.conditionExpression = conditionExpression;
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Boolean getRequireStatusChange() {
        return requireStatusChange;
    }

    public void setRequireStatusChange(Boolean requireStatusChange) {
        this.requireStatusChange = requireStatusChange;
    }

    public Boolean getOnlyBusinessHours() {
        return onlyBusinessHours;
    }

    public void setOnlyBusinessHours(Boolean onlyBusinessHours) {
        this.onlyBusinessHours = onlyBusinessHours;
    }

    public Integer getMaxFrequencyPerDay() {
        return maxFrequencyPerDay;
    }

    public void setMaxFrequencyPerDay(Integer maxFrequencyPerDay) {
        this.maxFrequencyPerDay = maxFrequencyPerDay;
    }

    public LocalDateTime getLastTriggeredAt() {
        return lastTriggeredAt;
    }

    public void setLastTriggeredAt(LocalDateTime lastTriggeredAt) {
        this.lastTriggeredAt = lastTriggeredAt;
    }

    public boolean canTriggerNow() {
        if (onlyBusinessHours != null && onlyBusinessHours) {
            LocalDateTime now = LocalDateTime.now();
            int hour = now.getHour();
            int dayOfWeek = now.getDayOfWeek().getValue();
            
            // 工作时间：周一到周五，上午9点到下午6点
            if (dayOfWeek > 5 || hour < 9 || hour >= 18) {
                return false;
            }
        }

        if (maxFrequencyPerDay != null && lastTriggeredAt != null) {
            LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
            if (lastTriggeredAt.isAfter(todayStart)) {
                // 今天已经触发过，检查频率限制
                // 简化实现：当天只能触发一次
                return false;
            }
        }

        return true;
    }

    public void markTriggered() {
        this.lastTriggeredAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "AlertTriggerCondition{" +
                "conditionExpression='" + conditionExpression + '\'' +
                ", requireStatusChange=" + requireStatusChange +
                ", onlyBusinessHours=" + onlyBusinessHours +
                ", maxFrequencyPerDay=" + maxFrequencyPerDay +
                '}';
    }
}