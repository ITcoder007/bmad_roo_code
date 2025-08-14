#!/bin/bash

echo "============================================"
echo "启动证书管理系统前端服务"
echo "============================================"

# 切换到前端目录
cd ../frontend

# 检查Node.js是否安装
if ! command -v node &> /dev/null; then
    echo "错误: Node.js 未安装，请先安装 Node.js"
    exit 1
fi

# 检查npm是否安装
if ! command -v npm &> /dev/null; then
    echo "错误: npm 未安装，请先安装 npm"
    exit 1
fi

# 显示Node和npm版本
echo "Node.js 版本: $(node -v)"
echo "npm 版本: $(npm -v)"

# 安装依赖
if [ ! -d "node_modules" ]; then
    echo "检测到未安装依赖，正在安装..."
    npm install
    
    if [ $? -eq 0 ]; then
        echo "✓ 依赖安装成功"
    else
        echo "✗ 依赖安装失败"
        exit 1
    fi
else
    echo "✓ 依赖已安装"
fi

# 启动开发服务器
echo "============================================"
echo "启动Vite开发服务器..."
echo "============================================"
npm run dev

# 这行代码通常不会执行到，因为上面的命令会一直运行
echo "前端服务已启动在 http://localhost:3000"