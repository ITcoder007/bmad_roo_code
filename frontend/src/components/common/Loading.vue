<template>
  <div v-if="visible" class="loading-container" :class="containerClass">
    <div class="loading-mask" :class="maskClass">
      <div class="loading-spinner">
        <el-icon v-if="icon" class="loading-icon" :class="iconClass">
          <component :is="icon" />
        </el-icon>
        <div v-else class="default-spinner">
          <div class="spinner-dots">
            <div class="dot dot1"></div>
            <div class="dot dot2"></div>
            <div class="dot dot3"></div>
          </div>
        </div>
        <p v-if="text" class="loading-text">{{ text }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

// 定义 props
const props = defineProps({
  visible: {
    type: Boolean,
    default: true
  },
  text: {
    type: String,
    default: '加载中...'
  },
  icon: {
    type: [String, Object],
    default: null
  },
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['small', 'default', 'large'].includes(value)
  },
  overlay: {
    type: Boolean,
    default: true
  },
  background: {
    type: String,
    default: 'rgba(255, 255, 255, 0.9)'
  }
})

// 计算属性
const containerClass = computed(() => ({
  'loading-overlay': props.overlay,
  'loading-inline': !props.overlay
}))

const maskClass = computed(() => ({
  [`loading-${props.size}`]: true
}))

const iconClass = computed(() => ({
  [`icon-${props.size}`]: true
}))
</script>

<style scoped>
.loading-container {
  position: relative;
  width: 100%;
  height: 100%;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2000;
}

.loading-inline {
  position: relative;
  min-height: 100px;
}

.loading-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: v-bind(background);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
}

.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.loading-icon {
  animation: rotate 2s linear infinite;
  color: #409eff;
}

.loading-icon.icon-small {
  font-size: 24px;
}

.loading-icon.icon-default {
  font-size: 32px;
}

.loading-icon.icon-large {
  font-size: 48px;
}

.default-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
}

.spinner-dots {
  display: flex;
  gap: 4px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #409eff;
  animation: dotBounce 1.4s ease-in-out infinite both;
}

.loading-small .dot {
  width: 6px;
  height: 6px;
}

.loading-large .dot {
  width: 10px;
  height: 10px;
}

.dot1 {
  animation-delay: -0.32s;
}

.dot2 {
  animation-delay: -0.16s;
}

.dot3 {
  animation-delay: 0s;
}

.loading-text {
  margin-top: 16px;
  color: #666;
  font-size: 14px;
  text-align: center;
}

.loading-small .loading-text {
  font-size: 12px;
  margin-top: 12px;
}

.loading-large .loading-text {
  font-size: 16px;
  margin-top: 20px;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes dotBounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}
</style>