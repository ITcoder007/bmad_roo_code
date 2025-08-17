<template>
  <div
    class="main-content"
    :class="{ 'sidebar-collapsed': sidebarCollapsed }"
  >
    <div class="content-container">
      <!-- 页面内容区域 -->
      <div class="page-content">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup>
// 定义 props
const props = defineProps({
  sidebarCollapsed: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.main-content {
  flex: 1;
  min-height: calc(100vh - 60px);
  background-color: #f5f5f5;
  transition: all 0.3s ease;
  margin-left: 240px;
}

.main-content.sidebar-collapsed {
  margin-left: 64px;
}

.content-container {
  padding: 24px;
  max-width: 100%;
  min-height: 100%;
}

.page-content {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  min-height: calc(100vh - 108px);
  padding: 24px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .content-container {
    padding: 16px;
  }
  
  .page-content {
    padding: 16px;
    min-height: calc(100vh - 92px);
  }
}

@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
  }
  
  .main-content.sidebar-collapsed {
    margin-left: 0;
  }
  
  .content-container {
    padding: 12px;
  }
  
  .page-content {
    padding: 12px;
    border-radius: 4px;
    min-height: calc(100vh - 84px);
  }
}

/* 为页面内容提供一些基础样式 */
.page-content :deep(.page-header) {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e6e6e6;
}

.page-content :deep(.page-title) {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.page-content :deep(.page-description) {
  font-size: 14px;
  color: #6b7280;
  margin-top: 8px;
}

.page-content :deep(.page-actions) {
  margin-top: 16px;
}

.page-content :deep(.content-section) {
  margin-bottom: 24px;
}

.page-content :deep(.section-title) {
  font-size: 16px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 16px;
}
</style>