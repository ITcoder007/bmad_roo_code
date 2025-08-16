<template>
  <div class="header">
    <div class="header-left">
      <!-- Logo 和应用名称 -->
      <div class="logo-section">
        <img 
          src="/favicon.ico" 
          alt="Logo" 
          class="logo-icon"
        />
        <span class="app-title">{{ appConfig.title }}</span>
      </div>
      
      <!-- 侧边栏切换按钮 -->
      <el-button 
        type="text" 
        class="sidebar-toggle"
        @click="toggleSidebar"
      >
        <el-icon><Menu /></el-icon>
      </el-button>
    </div>
    
    <div class="header-center">
      <!-- 面包屑导航 -->
      <el-breadcrumb separator="/">
        <el-breadcrumb-item 
          v-for="item in breadcrumbs" 
          :key="item.path"
          :to="item.path"
        >
          {{ item.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <div class="header-right">
      <!-- 用户信息和操作 -->
      <div class="user-section">
        <!-- 通知按钮 -->
        <el-button 
          type="text" 
          class="notification-btn"
        >
          <el-badge :value="notificationCount" class="notification-badge">
            <el-icon><Bell /></el-icon>
          </el-badge>
        </el-button>
        
        <!-- 用户下拉菜单 -->
        <el-dropdown class="user-dropdown">
          <span class="user-info">
            <el-avatar :size="32" class="user-avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
            <span class="username">{{ currentUser.name }}</span>
            <el-icon class="dropdown-icon"><CaretBottom /></el-icon>
          </span>
          
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleProfile">
                <el-icon><User /></el-icon>
                个人信息
              </el-dropdown-item>
              <el-dropdown-item @click="handleSettings">
                <el-icon><Setting /></el-icon>
                系统设置
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, inject } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { 
  Menu, 
  Bell, 
  User, 
  Setting, 
  CaretBottom 
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const appConfig = inject('appConfig', {})

// 定义事件
const emit = defineEmits(['toggle-sidebar'])

// 模拟当前用户信息
const currentUser = ref({
  name: '系统管理员',
  avatar: null
})

// 模拟通知数量
const notificationCount = ref(3)

// 面包屑导航
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  return matched.map(item => ({
    path: item.path,
    title: item.meta.title
  }))
})

// 切换侧边栏
const toggleSidebar = () => {
  emit('toggle-sidebar')
}

// 处理用户操作
const handleProfile = () => {
  router.push('/profile')
}

const handleSettings = () => {
  router.push('/system/settings')
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 20px;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-icon {
  width: 32px;
  height: 32px;
}

.app-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.sidebar-toggle {
  padding: 8px;
  font-size: 18px;
}

.header-center {
  flex: 1;
  padding: 0 24px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.notification-btn {
  padding: 8px;
  font-size: 18px;
}

.notification-badge :deep(.el-badge__content) {
  border: 2px solid #fff;
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.username {
  font-size: 14px;
  color: #374151;
}

.dropdown-icon {
  font-size: 12px;
  color: #9ca3af;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    padding: 0 12px;
  }
  
  .header-center {
    display: none;
  }
  
  .app-title {
    display: none;
  }
}
</style>