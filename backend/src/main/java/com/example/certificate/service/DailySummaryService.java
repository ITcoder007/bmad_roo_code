package com.example.certificate.service;

/**
 * 每日摘要服务接口
 * 负责生成和发送证书状态的每日摘要报告
 */
public interface DailySummaryService {
    
    /**
     * 生成并发送每日摘要报告
     * 统计即将过期和已过期的证书，发送摘要邮件
     */
    void generateAndSendDailySummary();
    
    /**
     * 生成并发送每日摘要报告（指定收件人）
     * 
     * @param recipientEmail 收件人邮箱
     */
    void generateAndSendDailySummary(String recipientEmail);
}