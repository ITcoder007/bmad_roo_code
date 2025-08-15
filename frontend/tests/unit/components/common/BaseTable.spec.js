import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import BaseTable from '@/components/common/BaseTable.vue'

describe('BaseTable.vue', () => {
  const mockData = [
    { id: 1, name: '测试1', status: 'active' },
    { id: 2, name: '测试2', status: 'inactive' }
  ]

  it('renders correctly with data', () => {
    const wrapper = mount(BaseTable, {
      props: {
        data: mockData,
        total: 2
      },
      slots: {
        default: `
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="status" label="状态" />
        `
      }
    })
    
    expect(wrapper.find('.base-table').exists()).toBe(true)
    expect(wrapper.find('.el-table').exists()).toBe(true)
  })

  it('shows pagination when showPagination is true', () => {
    const wrapper = mount(BaseTable, {
      props: {
        data: mockData,
        total: 100,
        showPagination: true
      }
    })
    
    expect(wrapper.find('.pagination-wrapper').exists()).toBe(true)
    expect(wrapper.find('.el-pagination').exists()).toBe(true)
  })

  it('hides pagination when showPagination is false', () => {
    const wrapper = mount(BaseTable, {
      props: {
        data: mockData,
        total: 100,
        showPagination: false
      }
    })
    
    expect(wrapper.find('.pagination-wrapper').exists()).toBe(false)
  })

  it('emits page-change event when page changes', async () => {
    const wrapper = mount(BaseTable, {
      props: {
        data: mockData,
        total: 100,
        page: 1,
        size: 20
      }
    })
    
    await wrapper.vm.handleCurrentChange(2)
    
    expect(wrapper.emitted('page-change')).toBeTruthy()
    expect(wrapper.emitted('page-change')[0]).toEqual([2])
    expect(wrapper.emitted('update:page')).toBeTruthy()
    expect(wrapper.emitted('update:page')[0]).toEqual([2])
  })

  it('emits size-change event when page size changes', async () => {
    const wrapper = mount(BaseTable, {
      props: {
        data: mockData,
        total: 100,
        page: 1,
        size: 20
      }
    })
    
    await wrapper.vm.handleSizeChange(50)
    
    expect(wrapper.emitted('size-change')).toBeTruthy()
    expect(wrapper.emitted('size-change')[0]).toEqual([50])
    expect(wrapper.emitted('update:size')).toBeTruthy()
    expect(wrapper.emitted('update:size')[0]).toEqual([50])
  })

  it('exposes table methods correctly', () => {
    const wrapper = mount(BaseTable, {
      props: {
        data: mockData
      }
    })
    
    expect(wrapper.vm.clearSelection).toBeDefined()
    expect(wrapper.vm.toggleRowSelection).toBeDefined()
    expect(wrapper.vm.setCurrentRow).toBeDefined()
  })

  it('handles selection changes correctly', async () => {
    const wrapper = mount(BaseTable, {
      props: {
        data: mockData
      }
    })
    
    const mockSelection = [mockData[0]]
    await wrapper.vm.handleSelectionChange(mockSelection)
    
    expect(wrapper.emitted('selection-change')).toBeTruthy()
    expect(wrapper.emitted('selection-change')[0]).toEqual([mockSelection])
  })
})