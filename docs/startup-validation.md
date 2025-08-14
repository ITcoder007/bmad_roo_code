# 服务启动验证指南

## 概述

本文档说明如何验证证书生命周期管理系统的各个服务是否正常运行。

## 自动验证

运行启动验证脚本：

```bash
./scripts/test-startup.sh
```

该脚本会自动执行以下测试：
1. Spring Boot 后端启动测试
2. Vite 前端开发服务器启动测试
3. 健康检查端点测试
4. 前端到后端的代理测试

## 手动验证

### 1. 验证后端服务

启动后端：
```bash
cd backend
mvn spring-boot:run
```

验证步骤：
- 访问健康检查端点：http://localhost:8080/api/actuator/health
- 预期响应：`{"status":"UP"}`
- 访问自定义健康端点：http://localhost:8080/api/health
- 查看 Swagger 文档：http://localhost:8080/api/swagger-ui.html

### 2. 验证前端服务

启动前端：
```bash
cd frontend
npm run dev
```

验证步骤：
- 访问前端应用：http://localhost:5173
- 应该看到登录页面
- 检查浏览器控制台无错误

### 3. 验证数据库连接

运行数据库连接测试：
```bash
cd backend
mvn test -Dtest=DatabaseConnectionTest
```

或手动验证：
```sql
mysql -u root -p
USE cc_bmad_opus_certificate_management;
SHOW TABLES;
```

### 4. 验证前后端集成

同时启动前后端：
```bash
npm run dev
```

验证步骤：
1. 访问前端：http://localhost:5173
2. 尝试登录（使用任意用户名密码）
3. 检查网络请求是否正确代理到后端

## 常见问题排查

### 后端启动失败

1. **端口占用**
   ```bash
   lsof -i :8080
   ```
   解决：关闭占用端口的进程或修改配置中的端口

2. **数据库连接失败**
   - 检查 MySQL 是否运行
   - 验证数据库是否存在
   - 确认用户名密码正确

3. **依赖未安装**
   ```bash
   mvn dependency:resolve
   ```

### 前端启动失败

1. **端口占用**
   ```bash
   lsof -i :5173
   ```

2. **依赖未安装**
   ```bash
   npm install
   ```

3. **Node 版本不兼容**
   ```bash
   node -v  # 需要 >= 16.0.0
   ```

## 健康检查端点说明

| 端点 | 描述 | 预期响应 |
|------|------|----------|
| `/api/actuator/health` | Spring Boot Actuator 健康检查 | `{"status":"UP"}` |
| `/api/health` | 自定义健康检查 | 包含应用信息的 JSON |
| `/api/actuator/info` | 应用信息 | 应用版本等信息 |
| `/api/actuator/metrics` | 应用指标 | 各种运行时指标 |

## 日志位置

- 后端日志：`logs/certificate-management.log`
- 前端日志：浏览器控制台
- 错误日志：`logs/certificate-management-error.log`

## 验证清单

- [ ] 后端服务能成功启动
- [ ] 前端服务能成功启动
- [ ] 数据库连接正常
- [ ] 健康检查端点返回 UP 状态
- [ ] 前端能正确代理 API 请求到后端
- [ ] 日志文件正确生成
- [ ] Swagger 文档可访问
- [ ] 登录页面正常显示

## 性能基准

正常启动时间：
- 后端：10-15 秒
- 前端：3-5 秒
- 数据库连接：< 1 秒

如果启动时间超过以上基准，请检查：
1. 系统资源使用情况
2. 网络连接状态
3. 依赖下载速度