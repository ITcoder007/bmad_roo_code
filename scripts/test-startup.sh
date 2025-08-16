#!/bin/bash

# 测试前后端服务启动状态

set -e

echo "========================================="
echo "  测试服务启动状态"
echo "========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 5

# 测试前端服务
echo ""
echo "📱 测试前端服务 (http://localhost:5173)..."
if curl -s -o /dev/null -w "%{http_code}" http://localhost:5173 | grep -q "200\|304"; then
    echo -e "${GREEN}✅ 前端服务运行正常${NC}"
else
    echo -e "${RED}❌ 前端服务未响应${NC}"
    exit 1
fi

# 测试后端健康检查
echo ""
echo "🔧 测试后端健康检查 (http://localhost:8080/api/actuator/health)..."
HEALTH_RESPONSE=$(curl -s http://localhost:8080/api/actuator/health 2>/dev/null || echo "{}")
if echo "$HEALTH_RESPONSE" | grep -q "UP"; then
    echo -e "${GREEN}✅ 后端服务运行正常${NC}"
    echo "   状态: $(echo $HEALTH_RESPONSE | grep -o '"status":"[^"]*"' | cut -d'"' -f4)"
else
    echo -e "${RED}❌ 后端服务未响应或状态异常${NC}"
    echo "   响应: $HEALTH_RESPONSE"
    exit 1
fi

# 测试后端API端点
echo ""
echo "🔍 测试后端API端点 (http://localhost:8080/api/api/v1/certificates)..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/api/v1/certificates 2>/dev/null)

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
    echo -e "${GREEN}✅ API端点可访问${NC}"
    echo "   HTTP状态码: $HTTP_CODE"
    if [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
        echo -e "   ${YELLOW}⚠️  需要认证才能访问（这是正常的）${NC}"
    fi
else
    echo -e "${RED}❌ API端点不可访问${NC}"
    echo "   HTTP状态码: $HTTP_CODE"
fi

# 测试前后端通信（通过代理）
echo ""
echo "🔗 测试前后端代理配置 (http://localhost:5173/api/actuator/health)..."
PROXY_RESPONSE=$(curl -s http://localhost:5173/api/actuator/health 2>/dev/null || echo "{}")
if echo "$PROXY_RESPONSE" | grep -q "UP"; then
    echo -e "${GREEN}✅ 前后端代理配置正确${NC}"
else
    echo -e "${YELLOW}⚠️  前后端代理可能未配置${NC}"
    echo "   这在开发环境中是正常的，生产环境需要配置"
fi

echo ""
echo "========================================="
echo -e "${GREEN}🎉 所有服务测试完成！${NC}"
echo "========================================="
echo ""
echo "📋 服务访问地址："
echo "   前端: http://localhost:5173"
echo "   后端API: http://localhost:8080/api"
echo "   健康检查: http://localhost:8080/api/actuator/health"
echo ""
echo "💡 提示："
echo "   - 使用 Ctrl+C 停止所有服务"
echo "   - 查看日志: logs/ 目录"
echo "   - API文档: http://localhost:8080/swagger-ui.html"
echo ""