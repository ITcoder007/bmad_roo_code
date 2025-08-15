import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import Header from '@/components/layout/Header.vue'
import { createRouter, createWebHistory } from 'vue-router'

// 创建测试路由
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: { template: '<div>Home</div>' } }
  ]
})

// Mock 全局配置
const global = {
  plugins: [router],
  provide: {
    appConfig: {
      title: '测试应用',
      name: '证书管理系统'
    }
  }
}

describe('Header.vue', () => {
  it('renders correctly', () => {
    const wrapper = mount(Header, { global })
    
    expect(wrapper.find('.header').exists()).toBe(true)
    expect(wrapper.find('.app-title').text()).toBe('测试应用')
  })

  it('emits toggle-sidebar event when sidebar toggle button is clicked', async () => {
    const wrapper = mount(Header, { global })
    
    const toggleButton = wrapper.find('.sidebar-toggle')
    await toggleButton.trigger('click')
    
    expect(wrapper.emitted('toggle-sidebar')).toBeTruthy()
  })

  it('displays user information correctly', () => {
    const wrapper = mount(Header, { global })
    
    expect(wrapper.find('.username').text()).toBe('系统管理员')
    expect(wrapper.find('.user-avatar').exists()).toBe(true)
  })

  it('displays breadcrumbs when route has meta title', () => {
    const wrapper = mount(Header, { global })
    
    expect(wrapper.find('.header-center').exists()).toBe(true)
  })

  it('handles user dropdown menu actions', async () => {
    const mockPush = vi.fn()
    const wrapper = mount(Header, {
      global: {
        ...global,
        mocks: {
          $router: { push: mockPush }
        }
      }
    })

    // Test profile action
    await wrapper.vm.handleProfile()
    expect(mockPush).toHaveBeenCalledWith('/profile')

    // Test settings action
    await wrapper.vm.handleSettings()
    expect(mockPush).toHaveBeenCalledWith('/settings')
  })
})