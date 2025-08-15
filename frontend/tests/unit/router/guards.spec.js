import { describe, it, expect, vi, beforeEach } from 'vitest'
import { 
  authGuard, 
  permissionGuard, 
  titleGuard,
  progressGuard,
  progressDoneGuard 
} from '@/router/guards'

// Mock localStorage
const mockLocalStorage = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn()
}
global.localStorage = mockLocalStorage

describe('Router Guards', () => {
  let mockTo, mockFrom, mockNext

  beforeEach(() => {
    vi.clearAllMocks()
    
    mockTo = {
      path: '/test',
      meta: { 
        requiresAuth: true,
        title: '测试页面',
        permissions: ['test:view']
      }
    }
    
    mockFrom = {
      path: '/home'
    }
    
    mockNext = vi.fn()
  })

  describe('authGuard', () => {
    it('allows access when user is authenticated', () => {
      mockLocalStorage.getItem.mockReturnValue('mock-token')
      
      authGuard(mockTo, mockFrom, mockNext)
      
      expect(mockNext).toHaveBeenCalledWith()
    })

    it('redirects to login when user is not authenticated', () => {
      mockLocalStorage.getItem.mockReturnValue(null)
      
      authGuard(mockTo, mockFrom, mockNext)
      
      expect(mockNext).toHaveBeenCalledWith('/login')
    })

    it('allows access to public routes without authentication', () => {
      mockLocalStorage.getItem.mockReturnValue(null)
      mockTo.meta.requiresAuth = false
      
      authGuard(mockTo, mockFrom, mockNext)
      
      expect(mockNext).toHaveBeenCalledWith()
    })
  })

  describe('permissionGuard', () => {
    it('allows access when user has required permissions', () => {
      // This test assumes the getUserPermissions function returns the required permission
      permissionGuard(mockTo, mockFrom, mockNext)
      
      expect(mockNext).toHaveBeenCalledWith()
    })

    it('allows access when no permissions are required', () => {
      mockTo.meta.permissions = undefined
      
      permissionGuard(mockTo, mockFrom, mockNext)
      
      expect(mockNext).toHaveBeenCalledWith()
    })
  })

  describe('titleGuard', () => {
    beforeEach(() => {
      // Mock document.title
      Object.defineProperty(document, 'title', {
        writable: true,
        value: ''
      })
      
      // Mock import.meta.env  
      vi.stubEnv('VITE_APP_NAME', '测试应用')
    })

    it('sets document title with page title and app name', () => {
      titleGuard(mockTo, mockFrom, mockNext)
      
      expect(document.title).toBe('测试页面 - 测试应用')
      expect(mockNext).toHaveBeenCalledWith()
    })

    it('sets document title to app name when no page title', () => {
      mockTo.meta.title = undefined
      
      titleGuard(mockTo, mockFrom, mockNext)
      
      expect(document.title).toBe('测试应用')
      expect(mockNext).toHaveBeenCalledWith()
    })
  })

  describe('progressGuard', () => {
    it('calls next function', () => {
      progressGuard(mockTo, mockFrom, mockNext)
      
      expect(mockNext).toHaveBeenCalledWith()
    })
  })

  describe('progressDoneGuard', () => {
    it('executes without errors', () => {
      expect(() => {
        progressDoneGuard(mockTo, mockFrom)
      }).not.toThrow()
    })
  })
})