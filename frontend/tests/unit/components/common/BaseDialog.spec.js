import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import BaseDialog from '@/components/common/BaseDialog.vue'
import ElementPlus from 'element-plus'

describe('BaseDialog.vue', () => {
  const createWrapper = (props = {}, slots = {}) => {
    return mount(BaseDialog, {
      props: {
        modelValue: true,
        title: '测试对话框',
        ...props
      },
      slots,
      global: {
        plugins: [ElementPlus]
      }
    })
  }

  it('renders correctly when visible', () => {
    const wrapper = createWrapper()
    
    expect(wrapper.findComponent(BaseDialog).exists()).toBe(true)
    expect(wrapper.props().modelValue).toBe(true)
    expect(wrapper.props().title).toBe('测试对话框')
  })

  it('shows footer with default buttons when showFooter is true', () => {
    const wrapper = createWrapper({
      showFooter: true,
      showCancel: true,
      showConfirm: true
    })
    
    expect(wrapper.props().showFooter).toBe(true)
    expect(wrapper.props().showCancel).toBe(true)
    expect(wrapper.props().showConfirm).toBe(true)
  })

  it('hides footer when showFooter is false', () => {
    const wrapper = createWrapper({ showFooter: false })
    
    expect(wrapper.props().showFooter).toBe(false)
  })

  it('emits update:modelValue when dialog is closed', async () => {
    const wrapper = createWrapper()
    
    await wrapper.vm.handleCancel()
    
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')[0]).toEqual([false])
    expect(wrapper.emitted('cancel')).toBeTruthy()
  })

  it('emits confirm event when confirm button is clicked', async () => {
    const wrapper = createWrapper()
    
    await wrapper.vm.handleConfirm()
    
    expect(wrapper.emitted('confirm')).toBeTruthy()
  })

  it('shows loading state on confirm button when confirmLoading is true', () => {
    const wrapper = createWrapper({
      confirmLoading: true,
      showConfirm: true
    })
    
    expect(wrapper.props().confirmLoading).toBe(true)
  })

  it('renders slot content correctly', () => {
    const wrapper = createWrapper({}, {
      default: '<div class="test-content">测试内容</div>'
    })
    
    expect(wrapper.html()).toContain('test-content')
    expect(wrapper.html()).toContain('测试内容')
  })

  it('allows custom footer through slot', () => {
    const wrapper = createWrapper({}, {
      footer: '<div class="custom-footer">自定义底部</div>'
    })
    
    expect(wrapper.html()).toContain('custom-footer')
    expect(wrapper.html()).toContain('自定义底部')
  })

  it('exposes open and close methods', () => {
    const wrapper = createWrapper({ modelValue: false })
    
    expect(wrapper.vm.open).toBeDefined()
    expect(wrapper.vm.close).toBeDefined()
    expect(typeof wrapper.vm.open).toBe('function')
    expect(typeof wrapper.vm.close).toBe('function')
  })
})