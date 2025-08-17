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
  getCertificateStatistics
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
    sort: ''
  })
  const sortConfig = ref({
    prop: '',
    order: ''
  })

  // 计算属性
  const certificateList = computed(() => certificates.value)
  const isLoading = computed(() => loading.value)
  const paginationInfo = computed(() => pagination.value)
  const searchFilter = computed(() => searchParams.value)
  const statsData = computed(() => statistics.value)

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
      
      certificates.value = result.data || []
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

  // 重置搜索条件
  const resetSearchParams = () => {
    searchParams.value = {
      keyword: '',
      status: '',
      sort: ''
    }
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
    
    // 计算属性
    certificateList,
    isLoading,
    paginationInfo,
    searchFilter,
    statsData,
    
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
    refreshData
  }
})