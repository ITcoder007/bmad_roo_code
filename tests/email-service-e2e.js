/**
 * Email Service End-to-End Test
 * Story 2.3: 邮件通知服务（日志实现）功能测试
 * 
 * 使用 Playwright 进行前后端集成测试
 */

const { test, expect } = require('@playwright/test');
const fs = require('fs');
const path = require('path');

// 测试配置
const BASE_URL = 'http://localhost:5173';  // 前端地址
const API_BASE_URL = 'http://localhost:8080/api';  // 后端地址
const LOG_FILE = path.join(__dirname, '../backend/logs/certificate-management.log');

// 测试数据
const TEST_EMAIL = 'test@example.com';
const ADMIN_CREDENTIALS = { username: 'admin', password: 'admin123' };

test.describe('邮件服务功能测试 - Story 2.3', () => {
  
  test.beforeEach(async ({ page }) => {
    // 每个测试前清理和准备
    console.log('🧪 准备测试环境...');
    
    // 访问应用
    await page.goto(BASE_URL);
    
    // 等待页面加载
    await page.waitForLoadState('networkidle');
  });

  test('1. 验证邮件服务配置显示', async ({ page }) => {
    console.log('📧 测试邮件服务配置显示');
    
    // 登录系统
    await loginAsAdmin(page);
    
    // 导航到设置页面
    await page.click('[data-testid="settings-menu"]', { timeout: 5000 }).catch(() => {
      // 如果没有测试ID，尝试通过文本查找
      return page.click('text=设置');
    });
    
    // 检查邮件配置区域
    const emailSection = page.locator('text=邮件配置').or(page.locator('text=Email'));
    await expect(emailSection).toBeVisible({ timeout: 10000 });
    
    // 验证邮件模式显示为"日志模式"
    const modeText = page.locator('text=日志模式').or(page.locator('text=log'));
    await expect(modeText).toBeVisible({ timeout: 5000 });
    
    console.log('✅ 邮件配置显示正常');
  });

  test('2. 验证单个证书预警邮件功能', async ({ page, request }) => {
    console.log('📧 测试单个证书预警邮件功能');
    
    // 记录测试开始前的日志行数
    const logStartLine = getLogLineCount();
    
    // 通过API发送邮件预警
    const response = await request.post(`${API_BASE_URL}/api/v1/monitoring/email-alert`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Basic ' + Buffer.from(`${ADMIN_CREDENTIALS.username}:${ADMIN_CREDENTIALS.password}`).toString('base64')
      },
      data: {
        certificateId: 1,
        daysUntilExpiry: 7,
        recipientEmail: TEST_EMAIL
      }
    });
    
    // 验证API响应
    expect(response.status()).toBe(200);
    const responseData = await response.json();
    expect(responseData.success).toBe(true);
    
    // 等待日志写入
    await page.waitForTimeout(1000);
    
    // 验证日志记录
    const newLogs = getNewLogs(logStartLine);
    expect(newLogs).toContain('📧 邮件预警发送');
    expect(newLogs).toContain('证书详情');
    expect(newLogs).toContain(TEST_EMAIL);
    
    console.log('✅ 单个证书预警邮件功能正常');
  });

  test('3. 验证每日摘要邮件功能', async ({ page, request }) => {
    console.log('📧 测试每日摘要邮件功能');
    
    const logStartLine = getLogLineCount();
    
    // 通过API触发每日摘要
    const response = await request.get(`${API_BASE_URL}/api/v1/monitoring/daily-summary?recipient=${TEST_EMAIL}`, {
      headers: {
        'Authorization': 'Basic ' + Buffer.from(`${ADMIN_CREDENTIALS.username}:${ADMIN_CREDENTIALS.password}`).toString('base64')
      }
    });
    
    expect(response.status()).toBe(200);
    const responseData = await response.json();
    expect(responseData.success).toBe(true);
    
    // 等待日志写入
    await page.waitForTimeout(1000);
    
    // 验证日志记录
    const newLogs = getNewLogs(logStartLine);
    expect(newLogs).toContain('📧 每日摘要邮件');
    expect(newLogs).toContain(TEST_EMAIL);
    
    console.log('✅ 每日摘要邮件功能正常');
  });

  test('4. 验证批量邮件预警功能', async ({ page, request }) => {
    console.log('📧 测试批量邮件预警功能');
    
    const logStartLine = getLogLineCount();
    
    // 通过API触发批量预警
    const response = await request.post(`${API_BASE_URL}/api/v1/monitoring/batch-alerts`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Basic ' + Buffer.from(`${ADMIN_CREDENTIALS.username}:${ADMIN_CREDENTIALS.password}`).toString('base64')
      },
      data: {
        recipientEmail: TEST_EMAIL,
        maxCertificates: 5
      }
    });
    
    expect(response.status()).toBe(200);
    const responseData = await response.json();
    expect(responseData.success).toBe(true);
    
    // 等待日志写入
    await page.waitForTimeout(1500);
    
    // 验证日志记录
    const newLogs = getNewLogs(logStartLine);
    expect(newLogs).toContain('📧 批量邮件预警');
    expect(newLogs).toContain('批量邮件预警完成');
    
    console.log('✅ 批量邮件预警功能正常');
  });

  test('5. 验证前端邮件测试功能', async ({ page }) => {
    console.log('🖥️ 测试前端邮件测试功能');
    
    // 登录系统
    await loginAsAdmin(page);
    
    // 导航到证书管理页面
    await page.click('[data-testid="certificates-menu"]', { timeout: 5000 }).catch(() => {
      return page.click('text=证书管理');
    });
    
    // 等待证书列表加载
    await page.waitForSelector('[data-testid="certificate-list"], .certificate-item, table', { timeout: 10000 });
    
    // 查找第一个证书行并点击"发送预警"按钮
    const firstCertRow = page.locator('tr').nth(1).or(page.locator('.certificate-item').first());
    await expect(firstCertRow).toBeVisible({ timeout: 5000 });
    
    // 查找并点击发送预警按钮
    const alertButton = firstCertRow.locator('button:has-text("预警"), button:has-text("测试"), [data-testid="send-alert"]').first();
    
    if (await alertButton.count() > 0) {
      const logStartLine = getLogLineCount();
      
      await alertButton.click();
      
      // 等待操作完成
      await page.waitForTimeout(2000);
      
      // 验证成功消息
      const successMessage = page.locator('text=发送成功, text=邮件已发送, .success-message');
      await expect(successMessage).toBeVisible({ timeout: 5000 });
      
      // 验证日志记录
      const newLogs = getNewLogs(logStartLine);
      expect(newLogs).toContain('📧 邮件预警发送');
      
      console.log('✅ 前端邮件测试功能正常');
    } else {
      console.log('⚠️ 未找到邮件预警按钮，跳过此测试');
    }
  });

  test('6. 验证错误处理和用户反馈', async ({ page, request }) => {
    console.log('🚨 测试错误处理功能');
    
    // 测试无效证书ID的错误处理
    const response = await request.post(`${API_BASE_URL}/api/v1/monitoring/email-alert`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Basic ' + Buffer.from(`${ADMIN_CREDENTIALS.username}:${ADMIN_CREDENTIALS.password}`).toString('base64')
      },
      data: {
        certificateId: 99999,
        daysUntilExpiry: -1,
        recipientEmail: 'invalid-email'
      }
    });
    
    // 验证适当的错误响应
    if (response.status() === 200) {
      const responseData = await response.json();
      expect(responseData.success).toBe(false);
      expect(responseData.message).toBeDefined();
    } else {
      expect([400, 404, 422]).toContain(response.status());
    }
    
    console.log('✅ 错误处理功能正常');
  });

  test('7. 验证日志格式和内容完整性', async ({ page, request }) => {
    console.log('📝 验证日志格式和内容完整性');
    
    const logStartLine = getLogLineCount();
    
    // 发送一个完整的邮件预警请求
    await request.post(`${API_BASE_URL}/api/v1/monitoring/email-alert`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Basic ' + Buffer.from(`${ADMIN_CREDENTIALS.username}:${ADMIN_CREDENTIALS.password}`).toString('base64')
      },
      data: {
        certificateId: 1,
        daysUntilExpiry: 15,
        recipientEmail: TEST_EMAIL
      }
    });
    
    await page.waitForTimeout(1000);
    
    // 获取新日志并验证格式
    const newLogs = getNewLogs(logStartLine);
    
    // 验证日志包含所有必需元素
    const requiredElements = [
      '📧 邮件预警发送',
      '证书详情:',
      '📋 证书名称:',
      '🌐 域名:',
      '📅 颁发日期:',
      '⏰ 到期日期:',
      '📊 证书状态:',
      '⏳ 剩余天数:',
      '📧 收件人:',
      '🏷️ 预警类型:'
    ];
    
    const missingElements = requiredElements.filter(element => !newLogs.includes(element));
    
    if (missingElements.length === 0) {
      console.log('✅ 日志格式完整，包含所有必需元素');
    } else {
      console.log(`❌ 日志格式不完整，缺少: ${missingElements.join(', ')}`);
      throw new Error(`日志格式验证失败，缺少元素: ${missingElements.join(', ')}`);
    }
  });

  test('8. 性能测试 - 邮件服务响应时间', async ({ page, request }) => {
    console.log('⚡ 执行邮件服务性能测试');
    
    const startTime = Date.now();
    
    // 发送邮件预警请求
    const response = await request.post(`${API_BASE_URL}/api/v1/monitoring/email-alert`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Basic ' + Buffer.from(`${ADMIN_CREDENTIALS.username}:${ADMIN_CREDENTIALS.password}`).toString('base64')
      },
      data: {
        certificateId: 1,
        daysUntilExpiry: 30,
        recipientEmail: TEST_EMAIL
      }
    });
    
    const endTime = Date.now();
    const responseTime = endTime - startTime;
    
    expect(response.status()).toBe(200);
    
    // 验证响应时间（应该小于2秒）
    expect(responseTime).toBeLessThan(2000);
    
    console.log(`✅ 邮件服务响应时间: ${responseTime}ms (< 2000ms)`);
  });

});

// 工具函数
async function loginAsAdmin(page) {
  console.log('🔐 管理员登录...');
  
  // 检查是否已经登录
  const dashboardElement = page.locator('text=仪表板, text=Dashboard, [data-testid="dashboard"]');
  
  if (await dashboardElement.count() > 0) {
    console.log('✅ 已经登录');
    return;
  }
  
  // 查找登录表单
  const usernameInput = page.locator('input[name="username"], input[type="text"]').first();
  const passwordInput = page.locator('input[name="password"], input[type="password"]').first();
  const loginButton = page.locator('button:has-text("登录"), button:has-text("Login"), [type="submit"]').first();
  
  // 填写登录信息
  await usernameInput.fill(ADMIN_CREDENTIALS.username);
  await passwordInput.fill(ADMIN_CREDENTIALS.password);
  await loginButton.click();
  
  // 等待登录完成
  await page.waitForURL('**/dashboard', { timeout: 10000 }).catch(() => {
    // 如果URL不变，检查是否有成功指示器
    return page.waitForSelector('[data-testid="dashboard"], text=仪表板', { timeout: 10000 });
  });
  
  console.log('✅ 登录成功');
}

function getLogLineCount() {
  try {
    if (fs.existsSync(LOG_FILE)) {
      const content = fs.readFileSync(LOG_FILE, 'utf8');
      return content.split('\n').length;
    }
    return 0;
  } catch (error) {
    console.log('⚠️ 无法读取日志文件:', error.message);
    return 0;
  }
}

function getNewLogs(startLine) {
  try {
    if (fs.existsSync(LOG_FILE)) {
      const content = fs.readFileSync(LOG_FILE, 'utf8');
      const lines = content.split('\n');
      return lines.slice(startLine).join('\n');
    }
    return '';
  } catch (error) {
    console.log('⚠️ 无法读取新日志:', error.message);
    return '';
  }
}