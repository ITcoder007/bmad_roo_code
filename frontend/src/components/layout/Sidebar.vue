<template>
  <div class="sidebar" :class="{ 'is-collapsed': collapsed }">
    <div class="sidebar-content">
      <!-- 导航菜单 -->
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        :router="true"
        background-color="#001529"
        text-color="rgba(255, 255, 255, 0.85)"
        active-text-color="#1890ff"
        class="sidebar-menu"
      >
        <!-- 仪表板 -->
        <el-menu-item index="/dashboard" route="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>仪表板</template>
        </el-menu-item>
        
        <!-- 证书管理 -->
        <el-sub-menu index="certificates">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>证书管理</span>
          </template>
          
          <el-menu-item index="/certificates" route="/certificates">
            <el-icon><List /></el-icon>
            <template #title>证书列表</template>
          </el-menu-item>
          
          <el-menu-item index="/certificates/add" route="/certificates/add">
            <el-icon><Plus /></el-icon>
            <template #title>添加证书</template>
          </el-menu-item>
        </el-sub-menu>
        
        <!-- 监控中心 -->
        <el-sub-menu index="monitoring">
          <template #title>
            <el-icon><Monitor /></el-icon>
            <span>监控中心</span>
          </template>
          
          <el-menu-item index="/monitoring/dashboard" route="/monitoring/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>监控仪表板</template>
          </el-menu-item>
          
          <el-menu-item index="/monitoring/alerts" route="/monitoring/alerts">
            <el-icon><Warning /></el-icon>
            <template #title>预警信息</template>
          </el-menu-item>
          
          <el-menu-item index="/monitoring/logs" route="/monitoring/logs">
            <el-icon><DocumentCopy /></el-icon>
            <template #title>监控日志</template>
          </el-menu-item>
        </el-sub-menu>
        
        <!-- 系统管理 -->
        <el-sub-menu index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          
          <el-menu-item index="/system/users" route="/system/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          
          <el-menu-item index="/system/roles" route="/system/roles">
            <el-icon><UserFilled /></el-icon>
            <template #title>角色管理</template>
          </el-menu-item>
          
          <el-menu-item index="/system/settings" route="/system/settings">
            <el-icon><Tools /></el-icon>
            <template #title>系统设置</template>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </div>
    
    <!-- 折叠/展开按钮 -->
    <div class="sidebar-footer">
      <el-button 
        type="text" 
        class="collapse-btn"
        @click="toggleCollapse"
      >
        <el-icon>
          <component :is="collapsed ? 'Expand' : 'Fold'" />
        </el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  Odometer,
  Document,
  List,
  Plus,
  Monitor,
  DataAnalysis,
  Warning,
  DocumentCopy,
  Setting,
  User,
  UserFilled,
  Tools,
  Expand,
  Fold
} from '@element-plus/icons-vue'

const route = useRoute()

// 定义 props
const props = defineProps({
  collapsed: {
    type: Boolean,
    default: false
  }
})

// 定义事件
const emit = defineEmits(['update:collapsed'])

// 当前激活的菜单项
const activeMenu = computed(() => {
  return route.path
})

// 切换折叠状态
const toggleCollapse = () => {
  emit('update:collapsed', !props.collapsed)
}
</script>

<style scoped>
.sidebar {
  width: 240px;
  background-color: #001529;
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.sidebar.is-collapsed {
  width: 64px;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-menu {
  border-right: none;
  height: 100%;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 240px;
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid #1f2937;
  display: flex;
  justify-content: center;
}

.collapse-btn {
  color: rgba(255, 255, 255, 0.85);
  width: 100%;
  justify-content: center;
}

.collapse-btn:hover {
  color: #1890ff;
  background-color: rgba(24, 144, 255, 0.1);
}

/* 自定义滚动条 */
.sidebar-content::-webkit-scrollbar {
  width: 6px;
}

.sidebar-content::-webkit-scrollbar-track {
  background: #001529;
}

.sidebar-content::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.sidebar-content::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* Element Plus 菜单样式覆盖 */
.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: #1890ff !important;
}

.sidebar-menu :deep(.el-sub-menu__title:hover) {
  background-color: rgba(255, 255, 255, 0.1) !important;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.1) !important;
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item:hover) {
  background-color: rgba(24, 144, 255, 0.1) !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    top: 60px;
    left: 0;
    bottom: 0;
    z-index: 1000;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }
  
  .sidebar.is-open {
    transform: translateX(0);
  }
}
</style>