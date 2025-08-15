import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import MainContent from '@/components/layout/MainContent.vue'

describe('MainContent.vue', () => {
  it('renders correctly', () => {
    const wrapper = mount(MainContent, {
      props: {
        sidebarCollapsed: false
      }
    })
    
    expect(wrapper.find('.main-content').exists()).toBe(true)
    expect(wrapper.find('.content-container').exists()).toBe(true)
    expect(wrapper.find('.page-content').exists()).toBe(true)
  })

  it('applies sidebar-collapsed class when sidebar is collapsed', () => {
    const wrapper = mount(MainContent, {
      props: {
        sidebarCollapsed: true
      }
    })
    
    expect(wrapper.find('.main-content.sidebar-collapsed').exists()).toBe(true)
  })

  it('renders slot content correctly', () => {
    const wrapper = mount(MainContent, {
      props: {
        sidebarCollapsed: false
      },
      slots: {
        default: '<div class="test-content">Test Content</div>'
      }
    })
    
    expect(wrapper.find('.test-content').exists()).toBe(true)
    expect(wrapper.find('.test-content').text()).toBe('Test Content')
  })

  it('does not apply sidebar-collapsed class when sidebar is not collapsed', () => {
    const wrapper = mount(MainContent, {
      props: {
        sidebarCollapsed: false
      }
    })
    
    expect(wrapper.find('.main-content.sidebar-collapsed').exists()).toBe(false)
  })
})