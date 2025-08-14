#!/bin/bash

echo "============================================"
echo "初始化证书管理系统数据库"
echo "============================================"

# 数据库配置
DB_HOST="localhost"
DB_PORT="3306"
DB_USER="root"
DB_PASS="root"
DB_NAME="cc_vibe_opus_certificate_management"

# 检查MySQL是否安装
if ! command -v mysql &> /dev/null; then
    echo "错误: MySQL 客户端未安装，请先安装 MySQL"
    exit 1
fi

# 创建数据库
echo "正在创建数据库 ${DB_NAME}..."
mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASS} <<EOF
CREATE DATABASE IF NOT EXISTS ${DB_NAME} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ${DB_NAME};
EOF

if [ $? -eq 0 ]; then
    echo "✓ 数据库创建成功"
else
    echo "✗ 数据库创建失败"
    exit 1
fi

# 执行初始化脚本
echo "正在执行数据库初始化脚本..."
mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASS} ${DB_NAME} < ../backend/src/main/resources/db/migration/V1__init_database.sql

if [ $? -eq 0 ]; then
    echo "✓ 数据库初始化成功"
    echo "============================================"
    echo "数据库初始化完成！"
    echo "数据库名: ${DB_NAME}"
    echo "已创建表: certificates, monitoring_logs"
    echo "已插入测试数据: 10条证书记录"
    echo "============================================"
else
    echo "✗ 数据库初始化失败"
    exit 1
fi