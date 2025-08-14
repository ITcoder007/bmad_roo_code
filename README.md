# 证书生命周期管理系统

## 项目概述

证书生命周期管理系统是一个全面的自动化解决方案，旨在彻底改变企业证书管理方式。该系统通过集成监控、预警、管理功能，为企业提供一站式证书管理平台。

## 技术栈

- **后端**: Spring Boot 2.7.18 + JDK 8 + MyBatis Plus 3.5.3.1 + MySQL 8.0
- **前端**: Vue 3.4 + Vite 4.5 + Element Plus 2.4 + Pinia 2.1

## 项目结构

```
certificate-management-system/
├── backend/                 # Spring Boot 后端应用
│   ├── src/main/java/      # Java 源代码
│   ├── src/main/resources/ # 配置文件
│   └── src/test/java/      # 测试代码
├── frontend/               # Vue.js 前端应用
│   ├── src/               # Vue 源代码
│   ├── public/            # 静态资源
│   └── tests/             # 测试代码
├── scripts/               # 构建和部署脚本
└── docs/                  # 项目文档
```

## 快速开始

### 环境要求

- JDK 8+
- Node.js 16+
- MySQL 8.0+
- Maven 3.8+

### 数据库初始化

1. 创建数据库
```bash
mysql -u root -p < backend/src/main/resources/db/migration/V1__init_database.sql
```

### 后端启动

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 http://localhost:3000 启动

## 功能特性

### MVP 功能
- ✅ 证书信息管理（增删改查）
- ✅ 证书状态自动监控（每小时检查）
- ✅ 预警通知（日志记录形式）
- ✅ 统一管理界面

### 核心功能
1. **证书管理**
   - 添加、编辑、删除证书
   - 查看证书详细信息
   - 证书状态实时计算

2. **监控预警**
   - 每小时自动检查证书状态
   - 30/15/7/1天预警提醒
   - 预警日志记录

3. **用户界面**
   - 证书列表展示
   - 搜索和筛选功能
   - 状态可视化展示
   - 响应式设计

## API 接口

| 方法 | 路径 | 功能 |
|------|------|------|
| GET | /api/certificates | 获取证书列表 |
| GET | /api/certificates/{id} | 获取证书详情 |
| POST | /api/certificates | 创建证书 |
| PUT | /api/certificates/{id} | 更新证书 |
| DELETE | /api/certificates/{id} | 删除证书 |
| GET | /api/monitoring-logs | 获取监控日志 |
| GET | /api/system/status | 获取系统状态 |

## 开发团队

- 产品经理: John
- 架构师: Winston
- 开发团队: Development Team

## License

MIT