import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import Sidebar from '@/components/layout/Sidebar.vue'
import { createRouter, createWebHistory } from 'vue-router'

// 创建测试路由
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/dashboard', component: { template: '<div>Dashboard</div>' } },
    { path: '/certificates', component: { template: '<div>Certificates</div>' } }
  ]
})

describe('Sidebar.vue', () => {
  const defaultProps = {
    collapsed: false
  }

  it('renders correctly', () => {
    const wrapper = mount(Sidebar, {
      props: defaultProps,
      global: {
        plugins: [router]
      }
    })
    
    expect(wrapper.find('.sidebar').exists()).toBe(true)
    expect(wrapper.find('.sidebar-menu').exists()).toBe(true)
  })

  it('applies collapsed class when collapsed prop is true', () => {
    const wrapper = mount(Sidebar, {
      props: { collapsed: true },
      global: {
        plugins: [router]
      }
    })
    
    expect(wrapper.find('.sidebar.is-collapsed').exists()).toBe(true)
  })

  it('emits update:collapsed when collapse button is clicked', async () => {
    const wrapper = mount(Sidebar, {
      props: defaultProps,
      global: {
        plugins: [router]
      }
    })
    
    const collapseBtn = wrapper.find('.collapse-btn')
    await collapseBtn.trigger('click')
    
    expect(wrapper.emitted('update:collapsed')).toBeTruthy()
    expect(wrapper.emitted('update:collapsed')[0]).toEqual([true])
  })

  it('displays correct menu items', () => {
    const wrapper = mount(Sidebar, {
      props: defaultProps,
      global: {
        plugins: [router]
      }
    })
    
    const menuItems = wrapper.findAll('.el-menu-item')
    expect(menuItems.length).toBeGreaterThan(0)
    
    // Check for dashboard menu item
    const dashboardItem = wrapper.find('[index="/dashboard"]')
    expect(dashboardItem.exists()).toBe(true)
  })

  it('shows correct active menu based on current route', async () => {
    await router.push('/dashboard')
    
    const wrapper = mount(Sidebar, {
      props: defaultProps,
      global: {
        plugins: [router]
      }
    })
    
    expect(wrapper.vm.activeMenu).toBe('/dashboard')
  })
})