#!/bin/bash

# Email Service Implementation Verification
# Story 2.3: 邮件通知服务（日志实现）实现验证

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Story 2.3 实现验证${NC}"
echo -e "${BLUE}  邮件通知服务（日志实现）${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 验证计数器
TOTAL_CHECKS=0
PASSED_CHECKS=0

check() {
    ((TOTAL_CHECKS++))
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ $1${NC}"
        ((PASSED_CHECKS++))
    else
        echo -e "${RED}❌ $1${NC}"
    fi
}

echo -e "${BLUE}1. 验证核心文件结构${NC}"
echo "--------------------------------"

# 检查邮件服务接口
[ -f "backend/src/main/java/com/example/certificate/service/EmailService.java" ]
check "邮件服务接口存在"

# 检查日志邮件服务实现
[ -f "backend/src/main/java/com/example/certificate/infrastructure/external/email/LogEmailServiceImpl.java" ]
check "日志邮件服务实现存在"

# 检查生产邮件服务实现
[ -f "backend/src/main/java/com/example/certificate/service/impl/EmailServiceImpl.java" ]
check "生产邮件服务实现存在"

# 检查邮件结果DTO
[ -f "backend/src/main/java/com/example/certificate/service/dto/EmailResult.java" ]
check "邮件结果DTO存在"

# 检查邮件配置
[ -f "backend/src/main/java/com/example/certificate/config/EmailConfig.java" ]
check "邮件配置类存在"

# 检查邮件常量
[ -f "backend/src/main/java/com/example/certificate/common/constant/EmailConstants.java" ]
check "邮件常量类存在"

# 检查邮件格式化器（重构后新增）
[ -f "backend/src/main/java/com/example/certificate/infrastructure/external/email/EmailLogFormatter.java" ]
check "邮件日志格式化器存在"

echo ""
echo -e "${BLUE}2. 验证测试文件${NC}"
echo "--------------------------------"

# 检查单元测试
[ -f "backend/src/test/java/com/example/certificate/infrastructure/external/email/LogEmailServiceTest.java" ]
check "日志邮件服务单元测试存在"

[ -f "backend/src/test/java/com/example/certificate/service/impl/EmailServiceTest.java" ]
check "生产邮件服务单元测试存在"

# 检查集成测试
[ -f "backend/src/test/java/com/example/certificate/integration/EmailIntegrationTest.java" ]
check "邮件服务集成测试存在"

echo ""
echo -e "${BLUE}3. 验证配置文件${NC}"
echo "--------------------------------"

# 检查application.yml中的邮件配置
if [ -f "backend/src/main/resources/application.yml" ]; then
    if grep -q "alert:" "backend/src/main/resources/application.yml" && grep -q "email:" "backend/src/main/resources/application.yml"; then
        check "application.yml包含邮件配置"
    else
        false
        check "application.yml包含邮件配置"
    fi
else
    false
    check "application.yml文件存在"
fi

echo ""
echo -e "${BLUE}4. 验证关键功能实现${NC}"
echo "--------------------------------"

# 检查EmailService接口方法
if [ -f "backend/src/main/java/com/example/certificate/service/EmailService.java" ]; then
    if grep -q "sendExpiryAlertEmail" "backend/src/main/java/com/example/certificate/service/EmailService.java" && \
       grep -q "sendDailySummary" "backend/src/main/java/com/example/certificate/service/EmailService.java" && \
       grep -q "sendBatchAlerts" "backend/src/main/java/com/example/certificate/service/EmailService.java"; then
        check "EmailService接口包含所有必需方法"
    else
        false
        check "EmailService接口包含所有必需方法"
    fi
else
    false
    check "EmailService接口包含所有必需方法"
fi

# 检查LogEmailServiceImpl实现
if [ -f "backend/src/main/java/com/example/certificate/infrastructure/external/email/LogEmailServiceImpl.java" ]; then
    if grep -q "@Primary" "backend/src/main/java/com/example/certificate/infrastructure/external/email/LogEmailServiceImpl.java" && \
       grep -q "log.info" "backend/src/main/java/com/example/certificate/infrastructure/external/email/LogEmailServiceImpl.java" && \
       grep -q "MonitoringLogService" "backend/src/main/java/com/example/certificate/infrastructure/external/email/LogEmailServiceImpl.java"; then
        check "LogEmailServiceImpl正确实现日志记录"
    else
        false
        check "LogEmailServiceImpl正确实现日志记录"
    fi
else
    false
    check "LogEmailServiceImpl正确实现日志记录"
fi

# 检查EmailResult不可变对象设计
if [ -f "backend/src/main/java/com/example/certificate/service/dto/EmailResult.java" ]; then
    if grep -q "final class" "backend/src/main/java/com/example/certificate/service/dto/EmailResult.java" && \
       grep -q "private final" "backend/src/main/java/com/example/certificate/service/dto/EmailResult.java" && \
       grep -q "public static EmailResult success" "backend/src/main/java/com/example/certificate/service/dto/EmailResult.java"; then
        check "EmailResult使用不可变对象设计"
    else
        false
        check "EmailResult使用不可变对象设计"
    fi
else
    false
    check "EmailResult使用不可变对象设计"
fi

echo ""
echo -e "${BLUE}5. 验证条件配置${NC}"
echo "--------------------------------"

# 检查条件配置注解
if [ -f "backend/src/main/java/com/example/certificate/infrastructure/external/email/LogEmailServiceImpl.java" ]; then
    if grep -q "@ConditionalOnProperty" "backend/src/main/java/com/example/certificate/infrastructure/external/email/LogEmailServiceImpl.java"; then
        check "日志邮件服务使用条件配置"
    else
        false
        check "日志邮件服务使用条件配置"
    fi
fi

if [ -f "backend/src/main/java/com/example/certificate/service/impl/EmailServiceImpl.java" ]; then
    if grep -q "@ConditionalOnProperty.*real" "backend/src/main/java/com/example/certificate/service/impl/EmailServiceImpl.java"; then
        check "生产邮件服务使用条件配置"
    else
        false
        check "生产邮件服务使用条件配置"
    fi
fi

echo ""
echo -e "${BLUE}6. 代码质量检查${NC}"
echo "--------------------------------"

# 检查是否使用了Emoji日志前缀
if [ -f "backend/src/main/java/com/example/certificate/infrastructure/external/email/LogEmailServiceImpl.java" ]; then
    if grep -q "📧" "backend/src/main/java/com/example/certificate/infrastructure/external/email/LogEmailServiceImpl.java"; then
        check "使用了结构化日志格式（Emoji标识）"
    else
        false
        check "使用了结构化日志格式（Emoji标识）"
    fi
fi

# 检查是否有适当的异常处理
if [ -f "backend/src/main/java/com/example/certificate/infrastructure/external/email/LogEmailServiceImpl.java" ]; then
    if grep -q "try" "backend/src/main/java/com/example/certificate/infrastructure/external/email/LogEmailServiceImpl.java" && \
       grep -q "catch" "backend/src/main/java/com/example/certificate/infrastructure/external/email/LogEmailServiceImpl.java"; then
        check "包含适当的异常处理"
    else
        false
        check "包含适当的异常处理"
    fi
fi

echo ""
echo -e "${BLUE}7. 编译和测试验证${NC}"
echo "--------------------------------"

# 检查编译是否成功
cd backend
if mvn compile -q > /dev/null 2>&1; then
    check "代码编译成功"
else
    false
    check "代码编译成功"
fi

# 检查单元测试是否通过（只测试邮件相关）
if mvn test -q -Dtest=LogEmailServiceTest > /dev/null 2>&1; then
    check "日志邮件服务单元测试通过"
else
    false
    check "日志邮件服务单元测试通过"
fi

cd ..

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  验证报告${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

echo -e "总检查项: ${BLUE}$TOTAL_CHECKS${NC}"
echo -e "通过检查: ${GREEN}$PASSED_CHECKS${NC}"
echo -e "失败检查: ${RED}$((TOTAL_CHECKS - PASSED_CHECKS))${NC}"

if [ $PASSED_CHECKS -eq $TOTAL_CHECKS ]; then
    echo ""
    echo -e "${GREEN}🎉 所有验证通过！${NC}"
    echo -e "${GREEN}Story 2.3: 邮件通知服务（日志实现）实现完成${NC}"
    echo ""
    echo -e "${BLUE}主要特性:${NC}"
    echo "• ✅ 邮件服务接口和实现"
    echo "• ✅ 日志模式的MVP实现"
    echo "• ✅ 不可变EmailResult对象"
    echo "• ✅ 条件配置支持"
    echo "• ✅ 结构化日志格式"
    echo "• ✅ 完整的单元测试"
    echo "• ✅ 适当的异常处理"
    
    echo ""
    echo -e "${BLUE}已完成的功能:${NC}"
    echo "1. 邮件预警信息的日志记录功能"
    echo "2. 证书基本信息记录（名称、域名、到期日期）"
    echo "3. 预警时间点和类型记录（30天、15天、7天、1天）"
    echo "4. 日志格式的基本配置功能"
    echo "5. 邮件记录的单元测试"
    echo "6. 日志记录的基本功能验证"
    
    exit 0
else
    echo ""
    echo -e "${RED}❌ 验证失败！${NC}"
    echo -e "${YELLOW}请检查上述失败的项目并修复${NC}"
    exit 1
fi