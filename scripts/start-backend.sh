#!/bin/bash

echo "============================================"
echo "启动证书管理系统后端服务"
echo "============================================"

# 切换到后端目录
cd ../backend

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    echo "错误: Maven 未安装，请先安装 Maven"
    exit 1
fi

# 检查JDK版本
echo "检查JDK版本..."
java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f 2 | cut -d'.' -f 1-2)
echo "当前JDK版本: $java_version"

# 清理并编译项目
echo "清理并编译项目..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "✗ 编译失败，请检查代码"
    exit 1
fi

echo "✓ 编译成功"

# 启动Spring Boot应用
echo "============================================"
echo "启动Spring Boot应用..."
echo "============================================"
mvn spring-boot:run

# 这行代码通常不会执行到，因为上面的命令会一直运行
echo "后端服务已启动在 http://localhost:8080"