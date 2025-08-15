-- 插入示例证书数据
INSERT INTO `certificate` (`name`, `domain`, `issuer`, `issue_date`, `expiry_date`, `certificate_type`, `status`) VALUES
('生产环境SSL证书', 'www.example.com', 'DigiCert', '2024-01-01 00:00:00', '2025-01-01 00:00:00', 'SSL', 'NORMAL'),
('API服务证书', 'api.example.com', 'Let\'s Encrypt', '2024-06-01 00:00:00', '2024-09-01 00:00:00', 'SSL', 'EXPIRING_SOON'),
('测试环境证书', 'test.example.com', 'Let\'s Encrypt', '2023-12-01 00:00:00', '2024-03-01 00:00:00', 'SSL', 'EXPIRED'),
('CDN证书', 'cdn.example.com', 'Cloudflare', '2024-03-15 00:00:00', '2025-03-15 00:00:00', 'SSL', 'NORMAL'),
('邮件服务器证书', 'mail.example.com', 'DigiCert', '2024-02-01 00:00:00', '2025-02-01 00:00:00', 'SSL', 'NORMAL');

-- 插入示例监控日志数据
INSERT INTO `monitoring_log` (`certificate_id`, `log_type`, `message`, `days_until_expiry`) VALUES
(1, 'MONITORING', '证书状态正常', 365),
(2, 'MONITORING', '证书即将在30天内过期', 30),
(2, 'ALERT_EMAIL', '已发送证书过期提醒邮件', 30),
(3, 'MONITORING', '证书已过期', -120),
(3, 'ALERT_EMAIL', '已发送证书过期警告邮件', -120),
(4, 'MONITORING', '证书状态正常', 450),
(5, 'MONITORING', '证书状态正常', 390);