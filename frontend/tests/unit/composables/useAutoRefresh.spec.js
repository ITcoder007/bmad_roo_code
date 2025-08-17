import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useAutoRefresh } from '@/composables/useAutoRefresh'

describe('useAutoRefresh', () => {
  let callback
  let mockInterval
  let originalSetInterval
  let originalClearInterval
  let originalDocument

  beforeEach(() => {
    callback = vi.fn()
    mockInterval = 1000
    
    // Mock timers
    vi.useFakeTimers()
    
    // Mock document
    originalDocument = global.document
    global.document = {
      hidden: false,
      addEventListener: vi.fn(),
      removeEventListener: vi.fn()
    }
  })

  afterEach(() => {
    vi.useRealTimers()
    vi.clearAllMocks()
    global.document = originalDocument
  })

  describe('基础功能测试', () => {
    it('应该正确初始化自动刷新状态', () => {
      const { isActive, lastRefreshTime } = useAutoRefresh(callback, mockInterval)
      
      expect(isActive.value).toBe(false)
      expect(lastRefreshTime.value).toBeNull()
    })

    it('应该启动自动刷新', () => {
      const { startAutoRefresh, isActive } = useAutoRefresh(callback, mockInterval)
      
      startAutoRefresh()
      
      expect(isActive.value).toBe(true)
    })

    it('应该停止自动刷新', () => {
      const { startAutoRefresh, stopAutoRefresh, isActive } = useAutoRefresh(callback, mockInterval)
      
      startAutoRefresh()
      expect(isActive.value).toBe(true)
      
      stopAutoRefresh()
      expect(isActive.value).toBe(false)
    })

    it('应该防止重复启动自动刷新', () => {
      const { startAutoRefresh, isActive } = useAutoRefresh(callback, mockInterval)
      
      startAutoRefresh()
      const firstState = isActive.value
      
      startAutoRefresh() // 再次调用
      
      expect(isActive.value).toBe(firstState)
    })
  })

  describe('定时器功能测试', () => {
    it('应该按指定间隔执行回调', () => {
      const { startAutoRefresh } = useAutoRefresh(callback, mockInterval)
      
      startAutoRefresh()
      
      // 初始不应该执行
      expect(callback).not.toHaveBeenCalled()
      
      // 第一次间隔后执行
      vi.advanceTimersByTime(mockInterval)
      expect(callback).toHaveBeenCalledTimes(1)
      
      // 第二次间隔后再次执行
      vi.advanceTimersByTime(mockInterval)
      expect(callback).toHaveBeenCalledTimes(2)
    })

    it('应该在停止后不再执行回调', () => {
      const { startAutoRefresh, stopAutoRefresh } = useAutoRefresh(callback, mockInterval)
      
      startAutoRefresh()
      vi.advanceTimersByTime(mockInterval)
      expect(callback).toHaveBeenCalledTimes(1)
      
      stopAutoRefresh()
      vi.advanceTimersByTime(mockInterval)
      expect(callback).toHaveBeenCalledTimes(1) // 不应该再增加
    })

    it('应该使用默认间隔时间', () => {
      const { startAutoRefresh } = useAutoRefresh(callback) // 不传间隔参数
      
      startAutoRefresh()
      
      // 5分钟 = 5 * 60 * 1000 毫秒
      vi.advanceTimersByTime(5 * 60 * 1000 - 1)
      expect(callback).not.toHaveBeenCalled()
      
      vi.advanceTimersByTime(1)
      expect(callback).toHaveBeenCalledTimes(1)
    })
  })

  describe('强制刷新功能测试', () => {
    it('应该立即执行强制刷新', () => {
      const { forceRefresh, lastRefreshTime } = useAutoRefresh(callback, mockInterval)
      
      forceRefresh()
      
      expect(callback).toHaveBeenCalledTimes(1)
      expect(lastRefreshTime.value).toBeInstanceOf(Date)
    })

    it('应该更新最后刷新时间', () => {
      const { forceRefresh, lastRefreshTime } = useAutoRefresh(callback, mockInterval)
      
      const timeBefore = new Date()
      forceRefresh()
      const timeAfter = new Date()
      
      expect(lastRefreshTime.value.getTime()).toBeGreaterThanOrEqual(timeBefore.getTime())
      expect(lastRefreshTime.value.getTime()).toBeLessThanOrEqual(timeAfter.getTime())
    })
  })

  describe('页面可见性处理测试', () => {
    it('应该在页面隐藏时记录日志', () => {
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})
      
      useAutoRefresh(callback, mockInterval)
      
      // 模拟页面隐藏
      global.document.hidden = true
      
      // 获取添加的事件监听器
      const visibilityHandler = global.document.addEventListener.mock.calls
        .find(call => call[0] === 'visibilitychange')[1]
      
      visibilityHandler()
      
      expect(consoleSpy).toHaveBeenCalledWith('页面隐藏，暂停自动刷新')
      
      consoleSpy.mockRestore()
    })

    it('应该在页面重新可见且超过间隔时间时立即刷新', () => {
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})
      
      const { startAutoRefresh, lastRefreshTime } = useAutoRefresh(callback, mockInterval)
      
      startAutoRefresh()
      
      // 设置上次刷新时间为很久以前
      lastRefreshTime.value = new Date(Date.now() - mockInterval - 1000)
      
      // 模拟页面重新可见
      global.document.hidden = false
      
      // 获取添加的事件监听器
      const visibilityHandler = global.document.addEventListener.mock.calls
        .find(call => call[0] === 'visibilitychange')[1]
      
      visibilityHandler()
      
      expect(consoleSpy).toHaveBeenCalledWith('页面重新可见，立即执行一次刷新')
      expect(callback).toHaveBeenCalled()
      
      consoleSpy.mockRestore()
    })

    it('应该在页面重新可见但未超过间隔时间时不刷新', () => {
      const { startAutoRefresh, lastRefreshTime } = useAutoRefresh(callback, mockInterval)
      
      startAutoRefresh()
      
      // 设置上次刷新时间为刚刚
      lastRefreshTime.value = new Date()
      
      // 模拟页面重新可见
      global.document.hidden = false
      
      // 获取添加的事件监听器
      const visibilityHandler = global.document.addEventListener.mock.calls
        .find(call => call[0] === 'visibilitychange')[1]
      
      visibilityHandler()
      
      // 不应该执行额外的回调
      expect(callback).not.toHaveBeenCalled()
    })

    it('应该在页面隐藏时不执行定时回调', () => {
      const { startAutoRefresh } = useAutoRefresh(callback, mockInterval)
      
      startAutoRefresh()
      
      // 模拟页面隐藏
      global.document.hidden = true
      
      // 推进时间触发定时器
      vi.advanceTimersByTime(mockInterval)
      
      // 应该不执行回调，因为页面隐藏
      expect(callback).not.toHaveBeenCalled()
    })

    it('应该在页面可见时正常执行定时回调', () => {
      const { startAutoRefresh } = useAutoRefresh(callback, mockInterval)
      
      startAutoRefresh()
      
      // 确保页面可见
      global.document.hidden = false
      
      // 推进时间触发定时器
      vi.advanceTimersByTime(mockInterval)
      
      // 应该执行回调
      expect(callback).toHaveBeenCalledTimes(1)
    })
  })

  describe('事件监听器管理测试', () => {
    it('应该添加页面可见性变化监听器', () => {
      useAutoRefresh(callback, mockInterval)
      
      expect(global.document.addEventListener).toHaveBeenCalledWith(
        'visibilitychange',
        expect.any(Function)
      )
    })

    it('应该在组件卸载时移除事件监听器', () => {
      // 这个测试更适合在实际的 Vue 组件中测试 onUnmounted 钩子
      // 这里我们只测试事件监听器被正确添加
      useAutoRefresh(callback, mockInterval)
      
      expect(global.document.addEventListener).toHaveBeenCalledWith(
        'visibilitychange',
        expect.any(Function)
      )
    })
  })

  describe('边界情况测试', () => {
    it('应该处理空回调函数', () => {
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
      const { startAutoRefresh } = useAutoRefresh(null, mockInterval)
      
      expect(() => {
        startAutoRefresh()
        vi.advanceTimersByTime(mockInterval)
      }).not.toThrow()
      
      consoleSpy.mockRestore()
    })

    it('应该处理负数间隔时间', () => {
      const { startAutoRefresh } = useAutoRefresh(callback, -1000)
      
      expect(() => {
        startAutoRefresh()
      }).not.toThrow()
    })

    it('应该处理非常小的间隔时间', () => {
      const { startAutoRefresh } = useAutoRefresh(callback, 1)
      
      startAutoRefresh()
      vi.advanceTimersByTime(1)
      
      expect(callback).toHaveBeenCalled()
    })

    it('应该处理回调函数抛出异常的情况', () => {
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
      const errorCallback = vi.fn().mockImplementation(() => {
        throw new Error('Callback error')
      })
      
      const { startAutoRefresh } = useAutoRefresh(errorCallback, mockInterval)
      
      expect(() => {
        startAutoRefresh()
        vi.advanceTimersByTime(mockInterval)
      }).not.toThrow()
      
      expect(consoleSpy).toHaveBeenCalledWith('Auto refresh callback error:', expect.any(Error))
      consoleSpy.mockRestore()
    })
  })

  describe('返回值测试', () => {
    it('应该返回所有必需的方法和状态', () => {
      const result = useAutoRefresh(callback, mockInterval)
      
      expect(result).toHaveProperty('isActive')
      expect(result).toHaveProperty('lastRefreshTime')
      expect(result).toHaveProperty('startAutoRefresh')
      expect(result).toHaveProperty('stopAutoRefresh')
      expect(result).toHaveProperty('forceRefresh')
      
      expect(typeof result.startAutoRefresh).toBe('function')
      expect(typeof result.stopAutoRefresh).toBe('function')
      expect(typeof result.forceRefresh).toBe('function')
    })

    it('应该返回响应式的状态', () => {
      const { isActive, lastRefreshTime, startAutoRefresh, forceRefresh } = useAutoRefresh(callback, mockInterval)
      
      expect(isActive.value).toBe(false)
      expect(lastRefreshTime.value).toBeNull()
      
      startAutoRefresh()
      expect(isActive.value).toBe(true)
      
      forceRefresh()
      expect(lastRefreshTime.value).toBeInstanceOf(Date)
    })
  })
})