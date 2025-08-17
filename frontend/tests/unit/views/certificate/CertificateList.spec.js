import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { ElTable, ElTableColumn, ElPagination, ElSelect, ElOption, ElButton, ElTag, ElLink } from 'element-plus'
import CertificateList from '@/views/certificate/CertificateList.vue'
import BaseTable from '@/components/common/BaseTable.vue'
import { useCertificateStore } from '@/stores/modules/certificate'

// Mock vue-router
const mockPush = vi.fn()
const mockReplace = vi.fn()
const mockRoute = {
  query: {},
  path: '/certificates'
}

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush,
    replace: mockReplace
  }),
  useRoute: () => mockRoute
}))

// Mock Element Plus 消息组件
vi.mock('element-plus', async (importOriginal) => {
  const mod = await importOriginal()
  return {
    ...mod,
    ElMessage: {
      error: vi.fn(),
      success: vi.fn()
    },
    ElMessageBox: {
      confirm: vi.fn()
    }
  }
})

// Mock CertificateSearchBox 组件
vi.mock('@/components/business/CertificateSearchBox.vue', () => ({
  default: {
    name: 'CertificateSearchBox',
    template: '<div class="certificate-search-box-mock"></div>',
    props: ['modelValue', 'loading'],
    emits: ['update:modelValue', 'search', 'clear']
  }
}))

// Mock highlight utilities
vi.mock('@/utils/highlight', () => ({
  smartHighlight: vi.fn((text, query) => text)
}))

describe('CertificateList.vue', () => {
  let wrapper
  let certificateStore

  // 模拟证书数据
  const mockCertificates = [
    {
      id: 1,
      name: '测试证书1',
      domain: 'test1.example.com',
      issuer: '测试CA',
      expiryDate: '2024-12-31T00:00:00Z',
      status: 'NORMAL'
    },
    {
      id: 2,
      name: '测试证书2',
      domain: 'test2.example.com',
      issuer: '测试CA2',
      expiryDate: '2024-01-15T00:00:00Z',
      status: 'EXPIRING_SOON'
    },
    {
      id: 3,
      name: '测试证书3',
      domain: 'test3.example.com',
      issuer: '测试CA3',
      expiryDate: '2023-12-01T00:00:00Z',
      status: 'EXPIRED'
    }
  ]

  beforeEach(() => {
    // 设置 Pinia
    setActivePinia(createPinia())
    
    // 重置 mock
    mockPush.mockClear()
    
    // 挂载组件
    wrapper = mount(CertificateList, {
      global: {
        components: {
          BaseTable,
          ElTable,
          ElTableColumn,
          ElPagination,
          ElSelect,
          ElOption,
          ElButton,
          ElTag,
          ElLink
        },
        stubs: {
          'el-icon': true,
          'Refresh': true
        }
      }
    })

    // 获取 store 实例
    certificateStore = useCertificateStore()
    
    // 设置模拟数据
    certificateStore.certificates = mockCertificates
    certificateStore.loading = false
  })

  describe('组件渲染', () => {
    it('should render certificates correctly when data is loaded', async () => {
      await wrapper.vm.$nextTick()
      
      // 检查是否正确渲染证书数据
      expect(wrapper.text()).toContain('测试证书1')
      expect(wrapper.text()).toContain('test1.example.com')
      expect(wrapper.text()).toContain('测试CA')
    })

    it('should render filter section with status select', () => {
      const filterSection = wrapper.find('.filter-section')
      expect(filterSection.exists()).toBe(true)
      
      const statusSelect = wrapper.findComponent({ name: 'ElSelect' })
      expect(statusSelect.exists()).toBe(true)
    })

    it('should render BaseTable component', () => {
      const baseTable = wrapper.findComponent(BaseTable)
      expect(baseTable.exists()).toBe(true)
    })

    it('should render pagination component', () => {
      const pagination = wrapper.findComponent({ name: 'ElPagination' })
      expect(pagination.exists()).toBe(true)
    })
  })

  describe('数据筛选功能', () => {
    it('should filter certificates by status', async () => {
      // 直接修改 ref 值
      wrapper.vm.statusFilter = 'NORMAL'
      await wrapper.vm.$nextTick()

      // 检查计算属性是否正确筛选
      const filteredCerts = wrapper.vm.filteredCertificates
      expect(filteredCerts).toHaveLength(1)
      expect(filteredCerts[0].status).toBe('NORMAL')
    })

    it('should show all certificates when no filter applied', async () => {
      wrapper.vm.statusFilter = ''
      await wrapper.vm.$nextTick()

      const filteredCerts = wrapper.vm.filteredCertificates
      expect(filteredCerts).toHaveLength(3)
    })

    it('should reset to first page when filter changes', async () => {
      wrapper.vm.currentPage = 2
      await wrapper.vm.handleStatusChange()
      
      expect(wrapper.vm.currentPage).toBe(1)
    })
  })

  describe('排序功能', () => {
    it('should sort certificates by expiry date ascending', async () => {
      certificateStore.sortConfig = { prop: 'expiryDate', order: 'ascending' }
      await wrapper.vm.$nextTick()

      const sortedCerts = wrapper.vm.sortedCertificates
      expect(new Date(sortedCerts[0].expiryDate).getTime()).toBeLessThan(new Date(sortedCerts[1].expiryDate).getTime())
    })

    it('should sort certificates by status priority', async () => {
      certificateStore.sortConfig = { prop: 'status', order: 'descending' }
      await wrapper.vm.$nextTick()

      const sortedCerts = wrapper.vm.sortedCertificates
      // EXPIRED(3) > EXPIRING_SOON(2) > NORMAL(1) when descending
      expect(sortedCerts[0].status).toBe('EXPIRED')
    })

    it('should handle sort change event', async () => {
      const setSortConfigSpy = vi.spyOn(certificateStore, 'setSortConfig')
      
      await wrapper.vm.handleSortChange({ prop: 'name', order: 'ascending' })
      
      expect(setSortConfigSpy).toHaveBeenCalledWith({ prop: 'name', order: 'ascending' })
    })
  })

  describe('分页功能', () => {
    it('should calculate total certificates correctly', () => {
      expect(wrapper.vm.totalCertificates).toBe(3)
    })

    it('should paginate certificates correctly', async () => {
      wrapper.vm.pageSize = 2
      wrapper.vm.currentPage = 1
      await wrapper.vm.$nextTick()

      const paginatedCerts = wrapper.vm.paginatedCertificates
      expect(paginatedCerts).toHaveLength(2)
    })

    it('should handle page change', async () => {
      await wrapper.vm.handlePageChange(2)
      expect(wrapper.vm.currentPage).toBe(2)
    })
  })

  describe('操作按钮交互', () => {
    it('should navigate to details page when view button clicked', async () => {
      await wrapper.vm.viewDetails(1)
      expect(mockPush).toHaveBeenCalledWith('/certificates/1')
    })

    it('should navigate to edit page when edit button clicked', async () => {
      await wrapper.vm.editCertificate(1)
      expect(mockPush).toHaveBeenCalledWith('/certificates/1/edit')
    })

    it('should call delete function when delete button clicked', async () => {
      // Mock ElMessageBox.confirm to resolve
      const { ElMessageBox } = await import('element-plus')
      ElMessageBox.confirm.mockResolvedValue(true)
      
      const removeExistingCertificateSpy = vi.spyOn(certificateStore, 'removeExistingCertificate')
      removeExistingCertificateSpy.mockResolvedValue(true)
      
      await wrapper.vm.deleteCertificate(1)
      
      expect(removeExistingCertificateSpy).toHaveBeenCalledWith(1)
    })
  })

  describe('数据加载', () => {
    it('should load certificates on mounted', () => {
      const fetchCertificateListSpy = vi.spyOn(certificateStore, 'fetchCertificateList')
      fetchCertificateListSpy.mockResolvedValue([])
      
      // 重新挂载组件来触发 onMounted
      mount(CertificateList, {
        global: {
          components: {
            BaseTable,
            ElTable,
            ElTableColumn,
            ElPagination,
            ElSelect,
            ElOption,
            ElButton,
            ElTag,
            ElLink
          },
          stubs: {
            'el-icon': true,
            'Refresh': true
          }
        }
      })
      
      expect(fetchCertificateListSpy).toHaveBeenCalled()
    })

    it('should handle refresh button click', async () => {
      const fetchCertificateListSpy = vi.spyOn(certificateStore, 'fetchCertificateList')
      fetchCertificateListSpy.mockResolvedValue([])
      
      await wrapper.vm.handleRefresh()
      
      expect(fetchCertificateListSpy).toHaveBeenCalled()
    })
  })

  describe('辅助方法', () => {
    it('should format date correctly', () => {
      const dateStr = '2024-12-31T00:00:00Z'
      const formatted = wrapper.vm.formatDate(dateStr)
      expect(formatted).toMatch(/\d{1,2}\/\d{1,2}\/\d{4}/)
    })

    it('should return - for empty date', () => {
      expect(wrapper.vm.formatDate('')).toBe('-')
      expect(wrapper.vm.formatDate(null)).toBe('-')
    })

    it('should get correct status type', () => {
      expect(wrapper.vm.getStatusType('NORMAL')).toBe('success')
      expect(wrapper.vm.getStatusType('EXPIRING_SOON')).toBe('warning')
      expect(wrapper.vm.getStatusType('EXPIRED')).toBe('danger')
      expect(wrapper.vm.getStatusType('UNKNOWN')).toBe('info')
    })

    it('should get correct status text', () => {
      expect(wrapper.vm.getStatusText('NORMAL')).toBe('正常')
      expect(wrapper.vm.getStatusText('EXPIRING_SOON')).toBe('即将过期')
      expect(wrapper.vm.getStatusText('EXPIRED')).toBe('已过期')
      expect(wrapper.vm.getStatusText('UNKNOWN')).toBe('未知')
    })
  })
})