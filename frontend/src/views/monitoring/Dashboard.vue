<template>
  <div class="dashboard">
    <div class="page-header">
      <h1>系统仪表板</h1>
      <div class="header-actions">
        <el-button 
          :icon="Refresh" 
          :loading="refreshing" 
          type="primary"
          @click="handleRefresh"
        >
          刷新数据
        </el-button>
        <span class="last-update">最后更新: {{ lastUpdateTime }}</span>
      </div>
    </div>
    
    <!-- 统计卡片区域 -->
    <el-row
      :gutter="20"
      class="stats-row"
    >
      <el-col
        :xs="24"
        :sm="12"
        :md="8"
        :lg="6"
      >
        <CertificateStatsCard 
          :stats="certificateStats" 
          :loading="statsLoading" 
        />
      </el-col>
    </el-row>
    
    <!-- 主要内容区域 -->
    <el-row
      :gutter="20"
      class="content-row"
    >
      <!-- 即将过期证书 -->
      <el-col
        :xs="24"
        :md="12"
      >
        <el-card>
          <template #header>
            <div class="card-header">
              <span>即将过期证书 (7天内)</span>
              <router-link to="/certificates?filter=expiring">
                <el-button
                  text
                  type="primary"
                >
                  查看全部
                </el-button>
              </router-link>
            </div>
          </template>
          <ExpiringCertificatesList 
            :certificates="expiringCertificates" 
            :loading="certificatesLoading" 
          />
        </el-card>
      </el-col>
      
      <!-- 最近添加证书 -->
      <el-col
        :xs="24"
        :md="12"
      >
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近添加证书</span>
              <router-link to="/certificates">
                <el-button
                  text
                  type="primary"
                >
                  查看全部
                </el-button>
              </router-link>
            </div>
          </template>
          <RecentCertificatesList 
            :certificates="recentCertificates" 
            :loading="certificatesLoading" 
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import CertificateStatsCard from '@/components/business/CertificateStatsCard.vue'
import ExpiringCertificatesList from '@/components/business/ExpiringCertificatesList.vue'
import RecentCertificatesList from '@/components/business/RecentCertificatesList.vue'
import { useAutoRefresh } from '@/composables/useAutoRefresh'
import { useCertificateStore } from '@/stores/modules/certificate'

// Store
const certificateStore = useCertificateStore()

// 响应式数据
const refreshing = ref(false)
const lastUpdateTime = ref(new Date().toLocaleTimeString())

// 计算属性 - 从 store 获取数据
const certificateStats = computed(() => certificateStore.dashboardStats)
const expiringCertificates = computed(() => certificateStore.expiringCertificates)
const recentCertificates = computed(() => certificateStore.recentCertificates)
const statsLoading = computed(() => certificateStore.isStatsLoading)
const certificatesLoading = computed(() => certificateStore.isStatsLoading)

// 方法
const fetchDashboardData = async () => {
  try {
    // 使用 store 的方法获取仪表板数据
    await certificateStore.fetchDashboardData()
    lastUpdateTime.value = new Date().toLocaleTimeString()
  } catch (error) {
    console.error('获取仪表板数据失败:', error)
    // 这里可以添加错误提示
  }
}

const handleRefresh = async () => {
  refreshing.value = true
  try {
    await fetchDashboardData()
  } finally {
    refreshing.value = false
  }
}

// 自动刷新功能 - 每5分钟刷新一次
const { startAutoRefresh, stopAutoRefresh } = useAutoRefresh(() => {
  fetchDashboardData()
}, 5 * 60 * 1000) // 5分钟

// 生命周期
onMounted(() => {
  fetchDashboardData()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.last-update {
  color: #909399;
  font-size: 12px;
}

.stats-row {
  margin-bottom: 20px;
}

.content-row .el-card {
  height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 300px;
}

@media (max-width: 768px) {
  .dashboard {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .header-actions {
    justify-content: space-between;
  }
  
  .content-row .el-card {
    height: auto;
    margin-bottom: 16px;
  }
}
</style>