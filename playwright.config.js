/**
 * Playwright Test Configuration
 * 用于 Story 2.3 邮件服务功能测试
 */

import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  
  /* 测试超时配置 */
  timeout: 30 * 1000,
  expect: {
    timeout: 10 * 1000,
  },
  
  /* 失败时重试 */
  retries: process.env.CI ? 2 : 1,
  
  /* 并行执行配置 */
  workers: process.env.CI ? 1 : undefined,
  
  /* 报告配置 */
  reporter: [
    ['html'],
    ['junit', { outputFile: 'test-results/junit.xml' }],
    ['list']
  ],
  
  /* 全局设置 */
  use: {
    /* 基本配置 */
    baseURL: 'http://localhost:5173',
    
    /* 收集失败时的调试信息 */
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    
    /* 等待策略 */
    actionTimeout: 10 * 1000,
    navigationTimeout: 30 * 1000,
  },

  /* 项目配置 */
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },
    
    /* 移动端测试（可选） */
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] },
    },
  ],

  /* 开发服务器配置 */
  webServer: [
    {
      command: 'npm run dev',
      url: 'http://localhost:5173',
      reuseExistingServer: !process.env.CI,
      timeout: 120 * 1000,
    },
    {
      command: 'cd backend && mvn spring-boot:run',
      url: 'http://localhost:8080/api/health',
      reuseExistingServer: !process.env.CI,
      timeout: 120 * 1000,
    }
  ],
});