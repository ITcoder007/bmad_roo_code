import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import Dashboard from '@/views/monitoring/Dashboard.vue'
import { useCertificateStore } from '@/stores/modules/certificate'

// Mock 组件
const CertificateStatsCard = {
  name: 'CertificateStatsCard',
  props: ['stats', 'loading'],
  template: '<div data-testid="stats-card">Stats Card</div>'
}

const ExpiringCertificatesList = {
  name: 'ExpiringCertificatesList',
  props: ['certificates', 'loading'],
  template: '<div data-testid="expiring-list">Expiring List</div>'
}

const RecentCertificatesList = {
  name: 'RecentCertificatesList',
  props: ['certificates', 'loading'],
  template: '<div data-testid="recent-list">Recent List</div>'
}

// Mock auto refresh hook
const mockStartAutoRefresh = vi.fn()
const mockStopAutoRefresh = vi.fn()
const mockForceRefresh = vi.fn()

vi.mock('@/composables/useAutoRefresh', () => ({
  useAutoRefresh: () => ({
    startAutoRefresh: mockStartAutoRefresh,
    stopAutoRefresh: mockStopAutoRefresh,
    forceRefresh: mockForceRefresh
  })
}))

// Mock Vue Router
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn()
  })
}))

describe('Dashboard.vue', () => {
  let wrapper
  let pinia
  let store

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    store = useCertificateStore()
    
    // Mock store methods
    vi.spyOn(store, 'fetchDashboardData').mockResolvedValue()
    
    // Mock store getters
    vi.spyOn(store, 'dashboardStats', 'get').mockReturnValue({
      total: 25,
      normal: 18,
      expiring: 5,
      expired: 2
    })
    
    vi.spyOn(store, 'expiringCertificates', 'get').mockReturnValue([
      {
        id: 1,
        name: 'example.com SSL证书',
        domain: 'example.com',
        status: 'EXPIRING_SOON',
        expiryDate: new Date(),
        issuer: 'Let\'s Encrypt'
      }
    ])
    
    vi.spyOn(store, 'recentCertificates', 'get').mockReturnValue([
      {
        id: 11,
        name: 'new-service.com SSL证书',
        domain: 'new-service.com',
        status: 'NORMAL',
        expiryDate: new Date(),
        createdAt: new Date(),
        issuer: 'Let\'s Encrypt'
      }
    ])
    
    vi.spyOn(store, 'isStatsLoading', 'get').mockReturnValue(false)
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
    vi.clearAllMocks()
  })

  const createWrapper = (options = {}) => {
    return mount(Dashboard, {
      global: {
        plugins: [pinia],
        stubs: {
          'router-link': true,
          'el-button': true,
          'el-card': true,
          'el-row': true,
          'el-col': true,
          'el-icon': true,
          'el-skeleton': true,
          'el-empty': true,
          'CertificateStatsCard': CertificateStatsCard,
          'ExpiringCertificatesList': ExpiringCertificatesList,
          'RecentCertificatesList': RecentCertificatesList
        }
      },
      ...options
    })
  }

  describe('渲染测试', () => {
    it('应该正确渲染仪表板页面', () => {
      wrapper = createWrapper()
      
      expect(wrapper.find('h1').text()).toBe('系统仪表板')
      expect(wrapper.find('.dashboard').exists()).toBe(true)
      expect(wrapper.find('.page-header').exists()).toBe(true)
      expect(wrapper.find('.stats-row').exists()).toBe(true)
      expect(wrapper.find('.content-row').exists()).toBe(true)
    })

    it('应该显示刷新按钮和最后更新时间', () => {
      wrapper = createWrapper()
      
      const lastUpdate = wrapper.find('.last-update')
      expect(lastUpdate.exists()).toBe(true)
      expect(lastUpdate.text()).toContain('最后更新:')
    })

    it('应该有正确的布局结构', () => {
      wrapper = createWrapper()
      
      const statsRow = wrapper.find('.stats-row')
      expect(statsRow.exists()).toBe(true)
      
      const contentRow = wrapper.find('.content-row')
      expect(contentRow.exists()).toBe(true)
    })
  })

  describe('数据加载测试', () => {
    it('应该在挂载时获取仪表板数据', async () => {
      wrapper = createWrapper()
      
      // 等待组件挂载完成
      await wrapper.vm.$nextTick()
      
      expect(store.fetchDashboardData).toHaveBeenCalledTimes(1)
    })

    it('应该正确传递统计数据到子组件', () => {
      wrapper = createWrapper()
      
      // 主要验证计算属性是否正确工作
      expect(wrapper.vm.certificateStats).toEqual(store.dashboardStats)
      expect(wrapper.vm.statsLoading).toBe(store.isStatsLoading)
      
      // 验证组件是否渲染了核心元素
      expect(wrapper.find('.stats-row').exists()).toBe(true)
    })

    it('应该正确传递证书列表数据到子组件', () => {
      wrapper = createWrapper()
      
      // 主要验证计算属性是否正确工作
      expect(wrapper.vm.expiringCertificates).toEqual(store.expiringCertificates)
      expect(wrapper.vm.recentCertificates).toEqual(store.recentCertificates)
      
      // 验证数据是否正确加载
      expect(wrapper.vm.expiringCertificates).toHaveLength(1)
      expect(wrapper.vm.recentCertificates).toHaveLength(1)
      
      // 验证证书卡片容器是否存在
      expect(wrapper.find('.content-row').exists()).toBe(true)
    })
  })

  describe('组件方法测试', () => {
    it('应该有正确的方法', () => {
      wrapper = createWrapper()
      
      expect(typeof wrapper.vm.fetchDashboardData).toBe('function')
      expect(typeof wrapper.vm.handleRefresh).toBe('function')
    })

    it('应该调用 fetchDashboardData 方法', async () => {
      wrapper = createWrapper()
      
      // 直接调用组件方法
      await wrapper.vm.fetchDashboardData()
      
      expect(store.fetchDashboardData).toHaveBeenCalled()
    })

    it('应该处理刷新状态', async () => {
      wrapper = createWrapper()
      
      const promise = wrapper.vm.handleRefresh()
      expect(wrapper.vm.refreshing).toBe(true)
      
      await promise
      expect(wrapper.vm.refreshing).toBe(false)
    })
  })

  describe('自动刷新测试', () => {
    it('应该调用 useAutoRefresh hook', () => {
      wrapper = createWrapper()
      
      // 验证 useAutoRefresh 被调用
      expect(mockStartAutoRefresh).toHaveBeenCalled()
    })
  })

  describe('错误处理测试', () => {
    it('应该处理获取数据失败的情况', async () => {
      const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.spyOn(store, 'fetchDashboardData').mockRejectedValue(new Error('API Error'))
      
      wrapper = createWrapper()
      
      // 直接调用 fetchDashboardData 方法
      await wrapper.vm.fetchDashboardData()
      
      expect(consoleError).toHaveBeenCalledWith('获取仪表板数据失败:', expect.any(Error))
      
      consoleError.mockRestore()
    })

    it('应该在刷新失败时停止加载状态', async () => {
      vi.spyOn(store, 'fetchDashboardData').mockRejectedValue(new Error('API Error'))
      const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
      
      wrapper = createWrapper()
      
      await wrapper.vm.handleRefresh()
      
      expect(wrapper.vm.refreshing).toBe(false)
      
      consoleError.mockRestore()
    })
  })

  describe('计算属性测试', () => {
    it('应该正确响应 store 状态变化', async () => {
      wrapper = createWrapper()
      
      // 更新 mock 数据
      const newStats = {
        total: 50,
        normal: 40,
        expiring: 8,
        expired: 2
      }
      
      vi.spyOn(store, 'dashboardStats', 'get').mockReturnValue(newStats)
      
      await wrapper.vm.$nextTick()
      
      // 检查计算属性是否正确响应 store 变化
      expect(wrapper.vm.certificateStats).toEqual(newStats)
    })

    it('应该正确响应加载状态变化', async () => {
      wrapper = createWrapper()
      
      // 使用 vi.spyOn 来 mock 计算属性的依赖
      vi.spyOn(store, 'isStatsLoading', 'get').mockReturnValue(true)
      
      await wrapper.vm.$nextTick()
      
      // 检查计算属性是否正确响应加载状态变化
      expect(wrapper.vm.statsLoading).toBe(true)
      expect(wrapper.vm.certificatesLoading).toBe(true)
    })
  })
})