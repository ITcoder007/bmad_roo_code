-- 创建数据库
CREATE DATABASE IF NOT EXISTS certificate_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE certificate_db;

-- 证书表
CREATE TABLE IF NOT EXISTS `certificate` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '证书名称',
    `domain` VARCHAR(255) NOT NULL COMMENT '域名',
    `issuer` VARCHAR(100) COMMENT '颁发机构',
    `issue_date` DATE COMMENT '颁发日期',
    `expiry_date` DATE NOT NULL COMMENT '到期日期',
    `certificate_type` VARCHAR(50) COMMENT '证书类型',
    `status` VARCHAR(20) COMMENT '状态: NORMAL/EXPIRING_SOON/EXPIRED',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_expiry_date (expiry_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证书信息表';

-- 监控日志表
CREATE TABLE IF NOT EXISTS `monitoring_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `certificate_id` BIGINT NOT NULL COMMENT '证书ID',
    `log_type` VARCHAR(50) COMMENT '日志类型: MONITORING/ALERT_EMAIL/ALERT_SMS',
    `log_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '日志时间',
    `message` TEXT COMMENT '日志消息',
    `days_until_expiry` INT COMMENT '距离过期天数',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (certificate_id) REFERENCES certificate(id) ON DELETE CASCADE,
    INDEX idx_certificate_id (certificate_id),
    INDEX idx_log_time (log_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监控日志表';

-- 插入测试数据
INSERT INTO `certificate` (`name`, `domain`, `issuer`, `issue_date`, `expiry_date`, `certificate_type`, `status`) VALUES
('主站SSL证书', 'www.example.com', 'Let\'s Encrypt', '2025-01-01', '2025-04-01', 'SSL/TLS', 'NORMAL'),
('API证书', 'api.example.com', 'DigiCert', '2024-12-01', '2025-09-01', 'SSL/TLS', 'EXPIRING_SOON'),
('测试环境证书', 'test.example.com', 'Let\'s Encrypt', '2024-11-01', '2025-02-01', 'SSL/TLS', 'EXPIRING_SOON'),
('邮件服务器证书', 'mail.example.com', 'GlobalSign', '2024-10-01', '2025-10-01', 'SSL/TLS', 'NORMAL'),
('过期证书示例', 'old.example.com', 'Comodo', '2024-01-01', '2025-01-01', 'SSL/TLS', 'EXPIRED');