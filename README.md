# 证书生命周期管理系统

一个企业级证书监控和管理解决方案，旨在通过自动化减少证书管理人工成本，提高业务连续性。

## 项目概述

证书生命周期管理系统提供了完整的证书管理功能，包括：
- 证书信息的增删改查
- 自动状态监控和预警
- 可视化仪表板
- 批量管理功能

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

## 环境要求

- **Node.js**: >= 16.0.0
- **npm**: >= 7.0.0
- **Java**: JDK 8
- **Maven**: >= 3.8.0
- **MySQL**: 8.0

## 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd certificate-lifecycle-management
```

### 2. 环境设置

运行设置脚本自动安装依赖：

```bash
chmod +x scripts/*.sh
./scripts/setup.sh
```

或手动安装：

```bash
# 安装根目录依赖
npm install

# 安装前端依赖
cd frontend
npm install

# 安装后端依赖
cd ../backend
mvn dependency:resolve
```

### 3. 数据库配置

创建数据库：

```sql
CREATE DATABASE cc_bmad_opus_certificate_management 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_general_ci;
```

数据库连接配置（开发环境）：
- 主机：localhost
- 端口：3306
- 用户名：root
- 密码：root
- 数据库：cc_bmad_opus_certificate_management

### 4. 启动开发服务器

同时启动前后端：

```bash
npm run dev
```

或分别启动：

```bash
# 启动前端 (http://localhost:5173)
npm run dev:frontend

# 启动后端 (http://localhost:8080)
npm run dev:backend
```

### 5. 访问应用

- 前端应用：http://localhost:5173
- 后端 API：http://localhost:8080/api
- Swagger 文档：http://localhost:8080/api/swagger-ui.html
- 健康检查：http://localhost:8080/api/actuator/health

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
│   │   ├── infrastructure/ # 基础设施层
│   │   └── config/     # 配置类
│   └── pom.xml
├── scripts/           # 构建和部署脚本
├── docs/             # 项目文档
└── package.json      # Monorepo 配置
```

## 构建项目

构建生产版本：

```bash
npm run build
```

或使用构建脚本：

```bash
./scripts/build.sh
```

构建产物：
- 前端：`frontend/dist/`
- 后端：`backend/target/certificate-lifecycle-management-backend-1.0.0.jar`

## 测试

运行所有测试：

```bash
npm test
```

分别运行：

```bash
# 前端测试
npm run test:frontend

# 后端测试
npm run test:backend
```

## 开发规范

### 代码风格
- 前端使用 ESLint 和 Prettier 进行代码检查和格式化
- 后端遵循 Java 标准编码规范
- 提交前运行 `npm run lint` 检查代码

### Git 工作流
- 使用 feature 分支进行功能开发
- 提交信息清晰描述变更内容
- 合并前进行代码审查

### 命名规范
- 前端：camelCase (变量/函数), PascalCase (组件)
- 后端：camelCase (变量/方法), PascalCase (类)
- 数据库：snake_case
- API 路由：kebab-case

## 部署

### 开发环境
使用内置开发服务器，支持热重载。

### 生产环境
1. 前端：将 `dist` 目录部署到 Nginx
2. 后端：运行 JAR 包或使用 Docker 容器
3. 数据库：配置 MySQL 主从复制

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

MIT License

## 联系方式

项目维护者：Your Organization

---

更多详细文档请查看 `docs/` 目录。