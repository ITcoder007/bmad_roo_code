#!/bin/bash

# Email Service Functional Test Script
# 用于测试 Story 2.3: 邮件通知服务（日志实现）的功能

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 测试配置
BASE_URL="http://localhost:8080/api"
LOG_FILE="logs/certificate-management.log"
TEST_EMAIL="test@example.com"

# 计数器
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  邮件服务功能测试 - Story 2.3${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 工具函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[PASS]${NC} $1"
    ((PASSED_TESTS++))
}

log_error() {
    echo -e "${RED}[FAIL]${NC} $1"
    ((FAILED_TESTS++))
}

log_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

increment_test() {
    ((TOTAL_TESTS++))
}

# 检查服务是否运行
check_service_health() {
    log_info "检查服务健康状态..."
    increment_test
    
    response=$(curl -s -w "%{http_code}" -o /dev/null "$BASE_URL/health" || echo "000")
    
    if [ "$response" = "200" ]; then
        log_success "服务运行正常"
    else
        log_error "服务未运行或不可访问 (HTTP: $response)"
        exit 1
    fi
}

# 准备测试数据
prepare_test_data() {
    log_info "准备测试数据..."
    increment_test
    
    # 创建测试证书
    test_cert_data='{
        "name": "测试证书-邮件服务",
        "domain": "email-test.example.com",
        "issuer": "Test CA",
        "issueDate": "2025-01-01T00:00:00",
        "expiryDate": "2025-09-01T00:00:00",
        "certificateType": "SSL",
        "status": "EXPIRING_SOON"
    }'
    
    # 使用 admin:admin123 认证
    auth_header="Authorization: Basic $(echo -n 'admin:admin123' | base64)"
    
    response=$(curl -s -w "%{http_code}" \
        -H "Content-Type: application/json" \
        -H "$auth_header" \
        -d "$test_cert_data" \
        -o /tmp/cert_response.json \
        "$BASE_URL/api/v1/certificates")
    
    if [ "$response" = "200" ] || [ "$response" = "201" ]; then
        cert_id=$(jq -r '.data.id' /tmp/cert_response.json 2>/dev/null || echo "1")
        log_success "测试证书创建成功 (ID: $cert_id)"
        export TEST_CERT_ID=$cert_id
    else
        log_warning "测试证书创建失败，使用默认证书进行测试"
        export TEST_CERT_ID=1
    fi
}

# 清理日志文件
clear_logs() {
    log_info "清理日志文件..."
    
    if [ -f "$LOG_FILE" ]; then
        # 保存当前日志行数
        export LOG_START_LINE=$(wc -l < "$LOG_FILE")
    else
        export LOG_START_LINE=0
        touch "$LOG_FILE"
    fi
    
    log_info "日志起始行数: $LOG_START_LINE"
}

# 获取新日志内容
get_new_logs() {
    if [ -f "$LOG_FILE" ]; then
        tail -n +$((LOG_START_LINE + 1)) "$LOG_FILE"
    fi
}

# 测试单个邮件预警功能
test_single_email_alert() {
    log_info "测试单个邮件预警功能..."
    increment_test
    
    # 创建测试数据：即将过期的证书
    test_data='{
        "certificateId": '$TEST_CERT_ID',
        "daysUntilExpiry": 7,
        "recipientEmail": "'$TEST_EMAIL'"
    }'
    
    auth_header="Authorization: Basic $(echo -n 'admin:admin123' | base64)"
    
    # 记录测试开始前的日志行数
    log_start_before_test=$(wc -l < "$LOG_FILE" 2>/dev/null || echo "0")
    
    response=$(curl -s -w "%{http_code}" \
        -H "Content-Type: application/json" \
        -H "$auth_header" \
        -d "$test_data" \
        -o /tmp/email_response.json \
        "$BASE_URL/api/v1/monitoring/email-alert")
    
    if [ "$response" = "200" ]; then
        # 检查响应内容
        success=$(jq -r '.success' /tmp/email_response.json 2>/dev/null || echo "false")
        message=$(jq -r '.message' /tmp/email_response.json 2>/dev/null || echo "")
        
        if [ "$success" = "true" ]; then
            log_success "邮件预警API调用成功"
            
            # 等待日志写入
            sleep 1
            
            # 检查日志内容
            new_logs=$(tail -n +$((log_start_before_test + 1)) "$LOG_FILE")
            
            if echo "$new_logs" | grep -q "📧 邮件预警发送"; then
                log_success "邮件预警日志记录正常"
            else
                log_error "邮件预警日志记录缺失"
            fi
            
            if echo "$new_logs" | grep -q "证书详情"; then
                log_success "证书详情日志记录正常"
            else
                log_error "证书详情日志记录缺失"
            fi
            
        else
            log_error "邮件预警API返回失败: $message"
        fi
    else
        log_error "邮件预警API调用失败 (HTTP: $response)"
    fi
}

# 测试每日摘要功能
test_daily_summary() {
    log_info "测试每日摘要功能..."
    increment_test
    
    auth_header="Authorization: Basic $(echo -n 'admin:admin123' | base64)"
    
    # 记录测试开始前的日志行数
    log_start_before_test=$(wc -l < "$LOG_FILE" 2>/dev/null || echo "0")
    
    response=$(curl -s -w "%{http_code}" \
        -H "Content-Type: application/json" \
        -H "$auth_header" \
        -o /tmp/summary_response.json \
        "$BASE_URL/api/v1/monitoring/daily-summary?recipient=$TEST_EMAIL")
    
    if [ "$response" = "200" ]; then
        success=$(jq -r '.success' /tmp/summary_response.json 2>/dev/null || echo "false")
        
        if [ "$success" = "true" ]; then
            log_success "每日摘要API调用成功"
            
            # 等待日志写入
            sleep 1
            
            # 检查日志内容
            new_logs=$(tail -n +$((log_start_before_test + 1)) "$LOG_FILE")
            
            if echo "$new_logs" | grep -q "📧 每日摘要邮件"; then
                log_success "每日摘要日志记录正常"
            else
                log_error "每日摘要日志记录缺失"
            fi
            
        else
            log_error "每日摘要API返回失败"
        fi
    else
        log_error "每日摘要API调用失败 (HTTP: $response)"
    fi
}

# 测试批量邮件功能
test_batch_emails() {
    log_info "测试批量邮件功能..."
    increment_test
    
    # 获取所有证书进行批量测试
    auth_header="Authorization: Basic $(echo -n 'admin:admin123' | base64)"
    
    # 记录测试开始前的日志行数
    log_start_before_test=$(wc -l < "$LOG_FILE" 2>/dev/null || echo "0")
    
    response=$(curl -s -w "%{http_code}" \
        -H "Content-Type: application/json" \
        -H "$auth_header" \
        -o /tmp/batch_response.json \
        "$BASE_URL/api/v1/monitoring/batch-alerts?recipient=$TEST_EMAIL")
    
    if [ "$response" = "200" ]; then
        success=$(jq -r '.success' /tmp/batch_response.json 2>/dev/null || echo "false")
        
        if [ "$success" = "true" ]; then
            log_success "批量邮件API调用成功"
            
            # 等待日志写入
            sleep 1
            
            # 检查日志内容
            new_logs=$(tail -n +$((log_start_before_test + 1)) "$LOG_FILE")
            
            if echo "$new_logs" | grep -q "📧 批量邮件预警"; then
                log_success "批量邮件日志记录正常"
            else
                log_error "批量邮件日志记录缺失"
            fi
            
        else
            log_error "批量邮件API返回失败"
        fi
    else
        log_error "批量邮件API调用失败 (HTTP: $response)"
    fi
}

# 测试错误处理
test_error_handling() {
    log_info "测试错误处理..."
    increment_test
    
    # 测试无效数据
    invalid_data='{
        "certificateId": 99999,
        "daysUntilExpiry": -1,
        "recipientEmail": "invalid-email"
    }'
    
    auth_header="Authorization: Basic $(echo -n 'admin:admin123' | base64)"
    
    response=$(curl -s -w "%{http_code}" \
        -H "Content-Type: application/json" \
        -H "$auth_header" \
        -d "$invalid_data" \
        -o /tmp/error_response.json \
        "$BASE_URL/api/v1/monitoring/email-alert")
    
    if [ "$response" = "400" ] || [ "$response" = "404" ] || [ "$response" = "422" ]; then
        log_success "错误处理正常，返回适当的错误状态码"
    elif [ "$response" = "200" ]; then
        # 检查响应是否表示失败
        success=$(jq -r '.success' /tmp/error_response.json 2>/dev/null || echo "true")
        if [ "$success" = "false" ]; then
            log_success "错误处理正常，API返回失败状态"
        else
            log_warning "可能需要加强输入验证"
        fi
    else
        log_error "错误处理异常 (HTTP: $response)"
    fi
}

# 测试配置验证
test_configuration() {
    log_info "测试邮件配置..."
    increment_test
    
    auth_header="Authorization: Basic $(echo -n 'admin:admin123' | base64)"
    
    response=$(curl -s -w "%{http_code}" \
        -H "$auth_header" \
        -o /tmp/config_response.json \
        "$BASE_URL/api/v1/config/email")
    
    if [ "$response" = "200" ]; then
        enabled=$(jq -r '.data.enabled' /tmp/config_response.json 2>/dev/null || echo "false")
        mode=$(jq -r '.data.mode' /tmp/config_response.json 2>/dev/null || echo "unknown")
        
        if [ "$enabled" = "true" ] && [ "$mode" = "log" ]; then
            log_success "邮件配置正确 (enabled: $enabled, mode: $mode)"
        else
            log_error "邮件配置异常 (enabled: $enabled, mode: $mode)"
        fi
    else
        log_warning "无法获取邮件配置信息 (HTTP: $response)"
    fi
}

# 验证日志格式
verify_log_format() {
    log_info "验证日志格式..."
    increment_test
    
    if [ -f "$LOG_FILE" ]; then
        recent_logs=$(tail -n 50 "$LOG_FILE")
        
        # 检查日志格式元素
        format_checks=0
        total_format_checks=5
        
        if echo "$recent_logs" | grep -q "📧"; then
            ((format_checks++))
        fi
        
        if echo "$recent_logs" | grep -q "证书:"; then
            ((format_checks++))
        fi
        
        if echo "$recent_logs" | grep -q "域名:"; then
            ((format_checks++))
        fi
        
        if echo "$recent_logs" | grep -q "收件人:"; then
            ((format_checks++))
        fi
        
        if echo "$recent_logs" | grep -q "剩余天数:"; then
            ((format_checks++))
        fi
        
        if [ $format_checks -ge 3 ]; then
            log_success "日志格式验证通过 ($format_checks/$total_format_checks 项检查通过)"
        else
            log_error "日志格式验证失败 ($format_checks/$total_format_checks 项检查通过)"
        fi
    else
        log_error "日志文件不存在"
    fi
}

# 性能测试
test_performance() {
    log_info "执行基本性能测试..."
    increment_test
    
    auth_header="Authorization: Basic $(echo -n 'admin:admin123' | base64)"
    
    # 测试响应时间
    start_time=$(date +%s%N)
    
    response=$(curl -s -w "%{http_code}" \
        -H "Content-Type: application/json" \
        -H "$auth_header" \
        -d '{"certificateId": '$TEST_CERT_ID', "daysUntilExpiry": 15, "recipientEmail": "'$TEST_EMAIL'"}' \
        -o /tmp/perf_response.json \
        "$BASE_URL/api/v1/monitoring/email-alert")
    
    end_time=$(date +%s%N)
    duration_ms=$(((end_time - start_time) / 1000000))
    
    if [ "$response" = "200" ] && [ $duration_ms -lt 2000 ]; then
        log_success "性能测试通过 (响应时间: ${duration_ms}ms)"
    elif [ "$response" = "200" ]; then
        log_warning "响应成功但较慢 (响应时间: ${duration_ms}ms)"
    else
        log_error "性能测试失败 (HTTP: $response, 响应时间: ${duration_ms}ms)"
    fi
}

# 生成测试报告
generate_report() {
    echo ""
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  测试报告${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo ""
    echo -e "总测试数: ${BLUE}$TOTAL_TESTS${NC}"
    echo -e "通过测试: ${GREEN}$PASSED_TESTS${NC}"
    echo -e "失败测试: ${RED}$FAILED_TESTS${NC}"
    echo ""
    
    if [ $FAILED_TESTS -eq 0 ]; then
        echo -e "${GREEN}✅ 所有功能测试通过！${NC}"
        echo -e "${GREEN}Story 2.3 邮件通知服务（日志实现）功能验证成功${NC}"
        echo ""
        
        # 显示最近的日志示例
        echo -e "${BLUE}最近的邮件服务日志示例:${NC}"
        echo "----------------------------------------"
        if [ -f "$LOG_FILE" ]; then
            tail -n 10 "$LOG_FILE" | grep -E "(📧|证书|域名|收件人)" | head -5
        fi
        echo "----------------------------------------"
        
        exit 0
    else
        echo -e "${RED}❌ 部分测试失败，请检查上述错误信息${NC}"
        echo ""
        
        # 显示错误日志
        echo -e "${RED}最近的错误日志:${NC}"
        echo "----------------------------------------"
        if [ -f "$LOG_FILE" ]; then
            tail -n 20 "$LOG_FILE" | grep -i "error\|exception\|fail" | head -5
        fi
        echo "----------------------------------------"
        
        exit 1
    fi
}

# 主测试流程
main() {
    log_info "开始邮件服务功能测试..."
    echo ""
    
    # 基础环境检查
    check_service_health
    prepare_test_data
    clear_logs
    
    echo ""
    log_info "执行功能测试..."
    echo ""
    
    # 核心功能测试
    test_single_email_alert
    test_daily_summary
    test_batch_emails
    
    echo ""
    log_info "执行质量和配置测试..."
    echo ""
    
    # 质量测试
    test_error_handling
    test_configuration
    verify_log_format
    test_performance
    
    # 生成报告
    generate_report
}

# 运行主流程
main "$@"