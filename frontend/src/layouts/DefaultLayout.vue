<template>
  <div class="default-layout">
    <div class="layout-container">
      <!-- 页头组件 -->
      <Header 
        class="layout-header" 
        @toggle-sidebar="sidebarCollapsed = !sidebarCollapsed"
      />
      
      <div class="layout-content">
        <!-- 侧边栏组件 -->
        <Sidebar 
          v-model:collapsed="sidebarCollapsed" 
          class="layout-sidebar" 
        />
        
        <!-- 主内容区组件 -->
        <MainContent 
          :sidebar-collapsed="sidebarCollapsed"
          class="layout-main"
        >
          <router-view />
        </MainContent>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Header from '@/components/layout/Header.vue'
import Sidebar from '@/components/layout/Sidebar.vue'
import MainContent from '@/components/layout/MainContent.vue'

// 侧边栏折叠状态
const sidebarCollapsed = ref(false)
</script>

<style scoped>
.default-layout {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.layout-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.layout-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.layout-content {
  display: flex;
  flex: 1;
  margin-top: 60px; /* 为固定头部预留空间 */
}

.layout-sidebar {
  position: fixed;
  left: 0;
  top: 60px;
  bottom: 0;
  z-index: 900;
  transition: all 0.3s ease;
}

.layout-main {
  flex: 1;
  margin-left: 240px; /* 为侧边栏预留空间 */
  transition: all 0.3s ease;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .layout-main {
    margin-left: 0;
  }
  
  .layout-sidebar {
    transform: translateX(-100%);
  }
  
  .layout-sidebar.is-open {
    transform: translateX(0);
  }
}
</style>