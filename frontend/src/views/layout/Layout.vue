<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="layout-aside">
      <div class="logo">
        <h3>证书管理系统</h3>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="layout-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>仪表板</span>
        </el-menu-item>
        <el-menu-item index="/certificates">
          <el-icon><Document /></el-icon>
          <span>证书列表</span>
        </el-menu-item>
        <el-menu-item index="/monitoring-logs">
          <el-icon><Reading /></el-icon>
          <span>监控日志</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部栏 -->
      <el-header class="layout-header">
        <div class="header-content">
          <h2>{{ pageTitle }}</h2>
          <div class="header-right">
            <span class="time">{{ currentTime }}</span>
          </div>
        </div>
      </el-header>
      
      <!-- 页面内容 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { DataLine, Document, Reading } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const route = useRoute()

const currentTime = ref(dayjs().format('YYYY-MM-DD HH:mm:ss'))
let timer = null

const activeMenu = computed(() => {
  const path = route.path
  if (path.includes('certificates')) return '/certificates'
  if (path.includes('monitoring')) return '/monitoring-logs'
  return path
})

const pageTitle = computed(() => route.meta.title || '证书管理系统')

onMounted(() => {
  timer = setInterval(() => {
    currentTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-aside {
  background-color: #304156;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3647;
  color: #fff;
}

.logo h3 {
  margin: 0;
  font-size: 16px;
}

.layout-menu {
  border-right: none;
  height: calc(100vh - 60px);
}

.layout-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, .08);
  padding: 0;
}

.header-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-content h2 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.time {
  color: #666;
  font-size: 14px;
}

.layout-main {
  background-color: #f5f7fa;
  padding: 20px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>