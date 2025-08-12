# 证书生命周期管理系统

企业级证书生命周期管理平台，提供证书的全生命周期管理功能，包括证书创建、监控、更新、到期提醒等功能。

## 技术栈

### 后端技术
- **Java 8** - 编程语言
- **Spring Boot 2.7.x** - 后端框架
- **MyBatis Plus 3.5.x** - ORM框架
- **MySQL 8.0** - 数据库
- **Spring Security + JWT** - 认证授权
- **Maven 3.8.x** - 项目构建工具

### 前端技术
- **Vue.js 3.x** - 前端框架
- **Vue Router 4.x** - 路由管理
- **Pinia 2.x** - 状态管理
- **Element Plus 2.x** - UI组件库
- **Vite 4.x** - 构建工具
- **JavaScript ES2020+** - 编程语言

## 项目结构

```
certificate-lifecycle-management/
├── frontend/           # Vue.js前端应用
├── backend/            # Spring Boot后端应用
├── scripts/            # 构建和部署脚本
├── infrastructure/     # 基础设施配置
├── docs/              # 项目文档
└── package.json       # 项目根配置
```

## 环境要求

- **Node.js** >= 16.x
- **npm** >= 8.x
- **Java** 8
- **Maven** >= 3.6.x
- **MySQL** 8.0

## 快速开始

### 1. 克隆项目
```bash
git clone <repository-url>
cd certificate-lifecycle-management
```

### 2. 安装依赖
```bash
# 安装根目录依赖（用于并行开发）
npm install

# 安装前端依赖
cd frontend
npm install
cd ..
```

### 3. 配置数据库
- 创建MySQL数据库：`certificate_management`
- 修改 `backend/src/main/resources/application-dev.yml` 中的数据库配置

### 4. 启动开发服务器

#### 方式一：使用npm脚本（推荐）
```bash
# 在项目根目录执行，同时启动前后端
npm run dev
```

#### 方式二：使用shell脚本
```bash
# 使用开发脚本启动
./scripts/dev.sh
```

#### 方式三：分别启动
```bash
# 启动后端
cd backend
mvn spring-boot:run

# 启动前端（新终端）
cd frontend
npm run dev
```

### 5. 访问应用
- 前端应用：http://localhost:3000
- 后端API：http://localhost:8080/api
- API健康检查：http://localhost:8080/api/actuator/health

## 构建项目

### 使用npm脚本
```bash
npm run build
```

### 使用shell脚本
```bash
./scripts/build.sh
```

构建产物：
- 后端JAR包：`backend/target/*.jar`
- 前端静态文件：`frontend/dist/`

## 部署

### 创建部署包
```bash
./scripts/deploy.sh [staging|production]
```

### 部署步骤
1. 将生成的tar.gz包上传到服务器
2. 解压部署包
3. 配置环境变量
4. 启动应用

详细部署说明请参考 [部署文档](docs/deployment.md)

## 开发指南

- [前端开发指南](frontend/README.md)
- [后端开发指南](backend/README.md)
- [API文档](docs/api.md)
- [架构设计](docs/architecture.md)

## 环境配置

### 后端环境配置文件
- `application.yml` - 基础配置
- `application-dev.yml` - 开发环境
- `application-test.yml` - 测试环境
- `application-prod.yml` - 生产环境

### 前端环境配置文件
- `.env` - 基础环境变量
- `.env.development` - 开发环境
- `.env.production` - 生产环境
- `.env.staging` - 预发布环境

## 常用命令

```bash
# 开发
npm run dev              # 同时启动前后端
npm run dev:web         # 仅启动前端
npm run dev:api         # 仅启动后端

# 构建
npm run build           # 构建前后端
npm run build:web       # 仅构建前端
npm run build:api       # 仅构建后端

# 测试
npm run test            # 运行所有测试
npm run test:web        # 前端测试
npm run test:api        # 后端测试
```

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目为私有项目，未经授权不得使用。

## 联系方式

项目维护团队：Certificate Management Team

---

*最后更新：2025-08-12*