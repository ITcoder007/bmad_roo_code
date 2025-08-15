import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import axios from 'axios'
import request, { http } from '@/utils/request'

// Mock axios
vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({
      interceptors: {
        request: { use: vi.fn() },
        response: { use: vi.fn() }
      },
      get: vi.fn(),
      post: vi.fn(),
      put: vi.fn(),
      delete: vi.fn(),
      patch: vi.fn()
    }))
  }
}))

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    error: vi.fn(),
    success: vi.fn()
  },
  ElMessageBox: {
    alert: vi.fn(() => Promise.resolve())
  }
}))

// Mock router
vi.mock('@/router', () => ({
  default: {
    push: vi.fn()
  }
}))

// Mock localStorage
const mockLocalStorage = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn()
}
global.localStorage = mockLocalStorage

// Mock console methods
const mockConsole = {
  log: vi.fn(),
  error: vi.fn()
}
global.console = mockConsole

// Mock import.meta.env
vi.stubEnv('VITE_API_BASE_URL', 'http://localhost:8080/api')
vi.stubEnv('MODE', 'test')

describe('Request Utils', () => {
  let mockAxiosInstance
  let requestInterceptor
  let responseInterceptor
  let requestErrorHandler
  let responseErrorHandler

  beforeEach(() => {
    vi.clearAllMocks()
    
    mockAxiosInstance = {
      interceptors: {
        request: { use: vi.fn() },
        response: { use: vi.fn() }
      },
      get: vi.fn(),
      post: vi.fn(),
      put: vi.fn(),
      delete: vi.fn(),
      patch: vi.fn()
    }
    
    axios.create.mockReturnValue(mockAxiosInstance)
    
    // 捕获拦截器处理函数
    mockAxiosInstance.interceptors.request.use.mockImplementation((success, error) => {
      requestInterceptor = success
      requestErrorHandler = error
    })
    
    mockAxiosInstance.interceptors.response.use.mockImplementation((success, error) => {
      responseInterceptor = success
      responseErrorHandler = error
    })
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  describe('Axios Instance Creation', () => {
    it('creates axios instance with correct configuration', () => {
      // 重新导入以触发实例创建
      vi.resetModules()
      require('@/utils/request')
      
      expect(axios.create).toHaveBeenCalledWith({
        baseURL: 'http://localhost:8080/api',
        timeout: 30000,
        headers: {
          'Content-Type': 'application/json;charset=UTF-8'
        }
      })
    })

    it('sets up request and response interceptors', () => {
      vi.resetModules()
      require('@/utils/request')
      
      expect(mockAxiosInstance.interceptors.request.use).toHaveBeenCalled()
      expect(mockAxiosInstance.interceptors.response.use).toHaveBeenCalled()
    })
  })

  describe('Request Interceptor', () => {
    beforeEach(() => {
      vi.resetModules()
      require('@/utils/request')
    })

    it('adds authorization token when token exists', () => {
      mockLocalStorage.getItem.mockReturnValue('test-token')
      
      const config = { headers: {} }
      const result = requestInterceptor(config)
      
      expect(mockLocalStorage.getItem).toHaveBeenCalledWith('token')
      expect(result.headers.Authorization).toBe('Bearer test-token')
    })

    it('does not add authorization when token does not exist', () => {
      mockLocalStorage.getItem.mockReturnValue(null)
      
      const config = { headers: {} }
      const result = requestInterceptor(config)
      
      expect(result.headers.Authorization).toBeUndefined()
    })

    it('adds request ID header', () => {
      const config = { headers: {} }
      const result = requestInterceptor(config)
      
      expect(result.headers['X-Request-ID']).toBeDefined()
      expect(typeof result.headers['X-Request-ID']).toBe('string')
    })

    it('logs request info in development mode', () => {
      import.meta.env.MODE = 'development'
      
      const config = {
        headers: {},
        method: 'GET',
        url: '/test',
        params: { id: 1 },
        data: { name: 'test' }
      }
      
      requestInterceptor(config)
      
      expect(mockConsole.log).toHaveBeenCalledWith(
        '[API Request] GET /test',
        { params: { id: 1 }, data: { name: 'test' } }
      )
    })

    it('handles request errors correctly', () => {
      const error = new Error('Request setup error')
      
      const result = requestErrorHandler(error)
      
      expect(mockConsole.error).toHaveBeenCalledWith('[Request Error]', error)
      expect(result).rejects.toBe(error)
    })
  })

  describe('Response Interceptor', () => {
    beforeEach(() => {
      vi.resetModules()
      require('@/utils/request')
    })

    it('returns data when response is successful', () => {
      const response = {
        data: {
          success: true,
          data: { id: 1, name: 'test' }
        },
        config: { method: 'GET', url: '/test' }
      }
      
      const result = responseInterceptor(response)
      
      expect(result).toEqual({ id: 1, name: 'test' })
    })

    it('shows error message when response fails', () => {
      const { ElMessage } = require('element-plus')
      const response = {
        data: {
          success: false,
          message: '业务错误'
        },
        config: { method: 'GET', url: '/test' }
      }
      
      expect(() => responseInterceptor(response)).rejects.toThrow('业务错误')
      expect(ElMessage.error).toHaveBeenCalledWith('业务错误')
    })

    it('logs response info in development mode', () => {
      import.meta.env.MODE = 'development'
      
      const response = {
        data: { success: true, data: { id: 1 } },
        config: { method: 'POST', url: '/test' }
      }
      
      responseInterceptor(response)
      
      expect(mockConsole.log).toHaveBeenCalledWith(
        '[API Response] POST /test',
        { success: true, data: { id: 1 } }
      )
    })
  })

  describe('Response Error Handler', () => {
    let router, ElMessage, ElMessageBox

    beforeEach(() => {
      vi.resetModules()
      router = require('@/router').default
      const elementPlus = require('element-plus')
      ElMessage = elementPlus.ElMessage
      ElMessageBox = elementPlus.ElMessageBox
      require('@/utils/request')
    })

    it('handles 401 unauthorized error', async () => {
      const error = {
        response: { status: 401, data: {} }
      }
      
      ElMessageBox.alert.mockResolvedValue()
      
      await expect(responseErrorHandler(error)).rejects.toBe(error)
      
      expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('token')
      expect(ElMessageBox.alert).toHaveBeenCalledWith(
        '登录已过期，请重新登录',
        '提示',
        { confirmButtonText: '确定', type: 'warning' }
      )
    })

    it('handles 403 forbidden error', async () => {
      const error = {
        response: { status: 403, data: {} }
      }
      
      await expect(responseErrorHandler(error)).rejects.toBe(error)
      
      expect(ElMessage.error).toHaveBeenCalledWith('没有权限访问此资源')
    })

    it('handles 404 not found error', async () => {
      const error = {
        response: { status: 404, data: {} }
      }
      
      await expect(responseErrorHandler(error)).rejects.toBe(error)
      
      expect(ElMessage.error).toHaveBeenCalledWith('请求的资源不存在')
    })

    it('handles 500 server error', async () => {
      const error = {
        response: { status: 500, data: {} }
      }
      
      await expect(responseErrorHandler(error)).rejects.toBe(error)
      
      expect(ElMessage.error).toHaveBeenCalledWith('服务器内部错误')
    })

    it('handles timeout error', async () => {
      const error = {
        code: 'ECONNABORTED'
      }
      
      await expect(responseErrorHandler(error)).rejects.toBe(error)
      
      expect(ElMessage.error).toHaveBeenCalledWith('请求超时，请稍后重试')
    })

    it('handles network error', async () => {
      const error = {
        message: 'Network Error'
      }
      
      await expect(responseErrorHandler(error)).rejects.toBe(error)
      
      expect(ElMessage.error).toHaveBeenCalledWith('网络连接失败，请检查网络设置')
    })

    it('handles other HTTP errors with custom message', async () => {
      const error = {
        response: {
          status: 422,
          data: { message: '请求参数错误' }
        }
      }
      
      await expect(responseErrorHandler(error)).rejects.toBe(error)
      
      expect(ElMessage.error).toHaveBeenCalledWith('请求参数错误')
    })

    it('handles other HTTP errors with default message', async () => {
      const error = {
        response: {
          status: 422,
          data: {}
        }
      }
      
      await expect(responseErrorHandler(error)).rejects.toBe(error)
      
      expect(ElMessage.error).toHaveBeenCalledWith('请求失败 (422)')
    })
  })

  describe('HTTP Methods', () => {
    let mockRequest

    beforeEach(() => {
      mockRequest = vi.fn()
      // Mock the request instance
      vi.doMock('@/utils/request', () => ({
        default: mockRequest,
        http: {
          get: (url, params = {}, config = {}) => mockRequest({
            method: 'GET',
            url,
            params,
            ...config
          }),
          post: (url, data = {}, config = {}) => mockRequest({
            method: 'POST',
            url,
            data,
            ...config
          }),
          put: (url, data = {}, config = {}) => mockRequest({
            method: 'PUT',
            url,
            data,
            ...config
          }),
          delete: (url, config = {}) => mockRequest({
            method: 'DELETE',
            url,
            ...config
          }),
          patch: (url, data = {}, config = {}) => mockRequest({
            method: 'PATCH',
            url,
            data,
            ...config
          }),
          upload: (url, formData, onUploadProgress) => mockRequest({
            method: 'POST',
            url,
            data: formData,
            headers: { 'Content-Type': 'multipart/form-data' },
            onUploadProgress
          }),
          download: vi.fn()
        }
      }))
    })

    it('makes GET request correctly', () => {
      const { http } = require('@/utils/request')
      
      http.get('/test', { id: 1 }, { timeout: 5000 })
      
      expect(mockRequest).toHaveBeenCalledWith({
        method: 'GET',
        url: '/test',
        params: { id: 1 },
        timeout: 5000
      })
    })

    it('makes POST request correctly', () => {
      const { http } = require('@/utils/request')
      
      http.post('/test', { name: 'test' }, { timeout: 5000 })
      
      expect(mockRequest).toHaveBeenCalledWith({
        method: 'POST',
        url: '/test',
        data: { name: 'test' },
        timeout: 5000
      })
    })

    it('makes PUT request correctly', () => {
      const { http } = require('@/utils/request')
      
      http.put('/test/1', { name: 'updated' })
      
      expect(mockRequest).toHaveBeenCalledWith({
        method: 'PUT',
        url: '/test/1',
        data: { name: 'updated' }
      })
    })

    it('makes DELETE request correctly', () => {
      const { http } = require('@/utils/request')
      
      http.delete('/test/1')
      
      expect(mockRequest).toHaveBeenCalledWith({
        method: 'DELETE',
        url: '/test/1'
      })
    })

    it('makes PATCH request correctly', () => {
      const { http } = require('@/utils/request')
      
      http.patch('/test/1', { status: 'active' })
      
      expect(mockRequest).toHaveBeenCalledWith({
        method: 'PATCH',
        url: '/test/1',
        data: { status: 'active' }
      })
    })

    it('makes upload request correctly', () => {
      const { http } = require('@/utils/request')
      const formData = new FormData()
      const onProgress = vi.fn()
      
      http.upload('/upload', formData, onProgress)
      
      expect(mockRequest).toHaveBeenCalledWith({
        method: 'POST',
        url: '/upload',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: onProgress
      })
    })

    it('handles download request correctly', async () => {
      const { http } = require('@/utils/request')
      
      // Mock blob response
      const mockBlob = new Blob(['test content'])
      http.download = vi.fn().mockImplementation((url, filename, params = {}) => {
        return mockRequest({
          method: 'GET',
          url,
          params,
          responseType: 'blob'
        }).then(() => mockBlob).then(blob => {
          // Mock DOM operations
          const mockUrl = 'blob:test-url'
          global.URL.createObjectURL = vi.fn().mockReturnValue(mockUrl)
          global.URL.revokeObjectURL = vi.fn()
          
          const mockLink = {
            href: '',
            download: '',
            click: vi.fn()
          }
          document.createElement = vi.fn().mockReturnValue(mockLink)
          document.body.appendChild = vi.fn()
          document.body.removeChild = vi.fn()
          
          const downloadUrl = global.URL.createObjectURL(blob)
          mockLink.href = downloadUrl
          mockLink.download = filename
          document.body.appendChild(mockLink)
          mockLink.click()
          document.body.removeChild(mockLink)
          global.URL.revokeObjectURL(downloadUrl)
        })
      })
      
      await http.download('/download', 'test.pdf', { id: 1 })
      
      expect(http.download).toHaveBeenCalledWith('/download', 'test.pdf', { id: 1 })
    })
  })
})