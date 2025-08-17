package com.example.certificate.service;

import com.example.certificate.domain.model.AlertRule;
import com.example.certificate.domain.model.AlertRuleType;
import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;
import com.example.certificate.service.impl.AlertRuleConfigServiceImpl;
import com.example.certificate.service.impl.AlertRuleEngineImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AlertRuleIntegrationTest {

    private AlertRuleConfigService alertRuleConfigService;
    private AlertRuleEngine alertRuleEngine;

    @BeforeEach
    void setUp() {
        // 创建真实的配置服务实例
        alertRuleConfigService = new AlertRuleConfigServiceImpl();
        
        // 设置配置属性
        ReflectionTestUtils.setField(alertRuleConfigService, "defaultRulesEnabled", true);
        ReflectionTestUtils.setField(alertRuleConfigService, "cacheEnabled", true);
        
        // 手动调用初始化方法
        ((AlertRuleConfigServiceImpl) alertRuleConfigService).init();
        
        // 创建规则引擎实例
        alertRuleEngine = new AlertRuleEngineImpl();
        ReflectionTestUtils.setField(alertRuleEngine, "alertRuleConfigService", alertRuleConfigService);
    }

    @Test
    void testCompleteAlertRuleWorkflow() {
        // Subtask 6.1: 验证规则引擎与配置服务的集成

        // Given - 准备测试数据
        Certificate cert30Days = createCertificate("cert-30-days", 25); // 25天后到期
        Certificate cert15Days = createCertificate("cert-15-days", 10); // 10天后到期
        Certificate cert7Days = createCertificate("cert-7-days", 5);    // 5天后到期
        Certificate cert1Day = createCertificate("cert-1-day", 0);      // 今天到期
        Certificate normalCert = createCertificate("normal-cert", 60);  // 60天后到期

        // When & Then - 验证不同场景的规则触发
        
        // Subtask 6.3: 验证30天规则的准确性
        assertTrue(alertRuleEngine.evaluateRules(cert30Days), "25天后到期的证书应该触发30天预警");
        List<AlertRule> triggeredRules30 = alertRuleEngine.getTriggeredRules(cert30Days);
        assertEquals(1, triggeredRules30.size());
        assertEquals("30天预警", triggeredRules30.get(0).getName());

        // Subtask 6.4: 验证15天、7天、1天规则的准确性
        assertTrue(alertRuleEngine.evaluateRules(cert15Days), "10天后到期的证书应该触发预警");
        List<AlertRule> triggeredRules15 = alertRuleEngine.getTriggeredRules(cert15Days);
        assertEquals(2, triggeredRules15.size()); // 应该触发30天和15天规则
        assertTrue(triggeredRules15.stream().anyMatch(r -> r.getName().equals("30天预警")));
        assertTrue(triggeredRules15.stream().anyMatch(r -> r.getName().equals("15天预警")));

        assertTrue(alertRuleEngine.evaluateRules(cert7Days), "5天后到期的证书应该触发预警");
        List<AlertRule> triggeredRules7 = alertRuleEngine.getTriggeredRules(cert7Days);
        assertEquals(3, triggeredRules7.size()); // 应该触发30天、15天、7天规则
        assertTrue(triggeredRules7.stream().anyMatch(r -> r.getName().equals("30天预警")));
        assertTrue(triggeredRules7.stream().anyMatch(r -> r.getName().equals("15天预警")));
        assertTrue(triggeredRules7.stream().anyMatch(r -> r.getName().equals("7天预警")));

        assertTrue(alertRuleEngine.evaluateRules(cert1Day), "今天到期的证书应该触发预警");
        List<AlertRule> triggeredRules1 = alertRuleEngine.getTriggeredRules(cert1Day);
        assertEquals(4, triggeredRules1.size()); // 应该触发所有规则

        // 验证正常证书不触发预警
        assertFalse(alertRuleEngine.evaluateRules(normalCert), "60天后到期的证书不应该触发任何预警");
        List<AlertRule> triggeredRulesNormal = alertRuleEngine.getTriggeredRules(normalCert);
        assertEquals(0, triggeredRulesNormal.size());
    }

    @Test
    void testRulePriorityHandling() {
        // Subtask 6.5: 验证规则优先级和多规则触发场景

        // Given
        Certificate cert = createCertificate("test-cert", 10); // 10天后到期

        // When
        List<AlertRule> triggeredRules = alertRuleEngine.getTriggeredRules(cert);
        AlertRule highestPriorityRule = alertRuleEngine.getHighestPriorityRule(triggeredRules);

        // Then
        assertEquals(2, triggeredRules.size(), "应该触发2个规则");
        
        // 验证按优先级排序
        for (int i = 0; i < triggeredRules.size() - 1; i++) {
            assertTrue(triggeredRules.get(i).getPriority() <= triggeredRules.get(i + 1).getPriority(),
                      "规则应该按优先级升序排列");
        }

        // 验证最高优先级规则
        assertNotNull(highestPriorityRule);
        assertEquals("30天预警", highestPriorityRule.getName());
        assertEquals(1, highestPriorityRule.getPriority());
    }

    @Test
    void testCustomRuleConfiguration() {
        // Subtask 6.2: 测试规则配置功能

        // Given - 添加自定义规则
        AlertRule customRule = new AlertRule();
        customRule.setName("60天预警");
        customRule.setType(AlertRuleType.TIME_BASED);
        customRule.setDaysBefore(60);
        customRule.setPriority(1); // 最高优先级
        customRule.setEnabled(true);
        customRule.setAlertChannels(Arrays.asList("EMAIL", "SMS"));

        // When
        AlertRule addedRule = alertRuleConfigService.addRule(customRule);
        Certificate cert = createCertificate("test-cert", 50); // 50天后到期

        // Then
        assertNotNull(addedRule);
        assertNotNull(addedRule.getId());
        assertEquals("60天预警", addedRule.getName());

        // 验证新规则被触发
        assertTrue(alertRuleEngine.evaluateRules(cert), "50天后到期的证书应该触发60天预警");
        List<AlertRule> triggeredRules = alertRuleEngine.getTriggeredRules(cert);
        assertTrue(triggeredRules.stream().anyMatch(r -> r.getName().equals("60天预警")));

        // 验证优先级
        AlertRule highestPriorityRule = alertRuleEngine.getHighestPriorityRule(triggeredRules);
        assertEquals("60天预警", highestPriorityRule.getName());
        assertEquals(1, highestPriorityRule.getPriority());
    }

    @Test
    void testRuleEnableDisableFunctionality() {
        // Given
        List<AlertRule> allRules = alertRuleConfigService.getAllRules();
        AlertRule ruleToDisable = allRules.get(0); // 获取第一个规则
        Long ruleId = ruleToDisable.getId();
        
        Certificate cert = createCertificate("test-cert", 25); // 25天后到期

        // 验证规则初始状态为启用
        assertTrue(alertRuleEngine.evaluateRules(cert), "初始状态应该触发预警");

        // When - 禁用规则
        boolean disableResult = alertRuleConfigService.disableRule(ruleId);

        // Then
        assertTrue(disableResult, "禁用操作应该成功");
        
        // 验证启用的规则数量减少
        List<AlertRule> enabledRulesAfterDisable = alertRuleConfigService.getEnabledRules();
        assertEquals(3, enabledRulesAfterDisable.size(), "启用的规则应该减少到3个");

        // When - 重新启用规则
        boolean enableResult = alertRuleConfigService.enableRule(ruleId);

        // Then
        assertTrue(enableResult, "启用操作应该成功");
        
        List<AlertRule> enabledRulesAfterEnable = alertRuleConfigService.getEnabledRules();
        assertEquals(4, enabledRulesAfterEnable.size(), "启用的规则应该恢复到4个");
    }

    @Test
    void testRuleValidationAndDuplicateCheck() {
        // Given - 创建无效规则
        AlertRule invalidRule = new AlertRule();
        invalidRule.setName(""); // 空名称
        invalidRule.setType(AlertRuleType.TIME_BASED);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            alertRuleConfigService.addRule(invalidRule);
        }, "添加无效规则应该抛出异常");

        // Given - 创建重复规则
        AlertRule duplicateRule = new AlertRule();
        duplicateRule.setName("重复的30天预警");
        duplicateRule.setType(AlertRuleType.TIME_BASED);
        duplicateRule.setDaysBefore(30); // 与现有规则相同的天数
        duplicateRule.setPriority(5);
        duplicateRule.setEnabled(true);
        duplicateRule.setAlertChannels(Arrays.asList("EMAIL"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            alertRuleConfigService.addRule(duplicateRule);
        }, "添加重复规则应该抛出异常");
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
}