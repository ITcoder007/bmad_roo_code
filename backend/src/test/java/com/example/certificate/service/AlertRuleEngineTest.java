package com.example.certificate.service;

import com.example.certificate.domain.model.AlertRule;
import com.example.certificate.domain.model.AlertRuleType;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.service.impl.AlertRuleEngineImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertRuleEngineTest {

    @Mock
    private AlertRuleConfigService alertRuleConfigService;

    @InjectMocks
    private AlertRuleEngineImpl alertRuleEngine;

    private Certificate testCertificate;
    private AlertRule rule30Days;
    private AlertRule rule15Days;
    private AlertRule rule7Days;
    private AlertRule rule1Day;

    @BeforeEach
    void setUp() {
        // 创建测试证书
        testCertificate = createCertificate("test-cert", 25); // 25天后到期

        // 创建测试规则
        rule30Days = createTimeBasedRule("30天预警", 30, 1, true);
        rule15Days = createTimeBasedRule("15天预警", 15, 2, true);
        rule7Days = createTimeBasedRule("7天预警", 7, 3, true);
        rule1Day = createTimeBasedRule("1天预警", 1, 4, true);
    }

    @Test
    void testEvaluateRules_30DaysRule_shouldTrigger() {
        // Given
        Certificate certificate = createCertificate("test-cert", 25); // 25天后到期
        when(alertRuleConfigService.getEnabledRules()).thenReturn(Arrays.asList(rule30Days));

        // When
        boolean result = alertRuleEngine.evaluateRules(certificate);

        // Then
        assertTrue(result, "证书在30天内到期，应该触发30天预警规则");
    }

    @Test
    void testEvaluateRules_noCertificate_shouldReturnFalse() {
        // Given
        Certificate certificate = null;

        // When
        boolean result = alertRuleEngine.evaluateRules(certificate);

        // Then
        assertFalse(result, "证书为空时应该返回false");
    }

    @Test
    void testEvaluateRules_noRulesTriggered_shouldReturnFalse() {
        // Given
        Certificate certificate = createCertificate("test-cert", 60); // 60天后到期
        when(alertRuleConfigService.getEnabledRules()).thenReturn(Arrays.asList(rule30Days, rule15Days, rule7Days, rule1Day));

        // When
        boolean result = alertRuleEngine.evaluateRules(certificate);

        // Then
        assertFalse(result, "证书60天后到期，不应该触发任何预警规则");
    }

    @Test
    void testEvaluateRules_MultipleDaysRules_shouldTriggerMultiple() {
        // Given
        Certificate certificate = createCertificate("test-cert", 5); // 5天后到期
        when(alertRuleConfigService.getEnabledRules()).thenReturn(Arrays.asList(rule30Days, rule15Days, rule7Days, rule1Day));

        // When
        boolean result = alertRuleEngine.evaluateRules(certificate);

        // Then
        assertTrue(result, "证书5天后到期，应该触发多个预警规则");
    }

    @Test
    void testGetTriggeredRules_multipleRules_shouldReturnCorrectRules() {
        // Given
        Certificate certificate = createCertificate("test-cert", 5); // 5天后到期
        List<AlertRule> allRules = Arrays.asList(rule30Days, rule15Days, rule7Days, rule1Day);
        when(alertRuleConfigService.getEnabledRules()).thenReturn(allRules);

        // When
        List<AlertRule> triggeredRules = alertRuleEngine.getTriggeredRules(certificate);

        // Then
        assertEquals(3, triggeredRules.size(), "应该触发3个规则（30天、15天、7天）");
        assertTrue(triggeredRules.stream().anyMatch(r -> r.getName().equals("30天预警")));
        assertTrue(triggeredRules.stream().anyMatch(r -> r.getName().equals("15天预警")));
        assertTrue(triggeredRules.stream().anyMatch(r -> r.getName().equals("7天预警")));
        assertFalse(triggeredRules.stream().anyMatch(r -> r.getName().equals("1天预警")));
    }

    @Test
    void testGetTriggeredRules_sortedByPriority() {
        // Given
        Certificate certificate = createCertificate("test-cert", 10); // 10天后到期，应该触发30天和15天规则
        List<AlertRule> allRules = Arrays.asList(rule30Days, rule15Days);
        when(alertRuleConfigService.getEnabledRules()).thenReturn(allRules);

        // When
        List<AlertRule> triggeredRules = alertRuleEngine.getTriggeredRules(certificate);

        // Then
        assertEquals(2, triggeredRules.size());
        assertEquals(1, triggeredRules.get(0).getPriority(), "第一个规则应该是优先级最高的（数字最小）");
        assertEquals(2, triggeredRules.get(1).getPriority(), "第二个规则应该是优先级第二的");
    }

    @Test
    void testGetHighestPriorityRule_withValidRules_shouldReturnHighestPriority() {
        // Given
        List<AlertRule> rules = Arrays.asList(rule30Days, rule15Days, rule7Days);

        // When
        AlertRule highestPriorityRule = alertRuleEngine.getHighestPriorityRule(rules);

        // Then
        assertNotNull(highestPriorityRule);
        assertEquals("30天预警", highestPriorityRule.getName());
        assertEquals(1, highestPriorityRule.getPriority());
    }

    @Test
    void testGetHighestPriorityRule_withEmptyList_shouldReturnNull() {
        // Given
        List<AlertRule> rules = Collections.emptyList();

        // When
        AlertRule highestPriorityRule = alertRuleEngine.getHighestPriorityRule(rules);

        // Then
        assertNull(highestPriorityRule, "空列表应该返回null");
    }

    @Test
    void testEvaluateRule_timeBasedRule_exactThreshold() {
        // Given
        Certificate certificate = createCertificate("test-cert", 30); // 正好30天
        AlertRule rule = createTimeBasedRule("30天预警", 30, 1, true);

        // When
        boolean result = alertRuleEngine.evaluateRule(rule, certificate);

        // Then
        assertTrue(result, "正好30天时应该触发30天预警规则");
    }

    @Test
    void testEvaluateRule_timeBasedRule_belowThreshold() {
        // Given
        Certificate certificate = createCertificate("test-cert", 25); // 25天
        AlertRule rule = createTimeBasedRule("30天预警", 30, 1, true);

        // When
        boolean result = alertRuleEngine.evaluateRule(rule, certificate);

        // Then
        assertTrue(result, "25天应该触发30天预警规则");
    }

    @Test
    void testEvaluateRule_timeBasedRule_aboveThreshold() {
        // Given
        Certificate certificate = createCertificate("test-cert", 35); // 35天
        AlertRule rule = createTimeBasedRule("30天预警", 30, 1, true);

        // When
        boolean result = alertRuleEngine.evaluateRule(rule, certificate);

        // Then
        assertFalse(result, "35天不应该触发30天预警规则");
    }

    @Test
    void testEvaluateRule_disabledRule_shouldNotTrigger() {
        // Given
        Certificate certificate = createCertificate("test-cert", 25); // 25天
        AlertRule rule = createTimeBasedRule("30天预警", 30, 1, false); // 禁用

        // When
        boolean result = alertRuleEngine.evaluateRule(rule, certificate);

        // Then
        assertFalse(result, "禁用的规则不应该触发");
    }

    @Test
    void testEvaluateRule_expiredCertificate_shouldNotTrigger() {
        // Given
        Certificate certificate = createCertificate("test-cert", -5); // 已过期5天
        AlertRule rule = createTimeBasedRule("30天预警", 30, 1, true);

        // When
        boolean result = alertRuleEngine.evaluateRule(rule, certificate);

        // Then
        assertFalse(result, "已过期的证书不应该触发预警规则");
    }

    @Test
    void testCalculateDaysUntilExpiry_validCertificate() {
        // Given
        Certificate certificate = createCertificate("test-cert", 15);

        // When
        int days = alertRuleEngine.calculateDaysUntilExpiry(certificate);

        // Then
        assertEquals(15, days, "应该正确计算距离到期的天数");
    }

    @Test
    void testCalculateDaysUntilExpiry_nullCertificate() {
        // Given
        Certificate certificate = null;

        // When
        int days = alertRuleEngine.calculateDaysUntilExpiry(certificate);

        // Then
        assertEquals(Integer.MAX_VALUE, days, "空证书应该返回Integer.MAX_VALUE");
    }

    // 辅助方法：创建测试证书
    private Certificate createCertificate(String name, int daysUntilExpiry) {
        Certificate certificate = new Certificate();
        certificate.setId(1L);
        certificate.setName(name);
        certificate.setDomain("example.com");
        certificate.setStatus(CertificateStatus.NORMAL);

        // 计算到期日期
        Date expiryDate = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(daysUntilExpiry));
        certificate.setExpiryDate(expiryDate);

        return certificate;
    }

    // 辅助方法：创建时间基础的预警规则
    private AlertRule createTimeBasedRule(String name, int daysBefore, int priority, boolean enabled) {
        AlertRule rule = new AlertRule();
        rule.setId((long) priority);
        rule.setName(name);
        rule.setType(AlertRuleType.TIME_BASED);
        rule.setDaysBefore(daysBefore);
        rule.setPriority(priority);
        rule.setEnabled(enabled);
        rule.setAlertChannels(Arrays.asList("EMAIL"));
        return rule;
    }
}