<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :fullscreen="fullscreen"
    :top="top"
    :modal="modal"
    :lock-scroll="lockScroll"
    :close-on-click-modal="closeOnClickModal"
    :close-on-press-escape="closeOnPressEscape"
    :show-close="showClose"
    :before-close="handleBeforeClose"
    :destroy-on-close="destroyOnClose"
    :append-to-body="appendToBody"
    v-bind="$attrs"
    @open="handleOpen"
    @opened="handleOpened"
    @close="handleClose"
    @closed="handleClosed"
  >
    <!-- 自定义头部 -->
    <template v-if="$slots.header" #header>
      <slot name="header" />
    </template>
    
    <!-- 主要内容 -->
    <div class="dialog-content">
      <slot />
    </div>
    
    <!-- 自定义底部 -->
    <template #footer>
      <slot name="footer">
        <div v-if="showFooter" class="dialog-footer">
          <el-button 
            v-if="showCancel"
            @click="handleCancel"
          >
            {{ cancelText }}
          </el-button>
          <el-button 
            v-if="showConfirm"
            type="primary"
            :loading="confirmLoading"
            @click="handleConfirm"
          >
            {{ confirmText }}
          </el-button>
        </div>
      </slot>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, watch } from 'vue'

// 定义 props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ''
  },
  width: {
    type: [String, Number],
    default: '50%'
  },
  fullscreen: {
    type: Boolean,
    default: false
  },
  top: {
    type: String,
    default: '15vh'
  },
  modal: {
    type: Boolean,
    default: true
  },
  lockScroll: {
    type: Boolean,
    default: true
  },
  closeOnClickModal: {
    type: Boolean,
    default: true
  },
  closeOnPressEscape: {
    type: Boolean,
    default: true
  },
  showClose: {
    type: Boolean,
    default: true
  },
  destroyOnClose: {
    type: Boolean,
    default: false
  },
  appendToBody: {
    type: Boolean,
    default: false
  },
  showFooter: {
    type: Boolean,
    default: true
  },
  showCancel: {
    type: Boolean,
    default: true
  },
  showConfirm: {
    type: Boolean,
    default: true
  },
  cancelText: {
    type: String,
    default: '取消'
  },
  confirmText: {
    type: String,
    default: '确定'
  },
  confirmLoading: {
    type: Boolean,
    default: false
  },
  beforeClose: {
    type: Function,
    default: null
  }
})

// 定义事件
const emit = defineEmits([
  'update:modelValue',
  'open',
  'opened',
  'close',
  'closed',
  'cancel',
  'confirm'
])

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 事件处理
const handleOpen = () => {
  emit('open')
}

const handleOpened = () => {
  emit('opened')
}

const handleClose = () => {
  emit('close')
}

const handleClosed = () => {
  emit('closed')
}

const handleBeforeClose = (done) => {
  if (props.beforeClose) {
    props.beforeClose(done)
  } else {
    done()
  }
}

const handleCancel = () => {
  visible.value = false
  emit('cancel')
}

const handleConfirm = () => {
  emit('confirm')
}

// 暴露方法
const open = () => {
  visible.value = true
}

const close = () => {
  visible.value = false
}

defineExpose({
  open,
  close
})
</script>

<style scoped>
.dialog-content {
  min-height: 60px;
}

.dialog-footer {
  text-align: right;
}

.dialog-footer .el-button {
  margin-left: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dialog-footer {
    text-align: center;
  }
  
  .dialog-footer .el-button {
    margin: 0 6px 8px 6px;
  }
}
</style>