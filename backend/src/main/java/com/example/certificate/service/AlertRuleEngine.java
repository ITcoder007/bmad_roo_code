package com.example.certificate.service;

import com.example.certificate.domain.model.AlertRule;
import com.example.certificate.domain.model.Certificate;

import java.util.List;

/**
 * 预警规则引擎接口
 * 负责评估证书是否触发预警规则，并管理规则的优先级和筛选逻辑
 */
public interface AlertRuleEngine {

    /**
     * 评估证书是否触发任何预警规则
     *
     * @param certificate 要评估的证书
     * @return 如果触发任何预警规则返回true，否则返回false
     */
    boolean evaluateRules(Certificate certificate);

    /**
     * 获取被触发的预警规则列表
     *
     * @param certificate 要评估的证书
     * @return 被触发的规则列表，按优先级排序
     */
    List<AlertRule> getTriggeredRules(Certificate certificate);

    /**
     * 根据优先级获取最高优先级的规则
     *
     * @param rules 规则列表
     * @return 最高优先级的规则，如果列表为空则返回null
     */
    AlertRule getHighestPriorityRule(List<AlertRule> rules);

    /**
     * 评估单个规则是否被触发
     *
     * @param rule        要评估的规则
     * @param certificate 证书信息
     * @return 如果规则被触发返回true，否则返回false
     */
    boolean evaluateRule(AlertRule rule, Certificate certificate);

    /**
     * 计算证书距离到期的天数
     *
     * @param certificate 证书信息
     * @return 距离到期的天数，已过期返回负数
     */
    int calculateDaysUntilExpiry(Certificate certificate);
}