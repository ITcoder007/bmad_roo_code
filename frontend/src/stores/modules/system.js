/**
 * 系统状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useSystemStore = defineStore('system', () => {
  // 状态
  const theme = ref('light')
  const language = ref('zh-cn')
  const sidebarCollapsed = ref(false)
  const breadcrumbs = ref([])
  const settings = ref({
    showNotifications: true,
    autoSave: true,
    refreshInterval: 30000
  })
  const systemHealth = ref({
    status: 'healthy',
    lastCheck: null,
    services: {}
  })

  // 计算属性
  const isDarkTheme = computed(() => theme.value === 'dark')
  const currentLanguage = computed(() => language.value)
  const isSidebarCollapsed = computed(() => sidebarCollapsed.value)
  const currentBreadcrumbs = computed(() => breadcrumbs.value)
  const systemSettings = computed(() => settings.value)
  const healthStatus = computed(() => systemHealth.value)

  // 切换主题
  const toggleTheme = () => {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    localStorage.setItem('theme', theme.value)
    
    // 应用主题到document
    document.documentElement.setAttribute('data-theme', theme.value)
  }

  // 设置主题
  const setTheme = (newTheme) => {
    theme.value = newTheme
    localStorage.setItem('theme', newTheme)
    document.documentElement.setAttribute('data-theme', newTheme)
  }

  // 切换语言
  const setLanguage = (lang) => {
    language.value = lang
    localStorage.setItem('language', lang)
  }

  // 切换侧边栏
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
    localStorage.setItem('sidebarCollapsed', sidebarCollapsed.value.toString())
  }

  // 设置侧边栏状态
  const setSidebarCollapsed = (collapsed) => {
    sidebarCollapsed.value = collapsed
    localStorage.setItem('sidebarCollapsed', collapsed.toString())
  }

  // 更新面包屑
  const updateBreadcrumbs = (newBreadcrumbs) => {
    breadcrumbs.value = newBreadcrumbs
  }

  // 添加面包屑项
  const addBreadcrumb = (breadcrumb) => {
    breadcrumbs.value.push(breadcrumb)
  }

  // 移除面包屑项
  const removeBreadcrumb = (index) => {
    breadcrumbs.value.splice(index, 1)
  }

  // 清除面包屑
  const clearBreadcrumbs = () => {
    breadcrumbs.value = []
  }

  // 更新设置
  const updateSettings = (newSettings) => {
    settings.value = { ...settings.value, ...newSettings }
    localStorage.setItem('systemSettings', JSON.stringify(settings.value))
  }

  // 更新系统健康状态
  const updateSystemHealth = (healthData) => {
    systemHealth.value = {
      ...systemHealth.value,
      ...healthData,
      lastCheck: new Date().toISOString()
    }
  }

  // 初始化系统设置
  const initializeSystem = () => {
    // 恢复主题设置
    const savedTheme = localStorage.getItem('theme')
    if (savedTheme) {
      setTheme(savedTheme)
    }

    // 恢复语言设置
    const savedLanguage = localStorage.getItem('language')
    if (savedLanguage) {
      language.value = savedLanguage
    }

    // 恢复侧边栏状态
    const savedSidebarState = localStorage.getItem('sidebarCollapsed')
    if (savedSidebarState !== null) {
      sidebarCollapsed.value = savedSidebarState === 'true'
    }

    // 恢复系统设置
    const savedSettings = localStorage.getItem('systemSettings')
    if (savedSettings) {
      try {
        settings.value = { ...settings.value, ...JSON.parse(savedSettings) }
      } catch (error) {
        console.error('解析系统设置失败:', error)
      }
    }
  }

  // 重置系统设置
  const resetSystemSettings = () => {
    theme.value = 'light'
    language.value = 'zh-cn'
    sidebarCollapsed.value = false
    settings.value = {
      showNotifications: true,
      autoSave: true,
      refreshInterval: 30000
    }
    
    // 清除本地存储
    localStorage.removeItem('theme')
    localStorage.removeItem('language')
    localStorage.removeItem('sidebarCollapsed')
    localStorage.removeItem('systemSettings')
    
    // 重新应用设置
    document.documentElement.setAttribute('data-theme', theme.value)
  }

  return {
    // 状态
    theme,
    language,
    sidebarCollapsed,
    breadcrumbs,
    settings,
    systemHealth,
    
    // 计算属性
    isDarkTheme,
    currentLanguage,
    isSidebarCollapsed,
    currentBreadcrumbs,
    systemSettings,
    healthStatus,
    
    // 方法
    toggleTheme,
    setTheme,
    setLanguage,
    toggleSidebar,
    setSidebarCollapsed,
    updateBreadcrumbs,
    addBreadcrumb,
    removeBreadcrumb,
    clearBreadcrumbs,
    updateSettings,
    updateSystemHealth,
    initializeSystem,
    resetSystemSettings
  }
})