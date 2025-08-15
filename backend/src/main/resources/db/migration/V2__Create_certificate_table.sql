CREATE TABLE IF NOT EXISTS `certificate` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '证书名称',
  `domain` varchar(255) NOT NULL COMMENT '证书关联的域名',
  `issuer` varchar(100) NOT NULL COMMENT '证书颁发机构',
  `issue_date` datetime NOT NULL COMMENT '证书颁发日期',
  `expiry_date` datetime NOT NULL COMMENT '证书到期日期',
  `certificate_type` varchar(50) NOT NULL COMMENT '证书类型',
  `status` varchar(20) NOT NULL DEFAULT 'NORMAL' COMMENT '证书状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_domain` (`domain`),
  KEY `idx_status` (`status`),
  KEY `idx_expiry_date` (`expiry_date`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='证书信息表';