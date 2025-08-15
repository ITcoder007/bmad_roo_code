# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

证书生命周期管理系统 - 一个企业级证书监控和管理解决方案，旨在通过自动化减少证书管理人工成本，提高业务连续性。该系统采用 Monorepo 结构，包含 Vue.js 前端和 Spring Boot 后端。

## 快速命令参考

### 初始化设置
```bash
./scripts/setup.sh          # 一键安装所有依赖和环境设置
```

### 开发启动
```bash
npm run dev                 # 同时启动前后端开发服务器
npm run dev:frontend        # 仅启动前端 (http://localhost:5173)
npm run dev:backend         # 仅启动后端 (http://localhost:8080/api)
./scripts/test-startup.sh   # 测试服务启动并验证健康状态
```

### 构建命令
```bash
npm run build              # 构建前后端生产版本
npm run build:frontend     # 仅构建前端 (输出到 frontend/dist)
npm run build:backend      # 仅构建后端 (输出 JAR 到 backend/target)
./scripts/build.sh         # 使用脚本构建完整项目
```

### 测试命令
```bash
npm run test               # 运行所有测试
npm run test:frontend      # 运行前端测试
npm run test:backend       # 运行后端测试

# 运行单个后端测试
cd backend && mvn test -Dtest=CertificateServiceTest
cd backend && mvn test -Dtest=CertificateServiceTest#testMethodName
```

### 代码质量
```bash
npm run lint               # 运行前端代码检查
npm run format             # 格式化前端代码
cd backend && mvn checkstyle:check  # 后端代码风格检查
```

### 数据库
```bash
# 创建数据库
mysql -u root -proot -e "CREATE DATABASE cc_bmad_opus_certificate_management DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_general_ci;"

# 运行数据库迁移脚本
mysql -u root -proot cc_bmad_opus_certificate_management < backend/src/main/resources/db/migration/V1__init.sql
```

## 技术栈

### 前端
- **Vue.js 3.x** - 响应式用户界面框架
- **Vue Router 4.x** - 前端路由管理
- **Pinia 2.x** - 状态管理
- **Element Plus 2.x** - UI 组件库
- **Vite 4.x** - 构建工具
- **JavaScript ES2020+** - 开发语言

### 后端
- **Java 8** (JDK 8) - 后端开发语言
- **Spring Boot 2.7.x** - 后端框架
- **MyBatis Plus 3.5.x** - ORM 框架
- **MySQL 8.0** - 数据库
- **Spring Security + JWT** - 认证授权
- **Maven 3.8.x** - 项目构建工具
- **Logback 1.3.x** - 日志框架

## 项目结构

```
certificate-lifecycle-management/
├── frontend/          # Vue.js 前端应用
│   ├── src/
│   │   ├── api/       # API 接口层
│   │   ├── components/ # 可复用组件
│   │   ├── views/     # 页面视图
│   │   ├── stores/    # Pinia 状态管理
│   │   ├── router/    # 路由配置
│   │   └── utils/     # 工具函数
│   └── vite.config.js
├── backend/           # Spring Boot 后端应用
│   ├── src/main/java/com/example/certificate/
│   │   ├── controller/ # REST API 控制器
│   │   ├── service/    # 业务逻辑层
│   │   ├── domain/     # 领域模型
│   │   ├── repository/ # 数据访问层
│   │   └── config/     # 配置类
│   └── pom.xml
├── scripts/           # 运行和部署脚本
└── docs/             # 项目文档
```

## 开发命令

### 前端开发
```bash
cd frontend
npm install         # 安装依赖
npm run dev        # 启动开发服务器
npm run build      # 构建生产版本
npm run test       # 运行测试
npm run lint       # 代码检查
```

### 后端开发
```bash
cd backend
mvn clean install   # 构建项目
mvn spring-boot:run # 运行应用
mvn test           # 运行测试
mvn package        # 打包成 JAR
```

### 数据库
```bash
# MySQL 8.0 需要预先安装并配置
# 数据库名: certificate_db
# 默认端口: 3306
```

## 核心功能模块

### 证书管理
- **CRUD 操作**: 证书信息的增删改查
- **状态计算**: 自动计算证书状态（正常/即将过期/已过期）
- **批量操作**: 支持批量导入和管理证书

### 监控预警
- **定时检查**: 每小时自动检查证书状态
- **预警规则**: 30天、15天、7天、1天预警
- **通知服务**: 邮件和短信通知（MVP阶段为日志记录）

### 用户界面
- **仪表板**: 证书状态总览和分布
- **列表管理**: 搜索、筛选、排序功能
- **详情查看**: 完整证书信息展示

## 架构设计原则

### DDD 领域驱动设计
- **领域层**: 核心业务逻辑和领域模型
- **应用层**: 业务用例和流程编排
- **基础设施层**: 技术实现和外部集成
- **表现层**: API 接口和用户界面

### RESTful API 设计
- 统一的资源路径规范
- 标准 HTTP 方法使用
- 统一的响应格式和错误处理

### 前后端分离
- 通过 REST API 通信
- 前端独立部署
- JWT 无状态认证

## 测试策略

### 单元测试
- 前端: Jest + Vue Test Utils
- 后端: JUnit 5 + Mockito
- 覆盖率目标: 核心业务逻辑 70%+

### 集成测试
- API 测试: Spring Test
- 数据库测试: MyBatis Test

### E2E 测试
- Selenium WebDriver
- 关键用户流程覆盖

## 开发规范

### 代码组织
- 单文件不超过 300 行（动态语言）
- 单文件不超过 400 行（静态语言）
- 每层文件夹不超过 8 个文件

### 命名规范
- 前端: camelCase (变量/函数), PascalCase (组件)
- 后端: camelCase (变量/方法), PascalCase (类)
- 数据库: snake_case

### Git 工作流
- feature 分支开发
- main 分支为稳定版本
- 提交信息清晰描述变更

## 部署说明

### 开发环境
- 前端: Vite 开发服务器 (默认 5173 端口)
- 后端: Spring Boot 内嵌 Tomcat (默认 8080 端口)

### 生产环境
- 前端: 静态文件部署到 Nginx
- 后端: JAR 包部署或 Docker 容器
- 数据库: MySQL 主从复制配置

## 性能要求

- 证书状态检查响应时间 < 2 秒
- 系统启动时间 < 30 秒
- 支持 100+ 证书并发管理
- 系统可用性 > 99%

## 安全要求

- HTTPS/TLS 加密通信
- JWT Token 认证
- Spring Security 权限控制
- 敏感数据加密存储
- 完整的审计日志

## 监控和日志

- Spring Boot Actuator 健康检查
- Logback 日志记录
- 统一的日志格式和级别
- 错误追踪和告警机制

## 高层架构设计

### 领域驱动设计 (DDD) 分层
项目采用 DDD 架构，清晰分离关注点：

1. **表现层 (Controller)** - `backend/src/main/java/com/example/certificate/controller/`
   - REST API 端点定义
   - 请求参数验证
   - 响应格式统一封装

2. **应用服务层 (Service)** - `backend/src/main/java/com/example/certificate/service/`
   - 业务流程编排
   - 事务管理
   - DTO 转换

3. **领域层 (Domain)** - `backend/src/main/java/com/example/certificate/domain/`
   - 核心业务逻辑
   - 领域模型和实体
   - 领域服务和仓库接口

4. **基础设施层 (Infrastructure)** - `backend/src/main/java/com/example/certificate/infrastructure/`
   - 数据持久化实现
   - 外部服务集成
   - 技术实现细节

### 关键设计模式

1. **统一响应格式** - 所有 API 返回 `ApiResponse<T>` 包装
2. **全局异常处理** - `GlobalExceptionHandler` 集中处理所有异常
3. **仓库模式** - Repository 接口与实现分离
4. **DTO 模式** - 使用 DTO 隔离领域模型与外部表示
5. **转换器模式** - `ServiceConverter` 负责对象转换

### 核心业务流程

1. **证书状态计算流程**
   - 领域模型 `Certificate` 包含 `calculateStatus()` 方法
   - `CertificateStatusService` 提供批量计算和定时更新
   - 状态规则：30天内即将过期，0天内已过期

2. **定时监控流程**
   - `CertificateStatusScheduler` 每天凌晨自动更新所有证书状态
   - 支持手动触发监控
   - 监控结果记录到 `MonitoringLog`

3. **预警通知流程**
   - MVP 阶段仅记录日志，不实际发送通知
   - 预警规则可配置（30天、15天、7天、1天）

### BMad 开发流程
项目使用 BMad (Business Model Agile Development) 方法论：

1. **故事文档** - `docs/stories/` 包含详细的用户故事和实现任务
2. **架构文档** - `docs/architecture/` 包含系统架构规范
3. **PRD 文档** - `docs/prd/` 包含产品需求和 Epic 定义
4. **配置文件** - `.bmad-core/core-config.yaml` 定义项目配置

开发新功能时，先查看对应的故事文档获取完整上下文。

## 注意事项

1. **环境配置**: 开发前确保配置好 JDK 8、Node.js 16+、MySQL 8.0
2. **依赖管理**: 使用 Maven 管理后端依赖，npm 管理前端依赖
3. **跨域配置**: 开发环境前端已配置代理到后端 `/api` 路径
4. **数据库迁移**: 使用 SQL 脚本管理数据库版本（位于 `backend/src/main/resources/db/migration/`）
5. **代码质量**: 提交前运行 lint 和测试，确保代码质量
6. **Java 版本**: 必须使用 JDK 8，代码需要兼容 Java 8 特性（避免使用 Java 9+ 的 API）