#!/bin/bash

echo "============================================"
echo "证书管理系统一键启动脚本"
echo "============================================"
echo ""

# 获取脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# 步骤1: 初始化数据库
echo "[1/3] 初始化数据库..."
echo "----------------------------------------"
cd "$SCRIPT_DIR"
./init-database.sh

if [ $? -ne 0 ]; then
    echo "✗ 数据库初始化失败，停止启动"
    exit 1
fi

echo ""
echo "[1/3] ✓ 数据库初始化完成"
echo ""

# 步骤2: 启动后端服务（在后台运行）
echo "[2/3] 启动后端服务..."
echo "----------------------------------------"
cd "$SCRIPT_DIR"
./start-backend.sh &
BACKEND_PID=$!
echo "后端服务进程PID: $BACKEND_PID"

# 等待后端服务启动
echo "等待后端服务启动..."
sleep 10

# 检查后端服务是否启动成功
curl -s http://localhost:8080/actuator/health > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "[2/3] ✓ 后端服务启动成功 (http://localhost:8080)"
else
    echo "[2/3] ⚠ 后端服务可能还在启动中..."
fi

echo ""

# 步骤3: 启动前端服务
echo "[3/3] 启动前端服务..."
echo "----------------------------------------"
cd "$SCRIPT_DIR"
./start-frontend.sh

# 通常不会执行到这里，因为前端服务会持续运行
echo ""
echo "============================================"
echo "所有服务已启动！"
echo "后端服务: http://localhost:8080"
echo "前端服务: http://localhost:3000"
echo "============================================"