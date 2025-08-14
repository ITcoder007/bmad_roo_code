<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67C23A20;">
              <el-icon :size="30" style="color: #67C23A;"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ systemStatus.normalCertificates || 0 }}</div>
              <div class="stat-label">正常证书</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #E6A23C20;">
              <el-icon :size="30" style="color: #E6A23C;"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ systemStatus.expiringSoonCertificates || 0 }}</div>
              <div class="stat-label">即将过期</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #F56C6C20;">
              <el-icon :size="30" style="color: #F56C6C;"><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ systemStatus.expiredCertificates || 0 }}</div>
              <div class="stat-label">已过期</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409EFF20;">
              <el-icon :size="30" style="color: #409EFF;"><Files /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ systemStatus.totalCertificates || 0 }}</div>
              <div class="stat-label">证书总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 即将过期证书列表 -->
    <el-card class="expiring-card">
      <template #header>
        <div class="card-header">
          <span>即将过期证书（30天内）</span>
          <el-button type="primary" link @click="goToCertificates">
            查看全部
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </template>
      
      <el-table :data="expiringCertificates" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="证书名称" />
        <el-table-column prop="domain" label="域名" />
        <el-table-column prop="expiryDate" label="到期日期">
          <template #default="{ row }">
            <span :class="getExpiryClass(row.daysUntilExpiry)">
              {{ formatDate(row.expiryDate) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="daysUntilExpiry" label="剩余天数">
          <template #default="{ row }">
            <el-tag :type="getDaysType(row.daysUntilExpiry)">
              {{ row.daysUntilExpiry }} 天
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="!loading && expiringCertificates.length === 0" description="暂无即将过期的证书" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { systemApi } from '@/api/system'
import { certificateApi } from '@/api/certificate'
import { formatDate } from '@/utils/format'
import { ElMessage } from 'element-plus'
import { CircleCheck, Warning, CircleClose, Files, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const systemStatus = ref({})
const expiringCertificates = ref([])

// 获取系统状态
const fetchSystemStatus = async () => {
  try {
    const res = await systemApi.getStatus()
    if (res.success) {
      systemStatus.value = res.data
    }
  } catch (error) {
    console.error('获取系统状态失败', error)
  }
}

// 获取即将过期的证书
const fetchExpiringCertificates = async () => {
  loading.value = true
  try {
    const res = await certificateApi.getList({
      page: 1,
      size: 10,
      status: 'EXPIRING_SOON'
    })
    if (res.success) {
      expiringCertificates.value = res.data.content
    }
  } finally {
    loading.value = false
  }
}

// 获取过期状态样式
const getExpiryClass = (days) => {
  if (days <= 7) return 'text-danger'
  if (days <= 15) return 'text-warning'
  return ''
}

// 获取剩余天数标签类型
const getDaysType = (days) => {
  if (days <= 7) return 'danger'
  if (days <= 15) return 'warning'
  return 'info'
}

// 查看详情
const viewDetail = (row) => {
  router.push(`/certificates/${row.id}`)
}

// 跳转到证书列表
const goToCertificates = () => {
  router.push('/certificates')
}

onMounted(() => {
  fetchSystemStatus()
  fetchExpiringCertificates()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.expiring-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.text-danger {
  color: #F56C6C;
  font-weight: bold;
}

.text-warning {
  color: #E6A23C;
  font-weight: bold;
}
</style>