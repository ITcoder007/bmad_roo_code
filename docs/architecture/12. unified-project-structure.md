# 统一项目结构

## 概述

证书生命周期管理系统采用 monorepo 结构，将前端和后端代码放在同一个代码仓库中管理。这种结构便于统一版本控制、依赖管理和 CI/CD 流程，特别适合中小型团队开发。

## 项目结构

```
certificate-lifecycle-management/
├── .github/                    # CI/CD 工作流
│   └── workflows/
│       ├── ci.yaml
│       └── deploy.yaml
├── apps/                       # 应用程序包
│   ├── web/                    # 前端应用程序
│   │   ├── src/
│   │   │   ├── components/     # UI 组件
│   │   │   ├── pages/          # 页面组件/路由
│   │   │   ├── hooks/          # 自定义 Vue hooks
│   │   │   ├── services/       # API 客户端服务
│   │   │   ├── stores/         # 状态管理
│   │   │   ├── styles/         # 全局样式/主题
│   │   │   └── utils/          # 前端工具
│   │   ├── public/             # 静态资源
│   │   ├── tests/              # 前端测试
│   │   └── package.json
│   └── api/                    # 后端应用程序
│       ├── src/
│       │   ├── main/           # Spring Boot 主程序
│       │   ├── controller/     # API 路由/控制器
│       │   ├── service/        # 业务逻辑
│       │   ├── domain/         # 领域模型
│       │   ├── infrastructure/ # 基础设施层
│       │   │   └── persistence/ # 数据访问层
│       │   └── config/         # 配置类
│       ├── tests/              # 后端测试
│       └── pom.xml
├── packages/                   # 共享包
│   ├── shared/                 # 共享类型/工具
│   │   ├── src/
│   │   │   ├── types/          # TypeScript 接口
│   │   │   ├── constants/      # 共享常量
│   │   │   └── utils/          # 共享工具
│   │   └── package.json
│   └── config/                 # 共享配置
│       ├── eslint/
│       ├── typescript/
│       └── jest/
├── infrastructure/             # IaC 定义
│   └── docker/                 # Docker 配置
│       ├── web/
│       │   └── Dockerfile
│       ├── api/
│       │   └── Dockerfile
│       └── docker-compose.yml
├── scripts/                    # 构建和部署脚本
│   ├── build.sh
│   ├── deploy.sh
│   └── setup.sh
├── docs/                       # 文档
│   ├── prd.md
│   ├── architecture/
│   │   ├── README.md
│   │   ├── high-level-architecture.md
│   │   ├── tech-stack.md
│   │   ├── data-models.md
│   │   ├── api-specs.md
│   │   ├── components.md
│   │   ├── external-apis.md
│   │   ├── core-workflows.md
│   │   ├── database-schema.md
│   │   ├── frontend-architecture.md
│   │   ├── backend-architecture.md
│   │   └── unified-project-structure.md
│   └── front-end-spec.md
├── .env.example                # 环境变量模板
├── package.json                # 根 package.json
├── pom.xml                     # 根 pom.xml（用于多模块管理）
└── README.md
```

## 目录结构说明

### apps 目录

**web/** - Vue.js 前端应用程序
- `src/components/`: 可复用的 UI 组件
- `src/pages/`: 页面级组件，对应路由
- `src/hooks/`: 自定义 Vue Composition API hooks
- `src/services/`: API 调用服务层
- `src/stores/`: Pinia 状态管理
- `src/styles/`: 全局样式和主题定义
- `src/utils/`: 前端工具函数

**api/** - Spring Boot 后端应用程序
- `src/main/`: Spring Boot 主程序入口
- `src/controller/`: REST API 控制器
- `src/service/`: 业务逻辑服务层
- `src/domain/`: 领域模型和实体
- `src/infrastructure/persistence/`: MyBatis Plus 数据访问层
- `src/config/`: Spring 配置类

### packages 目录

**shared/** - 前后端共享代码
- `src/types/`: TypeScript 类型定义，可用于前后端接口定义
- `src/constants/`: 共享常量定义
- `src/utils/`: 通用工具函数

**config/** - 共享配置
- `eslint/`: ESLint 配置
- `typescript/`: TypeScript 配置
- `jest/`: Jest 测试配置

### infrastructure 目录

包含基础设施即代码（IaC）定义，主要用于容器化部署：
- `docker/web/Dockerfile`: 前端应用容器化配置
- `docker/api/Dockerfile`: 后端应用容器化配置
- `docker/docker-compose.yml`: 本地开发环境配置

### scripts 目录

包含构建和部署脚本：
- `build.sh`: 构建脚本
- `deploy.sh`: 部署脚本
- `setup.sh`: 环境设置脚本

### docs 目录

项目文档，包括：
- 产品需求文档 (PRD)
- 架构设计文档（分片存储）
- 前端规格说明

## Monorepo 管理工具

项目使用以下工具进行 monorepo 管理：

### 根级别配置

**package.json** - 管理 JavaScript/TypeScript 相关依赖和脚本
```json
{
  "name": "certificate-lifecycle-management",
  "private": true,
  "workspaces": [
    "apps/*",
    "packages/*"
  ],
  "scripts": {
    "build": "npm run build:web && npm run build:api",
    "build:web": "cd apps/web && npm run build",
    "build:api": "cd apps/api && mvn clean package",
    "test": "npm run test:web && npm run test:api",
    "test:web": "cd apps/web && npm test",
    "test:api": "cd apps/api && mvn test",
    "dev": "concurrently \"npm run dev:web\" \"npm run dev:api\"",
    "dev:web": "cd apps/web && npm run dev",
    "dev:api": "cd apps/api && mvn spring-boot:run",
    "lint": "eslint . --ext .vue,.js,.jsx,.cjs,.mjs,.ts,.tsx,.cts,.mts --fix --ignore-path .gitignore",
    "format": "prettier --write src/"
  },
  "devDependencies": {
    "concurrently": "^7.6.0",
    "eslint": "^8.45.0",
    "prettier": "^3.0.0"
  }
}
```

**pom.xml** - 管理 Java 相关依赖和构建（多模块 Maven 配置）
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.example</groupId>
    <artifactId>certificate-lifecycle-management</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    
    <modules>
        <module>apps/api</module>
    </modules>
    
    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
</project>
```

## 共享代码管理

### 类型共享

前后端共享的数据类型定义在 `packages/shared/src/types/` 目录中：

```typescript
// packages/shared/src/types/certificate.ts
export interface Certificate {
  id: number;
  name: string;
  domain: string;
  issuer: string;
  expiryDate: Date;
  type: CertificateType;
  status: CertificateStatus;
  createdAt: Date;
  updatedAt: Date;
}

export enum CertificateType {
  SSL = 'SSL',
  TLS = 'TLS',
  CODE_SIGNING = 'CODE_SIGNING',
  OTHER = 'OTHER'
}

export enum CertificateStatus {
  VALID = 'VALID',
  EXPIRING_SOON = 'EXPIRING_SOON',
  EXPIRED = 'EXPIRED'
}
```

### 常量共享

```typescript
// packages/shared/src/constants/index.ts
export const API_ENDPOINTS = {
  CERTIFICATES: '/api/certificates',
  CERTIFICATE_BY_ID: (id: number) => `/api/certificates/${id}`,
  HEALTH_CHECK: '/api/health'
};

export const ALERT_THRESHOLDS = {
  DAYS_30: 30,
  DAYS_15: 15,
  DAYS_7: 7,
  DAYS_1: 1
};
```

## 开发工作流程

### 本地开发

1. **环境设置**
   ```bash
   # 克隆仓库
   git clone <repository-url>
   cd certificate-lifecycle-management
   
   # 安装依赖
   npm install
   
   # 设置后端依赖
   cd apps/api && mvn install
   ```

2. **启动开发服务器**
   ```bash
   # 同时启动前端和后端
   npm run dev
   
   # 或分别启动
   npm run dev:web  # 前端开发服务器
   npm run dev:api  # 后端开发服务器
   ```

### 代码共享流程

1. **定义共享类型**：在 `packages/shared/src/types/` 中定义前后端共享的类型
2. **前端使用**：前端应用通过 `@shared/types` 导入类型定义
3. **后端使用**：后端应用参考类型定义，确保 API 响应格式一致
4. **版本控制**：共享代码变更需要同时考虑前后端兼容性

### 构建和部署

1. **构建应用**
   ```bash
   npm run build
   ```

2. **容器化部署**
   ```bash
   # 构建镜像
   docker build -f infrastructure/docker/web/Dockerfile -t clm-web:latest .
   docker build -f infrastructure/docker/api/Dockerfile -t clm-api:latest .
   
   # 使用 Docker Compose 启动
   docker-compose -f infrastructure/docker/docker-compose.yml up -d
   ```

## 优势与考虑

### Monorepo 优势

1. **统一版本控制**：前后端代码在同一个仓库中，版本管理更加一致
2. **依赖管理**：可以轻松管理跨应用依赖
3. **代码共享**：便于共享类型定义、工具函数和配置
4. **重构便利**：跨应用重构更加容易
5. **CI/CD 简化**：统一的构建和部署流程

### 注意事项

1. **构建时间**：Monorepo 可能会增加构建时间，需要合理配置并行构建
2. **权限管理**：需要明确团队成员对不同模块的访问权限
3. **代码组织**：需要良好的目录结构和命名约定
4. **工具链配置**：需要配置适合多项目的工具链

这种统一的项目结构为证书生命周期管理系统提供了清晰的组织方式，支持前后端协同开发，并为未来的扩展和维护奠定了良好基础。