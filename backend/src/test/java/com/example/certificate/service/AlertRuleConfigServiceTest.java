package com.example.certificate.service;

import com.example.certificate.domain.model.AlertRule;
import com.example.certificate.domain.model.AlertRuleType;
import com.example.certificate.service.impl.AlertRuleConfigServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AlertRuleConfigServiceTest {

    private AlertRuleConfigServiceImpl alertRuleConfigService;

    @BeforeEach
    void setUp() {
        alertRuleConfigService = new AlertRuleConfigServiceImpl();
        
        // 设置配置属性
        ReflectionTestUtils.setField(alertRuleConfigService, "defaultRulesEnabled", true);
        ReflectionTestUtils.setField(alertRuleConfigService, "cacheEnabled", true);
        
        // 手动调用初始化方法
        alertRuleConfigService.init();
    }

    @Test
    void testInitializeDefaultRules_shouldCreateFourRules() {
        // Given - setUp中已经初始化

        // When
        List<AlertRule> allRules = alertRuleConfigService.getAllRules();

        // Then
        assertEquals(4, allRules.size(), "应该创建4个默认预警规则");
        
        // 验证规则名称
        List<String> ruleNames = Arrays.asList("30天预警", "15天预警", "7天预警", "1天预警");
        for (String ruleName : ruleNames) {
            assertTrue(allRules.stream().anyMatch(rule -> rule.getName().equals(ruleName)), 
                      "应该包含规则: " + ruleName);
        }
    }

    @Test
    void testGetEnabledRules_shouldReturnSortedByPriority() {
        // Given - setUp中已经初始化

        // When
        List<AlertRule> enabledRules = alertRuleConfigService.getEnabledRules();

        // Then
        assertEquals(4, enabledRules.size(), "所有默认规则都应该是启用的");
        
        // 验证按优先级排序
        for (int i = 0; i < enabledRules.size() - 1; i++) {
            assertTrue(enabledRules.get(i).getPriority() <= enabledRules.get(i + 1).getPriority(),
                      "规则应该按优先级升序排列");
        }
    }

    @Test
    void testGetRulesByType_timeBasedType_shouldReturnAllRules() {
        // Given - setUp中已经初始化

        // When
        List<AlertRule> timeBasedRules = alertRuleConfigService.getRulesByType(AlertRuleType.TIME_BASED);

        // Then
        assertEquals(4, timeBasedRules.size(), "所有默认规则都是基于时间的");
        for (AlertRule rule : timeBasedRules) {
            assertEquals(AlertRuleType.TIME_BASED, rule.getType());
        }
    }

    @Test
    void testGetRulesByType_statusChangeType_shouldReturnEmpty() {
        // Given - setUp中已经初始化

        // When
        List<AlertRule> statusChangeRules = alertRuleConfigService.getRulesByType(AlertRuleType.STATUS_CHANGE);

        // Then
        assertEquals(0, statusChangeRules.size(), "默认规则中没有状态变更类型的规则");
    }

    @Test
    void testAddRule_validRule_shouldAddSuccessfully() {
        // Given
        AlertRule newRule = createTestRule("60天预警", AlertRuleType.TIME_BASED, 60, 5);

        // When
        AlertRule addedRule = alertRuleConfigService.addRule(newRule);

        // Then
        assertNotNull(addedRule, "添加的规则不应该为空");
        assertNotNull(addedRule.getId(), "添加的规则应该有ID");
        assertEquals("60天预警", addedRule.getName());
        
        // 验证规则已添加到缓存中
        assertEquals(5, alertRuleConfigService.getAllRules().size(), "应该有5个规则");
    }

    @Test
    void testAddRule_invalidRule_shouldThrowException() {
        // Given
        AlertRule invalidRule = new AlertRule();
        invalidRule.setName(""); // 空名称，不符合验证规则

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            alertRuleConfigService.addRule(invalidRule);
        }, "无效规则应该抛出异常");
    }

    @Test
    void testAddRule_duplicateRule_shouldThrowException() {
        // Given
        AlertRule duplicateRule = createTestRule("30天预警重复", AlertRuleType.TIME_BASED, 30, 5);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            alertRuleConfigService.addRule(duplicateRule);
        }, "重复的规则应该抛出异常");
    }

    @Test
    void testUpdateRule_existingRule_shouldUpdateSuccessfully() {
        // Given
        List<AlertRule> allRules = alertRuleConfigService.getAllRules();
        AlertRule existingRule = allRules.get(0);
        Long ruleId = existingRule.getId();
        
        existingRule.setName("更新后的30天预警");

        // When
        AlertRule updatedRule = alertRuleConfigService.updateRule(existingRule);

        // Then
        assertNotNull(updatedRule);
        assertEquals("更新后的30天预警", updatedRule.getName());
        assertEquals(ruleId, updatedRule.getId());
    }

    @Test
    void testUpdateRule_nonExistentRule_shouldThrowException() {
        // Given
        AlertRule nonExistentRule = createTestRule("不存在的规则", AlertRuleType.TIME_BASED, 45, 6);
        nonExistentRule.setId(999L); // 不存在的ID

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            alertRuleConfigService.updateRule(nonExistentRule);
        }, "更新不存在的规则应该抛出异常");
    }

    @Test
    void testDeleteRule_existingRule_shouldDeleteSuccessfully() {
        // Given
        List<AlertRule> beforeDelete = alertRuleConfigService.getAllRules();
        AlertRule ruleToDelete = beforeDelete.get(0);
        Long ruleId = ruleToDelete.getId();

        // When
        boolean result = alertRuleConfigService.deleteRule(ruleId);

        // Then
        assertTrue(result, "删除应该成功");
        assertEquals(3, alertRuleConfigService.getAllRules().size(), "应该剩余3个规则");
        assertNull(alertRuleConfigService.getRuleById(ruleId), "删除的规则应该无法再获取到");
    }

    @Test
    void testDeleteRule_nonExistentRule_shouldReturnFalse() {
        // Given
        Long nonExistentId = 999L;

        // When
        boolean result = alertRuleConfigService.deleteRule(nonExistentId);

        // Then
        assertFalse(result, "删除不存在的规则应该返回false");
        assertEquals(4, alertRuleConfigService.getAllRules().size(), "规则数量应该不变");
    }

    @Test
    void testEnableRule_existingRule_shouldEnableSuccessfully() {
        // Given
        List<AlertRule> allRules = alertRuleConfigService.getAllRules();
        AlertRule rule = allRules.get(0);
        Long ruleId = rule.getId();
        
        // 先禁用规则
        alertRuleConfigService.disableRule(ruleId);

        // When
        boolean result = alertRuleConfigService.enableRule(ruleId);

        // Then
        assertTrue(result, "启用规则应该成功");
        assertTrue(alertRuleConfigService.getRuleById(ruleId).getEnabled(), "规则应该是启用状态");
    }

    @Test
    void testDisableRule_existingRule_shouldDisableSuccessfully() {
        // Given
        List<AlertRule> allRules = alertRuleConfigService.getAllRules();
        AlertRule rule = allRules.get(0);
        Long ruleId = rule.getId();

        // When
        boolean result = alertRuleConfigService.disableRule(ruleId);

        // Then
        assertTrue(result, "禁用规则应该成功");
        assertFalse(alertRuleConfigService.getRuleById(ruleId).getEnabled(), "规则应该是禁用状态");
        assertEquals(3, alertRuleConfigService.getEnabledRules().size(), "启用的规则应该减少1个");
    }

    @Test
    void testValidateRule_validRule_shouldReturnEmptyString() {
        // Given
        AlertRule validRule = createTestRule("有效规则", AlertRuleType.TIME_BASED, 45, 5);

        // When
        String validationResult = alertRuleConfigService.validateRule(validRule);

        // Then
        assertEquals("", validationResult, "有效规则的验证结果应该是空字符串");
    }

    @Test
    void testValidateRule_invalidRule_shouldReturnErrorMessage() {
        // Given
        AlertRule invalidRule = new AlertRule();
        invalidRule.setName(""); // 空名称

        // When
        String validationResult = alertRuleConfigService.validateRule(invalidRule);

        // Then
        assertFalse(validationResult.isEmpty(), "无效规则应该返回错误信息");
        assertTrue(validationResult.contains("预警规则名称不能为空"), "错误信息应该包含具体的验证失败原因");
    }

    @Test
    void testHasDuplicateRule_duplicateTimeBasedRule_shouldReturnTrue() {
        // Given
        AlertRule duplicateRule = createTestRule("重复规则", AlertRuleType.TIME_BASED, 30, 5);

        // When
        boolean hasDuplicate = alertRuleConfigService.hasDuplicateRule(duplicateRule);

        // Then
        assertTrue(hasDuplicate, "相同天数的时间基础规则应该被识别为重复");
    }

    @Test
    void testHasDuplicateRule_uniqueTimeBasedRule_shouldReturnFalse() {
        // Given
        AlertRule uniqueRule = createTestRule("唯一规则", AlertRuleType.TIME_BASED, 45, 5);

        // When
        boolean hasDuplicate = alertRuleConfigService.hasDuplicateRule(uniqueRule);

        // Then
        assertFalse(hasDuplicate, "不同天数的时间基础规则不应该被识别为重复");
    }

    @Test
    void testGetRuleById_existingRule_shouldReturnRule() {
        // Given
        List<AlertRule> allRules = alertRuleConfigService.getAllRules();
        AlertRule existingRule = allRules.get(0);
        Long ruleId = existingRule.getId();

        // When
        AlertRule foundRule = alertRuleConfigService.getRuleById(ruleId);

        // Then
        assertNotNull(foundRule, "应该能找到存在的规则");
        assertEquals(existingRule.getName(), foundRule.getName());
        assertEquals(ruleId, foundRule.getId());
    }

    @Test
    void testGetRuleById_nonExistentRule_shouldReturnNull() {
        // Given
        Long nonExistentId = 999L;

        // When
        AlertRule foundRule = alertRuleConfigService.getRuleById(nonExistentId);

        // Then
        assertNull(foundRule, "不存在的规则应该返回null");
    }

    // 辅助方法：创建测试规则
    private AlertRule createTestRule(String name, AlertRuleType type, int daysBefore, int priority) {
        AlertRule rule = new AlertRule();
        rule.setName(name);
        rule.setType(type);
        rule.setDaysBefore(daysBefore);
        rule.setPriority(priority);
        rule.setEnabled(true);
        rule.setAlertChannels(Arrays.asList("EMAIL"));
        return rule;
    }
}