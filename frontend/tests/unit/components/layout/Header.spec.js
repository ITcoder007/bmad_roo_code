import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import Header from '@/components/layout/Header.vue'
import ElementPlus from 'element-plus'

// 创建 mock 函数
const mockPush = vi.fn()
const mockRouter = { push: mockPush }
const mockRoute = { 
  path: '/', 
  matched: [{ meta: { title: '首页' }, path: '/' }] 
}

// Mock vue-router hooks
vi.mock('vue-router', async () => {
  const actual = await vi.importActual('vue-router')
  return {
    ...actual,
    useRouter: () => mockRouter,
    useRoute: () => mockRoute
  }
})

// Mock 全局配置
const global = {
  plugins: [ElementPlus],
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
    const wrapper = mount(Header, { global })

    // 清空之前的调用记录
    mockPush.mockClear()
    
    // Test profile action by calling the method directly
    await wrapper.vm.handleProfile()
    expect(mockPush).toHaveBeenCalledWith('/profile')
    
    // Test settings action
    await wrapper.vm.handleSettings()
    expect(mockPush).toHaveBeenCalledWith('/system/settings')
    
    // Check that the component exists and methods are callable
    expect(wrapper.findComponent(Header).exists()).toBe(true)
    expect(typeof wrapper.vm.handleProfile).toBe('function')
    expect(typeof wrapper.vm.handleSettings).toBe('function')
  })
})