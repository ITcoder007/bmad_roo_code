package com.example.certificate.service;

import com.example.certificate.domain.model.AlertRule;
import com.example.certificate.domain.model.AlertRuleType;

import java.util.List;

/**
 * 预警规则配置服务接口
 * 负责管理预警规则的配置、加载和缓存
 */
public interface AlertRuleConfigService {

    /**
     * 获取所有启用的预警规则
     *
     * @return 启用的预警规则列表，按优先级排序
     */
    List<AlertRule> getEnabledRules();

    /**
     * 获取所有预警规则（包括禁用的）
     *
     * @return 所有预警规则列表
     */
    List<AlertRule> getAllRules();

    /**
     * 根据类型获取预警规则
     *
     * @param type 预警规则类型
     * @return 指定类型的预警规则列表
     */
    List<AlertRule> getRulesByType(AlertRuleType type);

    /**
     * 根据ID获取预警规则
     *
     * @param ruleId 规则ID
     * @return 预警规则，如果不存在则返回null
     */
    AlertRule getRuleById(Long ruleId);

    /**
     * 初始化默认预警规则
     * 包括30天、15天、7天、1天的基础预警规则
     */
    void initializeDefaultRules();

    /**
     * 添加新的预警规则
     *
     * @param rule 要添加的预警规则
     * @return 添加后的预警规则（包含生成的ID）
     */
    AlertRule addRule(AlertRule rule);

    /**
     * 更新预警规则
     *
     * @param rule 要更新的预警规则
     * @return 更新后的预警规则
     */
    AlertRule updateRule(AlertRule rule);

    /**
     * 删除预警规则
     *
     * @param ruleId 要删除的规则ID
     * @return 是否删除成功
     */
    boolean deleteRule(Long ruleId);

    /**
     * 启用预警规则
     *
     * @param ruleId 规则ID
     * @return 是否操作成功
     */
    boolean enableRule(Long ruleId);

    /**
     * 禁用预警规则
     *
     * @param ruleId 规则ID
     * @return 是否操作成功
     */
    boolean disableRule(Long ruleId);

    /**
     * 验证规则配置是否有效
     *
     * @param rule 要验证的规则
     * @return 验证结果，无错误返回空字符串
     */
    String validateRule(AlertRule rule);

    /**
     * 刷新规则缓存
     */
    void refreshCache();

    /**
     * 检查是否存在重复的规则配置
     *
     * @param rule 要检查的规则
     * @return 是否存在重复配置
     */
    boolean hasDuplicateRule(AlertRule rule);
}