# 证书生命周期管理系统 - 前端

基于 Vue 3 + Vite + Element Plus 的现代化前端应用。

## 技术栈

- Vue.js 3.x
- Vue Router 4.x
- Pinia 2.x
- Element Plus 2.x
- Vite 4.x
- Axios 1.x
- ECharts 5.x

## 开发

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 代码检查
npm run lint

# 格式化代码
npm run format

# 运行测试
npm test
```

## 目录结构

```
src/
├── api/          # API 接口封装
├── assets/       # 静态资源
├── components/   # 组件库
│   ├── common/   # 通用组件
│   ├── layout/   # 布局组件
│   └── business/ # 业务组件
├── composables/  # 组合式函数
├── directives/   # 自定义指令
├── hooks/        # 自定义钩子
├── layouts/      # 页面布局
├── plugins/      # 插件配置
├── router/       # 路由配置
├── stores/       # 状态管理
├── utils/        # 工具函数
└── views/        # 页面视图
```

## 环境变量

创建 `.env.development` 文件：

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_TITLE=证书生命周期管理系统
```

## 代码规范

- 使用 ESLint 进行代码检查
- 使用 Prettier 进行代码格式化
- 组件名使用 PascalCase
- 文件名使用 kebab-case（除组件外）

## 构建配置

查看 `vite.config.js` 了解详细的构建配置。

## API 代理

开发环境下，所有 `/api` 请求会代理到 `http://localhost:8080`。