import { ref, onUnmounted } from 'vue'

/**
 * 自动刷新组合函数
 * @param {Function} callback 刷新回调函数
 * @param {number} interval 刷新间隔（毫秒），默认5分钟
 * @returns {Object} 自动刷新控制方法
 */
export function useAutoRefresh(callback, interval = 5 * 60 * 1000) {
  const isActive = ref(false)
  const timer = ref(null)
  const lastRefreshTime = ref(null)
  
  const startAutoRefresh = () => {
    if (isActive.value) return
    
    isActive.value = true
    timer.value = setInterval(() => {
      // 只在页面可见时执行刷新
      if (!document.hidden) {
        try {
          if (typeof callback === 'function') {
            callback()
            lastRefreshTime.value = new Date()
          }
        } catch (error) {
          console.warn('Auto refresh callback error:', error)
        }
      }
    }, interval)
  }
  
  const stopAutoRefresh = () => {
    if (timer.value) {
      clearInterval(timer.value)
      timer.value = null
    }
    isActive.value = false
  }
  
  const forceRefresh = () => {
    try {
      if (typeof callback === 'function') {
        callback()
        lastRefreshTime.value = new Date()
      }
    } catch (error) {
      console.warn('Force refresh callback error:', error)
    }
  }
  
  // 页面可见性变化处理
  const handleVisibilityChange = () => {
    if (document.hidden) {
      // 页面隐藏时不需要停止定时器，只是不执行刷新
      console.log('页面隐藏，暂停自动刷新')
    } else if (isActive.value) {
      // 页面重新可见时，如果距离上次刷新超过间隔时间，立即刷新一次
      const now = new Date()
      if (lastRefreshTime.value && (now - lastRefreshTime.value) >= interval) {
        console.log('页面重新可见，立即执行一次刷新')
        forceRefresh()
      }
    }
  }
  
  // 添加页面可见性监听
  document.addEventListener('visibilitychange', handleVisibilityChange)
  
  // 清理
  onUnmounted(() => {
    stopAutoRefresh()
    document.removeEventListener('visibilitychange', handleVisibilityChange)
  })
  
  return {
    isActive,
    lastRefreshTime,
    startAutoRefresh,
    stopAutoRefresh,
    forceRefresh
  }
}