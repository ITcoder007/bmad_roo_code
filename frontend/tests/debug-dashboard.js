import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import Dashboard from '../src/views/monitoring/Dashboard.vue'
import { useCertificateStore } from '../src/stores/modules/certificate'

// Mock components
const CertificateStatsCard = {
  name: 'CertificateStatsCard',
  props: ['stats', 'loading'],
  template: '<div data-testid="stats-card">Stats Card</div>'
}

// Mock auto refresh hook
const mockUseAutoRefresh = () => ({
  startAutoRefresh: () => {},
  stopAutoRefresh: () => {}
})

// Mock Vue Router
const mockUseRouter = () => ({
  push: () => {}
})

// Set up pinia
const pinia = createPinia()
setActivePinia(pinia)

// Create wrapper
const wrapper = mount(Dashboard, {
  global: {
    plugins: [pinia],
    components: {
      CertificateStatsCard
    },
    mocks: {
      useAutoRefresh: mockUseAutoRefresh,
      useRouter: mockUseRouter
    },
    stubs: {
      'router-link': true,
      'el-button': true,
      'el-card': true,
      'el-row': true,
      'el-col': true,
      'el-icon': true
    }
  }
})

console.log('Dashboard HTML:', wrapper.html())
console.log('Stats card exists:', wrapper.find('[data-testid="stats-card"]').exists())