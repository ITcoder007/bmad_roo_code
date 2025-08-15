import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import Loading from '@/components/common/Loading.vue'

describe('Loading.vue', () => {
  it('renders correctly when visible', () => {
    const wrapper = mount(Loading, {
      props: {
        visible: true,
        text: '加载中...'
      }
    })
    
    expect(wrapper.find('.loading-container').exists()).toBe(true)
    expect(wrapper.find('.loading-mask').exists()).toBe(true)
    expect(wrapper.find('.loading-spinner').exists()).toBe(true)
  })

  it('does not render when visible is false', () => {
    const wrapper = mount(Loading, {
      props: {
        visible: false
      }
    })
    
    expect(wrapper.find('.loading-container').exists()).toBe(false)
  })

  it('displays loading text correctly', () => {
    const wrapper = mount(Loading, {
      props: {
        visible: true,
        text: '正在加载数据...'
      }
    })
    
    expect(wrapper.find('.loading-text').exists()).toBe(true)
    expect(wrapper.find('.loading-text').text()).toBe('正在加载数据...')
  })

  it('hides loading text when text prop is empty', () => {
    const wrapper = mount(Loading, {
      props: {
        visible: true,
        text: ''
      }
    })
    
    expect(wrapper.find('.loading-text').exists()).toBe(false)
  })

  it('applies overlay class when overlay is true', () => {
    const wrapper = mount(Loading, {
      props: {
        visible: true,
        overlay: true
      }
    })
    
    expect(wrapper.find('.loading-overlay').exists()).toBe(true)
    expect(wrapper.find('.loading-inline').exists()).toBe(false)
  })

  it('applies inline class when overlay is false', () => {
    const wrapper = mount(Loading, {
      props: {
        visible: true,
        overlay: false
      }
    })
    
    expect(wrapper.find('.loading-inline').exists()).toBe(true)
    expect(wrapper.find('.loading-overlay').exists()).toBe(false)
  })

  it('applies correct size class', () => {
    const wrapper = mount(Loading, {
      props: {
        visible: true,
        size: 'large'
      }
    })
    
    expect(wrapper.find('.loading-large').exists()).toBe(true)
  })

  it('shows custom icon when icon prop is provided', () => {
    const wrapper = mount(Loading, {
      props: {
        visible: true,
        icon: 'Loading'
      }
    })
    
    expect(wrapper.find('.loading-icon').exists()).toBe(true)
    expect(wrapper.find('.default-spinner').exists()).toBe(false)
  })

  it('shows default spinner when no icon is provided', () => {
    const wrapper = mount(Loading, {
      props: {
        visible: true
      }
    })
    
    expect(wrapper.find('.default-spinner').exists()).toBe(true)
    expect(wrapper.find('.spinner-dots').exists()).toBe(true)
    expect(wrapper.find('.loading-icon').exists()).toBe(false)
  })
})