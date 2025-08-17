/**
 * 证书状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { 
  getCertificateList, 
  getCertificateById,
  createCertificate,
  updateCertificate,
  deleteCertificate,
  getCertificateStatistics,
  searchCertificates as apiSearchCertificates,
  getDashboardStats,
  getExpiringCertificatesForDashboard,
  getRecentCertificates
} from '@/api/certificate'

export const useCertificateStore = defineStore('certificate', () => {
  // 状态
  const certificates = ref([])
  const currentCertificate = ref(null)
  const statistics = ref({
    total: 0,
    normal: 0,
    expiringSoon: 0,
    expired: 0
  })
  const loading = ref(false)
  const pagination = ref({
    page: 1,
    size: 20,
    total: 0
  })
  const searchParams = ref({
    keyword: '',
    status: '',
    sort: '',
    search: ''  // 新增搜索字段
  })
  const searchLoading = ref(false)
  const searchResults = ref([])
  const lastSearchQuery = ref('')
  const sortConfig = ref({
    prop: '',
    order: ''
  })
  
  // 仪表板相关状态
  const dashboardStats = ref({
    total: 0,
    normal: 0,
    expiring: 0,
    expired: 0
  })
  const expiringCertificates = ref([])
  const recentCertificates = ref([])
  const statsLoading = ref(false)
  const dashboardCacheTime = ref(null)

  // 计算属性
  const certificateList = computed(() => certificates.value)
  const isLoading = computed(() => loading.value)
  const paginationInfo = computed(() => pagination.value)
  const searchFilter = computed(() => searchParams.value)
  const statsData = computed(() => statistics.value)
  
  // 搜索相关计算属性
  const hasSearchQuery = computed(() => searchParams.value.search.trim() !== '')
  const isSearching = computed(() => searchLoading.value)
  const searchResultsCount = computed(() => searchResults.value.length)
  const displayCertificates = computed(() => {
    return hasSearchQuery.value ? searchResults.value : certificates.value
  })
  
  // 仪表板相关计算属性
  const totalCertificates = computed(() => dashboardStats.value.total)
  
  const normalPercentage = computed(() => {
    const total = dashboardStats.value.total
    return total > 0 ? Math.round((dashboardStats.value.normal / total) * 100) : 0
  })
  
  const criticalCertificatesCount = computed(() => {
    return dashboardStats.value.expiring + dashboardStats.value.expired
  })
  
  const hasExpiringCertificates = computed(() => expiringCertificates.value.length > 0)
  
  const isStatsLoading = computed(() => statsLoading.value)
  
  const isDashboardCacheValid = computed(() => {
    if (!dashboardCacheTime.value) return false
    const now = new Date()
    const cacheTime = new Date(dashboardCacheTime.value)
    const diffMinutes = (now - cacheTime) / (1000 * 60)
    return diffMinutes < 5 // 缓存5分钟
  })

  // 获取证书列表
  const fetchCertificateList = async (params = {}) => {
    try {
      loading.value = true
      
      const queryParams = {
        page: pagination.value.page,
        size: pagination.value.size,
        ...searchParams.value,
        ...params
      }
      
      const result = await getCertificateList(queryParams)
      
      certificates.value = result.records || []
      pagination.value.total = result.total || 0
      
      return result
    } catch (error) {
      console.error('获取证书列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取证书详情
  const fetchCertificateDetail = async (id) => {
    try {
      loading.value = true
      const result = await getCertificateById(id)
      currentCertificate.value = result
      return result
    } catch (error) {
      console.error('获取证书详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 创建证书
  const createNewCertificate = async (certificateData) => {
    try {
      loading.value = true
      const result = await createCertificate(certificateData)
      
      // 重新获取列表
      await fetchCertificateList()
      
      return result
    } catch (error) {
      console.error('创建证书失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 更新证书
  const updateExistingCertificate = async (id, certificateData) => {
    try {
      loading.value = true
      const result = await updateCertificate(id, certificateData)
      
      // 更新当前证书信息
      if (currentCertificate.value && currentCertificate.value.id === id) {
        currentCertificate.value = result
      }
      
      // 更新列表中的证书
      const index = certificates.value.findIndex(cert => cert.id === id)
      if (index !== -1) {
        certificates.value[index] = result
      }
      
      return result
    } catch (error) {
      console.error('更新证书失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 删除证书
  const removeExistingCertificate = async (id) => {
    try {
      loading.value = true
      await deleteCertificate(id)
      
      // 从列表中移除
      certificates.value = certificates.value.filter(cert => cert.id !== id)
      pagination.value.total -= 1
      
      // 清除当前证书（如果是被删除的证书）
      if (currentCertificate.value && currentCertificate.value.id === id) {
        currentCertificate.value = null
      }
      
      return true
    } catch (error) {
      console.error('删除证书失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取统计信息
  const fetchStatistics = async () => {
    try {
      const result = await getCertificateStatistics()
      statistics.value = result
      return result
    } catch (error) {
      console.error('获取统计信息失败:', error)
      throw error
    }
  }

  // 更新搜索参数
  const updateSearchParams = (params) => {
    searchParams.value = { ...searchParams.value, ...params }
  }

  // 更新分页参数
  const updatePagination = (params) => {
    pagination.value = { ...pagination.value, ...params }
  }

  // 搜索证书
  const searchCertificates = async (query, useApi = false) => {
    if (!query || query.trim() === '') {
      clearSearch()
      return
    }
    
    try {
      searchLoading.value = true
      lastSearchQuery.value = query.trim()
      searchParams.value.search = query.trim()
      
      if (useApi) {
        // 使用后端API搜索
        const searchOptions = {
          page: pagination.value.page,
          size: pagination.value.size,
          status: searchParams.value.status,
          sort: searchParams.value.sort
        }
        
        const result = await apiSearchCertificates(query.trim(), searchOptions)
        
        // 避免过时请求覆盖新结果
        if (query.trim() === lastSearchQuery.value) {
          searchResults.value = result.records || []
          pagination.value.total = result.total || 0
        }
      } else {
        // 执行本地搜索（前端过滤）
        const keyword = query.trim().toLowerCase()
        const results = certificates.value.filter(cert => {
          return (
            (cert.name && cert.name.toLowerCase().includes(keyword)) ||
            (cert.domain && cert.domain.toLowerCase().includes(keyword))
          )
        })
        
        // 避免过时请求覆盖新结果
        if (query.trim() === lastSearchQuery.value) {
          searchResults.value = results
        }
      }
    } catch (error) {
      console.error('搜索证书失败:', error)
      searchResults.value = []
      throw error
    } finally {
      searchLoading.value = false
    }
  }
  
  // 清除搜索
  const clearSearch = () => {
    searchParams.value.search = ''
    searchResults.value = []
    lastSearchQuery.value = ''
    searchLoading.value = false
  }
  
  // 更新搜索查询
  const updateSearchQuery = (query) => {
    if (!query || query.trim() === '') {
      clearSearch()
    } else {
      searchParams.value.search = query.trim()
    }
  }

  // 重置搜索条件
  const resetSearchParams = () => {
    searchParams.value = {
      keyword: '',
      status: '',
      sort: '',
      search: ''
    }
    searchResults.value = []
    lastSearchQuery.value = ''
    pagination.value.page = 1
  }

  // 设置排序配置
  const setSortConfig = (config) => {
    sortConfig.value = { ...sortConfig.value, ...config }
  }

  // 清除当前证书
  const clearCurrentCertificate = () => {
    currentCertificate.value = null
  }

  // 仪表板相关方法
  const fetchDashboardStats = async () => {
    if (isDashboardCacheValid.value) {
      return dashboardStats.value
    }
    
    try {
      statsLoading.value = true
      const result = await getDashboardStats()
      dashboardStats.value = result
      dashboardCacheTime.value = new Date()
      return result
    } catch (error) {
      console.error('获取仪表板统计失败:', error)
      throw error
    } finally {
      statsLoading.value = false
    }
  }
  
  const fetchExpiringCertificatesForDashboard = async (days = 7) => {
    try {
      const result = await getExpiringCertificatesForDashboard(days)
      expiringCertificates.value = result || []
      return result
    } catch (error) {
      console.error('获取即将过期证书失败:', error)
      expiringCertificates.value = []
      throw error
    }
  }
  
  const fetchRecentCertificatesForDashboard = async (limit = 5) => {
    try {
      const result = await getRecentCertificates(limit)
      recentCertificates.value = result || []
      return result
    } catch (error) {
      console.error('获取最近证书失败:', error)
      recentCertificates.value = []
      throw error
    }
  }
  
  const fetchDashboardData = async () => {
    try {
      statsLoading.value = true
      await Promise.all([
        fetchDashboardStats(),
        fetchExpiringCertificatesForDashboard(7),
        fetchRecentCertificatesForDashboard(5)
      ])
    } catch (error) {
      console.error('获取仪表板数据失败:', error)
      throw error
    } finally {
      statsLoading.value = false
    }
  }
  
  const invalidateDashboardCache = () => {
    dashboardCacheTime.value = null
  }

  // 刷新数据
  const refreshData = async () => {
    await Promise.all([
      fetchCertificateList(),
      fetchStatistics()
    ])
  }

  return {
    // 状态
    certificates,
    currentCertificate,
    statistics,
    loading,
    pagination,
    searchParams,
    sortConfig,
    searchLoading,
    searchResults,
    lastSearchQuery,
    
    // 仪表板相关状态
    dashboardStats,
    expiringCertificates,
    recentCertificates,
    statsLoading,
    dashboardCacheTime,
    
    // 计算属性
    certificateList,
    isLoading,
    paginationInfo,
    searchFilter,
    statsData,
    hasSearchQuery,
    isSearching,
    searchResultsCount,
    displayCertificates,
    
    // 仪表板相关计算属性
    totalCertificates,
    normalPercentage,
    criticalCertificatesCount,
    hasExpiringCertificates,
    isStatsLoading,
    isDashboardCacheValid,
    
    // 方法
    fetchCertificateList,
    fetchCertificateDetail,
    createNewCertificate,
    updateExistingCertificate,
    removeExistingCertificate,
    fetchStatistics,
    updateSearchParams,
    updatePagination,
    resetSearchParams,
    setSortConfig,
    clearCurrentCertificate,
    refreshData,
    searchCertificates,
    clearSearch,
    updateSearchQuery,
    
    // 仪表板相关方法
    fetchDashboardStats,
    fetchExpiringCertificatesForDashboard,
    fetchRecentCertificatesForDashboard,
    fetchDashboardData,
    invalidateDashboardCache
  }
})