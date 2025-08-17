import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ElCard, ElSkeleton, ElIcon } from 'element-plus'
import CertificateStatsCard from '@/components/business/CertificateStatsCard.vue'

// Mock ECharts
const mockChart = {
  setOption: vi.fn(),
  resize: vi.fn(),
  dispose: vi.fn()
}

vi.mock('echarts', () => ({
  init: vi.fn(() => mockChart),
  default: {
    init: vi.fn(() => mockChart)
  }
}))

describe('CertificateStatsCard.vue', () => {
  let wrapper

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
    vi.clearAllMocks()
  })

  const createWrapper = (props = {}) => {
    return mount(CertificateStatsCard, {
      props: {
        stats: {
          total: 25,
          normal: 18,
          expiring: 5,
          expired: 2
        },
        loading: false,
        ...props
      },
      global: {
        components: {
          ElCard,
          ElSkeleton,
          ElIcon
        },
        stubs: {
          'el-skeleton': true
        }
      }
    })
  }

  describe('渲染测试', () => {
    it('应该正确渲染统计卡片', () => {
      wrapper = createWrapper()
      
      expect(wrapper.find('.stats-card').exists()).toBe(true)
      expect(wrapper.find('.stats-header h3').text()).toBe('证书概览')
      expect(wrapper.find('.stats-icon').exists()).toBe(true)
    })

    it('应该显示正确的统计数据', () => {
      wrapper = createWrapper()
      
      expect(wrapper.find('.count-number').text()).toBe('25')
      expect(wrapper.find('.count-label').text()).toBe('证书总数')
      
      const statusItems = wrapper.findAll('.status-item')
      expect(statusItems[0].find('.status-count').text()).toBe('18')
      expect(statusItems[0].find('.status-label').text()).toBe('正常')
      
      expect(statusItems[1].find('.status-count').text()).toBe('5')
      expect(statusItems[1].find('.status-label').text()).toBe('即将过期')
      
      expect(statusItems[2].find('.status-count').text()).toBe('2')
      expect(statusItems[2].find('.status-label').text()).toBe('已过期')
    })

    it('应该在加载时显示骨架屏', () => {
      wrapper = createWrapper({ loading: true })
      
      expect(wrapper.find('.loading-container').exists()).toBe(true)
      expect(wrapper.find('.stats-content').exists()).toBe(false)
    })

    it('应该应用正确的状态颜色样式', () => {
      wrapper = createWrapper()
      
      const statusItems = wrapper.findAll('.status-item')
      expect(statusItems[0].classes()).toContain('status-normal')
      expect(statusItems[1].classes()).toContain('status-expiring')
      expect(statusItems[2].classes()).toContain('status-expired')
    })
  })

  describe('图表测试', () => {
    it('应该在挂载时初始化图表', async () => {
      const echarts = await import('echarts')
      
      wrapper = createWrapper()
      await wrapper.vm.$nextTick()
      
      expect(echarts.init).toHaveBeenCalled()
      expect(mockChart.setOption).toHaveBeenCalled()
    })

    it('应该在数据变化时更新图表', async () => {
      wrapper = createWrapper()
      await wrapper.vm.$nextTick()
      
      // 清除之前的调用
      mockChart.setOption.mockClear()
      
      // 更新 props
      await wrapper.setProps({
        stats: {
          total: 30,
          normal: 20,
          expiring: 8,
          expired: 2
        }
      })
      
      await wrapper.vm.$nextTick()
      
      expect(mockChart.setOption).toHaveBeenCalled()
    })

    it('应该在组件卸载时销毁图表', async () => {
      wrapper = createWrapper()
      await wrapper.vm.$nextTick()
      
      // 确保图表已初始化
      expect(mockChart.setOption).toHaveBeenCalled()
      
      wrapper.unmount()
      
      expect(mockChart.dispose).toHaveBeenCalled()
    })

    it('应该正确生成图表数据', () => {
      wrapper = createWrapper()
      
      const chartData = wrapper.vm.chartData
      expect(chartData).toEqual([
        { name: '正常', value: 18, color: '#67C23A' },
        { name: '即将过期', value: 5, color: '#E6A23C' },
        { name: '已过期', value: 2, color: '#F56C6C' }
      ])
    })
  })

  describe('Props 测试', () => {
    it('应该使用默认的统计数据', () => {
      wrapper = mount(CertificateStatsCard, {
        global: {
          components: { ElCard, ElSkeleton, ElIcon },
          stubs: { 'el-skeleton': true }
        }
      })
      
      expect(wrapper.find('.count-number').text()).toBe('0')
      
      const statusItems = wrapper.findAll('.status-item')
      statusItems.forEach(item => {
        expect(item.find('.status-count').text()).toBe('0')
      })
    })

    it('应该处理空的统计数据', () => {
      wrapper = createWrapper({ stats: {} })
      
      expect(wrapper.find('.count-number').text()).toBe('0')
      
      const statusItems = wrapper.findAll('.status-item')
      statusItems.forEach(item => {
        expect(item.find('.status-count').text()).toBe('0')
      })
    })

    it('应该正确响应 loading 状态变化', async () => {
      wrapper = createWrapper({ loading: false })
      
      expect(wrapper.find('.loading-container').exists()).toBe(false)
      expect(wrapper.find('.stats-content').exists()).toBe(true)
      
      await wrapper.setProps({ loading: true })
      
      expect(wrapper.find('.loading-container').exists()).toBe(true)
      expect(wrapper.find('.stats-content').exists()).toBe(false)
    })
  })

  describe('窗口大小变化测试', () => {
    it('应该在窗口大小变化时调整图表', async () => {
      wrapper = createWrapper()
      await wrapper.vm.$nextTick()
      
      // 确保图表已初始化
      expect(mockChart.setOption).toHaveBeenCalled()
      
      // 模拟窗口大小变化事件
      window.dispatchEvent(new Event('resize'))
      
      expect(mockChart.resize).toHaveBeenCalled()
    })

    it('应该在组件卸载时移除窗口事件监听', () => {
      const removeEventListener = vi.spyOn(window, 'removeEventListener')
      
      wrapper = createWrapper()
      wrapper.unmount()
      
      expect(removeEventListener).toHaveBeenCalledWith('resize', expect.any(Function))
      
      removeEventListener.mockRestore()
    })
  })

  describe('图表配置测试', () => {
    it('应该使用正确的图表配置', async () => {
      wrapper = createWrapper()
      await wrapper.vm.$nextTick()
      
      expect(mockChart.setOption).toHaveBeenCalledWith(
        expect.objectContaining({
          tooltip: expect.objectContaining({
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          }),
          series: expect.arrayContaining([
            expect.objectContaining({
              name: '证书状态分布',
              type: 'pie',
              radius: ['40%', '70%'],
              center: ['50%', '50%']
            })
          ])
        })
      )
    })

    it('应该为每个状态使用正确的颜色', () => {
      wrapper = createWrapper()
      
      const chartData = wrapper.vm.chartData
      expect(chartData[0].color).toBe('#67C23A') // 正常 - 绿色
      expect(chartData[1].color).toBe('#E6A23C') // 即将过期 - 橙色
      expect(chartData[2].color).toBe('#F56C6C') // 已过期 - 红色
    })
  })

  describe('边界情况测试', () => {
    it('应该处理图表容器不存在的情况', () => {
      // Mock chartRef.value 为 null
      wrapper = createWrapper()
      wrapper.vm.$refs.chartRef = null
      
      expect(() => {
        wrapper.vm.initChart()
      }).not.toThrow()
    })

    it('应该处理图表实例不存在的情况', () => {
      wrapper = createWrapper()
      wrapper.vm.chartInstance = null
      
      expect(() => {
        wrapper.vm.updateChart()
        wrapper.vm.resizeChart()
        wrapper.vm.destroyChart()
      }).not.toThrow()
    })

    it('应该处理极大数值的统计数据', () => {
      wrapper = createWrapper({
        stats: {
          total: 999999,
          normal: 888888,
          expiring: 77777,
          expired: 33334
        }
      })
      
      expect(wrapper.find('.count-number').text()).toBe('999999')
      
      const statusItems = wrapper.findAll('.status-item')
      expect(statusItems[0].find('.status-count').text()).toBe('888888')
      expect(statusItems[1].find('.status-count').text()).toBe('77777')
      expect(statusItems[2].find('.status-count').text()).toBe('33334')
    })
  })
})