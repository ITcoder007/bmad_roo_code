package com.example.certificate.service.impl;

import com.example.certificate.domain.model.AlertRule;
import com.example.certificate.domain.model.AlertRuleType;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.service.AlertRuleConfigService;
import com.example.certificate.service.AlertRuleEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertRuleEngineImpl implements AlertRuleEngine {

    private static final Logger log = LoggerFactory.getLogger(AlertRuleEngineImpl.class);

    @Autowired
    private AlertRuleConfigService alertRuleConfigService;

    @Override
    public boolean evaluateRules(Certificate certificate) {
        if (certificate == null) {
            log.warn("证书信息为空，无法评估预警规则");
            return false;
        }

        try {
            List<AlertRule> enabledRules = alertRuleConfigService.getEnabledRules();
            
            for (AlertRule rule : enabledRules) {
                if (evaluateRule(rule, certificate)) {
                    log.debug("证书 {} 触发预警规则: {}", certificate.getName(), rule.getName());
                    return true;
                }
            }
            
            log.debug("证书 {} 未触发任何预警规则", certificate.getName());
            return false;
            
        } catch (Exception e) {
            log.error("评估证书预警规则时发生错误: {}, 证书: {}", e.getMessage(), certificate.getName());
            return false;
        }
    }

    @Override
    public List<AlertRule> getTriggeredRules(Certificate certificate) {
        if (certificate == null) {
            log.warn("证书信息为空，无法获取触发的预警规则");
            return Collections.emptyList();
        }

        try {
            List<AlertRule> enabledRules = alertRuleConfigService.getEnabledRules();
            
            List<AlertRule> triggeredRules = enabledRules.stream()
                    .filter(rule -> evaluateRule(rule, certificate))
                    .sorted(Comparator.comparing(AlertRule::getPriority))
                    .collect(Collectors.toList());
                    
            log.debug("证书 {} 触发了 {} 个预警规则", certificate.getName(), triggeredRules.size());
            return triggeredRules;
            
        } catch (Exception e) {
            log.error("获取触发的预警规则时发生错误: {}, 证书: {}", e.getMessage(), certificate.getName());
            return Collections.emptyList();
        }
    }

    @Override
    public AlertRule getHighestPriorityRule(List<AlertRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return null;
        }

        return rules.stream()
                .filter(rule -> rule.getEnabled() != null && rule.getEnabled())
                .min(Comparator.comparing(AlertRule::getPriority))
                .orElse(null);
    }

    @Override
    public boolean evaluateRule(AlertRule rule, Certificate certificate) {
        if (rule == null || certificate == null) {
            return false;
        }

        if (!rule.getEnabled()) {
            return false;
        }

        try {
            switch (rule.getType()) {
                case TIME_BASED:
                    return evaluateTimeBasedRule(rule, certificate);
                case STATUS_CHANGE:
                    return evaluateStatusChangeRule(rule, certificate);
                case CUSTOM:
                    return evaluateCustomRule(rule, certificate);
                default:
                    log.warn("不支持的预警规则类型: {}", rule.getType());
                    return false;
            }
        } catch (Exception e) {
            log.error("评估单个规则时发生错误: {}, 规则: {}, 证书: {}", 
                     e.getMessage(), rule.getName(), certificate.getName());
            return false;
        }
    }

    @Override
    public int calculateDaysUntilExpiry(Certificate certificate) {
        if (certificate == null || certificate.getExpiryDate() == null) {
            return Integer.MAX_VALUE;
        }

        long days = certificate.getDaysUntilExpiry();
        
        // 将long转换为int，处理可能的溢出
        if (days > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else if (days < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        
        return (int) days;
    }

    /**
     * 评估基于时间的预警规则
     */
    private boolean evaluateTimeBasedRule(AlertRule rule, Certificate certificate) {
        int daysUntilExpiry = calculateDaysUntilExpiry(certificate);
        
        // 检查是否在规则指定的天数范围内
        if (rule.getDaysBefore() == null) {
            log.warn("基于时间的规则缺少daysBefore参数: {}", rule.getName());
            return false;
        }
        
        boolean triggered = daysUntilExpiry <= rule.getDaysBefore() && daysUntilExpiry >= 0;
        
        log.debug("时间规则评估 - 规则: {}, 阈值: {}天, 剩余天数: {}, 结果: {}", 
                 rule.getName(), rule.getDaysBefore(), daysUntilExpiry, triggered);
                 
        return triggered;
    }

    /**
     * 评估基于状态变更的预警规则
     */
    private boolean evaluateStatusChangeRule(AlertRule rule, Certificate certificate) {
        // 当前实现暂时返回false，未来可以通过比较历史状态来实现
        log.debug("状态变更规则暂未实现: {}", rule.getName());
        return false;
    }

    /**
     * 评估自定义预警规则
     */
    private boolean evaluateCustomRule(AlertRule rule, Certificate certificate) {
        // 当前实现暂时返回false，未来可以通过表达式引擎来实现
        log.debug("自定义规则暂未实现: {}", rule.getName());
        return false;
    }
}