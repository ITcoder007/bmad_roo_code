package com.example.certificate.infrastructure.external.email;

import com.example.certificate.config.EmailConfig;
import com.example.certificate.domain.model.Certificate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 邮件模板处理类
 * 负责邮件内容的格式化和模板变量替换
 */
@Component
public class EmailTemplate {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    /**
     * 生成证书过期预警邮件内容
     */
    public EmailContent generateExpiryAlertContent(Certificate certificate, 
                                                  int daysUntilExpiry, 
                                                  EmailConfig.EmailTemplateConfig templateConfig) {
        if (templateConfig == null) {
            return generateDefaultExpiryAlertContent(certificate, daysUntilExpiry);
        }
        
        Map<String, String> variables = createExpiryAlertVariables(certificate, daysUntilExpiry);
        
        String subject = replaceVariables(templateConfig.getSubject(), variables);
        String content = replaceVariables(templateConfig.getContentTemplate(), variables);
        
        return new EmailContent(subject, content, templateConfig.isHtmlEnabled());
    }
    
    /**
     * 生成每日摘要邮件内容
     */
    public EmailContent generateDailySummaryContent(List<Certificate> expiringSoonCertificates,
                                                   List<Certificate> expiredCertificates,
                                                   EmailConfig.EmailTemplateConfig templateConfig) {
        if (templateConfig == null) {
            return generateDefaultDailySummaryContent(expiringSoonCertificates, expiredCertificates);
        }
        
        Map<String, String> variables = createDailySummaryVariables(expiringSoonCertificates, expiredCertificates);
        
        String subject = replaceVariables(templateConfig.getSubject(), variables);
        String content = replaceVariables(templateConfig.getContentTemplate(), variables);
        
        return new EmailContent(subject, content, templateConfig.isHtmlEnabled());
    }
    
    /**
     * 创建证书过期预警的模板变量
     */
    private Map<String, String> createExpiryAlertVariables(Certificate certificate, int daysUntilExpiry) {
        Map<String, String> variables = new HashMap<>();
        variables.put("certificateName", certificate.getName());
        variables.put("domain", certificate.getDomain());
        variables.put("expiryDate", DATE_FORMAT.format(certificate.getExpiryDate()));
        variables.put("issueDate", DATE_FORMAT.format(certificate.getIssueDate()));
        variables.put("daysUntilExpiry", String.valueOf(daysUntilExpiry));
        variables.put("alertType", getAlertTypeByDays(daysUntilExpiry));
        variables.put("status", certificate.getStatus().toString());
        variables.put("currentDate", DATE_FORMAT.format(new Date()));
        variables.put("currentDateOnly", DATE_ONLY_FORMAT.format(new Date()));
        return variables;
    }
    
    /**
     * 创建每日摘要的模板变量
     */
    private Map<String, String> createDailySummaryVariables(List<Certificate> expiringSoonCertificates,
                                                           List<Certificate> expiredCertificates) {
        Map<String, String> variables = new HashMap<>();
        variables.put("expiringSoonCount", String.valueOf(expiringSoonCertificates.size()));
        variables.put("expiredCount", String.valueOf(expiredCertificates.size()));
        variables.put("currentDate", DATE_ONLY_FORMAT.format(new Date()));
        variables.put("currentDateTime", DATE_FORMAT.format(new Date()));
        
        // 添加即将过期证书的详细信息
        StringBuilder expiringSoonDetails = new StringBuilder();
        for (Certificate cert : expiringSoonCertificates) {
            expiringSoonDetails.append(String.format("- %s (%s) - 剩余%d天\n", 
                cert.getName(), cert.getDomain(), cert.getDaysUntilExpiry()));
        }
        variables.put("expiringSoonDetails", expiringSoonDetails.toString());
        
        // 添加已过期证书的详细信息
        StringBuilder expiredDetails = new StringBuilder();
        for (Certificate cert : expiredCertificates) {
            expiredDetails.append(String.format("- %s (%s) - 已过期%d天\n", 
                cert.getName(), cert.getDomain(), Math.abs(cert.getDaysUntilExpiry())));
        }
        variables.put("expiredDetails", expiredDetails.toString());
        
        return variables;
    }
    
    /**
     * 替换模板中的变量
     */
    private String replaceVariables(String template, Map<String, String> variables) {
        if (template == null) {
            return "";
        }
        
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(placeholder, value);
        }
        
        return result;
    }
    
    /**
     * 生成默认的证书过期预警邮件内容
     */
    private EmailContent generateDefaultExpiryAlertContent(Certificate certificate, int daysUntilExpiry) {
        String subject = String.format("🚨 证书即将过期预警 - %s", certificate.getName());
        
        String content = String.format(
            "尊敬的管理员，\n\n" +
            "证书预警通知：\n\n" +
            "证书名称：%s\n" +
            "证书域名：%s\n" +
            "到期日期：%s\n" +
            "剩余天数：%d天\n" +
            "预警类型：%s\n\n" +
            "请及时处理证书续期事宜。\n\n" +
            "此邮件由证书生命周期管理系统自动发送。\n" +
            "发送时间：%s",
            certificate.getName(),
            certificate.getDomain(),
            DATE_FORMAT.format(certificate.getExpiryDate()),
            daysUntilExpiry,
            getAlertTypeByDays(daysUntilExpiry),
            DATE_FORMAT.format(new Date())
        );
        
        return new EmailContent(subject, content, false);
    }
    
    /**
     * 生成默认的每日摘要邮件内容
     */
    private EmailContent generateDefaultDailySummaryContent(List<Certificate> expiringSoonCertificates,
                                                           List<Certificate> expiredCertificates) {
        String subject = String.format("📊 证书状态每日摘要 - %s", DATE_ONLY_FORMAT.format(new Date()));
        
        StringBuilder content = new StringBuilder();
        content.append("证书状态每日摘要报告\n\n");
        content.append(String.format("即将过期证书：%d个\n", expiringSoonCertificates.size()));
        content.append(String.format("已过期证书：%d个\n\n", expiredCertificates.size()));
        
        if (!expiringSoonCertificates.isEmpty()) {
            content.append("即将过期的证书详情：\n");
            for (Certificate cert : expiringSoonCertificates) {
                content.append(String.format("- %s (%s) - 剩余%d天\n", 
                    cert.getName(), cert.getDomain(), cert.getDaysUntilExpiry()));
            }
            content.append("\n");
        }
        
        if (!expiredCertificates.isEmpty()) {
            content.append("已过期的证书详情：\n");
            for (Certificate cert : expiredCertificates) {
                content.append(String.format("- %s (%s) - 已过期%d天\n", 
                    cert.getName(), cert.getDomain(), Math.abs(cert.getDaysUntilExpiry())));
            }
            content.append("\n");
        }
        
        content.append("详细信息请查看系统管理界面。\n\n");
        content.append("此邮件由证书生命周期管理系统自动发送。\n");
        content.append(String.format("发送时间：%s", DATE_FORMAT.format(new Date())));
        
        return new EmailContent(subject, content.toString(), false);
    }
    
    /**
     * 根据剩余天数确定预警类型
     */
    private String getAlertTypeByDays(int daysUntilExpiry) {
        if (daysUntilExpiry <= 0) {
            return "已过期预警";
        } else if (daysUntilExpiry <= 1) {
            return "1天预警";
        } else if (daysUntilExpiry <= 7) {
            return "7天预警";
        } else if (daysUntilExpiry <= 15) {
            return "15天预警";
        } else if (daysUntilExpiry <= 30) {
            return "30天预警";
        } else {
            return "常规监控";
        }
    }
    
    /**
     * 邮件内容类
     */
    public static class EmailContent {
        private final String subject;
        private final String content;
        private final boolean htmlEnabled;
        
        public EmailContent(String subject, String content, boolean htmlEnabled) {
            this.subject = subject;
            this.content = content;
            this.htmlEnabled = htmlEnabled;
        }
        
        public String getSubject() {
            return subject;
        }
        
        public String getContent() {
            return content;
        }
        
        public boolean isHtmlEnabled() {
            return htmlEnabled;
        }
        
        @Override
        public String toString() {
            return "EmailContent{" +
                    "subject='" + subject + '\'' +
                    ", contentLength=" + (content != null ? content.length() : 0) +
                    ", htmlEnabled=" + htmlEnabled +
                    '}';
        }
    }
}