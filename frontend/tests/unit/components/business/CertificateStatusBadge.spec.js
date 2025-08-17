import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { ElTag, ElIcon } from 'element-plus'
import { CheckCircle, WarningFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import CertificateStatusBadge from '@/components/business/CertificateStatusBadge.vue'

// Mock 日期工具函数
vi.mock('@/utils/date', () => ({
  formatDate: vi.fn((date) => {
    if (!date) return ''
    return '2024-12-31'
  }),
  getDaysFromToday: vi.fn((date) => {
    // 根据不同的日期返回不同的天数，用于测试
    if (date === '2024-12-31T00:00:00Z') return 30  // 30天后到期
    if (date === '2024-01-15T00:00:00Z') return 7   // 7天后到期
    if (date === '2023-12-01T00:00:00Z') return -30 // 已过期30天
    if (date === '2024-01-01T00:00:00Z') return 0   // 今天到期
    if (date === '2024-01-02T00:00:00Z') return 1   // 明天到期
    return 30
  })
}))

describe('CertificateStatusBadge.vue', () => {
  const createWrapper = (props) => {
    return mount(CertificateStatusBadge, {
      props,
      global: {
        components: {
          ElTag,
          ElIcon,
          CheckCircle,
          WarningFilled,
          CircleCloseFilled
        }
      }
    })
  }

  describe('正常状态证书', () => {
    it('should render normal status correctly', () => {
      const wrapper = createWrapper({
        status: 'NORMAL',
        expiryDate: '2024-12-31T00:00:00Z'
      })

      const tag = wrapper.findComponent(ElTag)
      expect(tag.exists()).toBe(true)
      expect(tag.props('type')).toBe('success')
      expect(tag.props('effect')).toBe('dark')
      
      expect(wrapper.text()).toContain('正常')
      expect(wrapper.text()).toContain('30 天后到期')
      expect(wrapper.text()).toContain('到期时间：2024-12-31')
    })

    it('should use correct icon for normal status', () => {
      const wrapper = createWrapper({
        status: 'NORMAL',
        expiryDate: '2024-12-31T00:00:00Z'
      })

      // 检查是否使用了正确的图标组件 
      // 注意：由于Vue组件的名称在测试中可能不可用，我们检查计算属性
      expect(wrapper.vm.statusConfig.icon).toBe(CheckCircle)
    })
  })

  describe('即将过期状态证书', () => {
    it('should render expiring soon status correctly', () => {
      const wrapper = createWrapper({
        status: 'EXPIRING_SOON',
        expiryDate: '2024-01-15T00:00:00Z'
      })

      const tag = wrapper.findComponent(ElTag)
      expect(tag.props('type')).toBe('warning')
      expect(tag.props('effect')).toBe('dark')
      
      expect(wrapper.text()).toContain('即将过期')
      expect(wrapper.text()).toContain('7 天后到期')
    })

    it('should use correct icon for expiring soon status', () => {
      const wrapper = createWrapper({
        status: 'EXPIRING_SOON',
        expiryDate: '2024-01-15T00:00:00Z'
      })

      expect(wrapper.vm.statusConfig.icon).toBe(WarningFilled)
    })
  })

  describe('已过期状态证书', () => {
    it('should render expired status correctly', () => {
      const wrapper = createWrapper({
        status: 'EXPIRED',
        expiryDate: '2023-12-01T00:00:00Z'
      })

      const tag = wrapper.findComponent(ElTag)
      expect(tag.props('type')).toBe('danger')
      expect(tag.props('effect')).toBe('dark')
      
      expect(wrapper.text()).toContain('已过期')
      expect(wrapper.text()).toContain('已过期 30 天')
    })

    it('should use correct icon for expired status', () => {
      const wrapper = createWrapper({
        status: 'EXPIRED',
        expiryDate: '2023-12-01T00:00:00Z'
      })

      expect(wrapper.vm.statusConfig.icon).toBe(CircleCloseFilled)
    })
  })

  describe('天数信息显示', () => {
    it('should show "今天到期" for today expiry', () => {
      const wrapper = createWrapper({
        status: 'EXPIRING_SOON',
        expiryDate: '2024-01-01T00:00:00Z'
      })

      expect(wrapper.text()).toContain('今天到期')
    })

    it('should show "明天到期" for tomorrow expiry', () => {
      const wrapper = createWrapper({
        status: 'EXPIRING_SOON',
        expiryDate: '2024-01-02T00:00:00Z'
      })

      expect(wrapper.text()).toContain('明天到期')
    })

    it('should show correct days for future expiry', () => {
      const wrapper = createWrapper({
        status: 'NORMAL',
        expiryDate: '2024-12-31T00:00:00Z'
      })

      expect(wrapper.text()).toContain('30 天后到期')
    })

    it('should show correct days for past expiry', () => {
      const wrapper = createWrapper({
        status: 'EXPIRED',
        expiryDate: '2023-12-01T00:00:00Z'
      })

      expect(wrapper.text()).toContain('已过期 30 天')
    })
  })

  describe('未知状态处理', () => {
    it('should handle unknown status gracefully', () => {
      const wrapper = createWrapper({
        status: 'UNKNOWN',
        expiryDate: '2024-12-31T00:00:00Z'
      })

      const tag = wrapper.findComponent(ElTag)
      expect(tag.props('type')).toBe('info')
      expect(tag.props('effect')).toBe('plain')
      
      expect(wrapper.text()).toContain('未知状态')
    })
  })

  describe('Props 验证', () => {
    it('should validate status prop correctly', () => {
      const { validator } = CertificateStatusBadge.props.status
      
      expect(validator('NORMAL')).toBe(true)
      expect(validator('EXPIRING_SOON')).toBe(true)
      expect(validator('EXPIRED')).toBe(true)
      expect(validator('INVALID')).toBe(false)
    })

    it('should require status and expiryDate props', () => {
      expect(CertificateStatusBadge.props.status.required).toBe(true)
      expect(CertificateStatusBadge.props.expiryDate.required).toBe(true)
    })
  })

  describe('样式和布局', () => {
    it('should apply correct CSS classes', () => {
      const wrapper = createWrapper({
        status: 'NORMAL',
        expiryDate: '2024-12-31T00:00:00Z'
      })

      expect(wrapper.find('.certificate-status-badge').exists()).toBe(true)
      expect(wrapper.find('.status-details').exists()).toBe(true)
      expect(wrapper.find('.days-info').exists()).toBe(true)
      expect(wrapper.find('.expiry-date').exists()).toBe(true)
    })

    it('should display status icon and text', () => {
      const wrapper = createWrapper({
        status: 'NORMAL',
        expiryDate: '2024-12-31T00:00:00Z'
      })

      const statusIcon = wrapper.find('.status-icon')
      const statusText = wrapper.find('.status-text')
      
      expect(statusIcon.exists()).toBe(true)
      expect(statusText.exists()).toBe(true)
      expect(statusText.text()).toBe('正常')
    })
  })

  describe('计算属性', () => {
    it('should compute status config correctly for all statuses', () => {
      const normalWrapper = createWrapper({
        status: 'NORMAL',
        expiryDate: '2024-12-31T00:00:00Z'
      })
      
      const warningWrapper = createWrapper({
        status: 'EXPIRING_SOON',
        expiryDate: '2024-01-15T00:00:00Z'
      })
      
      const dangerWrapper = createWrapper({
        status: 'EXPIRED',
        expiryDate: '2023-12-01T00:00:00Z'
      })

      // 验证不同状态的配置
      expect(normalWrapper.vm.statusConfig.type).toBe('success')
      expect(normalWrapper.vm.statusConfig.text).toBe('正常')
      
      expect(warningWrapper.vm.statusConfig.type).toBe('warning')
      expect(warningWrapper.vm.statusConfig.text).toBe('即将过期')
      
      expect(dangerWrapper.vm.statusConfig.type).toBe('danger')
      expect(dangerWrapper.vm.statusConfig.text).toBe('已过期')
    })
  })
})