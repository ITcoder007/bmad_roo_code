import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import BaseDialog from '@/components/common/BaseDialog.vue'

describe('BaseDialog.vue', () => {
  it('renders correctly when visible', () => {
    const wrapper = mount(BaseDialog, {
      props: {
        modelValue: true,
        title: '测试对话框'
      }
    })
    
    expect(wrapper.find('.el-dialog').exists()).toBe(true)
  })

  it('shows footer with default buttons when showFooter is true', () => {
    const wrapper = mount(BaseDialog, {
      props: {
        modelValue: true,
        title: '测试对话框',
        showFooter: true,
        showCancel: true,
        showConfirm: true
      }
    })
    
    expect(wrapper.find('.dialog-footer').exists()).toBe(true)
    const buttons = wrapper.findAll('.el-button')
    expect(buttons.length).toBe(2) // 取消和确定按钮
  })

  it('hides footer when showFooter is false', () => {
    const wrapper = mount(BaseDialog, {
      props: {
        modelValue: true,
        title: '测试对话框',
        showFooter: false
      }
    })
    
    expect(wrapper.find('.dialog-footer').exists()).toBe(false)
  })

  it('emits update:modelValue when dialog is closed', async () => {
    const wrapper = mount(BaseDialog, {
      props: {
        modelValue: true,
        title: '测试对话框'
      }
    })
    
    await wrapper.vm.handleCancel()
    
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')[0]).toEqual([false])
    expect(wrapper.emitted('cancel')).toBeTruthy()
  })

  it('emits confirm event when confirm button is clicked', async () => {
    const wrapper = mount(BaseDialog, {
      props: {
        modelValue: true,
        title: '测试对话框'
      }
    })
    
    await wrapper.vm.handleConfirm()
    
    expect(wrapper.emitted('confirm')).toBeTruthy()
  })

  it('shows loading state on confirm button when confirmLoading is true', () => {
    const wrapper = mount(BaseDialog, {
      props: {
        modelValue: true,
        title: '测试对话框',
        confirmLoading: true,
        showConfirm: true
      }
    })
    
    const confirmButton = wrapper.find('.el-button--primary')
    expect(confirmButton.attributes()).toHaveProperty('loading')
  })

  it('renders slot content correctly', () => {
    const wrapper = mount(BaseDialog, {
      props: {
        modelValue: true,
        title: '测试对话框'
      },
      slots: {
        default: '<div class="test-content">测试内容</div>'
      }
    })
    
    expect(wrapper.find('.test-content').exists()).toBe(true)
    expect(wrapper.find('.test-content').text()).toBe('测试内容')
  })

  it('allows custom footer through slot', () => {
    const wrapper = mount(BaseDialog, {
      props: {
        modelValue: true,
        title: '测试对话框'
      },
      slots: {
        footer: '<div class="custom-footer">自定义底部</div>'
      }
    })
    
    expect(wrapper.find('.custom-footer').exists()).toBe(true)
    expect(wrapper.find('.custom-footer').text()).toBe('自定义底部')
  })

  it('exposes open and close methods', () => {
    const wrapper = mount(BaseDialog, {
      props: {
        modelValue: false,
        title: '测试对话框'
      }
    })
    
    expect(wrapper.vm.open).toBeDefined()
    expect(wrapper.vm.close).toBeDefined()
  })
})