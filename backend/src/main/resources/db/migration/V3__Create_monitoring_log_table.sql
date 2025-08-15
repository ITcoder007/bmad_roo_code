CREATE TABLE IF NOT EXISTS `monitoring_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `certificate_id` bigint NOT NULL COMMENT '关联的证书ID',
  `log_type` varchar(20) NOT NULL COMMENT '日志类型',
  `log_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '日志记录时间',
  `message` varchar(500) NOT NULL COMMENT '日志消息内容',
  `days_until_expiry` int DEFAULT NULL COMMENT '距离到期的天数',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_certificate_id` (`certificate_id`),
  KEY `idx_log_type` (`log_type`),
  KEY `idx_log_time` (`log_time`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_monitoring_log_certificate` FOREIGN KEY (`certificate_id`) REFERENCES `certificate` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='监控日志表';