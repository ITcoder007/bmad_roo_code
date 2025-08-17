import { ref, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'

/**
 * URL 参数同步组合函数
 * 支持搜索参数与 URL 的双向同步
 */
export function useUrlSync() {
  const router = useRouter()
  const route = useRoute()
  
  /**
   * 更新 URL 查询参数
   * @param {object} params 要更新的参数
   * @param {boolean} replace 是否替换历史记录（默认 false）
   */
  const updateUrlParams = async (params, replace = false) => {
    try {
      const query = { ...route.query }
      
      // 更新参数
      Object.keys(params).forEach(key => {
        const value = params[key]
        if (value === null || value === undefined || value === '') {
          delete query[key]
        } else {
          query[key] = String(value)
        }
      })
      
      // 路由更新
      const method = replace ? 'replace' : 'push'
      await router[method]({
        path: route.path,
        query
      })
    } catch (error) {
      console.warn('URL 参数更新失败:', error)
    }
  }
  
  /**
   * 从 URL 获取查询参数
   * @param {string} key 参数名
   * @param {any} defaultValue 默认值
   * @returns {any} 参数值
   */
  const getUrlParam = (key, defaultValue = '') => {
    return route.query[key] || defaultValue
  }
  
  /**
   * 监听 URL 参数变化
   * @param {string|string[]} keys 要监听的参数名
   * @param {function} callback 回调函数
   */
  const watchUrlParams = (keys, callback) => {
    const keyArray = Array.isArray(keys) ? keys : [keys]
    
    return watch(
      () => keyArray.map(key => route.query[key]),
      (newValues, oldValues = []) => {
        const changes = {}
        keyArray.forEach((key, index) => {
          if (newValues[index] !== (oldValues[index] || undefined)) {
            changes[key] = newValues[index]
          }
        })
        
        if (Object.keys(changes).length > 0) {
          callback(changes)
        }
      },
      { immediate: true }
    )
  }
  
  return {
    updateUrlParams,
    getUrlParam,
    watchUrlParams
  }
}

/**
 * 搜索 URL 同步组合函数
 * 专门用于搜索功能的 URL 同步
 */
export function useSearchUrlSync() {
  const { updateUrlParams, getUrlParam, watchUrlParams } = useUrlSync()
  
  // 响应式搜索参数
  const searchQuery = ref('')
  const statusFilter = ref('')
  const currentPage = ref(1)
  
  // 初始化参数
  const initFromUrl = () => {
    searchQuery.value = getUrlParam('search', '')
    statusFilter.value = getUrlParam('status', '')
    currentPage.value = parseInt(getUrlParam('page', '1'), 10) || 1
  }
  
  // 同步到 URL
  const syncToUrl = () => {
    updateUrlParams({
      search: searchQuery.value,
      status: statusFilter.value,
      page: currentPage.value > 1 ? currentPage.value : null
    })
  }
  
  // 监听 URL 变化
  const stopWatching = watchUrlParams(
    ['search', 'status', 'page'],
    (changes) => {
      if (changes.search !== undefined) {
        searchQuery.value = changes.search || ''
      }
      if (changes.status !== undefined) {
        statusFilter.value = changes.status || ''
      }
      if (changes.page !== undefined) {
        currentPage.value = parseInt(changes.page, 10) || 1
      }
    }
  )
  
  // 更新搜索参数
  const updateSearch = (query) => {
    searchQuery.value = query
    currentPage.value = 1 // 搜索时重置页码
    syncToUrl()
  }
  
  // 更新状态筛选
  const updateStatus = (status) => {
    statusFilter.value = status
    currentPage.value = 1 // 筛选时重置页码
    syncToUrl()
  }
  
  // 更新页码
  const updatePage = (page) => {
    currentPage.value = page
    syncToUrl()
  }
  
  // 清除搜索
  const clearSearch = () => {
    searchQuery.value = ''
    currentPage.value = 1
    syncToUrl()
  }
  
  // 重置所有参数
  const resetParams = () => {
    searchQuery.value = ''
    statusFilter.value = ''
    currentPage.value = 1
    syncToUrl()
  }
  
  // 初始化
  nextTick(() => {
    initFromUrl()
  })
  
  return {
    searchQuery,
    statusFilter,
    currentPage,
    updateSearch,
    updateStatus,
    updatePage,
    clearSearch,
    resetParams,
    syncToUrl,
    stopWatching
  }
}