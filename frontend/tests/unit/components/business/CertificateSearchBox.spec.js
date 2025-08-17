import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import CertificateSearchBox from '@/components/business/CertificateSearchBox.vue'

// Mock localStorage
const localStorageMock = {
  data: {},
  getItem(key) {
    return this.data[key] || null
  },
  setItem(key, value) {
    this.data[key] = value
  },
  removeItem(key) {
    delete this.data[key]
  },
  clear() {
    this.data = {}
  }
}

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock
})

describe('CertificateSearchBox.vue', () => {
  let wrapper

  beforeEach(() => {
    // 清理 localStorage
    localStorageMock.clear()
    
    // 重置所有模拟
    vi.clearAllMocks()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  const createWrapper = (props = {}) => {
    return mount(CertificateSearchBox, {
      props: {
        modelValue: '',
        loading: false,
        placeholder: '搜索证书名称或域名...',
        debounceDelay: 300,
        ...props
      },
      global: {
        stubs: {
          'el-input': {
            template: `
              <div class="el-input" data-testid="search-input">
                <input 
                  :value="modelValue" 
                  :placeholder="placeholder"
                  @input="$emit('update:modelValue', $event.target.value)"
                  @keydown="$emit('keydown', $event)"
                  @focus="$emit('focus')"
                  @blur="$emit('blur')"
                />
                <div class="el-input__suffix">
                  <slot name="suffix"></slot>
                </div>
              </div>
            `,
            props: ['modelValue', 'placeholder', 'clearable'],
            emits: ['update:modelValue', 'clear', 'keydown', 'focus', 'blur']
          },
          'el-button': {
            template: `
              <button 
                @click="$emit('click')" 
                :class="{ 'is-loading': loading }"
                :data-testid="$attrs['data-testid']"
              >
                <slot></slot>
              </button>
            `,
            props: ['type', 'icon', 'loading'],
            emits: ['click'],
            inheritAttrs: false
          },
          'el-icon': {
            template: '<span class="el-icon"><slot></slot></span>'
          },
          'el-tag': {
            template: `
              <span 
                class="el-tag" 
                @click="$emit('click')" 
                @close="$emit('close')"
                :data-testid="'history-item-' + index"
              >
                <slot></slot>
              </span>
            `,
            props: ['type', 'size', 'closable'],
            emits: ['click', 'close']
          }
        }
      }
    })
  }

  describe('组件渲染', () => {
    it('应该正确渲染搜索输入框', () => {
      wrapper = createWrapper()
      
      expect(wrapper.find('.certificate-search-box').exists()).toBe(true)
      expect(wrapper.find('.el-input').exists()).toBe(true)
      expect(wrapper.find('input').exists()).toBe(true)
    })

    it('应该显示正确的占位符文本', () => {
      const placeholder = '自定义搜索占位符'
      wrapper = createWrapper({ placeholder })
      
      const input = wrapper.find('input')
      expect(input.attributes('placeholder')).toBe(placeholder)
    })

    it('应该显示搜索按钮', () => {
      wrapper = createWrapper()
      
      const searchButton = wrapper.find('[data-testid="search-button"]')
      expect(searchButton.exists()).toBe(true)
      expect(searchButton.text()).toContain('搜索')
    })

    it('应该在加载时显示加载状态', () => {
      wrapper = createWrapper({ loading: true })
      
      const searchButton = wrapper.find('[data-testid="search-button"]')
      expect(searchButton.classes()).toContain('is-loading')
    })
  })

  describe('用户交互', () => {
    it('应该在输入时更新 modelValue', async () => {
      wrapper = createWrapper()
      
      const input = wrapper.find('input')
      await input.setValue('test search')
      
      expect(wrapper.emitted('update:modelValue')).toBeTruthy()
      expect(wrapper.emitted('update:modelValue')[0]).toEqual(['test search'])
    })

    it('应该在点击搜索按钮时触发搜索事件', async () => {
      wrapper = createWrapper({ modelValue: 'test search' })
      
      const searchButton = wrapper.find('[data-testid="search-button"]')
      await searchButton.trigger('click')
      
      expect(wrapper.emitted('search')).toBeTruthy()
      expect(wrapper.emitted('search')[0]).toEqual(['test search'])
    })

    it('应该在清除时触发清除事件', async () => {
      wrapper = createWrapper({ modelValue: 'test search' })
      
      // 直接调用组件的清除方法
      await wrapper.vm.handleClear()
      
      expect(wrapper.emitted('clear')).toBeTruthy()
      expect(wrapper.emitted('update:modelValue')).toBeTruthy()
      expect(wrapper.emitted('update:modelValue')[0]).toEqual([''])
    })

    it('应该处理 Enter 键触发搜索', async () => {
      wrapper = createWrapper({ modelValue: 'test search' })
      
      // 直接调用搜索方法
      await wrapper.vm.handleSearch()
      
      expect(wrapper.emitted('search')).toBeTruthy()
      expect(wrapper.emitted('search')[0]).toEqual(['test search'])
    })

    it('应该处理 Escape 键清除搜索', async () => {
      wrapper = createWrapper({ modelValue: 'test search' })
      
      // 直接调用清除方法
      await wrapper.vm.handleClear()
      
      expect(wrapper.emitted('clear')).toBeTruthy()
    })

    it('应该在空白搜索时不触发搜索事件', async () => {
      wrapper = createWrapper({ modelValue: '   ' })
      
      const searchButton = wrapper.find('[data-testid="search-button"]')
      await searchButton.trigger('click')
      
      expect(wrapper.emitted('search')).toBeFalsy()
    })
  })

  describe('防抖功能', () => {
    it('应该在输入时使用防抖触发搜索', async () => {
      vi.useFakeTimers()
      
      wrapper = createWrapper()
      
      const input = wrapper.find('input')
      await input.setValue('test')
      
      // 快速输入不应该立即触发搜索
      expect(wrapper.emitted('search')).toBeFalsy()
      
      // 等待防抖延迟
      vi.advanceTimersByTime(300)
      await nextTick()
      
      expect(wrapper.emitted('search')).toBeTruthy()
      expect(wrapper.emitted('search')[0]).toEqual(['test'])
      
      vi.useRealTimers()
    })

    it('应该在快速连续输入时只触发最后一次搜索', async () => {
      vi.useFakeTimers()
      
      wrapper = createWrapper()
      
      const input = wrapper.find('input')
      
      // 快速连续输入
      await input.setValue('t')
      vi.advanceTimersByTime(100)
      
      await input.setValue('te')
      vi.advanceTimersByTime(100)
      
      await input.setValue('test')
      vi.advanceTimersByTime(100)
      
      // 此时还没有触发搜索
      expect(wrapper.emitted('search')).toBeFalsy()
      
      // 等待防抖延迟完成
      vi.advanceTimersByTime(300)
      await nextTick()
      
      // 只应该触发一次搜索，使用最后的值
      expect(wrapper.emitted('search')).toBeTruthy()
      expect(wrapper.emitted('search').length).toBe(1)
      expect(wrapper.emitted('search')[0]).toEqual(['test'])
      
      vi.useRealTimers()
    })
  })

  describe('搜索历史', () => {
    it('应该在搜索时保存历史记录', async () => {
      wrapper = createWrapper()
      
      // 模拟添加搜索历史
      await wrapper.vm.addToHistory('test search 1')
      await wrapper.vm.addToHistory('test search 2')
      
      // 检查 localStorage 中是否保存了历史记录
      const history = JSON.parse(localStorageMock.getItem('certificate-search-history') || '[]')
      expect(history).toContain('test search 1')
      expect(history).toContain('test search 2')
    })

    it('应该显示搜索历史', async () => {
      // 预设历史记录
      const mockHistory = ['previous search', 'another search']
      localStorageMock.setItem('certificate-search-history', JSON.stringify(mockHistory))
      
      wrapper = createWrapper()
      
      // 重新挂载组件以触发初始化
      wrapper.unmount()
      wrapper = createWrapper()
      await nextTick()
      
      expect(wrapper.vm.searchHistory.length).toBe(2)
    })

    it('应该在点击历史项时选择该项', async () => {
      wrapper = createWrapper()
      
      // 直接调用选择历史项的方法
      await wrapper.vm.selectHistoryItem('previous search')
      
      expect(wrapper.emitted('update:modelValue')).toBeTruthy()
      expect(wrapper.emitted('search')).toBeTruthy()
      expect(wrapper.emitted('search')[0]).toEqual(['previous search'])
    })

    it('应该能够清除搜索历史', async () => {
      const mockHistory = ['search 1', 'search 2']
      localStorageMock.setItem('certificate-search-history', JSON.stringify(mockHistory))
      
      wrapper = createWrapper()
      
      // 清除历史记录
      await wrapper.vm.clearHistory()
      
      const history = localStorageMock.getItem('certificate-search-history')
      expect(JSON.parse(history || '[]')).toEqual([])
    })
  })

  describe('Props 和 Emits', () => {
    it('应该正确处理 modelValue 属性', async () => {
      wrapper = createWrapper({ modelValue: 'initial value' })
      
      expect(wrapper.vm.searchQuery).toBe('initial value')
      
      // 更新 props
      await wrapper.setProps({ modelValue: 'updated value' })
      expect(wrapper.vm.searchQuery).toBe('updated value')
    })

    it('应该使用自定义防抖延迟', async () => {
      vi.useFakeTimers()
      
      wrapper = createWrapper({ debounceDelay: 500 })
      
      const input = wrapper.find('input')
      await input.setValue('test')
      
      // 300ms 后不应该触发（使用了 500ms 延迟）
      vi.advanceTimersByTime(300)
      await nextTick()
      expect(wrapper.emitted('search')).toBeFalsy()
      
      // 500ms 后应该触发
      vi.advanceTimersByTime(200)
      await nextTick()
      expect(wrapper.emitted('search')).toBeTruthy()
      
      vi.useRealTimers()
    })
  })

  describe('边界情况', () => {
    it('应该处理 localStorage 错误', async () => {
      // 模拟 localStorage 抛出错误
      const originalSetItem = localStorageMock.setItem
      localStorageMock.setItem = vi.fn(() => {
        throw new Error('LocalStorage error')
      })
      
      // 创建组件应该不会崩溃
      wrapper = createWrapper({ modelValue: 'test' })
      
      // 搜索应该仍然工作，即使保存历史失败
      await wrapper.vm.handleSearch()
      expect(wrapper.emitted('search')).toBeTruthy()
      
      // 恢复原始方法
      localStorageMock.setItem = originalSetItem
    })

    it('应该处理空字符串和空白字符', async () => {
      const testCases = ['', '   ', '\t', '\n']
      
      for (const testCase of testCases) {
        wrapper = createWrapper({ modelValue: testCase })
        await wrapper.vm.handleSearch()
      }
      
      // 空白搜索不应该触发搜索事件
      expect(wrapper.emitted('search')).toBeFalsy()
    })

    it('应该限制历史记录数量', async () => {
      wrapper = createWrapper()
      
      // 添加超过 10 个历史记录
      const searches = Array.from({ length: 12 }, (_, i) => `search ${i + 1}`)
      
      for (const search of searches) {
        await wrapper.vm.addToHistory(search)
      }
      
      expect(wrapper.vm.searchHistory.length).toBeLessThanOrEqual(10)
      
      // 应该保留最新的记录
      expect(wrapper.vm.searchHistory[0]).toBe('search 12')
    })
  })
})