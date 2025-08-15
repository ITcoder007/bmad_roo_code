<template>
  <el-form
    ref="formRef"
    :model="modelValue"
    :rules="rules"
    :label-width="labelWidth"
    :label-position="labelPosition"
    :inline="inline"
    :size="size"
    :disabled="disabled"
    v-bind="$attrs"
    @validate="handleValidate"
    @submit.prevent="handleSubmit"
  >
    <slot />
    
    <!-- 默认按钮组 -->
    <el-form-item v-if="showButtons" class="form-buttons">
      <el-button 
        type="primary" 
        :loading="submitLoading"
        @click="handleSubmit"
      >
        {{ submitText }}
      </el-button>
      <el-button 
        v-if="showReset"
        @click="handleReset"
      >
        {{ resetText }}
      </el-button>
      <el-button 
        v-if="showCancel"
        @click="handleCancel"
      >
        {{ cancelText }}
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, nextTick } from 'vue'

// 定义 props
const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  },
  rules: {
    type: Object,
    default: () => ({})
  },
  labelWidth: {
    type: String,
    default: '120px'
  },
  labelPosition: {
    type: String,
    default: 'right',
    validator: (value) => ['left', 'right', 'top'].includes(value)
  },
  inline: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['large', 'default', 'small'].includes(value)
  },
  disabled: {
    type: Boolean,
    default: false
  },
  showButtons: {
    type: Boolean,
    default: true
  },
  showReset: {
    type: Boolean,
    default: true
  },
  showCancel: {
    type: Boolean,
    default: false
  },
  submitText: {
    type: String,
    default: '提交'
  },
  resetText: {
    type: String,
    default: '重置'
  },
  cancelText: {
    type: String,
    default: '取消'
  },
  submitLoading: {
    type: Boolean,
    default: false
  }
})

// 定义事件
const emit = defineEmits([
  'update:modelValue',
  'submit',
  'reset',
  'cancel',
  'validate'
])

// 响应式数据
const formRef = ref(null)

// 事件处理
const handleValidate = (prop, isValid, message) => {
  emit('validate', prop, isValid, message)
}

const handleSubmit = async () => {
  try {
    const valid = await validate()
    if (valid) {
      emit('submit', props.modelValue)
    }
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleReset = () => {
  resetFields()
  emit('reset')
}

const handleCancel = () => {
  emit('cancel')
}

// 表单方法
const validate = async (callback) => {
  if (!formRef.value) return false
  
  try {
    const valid = await formRef.value.validate(callback)
    return valid
  } catch (error) {
    return false
  }
}

const validateField = (props, callback) => {
  if (!formRef.value) return
  formRef.value.validateField(props, callback)
}

const resetFields = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
}

const clearValidate = (props) => {
  if (!formRef.value) return
  formRef.value.clearValidate(props)
}

const scrollToField = (prop) => {
  if (!formRef.value) return
  formRef.value.scrollToField(prop)
}

// 暴露方法
defineExpose({
  formRef,
  validate,
  validateField,
  resetFields,
  clearValidate,
  scrollToField
})
</script>

<style scoped>
.form-buttons {
  margin-top: 24px;
  text-align: center;
}

.form-buttons :deep(.el-form-item__content) {
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .form-buttons {
    margin-top: 20px;
  }
  
  .form-buttons :deep(.el-button) {
    margin-bottom: 8px;
  }
}
</style>