# 证书管理系统 - 前端应用

基于 Vue 3 + Vite + Element Plus 构建的现代化前端应用。

## 技术栈

- **Vue.js 3.x** - 渐进式JavaScript框架
- **Vite 4.x** - 下一代前端构建工具
- **Vue Router 4.x** - 官方路由管理器
- **Pinia 2.x** - 新一代状态管理工具
- **Element Plus 2.x** - 基于Vue 3的组件库
- **JavaScript ES2020+** - 现代JavaScript特性

## 项目结构

```
frontend/
├── public/             # 静态资源
├── src/
│   ├── api/           # API接口封装
│   ├── assets/        # 静态资源（图片、字体等）
│   ├── components/    # 可复用组件
│   ├── composables/   # 组合式函数
│   ├── directives/    # 自定义指令
│   ├── hooks/         # 自定义Hook
│   ├── layouts/       # 布局组件
│   ├── mixins/        # 混入
│   ├── plugins/       # 插件配置
│   ├── router/        # 路由配置
│   ├── stores/        # Pinia状态管理
│   ├── utils/         # 工具函数
│   ├── views/         # 页面组件
│   ├── App.vue        # 根组件
│   └── main.js        # 应用入口
├── .env               # 环境变量
├── .env.development   # 开发环境变量
├── .env.production    # 生产环境变量
├── index.html         # HTML模板
├── package.json       # 项目配置
└── vite.config.js     # Vite配置
```

## 开发规范

### 命名规范

- **组件命名**：使用 PascalCase，如 `UserProfile.vue`
- **文件命名**：
  - 组件文件：PascalCase，如 `TodoList.vue`
  - 其他JS文件：camelCase，如 `apiClient.js`
  - 样式文件：kebab-case，如 `user-profile.css`
- **路由命名**：使用 kebab-case，如 `/user-profile`
- **常量命名**：使用 UPPER_SNAKE_CASE，如 `MAX_UPLOAD_SIZE`

### 组件规范

1. **单文件组件结构**
```vue
<template>
  <!-- 模板内容 -->
</template>

<script>
export default {
  name: 'ComponentName',
  // 组件逻辑
}
</script>

<style scoped>
/* 组件样式 */
</style>
```

2. **Props定义**
```javascript
props: {
  title: {
    type: String,
    required: true,
    default: ''
  }
}
```

3. **事件命名**
- 使用 kebab-case
- 动词开头，如 `@update-value`、`@delete-item`

### 状态管理规范

使用 Pinia 进行状态管理：

```javascript
// stores/user.js
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null
  }),
  getters: {
    isLoggedIn: (state) => !!state.userInfo
  },
  actions: {
    async login(credentials) {
      // 登录逻辑
    }
  }
})
```

## 开发指南

### 安装依赖
```bash
npm install
```

### 启动开发服务器
```bash
npm run dev
```
应用将在 http://localhost:3000 启动

### 构建生产版本
```bash
npm run build
```
构建产物将输出到 `dist/` 目录

### 预览生产构建
```bash
npm run preview
```

## API集成

### API配置
API基础URL配置在环境变量中：
- 开发环境：`.env.development`
- 生产环境：`.env.production`

### API调用示例
```javascript
// api/certificate.js
import request from '@/utils/request'

export function getCertificateList(params) {
  return request({
    url: '/certificates',
    method: 'get',
    params
  })
}
```

### 请求拦截
在 `utils/request.js` 中配置请求和响应拦截器。

## 路由配置

路由配置文件：`router/index.js`

```javascript
const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue')
  },
  // 更多路由...
]
```

## 环境变量

### 可用变量
- `VITE_APP_TITLE` - 应用标题
- `VITE_API_BASE_URL` - API基础URL
- `VITE_APP_ENV` - 当前环境

### 使用环境变量
```javascript
const apiUrl = import.meta.env.VITE_API_BASE_URL
```

## 样式规范

- 使用 Element Plus 提供的设计变量
- 组件样式使用 `scoped` 属性
- 全局样式放在 `assets/styles/` 目录

## 性能优化

1. **路由懒加载**
```javascript
component: () => import('@/views/SomeView.vue')
```

2. **组件懒加载**
```javascript
const MyComponent = defineAsyncComponent(() =>
  import('./components/MyComponent.vue')
)
```

3. **图片优化**
- 使用适当的图片格式
- 实现图片懒加载
- 使用CDN加速

## 测试

### 单元测试
```bash
npm run test:unit
```

### E2E测试
```bash
npm run test:e2e
```

## 部署

1. 构建生产版本
2. 将 `dist/` 目录部署到Web服务器
3. 配置nginx反向代理API请求

### Nginx配置示例
```nginx
location / {
  root /var/www/certificate-management;
  try_files $uri $uri/ /index.html;
}

location /api {
  proxy_pass http://backend-server:8080;
}
```

## 常见问题

### 1. 开发服务器无法启动
- 检查Node.js版本是否满足要求
- 清除node_modules并重新安装

### 2. API请求跨域问题
- 开发环境已在 `vite.config.js` 中配置代理
- 生产环境需要后端配置CORS或使用nginx代理

### 3. 构建失败
- 检查是否有语法错误
- 确保所有依赖正确安装

## 相关文档

- [Vue 3文档](https://vuejs.org/)
- [Vite文档](https://vitejs.dev/)
- [Element Plus文档](https://element-plus.org/)
- [Pinia文档](https://pinia.vuejs.org/)

---

*最后更新：2025-08-12*