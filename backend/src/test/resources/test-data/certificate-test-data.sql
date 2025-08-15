-- 清空测试数据
DELETE FROM monitoring_log WHERE 1=1;
DELETE FROM certificate WHERE 1=1;

-- 插入测试证书数据
INSERT INTO certificate (id, name, domain, issuer, issue_date, expiry_date, certificate_type, status, created_at, updated_at)
VALUES
    (99991, '测试证书1', 'test1.example.com', 'Test CA', '2024-01-01 00:00:00', '2025-01-01 00:00:00', 'SSL', 'NORMAL', NOW(), NOW()),
    (99992, '测试证书2', 'test2.example.com', 'Test CA', '2024-01-01 00:00:00', '2024-09-01 00:00:00', 'SSL', 'EXPIRING_SOON', NOW(), NOW()),
    (99993, '测试证书3', 'test3.example.com', 'Test CA', '2023-01-01 00:00:00', '2024-01-01 00:00:00', 'SSL', 'EXPIRED', NOW(), NOW());