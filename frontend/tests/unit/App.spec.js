import { mount } from '@vue/test-utils'
import App from '@/App.vue'

describe('App.vue', () => {
  it('renders the app component', () => {
    const wrapper = mount(App, {
      global: {
        stubs: ['router-view']
      }
    })
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('#app').exists()).toBe(true)
  })
})