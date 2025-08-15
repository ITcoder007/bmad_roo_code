import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import BaseForm from '@/components/common/BaseForm.vue'
import { ElForm, ElFormItem, ElButton } from 'element-plus'

const global = {
  components: {
    'el-form': ElForm,
    'el-form-item': ElFormItem,
    'el-button': ElButton
  }
}

describe('BaseForm.vue', () => {
  const mockFormData = {
    name: '测试名称',
    email: 'test@example.com'
  }

  const mockRules = {
    name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
    email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }]
  }

  it('renders form correctly', () => {
    const wrapper = mount(BaseForm, {
      props: {
        modelValue: mockFormData,
        rules: mockRules
      },
      global
    })

    expect(wrapper.find('.el-form').exists()).toBe(true)
  })

  it('shows form buttons when showButtons is true', () => {
    const wrapper = mount(BaseForm, {
      props: {
        modelValue: mockFormData,
        rules: mockRules,
        showButtons: true
      },
      global
    })

    expect(wrapper.find('.form-buttons').exists()).toBe(true)
    expect(wrapper.findAll('.el-button')).toHaveLength(2) // 提交和重置按钮
  })

  it('hides form buttons when showButtons is false', () => {
    const wrapper = mount(BaseForm, {
      props: {
        modelValue: mockFormData,
        rules: mockRules,
        showButtons: false
      },
      global
    })

    expect(wrapper.find('.form-buttons').exists()).toBe(false)
  })

  it('emits submit event when form is submitted', async () => {
    const wrapper = mount(BaseForm, {
      props: {
        modelValue: mockFormData,
        rules: mockRules
      },
      global
    })

    // Mock form validation
    const formRef = wrapper.vm.formRef = {
      validate: vi.fn().mockResolvedValue(true)
    }

    await wrapper.vm.handleSubmit()

    expect(wrapper.emitted('submit')).toBeTruthy()
    expect(wrapper.emitted('submit')[0]).toEqual([mockFormData])
  })

  it('emits reset event when reset button is clicked', async () => {
    const wrapper = mount(BaseForm, {
      props: {
        modelValue: mockFormData,
        rules: mockRules
      },
      global
    })

    // Mock resetFields method
    wrapper.vm.formRef = {
      resetFields: vi.fn()
    }

    await wrapper.vm.handleReset()

    expect(wrapper.emitted('reset')).toBeTruthy()
  })
})