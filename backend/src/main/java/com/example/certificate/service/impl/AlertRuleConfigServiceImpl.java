package com.example.certificate.service.impl;

import com.example.certificate.domain.model.AlertRule;
import com.example.certificate.domain.model.AlertRuleType;
import com.example.certificate.service.AlertRuleConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AlertRuleConfigServiceImpl implements AlertRuleConfigService {

    private static final Logger log = LoggerFactory.getLogger(AlertRuleConfigServiceImpl.class);

    @Value("${alert.rules.default.enabled:true}")
    private boolean defaultRulesEnabled;

    @Value("${alert.rules.cache.enabled:true}")
    private boolean cacheEnabled;

    // 内存中的规则存储（MVP阶段使用，生产环境应该使用数据库）
    private final Map<Long, AlertRule> ruleCache = new ConcurrentHashMap<>();
    private volatile long nextRuleId = 1L;

    @PostConstruct
    public void init() {
        if (defaultRulesEnabled) {
            log.info("初始化默认预警规则");
            initializeDefaultRules();
        }
    }

    @Override
    public List<AlertRule> getEnabledRules() {
        try {
            return ruleCache.values().stream()
                    .filter(rule -> rule.getEnabled() != null && rule.getEnabled())
                    .sorted(Comparator.comparing(AlertRule::getPriority))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取启用的预警规则时发生错误: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<AlertRule> getAllRules() {
        try {
            return new ArrayList<>(ruleCache.values());
        } catch (Exception e) {
            log.error("获取所有预警规则时发生错误: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<AlertRule> getRulesByType(AlertRuleType type) {
        if (type == null) {
            return Collections.emptyList();
        }

        try {
            return ruleCache.values().stream()
                    .filter(rule -> type.equals(rule.getType()))
                    .sorted(Comparator.comparing(AlertRule::getPriority))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据类型获取预警规则时发生错误: {}, type: {}", e.getMessage(), type);
            return Collections.emptyList();
        }
    }

    @Override
    public AlertRule getRuleById(Long ruleId) {
        if (ruleId == null) {
            return null;
        }

        try {
            return ruleCache.get(ruleId);
        } catch (Exception e) {
            log.error("根据ID获取预警规则时发生错误: {}, ruleId: {}", e.getMessage(), ruleId);
            return null;
        }
    }

    @Override
    public void initializeDefaultRules() {
        try {
            log.info("开始初始化默认预警规则");

            // 清空现有规则
            ruleCache.clear();
            nextRuleId = 1L;

            // 创建默认的时间基础预警规则
            createDefaultRule("30天预警", AlertRuleType.TIME_BASED, 30, Arrays.asList("EMAIL"), 1);
            createDefaultRule("15天预警", AlertRuleType.TIME_BASED, 15, Arrays.asList("EMAIL"), 2);
            createDefaultRule("7天预警", AlertRuleType.TIME_BASED, 7, Arrays.asList("EMAIL", "SMS"), 3);
            createDefaultRule("1天预警", AlertRuleType.TIME_BASED, 1, Arrays.asList("EMAIL", "SMS"), 4);

            log.info("默认预警规则初始化完成，共创建 {} 个规则", ruleCache.size());

        } catch (Exception e) {
            log.error("初始化默认预警规则时发生错误: {}", e.getMessage());
        }
    }

    @Override
    public AlertRule addRule(AlertRule rule) {
        if (rule == null) {
            log.warn("尝试添加空的预警规则");
            return null;
        }

        try {
            String validationError = validateRule(rule);
            if (!validationError.isEmpty()) {
                log.warn("预警规则验证失败: {}", validationError);
                throw new IllegalArgumentException(validationError);
            }

            if (hasDuplicateRule(rule)) {
                log.warn("预警规则已存在重复配置: {}", rule.getName());
                throw new IllegalArgumentException("预警规则已存在重复配置: " + rule.getName());
            }

            rule.setId(nextRuleId++);
            rule.setCreatedAt(LocalDateTime.now());
            rule.setUpdatedAt(LocalDateTime.now());

            ruleCache.put(rule.getId(), rule);
            log.info("成功添加预警规则: {}", rule.getName());

            return rule;

        } catch (Exception e) {
            log.error("添加预警规则时发生错误: {}, rule: {}", e.getMessage(), rule);
            throw new RuntimeException("添加预警规则失败: " + e.getMessage());
        }
    }

    @Override
    public AlertRule updateRule(AlertRule rule) {
        if (rule == null || rule.getId() == null) {
            log.warn("尝试更新无效的预警规则");
            return null;
        }

        try {
            if (!ruleCache.containsKey(rule.getId())) {
                log.warn("预警规则不存在: {}", rule.getId());
                throw new IllegalArgumentException("预警规则不存在: " + rule.getId());
            }

            String validationError = validateRule(rule);
            if (!validationError.isEmpty()) {
                log.warn("预警规则验证失败: {}", validationError);
                throw new IllegalArgumentException(validationError);
            }

            rule.setUpdatedAt(LocalDateTime.now());
            ruleCache.put(rule.getId(), rule);
            log.info("成功更新预警规则: {}", rule.getName());

            return rule;

        } catch (Exception e) {
            log.error("更新预警规则时发生错误: {}, rule: {}", e.getMessage(), rule);
            throw new RuntimeException("更新预警规则失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteRule(Long ruleId) {
        if (ruleId == null) {
            log.warn("尝试删除空的规则ID");
            return false;
        }

        try {
            AlertRule removedRule = ruleCache.remove(ruleId);
            if (removedRule != null) {
                log.info("成功删除预警规则: {}", removedRule.getName());
                return true;
            } else {
                log.warn("预警规则不存在，无法删除: {}", ruleId);
                return false;
            }

        } catch (Exception e) {
            log.error("删除预警规则时发生错误: {}, ruleId: {}", e.getMessage(), ruleId);
            return false;
        }
    }

    @Override
    public boolean enableRule(Long ruleId) {
        return setRuleEnabled(ruleId, true);
    }

    @Override
    public boolean disableRule(Long ruleId) {
        return setRuleEnabled(ruleId, false);
    }

    @Override
    public String validateRule(AlertRule rule) {
        if (rule == null) {
            return "预警规则不能为空";
        }

        try {
            rule.validate();
            return "";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }

    @Override
    public void refreshCache() {
        // MVP阶段使用内存存储，无需刷新
        // 生产环境中这里应该从数据库重新加载规则
        log.info("刷新预警规则缓存（当前为内存存储，无需操作）");
    }

    @Override
    public boolean hasDuplicateRule(AlertRule rule) {
        if (rule == null || rule.getType() == null) {
            return false;
        }

        try {
            return ruleCache.values().stream()
                    .filter(existingRule -> !Objects.equals(existingRule.getId(), rule.getId()))
                    .anyMatch(existingRule -> isDuplicateRule(existingRule, rule));

        } catch (Exception e) {
            log.error("检查重复规则时发生错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 创建默认预警规则
     */
    private void createDefaultRule(String name, AlertRuleType type, Integer daysBefore, 
                                  List<String> channels, Integer priority) {
        try {
            AlertRule rule = new AlertRule(name, type, daysBefore, priority, true, channels);
            rule.setId(nextRuleId++);
            rule.setCreatedAt(LocalDateTime.now());
            rule.setUpdatedAt(LocalDateTime.now());

            ruleCache.put(rule.getId(), rule);
            log.debug("创建默认预警规则: {}", name);

        } catch (Exception e) {
            log.error("创建默认预警规则失败: {}, name: {}", e.getMessage(), name);
        }
    }

    /**
     * 设置规则启用状态
     */
    private boolean setRuleEnabled(Long ruleId, boolean enabled) {
        if (ruleId == null) {
            log.warn("尝试设置空规则ID的启用状态");
            return false;
        }

        try {
            AlertRule rule = ruleCache.get(ruleId);
            if (rule == null) {
                log.warn("预警规则不存在: {}", ruleId);
                return false;
            }

            rule.setEnabled(enabled);
            rule.setUpdatedAt(LocalDateTime.now());
            
            log.info("预警规则 {} 状态设置为: {}", rule.getName(), enabled ? "启用" : "禁用");
            return true;

        } catch (Exception e) {
            log.error("设置预警规则启用状态时发生错误: {}, ruleId: {}, enabled: {}", 
                     e.getMessage(), ruleId, enabled);
            return false;
        }
    }

    /**
     * 判断两个规则是否重复
     */
    private boolean isDuplicateRule(AlertRule existingRule, AlertRule newRule) {
        if (existingRule.getType() != newRule.getType()) {
            return false;
        }

        if (AlertRuleType.TIME_BASED.equals(newRule.getType())) {
            return Objects.equals(existingRule.getDaysBefore(), newRule.getDaysBefore());
        }

        // 其他类型的重复检查逻辑可以在这里扩展
        return false;
    }
}