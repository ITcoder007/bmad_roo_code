#!/bin/bash

echo "启动后端服务..."

cd backend

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    echo "错误: Maven 未安装，请先安装 Maven"
    exit 1
fi

# 清理并编译项目
echo "编译项目..."
mvn clean compile

# 启动Spring Boot应用
echo "启动Spring Boot应用..."
mvn spring-boot:run

echo "后端服务已启动在 http://localhost:8080"