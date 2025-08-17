/**
 * Axios 请求封装
 * MVP 版本 - 无认证
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000, // 30秒超时
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加请求ID用于追踪
    config.headers['X-Request-ID'] = generateRequestId()
    
    // 开发环境下打印请求信息
    if (import.meta.env.MODE === 'development') {
      console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`, {
        params: config.params,
        data: config.data
      })
    }
    
    return config
  },
  (error) => {
    console.error('[Request Error]', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 开发环境下打印响应信息
    if (import.meta.env.MODE === 'development') {
      console.log(`[API Response] ${response.config.method?.toUpperCase()} ${response.config.url}`, res)
    }
    
    // 统一的响应格式处理
    if (res.success) {
      return res.data
    } else {
      // 业务错误处理
      const errorMessage = res.message || '请求失败'
      ElMessage.error(errorMessage)
      return Promise.reject(new Error(errorMessage))
    }
  },
  (error) => {
    console.error('[Response Error]', error)
    
    // 处理不同的错误状态
    const { response } = error
    
    if (response) {
      const { status, data } = response
      
      switch (status) {
      case 403:
        ElMessage.error('没有权限访问此资源')
        break
          
      case 404:
        ElMessage.error('请求的资源不存在')
        break
          
      case 500:
        ElMessage.error('服务器内部错误')
        break
          
      default: {
        // 尝试从响应中获取错误消息
        const errorMessage = data?.message || `请求失败 (${status})`
        ElMessage.error(errorMessage)
        break
      }
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else {
      ElMessage.error('网络连接失败，请检查网络设置')
    }
    
    return Promise.reject(error)
  }
)

/**
 * 生成请求ID
 * @returns {string} 请求ID
 */
function generateRequestId() {
  return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
}

/**
 * 通用请求方法
 */
export const http = {
  /**
   * GET 请求
   * @param {string} url 请求地址
   * @param {object} params 请求参数
   * @param {object} config 请求配置
   * @returns {Promise} 请求结果
   */
  get(url, params = {}, config = {}) {
    return request({
      method: 'GET',
      url,
      params,
      ...config
    })
  },

  /**
   * POST 请求
   * @param {string} url 请求地址
   * @param {object} data 请求数据
   * @param {object} config 请求配置
   * @returns {Promise} 请求结果
   */
  post(url, data = {}, config = {}) {
    return request({
      method: 'POST',
      url,
      data,
      ...config
    })
  },

  /**
   * PUT 请求
   * @param {string} url 请求地址
   * @param {object} data 请求数据
   * @param {object} config 请求配置
   * @returns {Promise} 请求结果
   */
  put(url, data = {}, config = {}) {
    return request({
      method: 'PUT',
      url,
      data,
      ...config
    })
  },

  /**
   * DELETE 请求
   * @param {string} url 请求地址
   * @param {object} config 请求配置
   * @returns {Promise} 请求结果
   */
  delete(url, config = {}) {
    return request({
      method: 'DELETE',
      url,
      ...config
    })
  },

  /**
   * PATCH 请求
   * @param {string} url 请求地址
   * @param {object} data 请求数据
   * @param {object} config 请求配置
   * @returns {Promise} 请求结果
   */
  patch(url, data = {}, config = {}) {
    return request({
      method: 'PATCH',
      url,
      data,
      ...config
    })
  },

  /**
   * 上传文件
   * @param {string} url 上传地址
   * @param {FormData} formData 文件数据
   * @param {function} onUploadProgress 上传进度回调
   * @returns {Promise} 上传结果
   */
  upload(url, formData, onUploadProgress) {
    return request({
      method: 'POST',
      url,
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress
    })
  },

  /**
   * 下载文件
   * @param {string} url 下载地址
   * @param {string} filename 文件名
   * @param {object} params 请求参数
   * @returns {Promise} 下载结果
   */
  download(url, filename, params = {}) {
    return request({
      method: 'GET',
      url,
      params,
      responseType: 'blob'
    }).then(blob => {
      const downloadUrl = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = filename
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(downloadUrl)
    })
  }
}

// 导出默认实例
export default request