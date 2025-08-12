# API 请求处理

## 1. API 请求架构

```javascript
// API实例配置示例
import axios from 'axios'
import { useAuthStore } from '@/stores/modules/auth'
import router from '@/router'

// 创建axios实例
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 未授权，跳转到登录页
          const authStore = useAuthStore()
          authStore.logout()
          router.push('/login')
          break
        case 403:
          // 禁止访问
          router.push('/403')
          break
        case 404:
          // 资源不存在
          router.push('/404')
          break
        case 500:
          // 服务器错误
          router.push('/500')
          break
        default:
          // 其他错误
          console.error('API Error:', error)
      }
    }
    return Promise.reject(error)
  }
)

export default api
```

## 2. API 模块化

- **按功能模块划分**：将API按功能模块划分，便于管理
- **统一错误处理**：统一的错误处理机制
- **请求/响应拦截**：请求和响应拦截器处理通用逻辑
- **取消请求**：支持取消未完成的请求
