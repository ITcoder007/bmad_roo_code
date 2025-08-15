-- H2 Database Schema for Testing
-- 用于单元测试的数据库表结构

CREATE TABLE IF NOT EXISTS certificate (
  id bigint NOT NULL AUTO_INCREMENT,
  name varchar(100) NOT NULL,
  domain varchar(255) NOT NULL,
  issuer varchar(100) NOT NULL,
  issue_date datetime NOT NULL,
  expiry_date datetime NOT NULL,
  certificate_type varchar(50) NOT NULL,
  status varchar(20) NOT NULL DEFAULT 'NORMAL',
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT uk_domain UNIQUE (domain)
);

CREATE TABLE IF NOT EXISTS monitoring_log (
  id bigint NOT NULL AUTO_INCREMENT,
  certificate_id bigint NOT NULL,
  log_type varchar(20) NOT NULL,
  log_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  message varchar(500) NOT NULL,
  days_until_expiry int DEFAULT NULL,
  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_monitoring_log_certificate FOREIGN KEY (certificate_id) REFERENCES certificate (id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_status ON certificate(status);
CREATE INDEX IF NOT EXISTS idx_expiry_date ON certificate(expiry_date);
CREATE INDEX IF NOT EXISTS idx_created_at ON certificate(created_at);

CREATE INDEX IF NOT EXISTS idx_certificate_id ON monitoring_log(certificate_id);
CREATE INDEX IF NOT EXISTS idx_log_type ON monitoring_log(log_type);
CREATE INDEX IF NOT EXISTS idx_log_time ON monitoring_log(log_time);
CREATE INDEX IF NOT EXISTS idx_log_created_at ON monitoring_log(created_at);