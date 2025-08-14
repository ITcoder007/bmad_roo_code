#!/bin/bash

echo "初始化数据库..."

# 默认数据库配置
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-3306}
DB_USER=${DB_USER:-root}
DB_PASS=${DB_PASS:-root123}

# 检查MySQL是否安装
if ! command -v mysql &> /dev/null; then
    echo "错误: MySQL 客户端未安装，请先安装 MySQL"
    exit 1
fi

# 执行SQL脚本
echo "执行数据库初始化脚本..."
mysql -h${DB_HOST} -P${DB_PORT} -u${DB_USER} -p${DB_PASS} < backend/src/main/resources/db/migration/V1__init_database.sql

if [ $? -eq 0 ]; then
    echo "数据库初始化成功！"
else
    echo "数据库初始化失败，请检查数据库连接配置"
    exit 1
fi