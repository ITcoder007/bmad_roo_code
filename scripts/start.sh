#!/bin/bash

# Certificate Lifecycle Management System - 启动脚本
# 同时启动前端和后端开发服务器

set -e

echo "========================================="
echo "  证书生命周期管理系统 - 启动服务"
echo "========================================="

# 获取脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="$( cd "$SCRIPT_DIR/.." && pwd )"

cd "$PROJECT_DIR"

# 检查依赖是否安装
if [ ! -d "node_modules" ] || [ ! -d "frontend/node_modules" ]; then
    echo "⚠️  依赖未安装，正在运行安装脚本..."
    ./scripts/setup.sh
fi

# 检查 Java 环境
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未找到 Java 环境，请安装 JDK 8"
    exit 1
fi

# 检查 Maven 环境
if ! command -v mvn &> /dev/null; then
    echo "❌ 错误: 未找到 Maven，请安装 Maven 3.8+"
    exit 1
fi

echo ""
echo "📦 环境检查完成"
echo ""
echo "🚀 正在启动服务..."
echo ""
echo "前端服务: http://localhost:5173"
echo "后端服务: http://localhost:8080"
echo "API 文档: http://localhost:8080/swagger-ui.html"
echo ""
echo "按 Ctrl+C 停止所有服务"
echo "========================================="
echo ""

# 使用 npm 脚本启动服务
npm run dev