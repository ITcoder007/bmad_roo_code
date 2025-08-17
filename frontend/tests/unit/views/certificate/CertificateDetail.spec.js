import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import CertificateDetail from '@/views/certificate/CertificateDetail.vue'
import { useCertificateStore } from '@/stores/modules/certificate'

// Mock vue-router
const mockPush = vi.fn()
const mockRoute = {
  params: { id: '1' }
}

vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal()
  return {
    ...actual,
    useRouter: () => ({
      push: mockPush
    }),
    useRoute: () => mockRoute
  }
})

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

// Mock 日期工具函数
vi.mock('@/utils/date', () => ({
  formatDate: vi.fn((date) => date ? '2024-01-01' : ''),
  formatDateTime: vi.fn((date) => date ? '2024-01-01 10:00:00' : ''),
  getDaysFromToday: vi.fn(() => 30)
}))

// Mock 组件
vi.mock('@/components/business/CertificateStatusBadge.vue', () => ({
  default: {
    template: '<div class="status-badge">Status Badge</div>',
    props: ['status', 'expiryDate']
  }
}))

vi.mock('@/components/common/Loading.vue', () => ({
  default: {
    template: '<div class="loading">Loading...</div>'
  }
}))

describe('CertificateDetail.vue', () => {
  let wrapper
  let certificateStore

  // 模拟证书数据
  const mockCertificate = {
    id: 1,
    name: '测试证书',
    domain: 'test.example.com',
    issuer: '测试CA',
    certificateType: 'SSL/TLS',
    issueDate: '2024-01-01T00:00:00Z',
    expiryDate: '2024-12-31T00:00:00Z',
    status: 'NORMAL',
    createdAt: '2024-01-01T10:00:00Z',
    updatedAt: '2024-01-01T10:00:00Z'
  }

  beforeEach(() => {
    // 设置 Pinia
    setActivePinia(createPinia())
    
    // 重置 mock
    mockPush.mockClear()
    
    // 挂载组件
    wrapper = mount(CertificateDetail, {
      global: {
        stubs: {
          'el-card': true,
          'el-breadcrumb': true,
          'el-breadcrumb-item': true,
          'el-descriptions': true,
          'el-descriptions-item': true,
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-select': true,
          'el-option': true,
          'el-date-picker': true,
          'el-button': true,
          'el-row': true,
          'el-col': true,
          'el-empty': true,
          'el-icon': true
        }
      }
    })

    // 获取 store 实例
    certificateStore = useCertificateStore()
  })

  describe('组件渲染', () => {
    it('should render loading state when loading is true', async () => {
      wrapper.vm.loading = true
      await wrapper.vm.$nextTick()
      
      expect(wrapper.find('.loading').exists()).toBe(true)
    })

    it('should render certificate details correctly when data is loaded', async () => {
      certificateStore.currentCertificate = mockCertificate
      wrapper.vm.loading = false
      await wrapper.vm.$nextTick()
      
      expect(wrapper.text()).toContain('测试证书')
    })

    it('should render breadcrumb navigation', () => {
      expect(wrapper.html()).toContain('el-breadcrumb')
    })

    it('should render error state when certificate is null', async () => {
      certificateStore.currentCertificate = null
      wrapper.vm.loading = false
      await wrapper.vm.$nextTick()
      
      expect(wrapper.html()).toContain('el-empty')
    })
  })

  describe('编辑模式功能', () => {
    beforeEach(async () => {
      certificateStore.currentCertificate = mockCertificate
      await wrapper.vm.$nextTick()
    })

    it('should toggle edit mode correctly', async () => {
      expect(wrapper.vm.editMode).toBe(false)
      
      wrapper.vm.toggleEditMode()
      
      expect(wrapper.vm.editMode).toBe(true)
      expect(wrapper.vm.editForm.name).toBe('测试证书')
      expect(wrapper.vm.editForm.domain).toBe('test.example.com')
    })

    it('should cancel edit mode correctly', async () => {
      wrapper.vm.editMode = true
      wrapper.vm.editForm = { name: '修改的名称' }
      
      wrapper.vm.cancelEdit()
      
      expect(wrapper.vm.editMode).toBe(false)
      expect(Object.keys(wrapper.vm.editForm)).toHaveLength(0)
    })

    it('should save changes when save button clicked', async () => {
      const updateSpy = vi.spyOn(certificateStore, 'updateExistingCertificate')
      updateSpy.mockResolvedValue(mockCertificate)
      
      wrapper.vm.editMode = true
      wrapper.vm.editForm = { ...mockCertificate, name: '修改的证书名称' }
      
      // Mock form validation
      wrapper.vm.formRef = {
        validate: vi.fn().mockResolvedValue(true)
      }
      
      await wrapper.vm.saveChanges()
      
      expect(updateSpy).toHaveBeenCalledWith(1, { ...mockCertificate, name: '修改的证书名称' })
      expect(wrapper.vm.editMode).toBe(false)
    })

    it('should handle save error gracefully', async () => {
      const { ElMessage } = await import('element-plus')
      const updateSpy = vi.spyOn(certificateStore, 'updateExistingCertificate')
      updateSpy.mockRejectedValue(new Error('更新失败'))
      
      wrapper.vm.editMode = true
      wrapper.vm.editForm = { ...mockCertificate }
      
      // Mock form validation
      wrapper.vm.formRef = {
        validate: vi.fn().mockResolvedValue(true)
      }
      
      await wrapper.vm.saveChanges()
      
      expect(ElMessage.error).toHaveBeenCalledWith('证书信息更新失败: 更新失败')
      expect(wrapper.vm.editMode).toBe(true) // 应该保持编辑模式
    })
  })

  describe('删除功能', () => {
    beforeEach(async () => {
      certificateStore.currentCertificate = mockCertificate
      await wrapper.vm.$nextTick()
    })

    it('should delete certificate and navigate back when confirmed', async () => {
      const { ElMessageBox, ElMessage } = await import('element-plus')
      ElMessageBox.confirm.mockResolvedValue(true)
      
      const deleteSpy = vi.spyOn(certificateStore, 'removeExistingCertificate')
      deleteSpy.mockResolvedValue(true)
      
      await wrapper.vm.handleDelete()
      
      expect(deleteSpy).toHaveBeenCalledWith(1)
      expect(ElMessage.success).toHaveBeenCalledWith('证书删除成功')
      expect(mockPush).toHaveBeenCalledWith('/certificates')
    })

    it('should handle delete cancellation', async () => {
      const { ElMessageBox } = await import('element-plus')
      ElMessageBox.confirm.mockRejectedValue('cancel')
      
      const deleteSpy = vi.spyOn(certificateStore, 'removeExistingCertificate')
      
      await wrapper.vm.handleDelete()
      
      expect(deleteSpy).not.toHaveBeenCalled()
    })

    it('should handle delete error', async () => {
      const { ElMessageBox, ElMessage } = await import('element-plus')
      ElMessageBox.confirm.mockResolvedValue(true)
      
      const deleteSpy = vi.spyOn(certificateStore, 'removeExistingCertificate')
      deleteSpy.mockRejectedValue(new Error('删除失败'))
      
      await wrapper.vm.handleDelete()
      
      expect(ElMessage.error).toHaveBeenCalledWith('证书删除失败')
    })
  })

  describe('导航功能', () => {
    it('should navigate back when goBack method called', () => {
      wrapper.vm.goBack()
      expect(mockPush).toHaveBeenCalledWith('/certificates')
    })
  })

  describe('数据加载', () => {
    it('should load certificate on mounted', () => {
      const fetchSpy = vi.spyOn(certificateStore, 'fetchCertificateDetail')
      fetchSpy.mockResolvedValue(mockCertificate)
      
      // 重新挂载组件来触发 onMounted
      mount(CertificateDetail, {
        global: {
          stubs: {
            'el-card': true,
            'el-breadcrumb': true,
            'el-breadcrumb-item': true,
            'el-descriptions': true,
            'el-descriptions-item': true,
            'el-form': true,
            'el-form-item': true,
            'el-input': true,
            'el-select': true,
            'el-option': true,
            'el-date-picker': true,
            'el-button': true,
            'el-row': true,
            'el-col': true,
            'el-empty': true,
            'el-icon': true
          }
        }
      })
      
      expect(fetchSpy).toHaveBeenCalledWith('1')
    })

    it('should handle load error gracefully', async () => {
      const { ElMessage } = await import('element-plus')
      const fetchSpy = vi.spyOn(certificateStore, 'fetchCertificateDetail')
      fetchSpy.mockRejectedValue(new Error('加载失败'))
      
      await wrapper.vm.loadCertificate()
      
      expect(ElMessage.error).toHaveBeenCalledWith('获取证书详情失败')
      expect(wrapper.vm.loading).toBe(false)
    })
  })

  describe('辅助方法', () => {
    it('should format certificate type correctly', () => {
      expect(wrapper.vm.formatCertificateType('SSL/TLS')).toBe('SSL/TLS证书')
      expect(wrapper.vm.formatCertificateType('CODE_SIGNING')).toBe('代码签名证书')
      expect(wrapper.vm.formatCertificateType('EMAIL')).toBe('邮件加密证书')
      expect(wrapper.vm.formatCertificateType('UNKNOWN')).toBe('UNKNOWN')
    })
  })

  describe('表单验证', () => {
    it('should have correct validation rules', () => {
      const rules = wrapper.vm.formRules
      
      // 检查证书名称验证规则
      expect(rules.name).toContainEqual(
        expect.objectContaining({
          required: true,
          message: '请输入证书名称'
        })
      )
      
      // 检查域名验证规则
      expect(rules.domain).toContainEqual(
        expect.objectContaining({
          required: true,
          message: '请输入域名'
        })
      )
      
      expect(rules.domain).toContainEqual(
        expect.objectContaining({
          pattern: /^[a-zA-Z0-9]([a-zA-Z0-9\-]*[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9\-]*[a-zA-Z0-9])?)*$/,
          message: '请输入有效的域名'
        })
      )
      
      // 检查必填字段验证
      expect(rules.issuer).toContainEqual(
        expect.objectContaining({
          required: true,
          message: '请输入颁发机构'
        })
      )
      
      expect(rules.certificateType).toContainEqual(
        expect.objectContaining({
          required: true,
          message: '请选择证书类型'
        })
      )
      
      expect(rules.issueDate).toContainEqual(
        expect.objectContaining({
          required: true,
          message: '请选择颁发日期'
        })
      )
      
      expect(rules.expiryDate).toContainEqual(
        expect.objectContaining({
          required: true,
          message: '请选择到期日期'
        })
      )
    })
  })
})