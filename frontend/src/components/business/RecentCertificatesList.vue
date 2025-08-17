<template>
  <div class="recent-certificates-list">
    <div
      v-if="loading"
      class="loading-container"
    >
      <el-skeleton
        :rows="4"
        animated
      />
    </div>
    
    <div
      v-else-if="certificates.length === 0"
      class="empty-container"
    >
      <el-empty description="暂无最近添加的证书" />
    </div>
    
    <div
      v-else
      class="certificates-container"
    >
      <div class="list-header">
        <span class="count-info">最近添加的 {{ certificates.length }} 个证书</span>
      </div>
      
      <div class="certificate-list">
        <div 
          v-for="certificate in certificates" 
          :key="certificate.id"
          class="certificate-item"
          @click="handleViewDetail(certificate)"
        >
          <div class="certificate-info">
            <div class="certificate-name">
              <span class="name">{{ certificate.name }}</span>
              <CertificateStatusBadge :status="certificate.status" />
            </div>
            <div class="certificate-domain">
              {{ certificate.domain }}
            </div>
            <div class="certificate-meta">
              <div class="created-info">
                <el-icon class="created-icon">
                  <Plus />
                </el-icon>
                <span class="created-text">添加于 {{ formatCreatedDate(certificate.createdAt) }}</span>
                <span class="time-ago">({{ getTimeAgo(certificate.createdAt) }})</span>
              </div>
              <div class="expiry-info">
                <el-icon class="expiry-icon">
                  <Clock />
                </el-icon>
                <span class="expiry-text">{{ formatExpiryDate(certificate.expiryDate) }} 到期</span>
              </div>
            </div>
          </div>
          
          <div class="certificate-actions">
            <el-button 
              size="small" 
              type="primary" 
              text
              @click.stop="handleViewDetail(certificate)"
            >
              查看详情
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Clock } from '@element-plus/icons-vue'
import CertificateStatusBadge from './CertificateStatusBadge.vue'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

// 配置 dayjs
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

// Props
const props = defineProps({
  certificates: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// 路由
const router = useRouter()

// 方法
const formatCreatedDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD')
}

const formatExpiryDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD')
}

const getTimeAgo = (date) => {
  return dayjs(date).fromNow()
}

const handleViewDetail = (certificate) => {
  router.push({
    name: 'CertificateDetail',
    params: { id: certificate.id }
  })
}
</script>

<style scoped>
.recent-certificates-list {
  height: 100%;
}

.loading-container {
  padding: 20px;
}

.empty-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 300px;
}

.list-header {
  padding: 0 0 12px 0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}

.count-info {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

.certificate-list {
  max-height: 280px;
  overflow-y: auto;
}

.certificate-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.certificate-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
  transform: translateY(-1px);
}

.certificate-item:last-child {
  margin-bottom: 0;
}

.certificate-info {
  flex: 1;
  min-width: 0;
}

.certificate-name {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.name {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

.certificate-domain {
  color: #606266;
  font-size: 12px;
  margin-bottom: 8px;
  word-break: break-all;
}

.certificate-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.created-info,
.expiry-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.created-icon {
  color: #67C23A;
  font-size: 14px;
}

.expiry-icon {
  color: #909399;
  font-size: 14px;
}

.created-text,
.expiry-text {
  color: #606266;
}

.time-ago {
  color: #67C23A;
  font-weight: 500;
}

.certificate-actions {
  flex-shrink: 0;
  margin-left: 12px;
}

/* 滚动条样式 */
.certificate-list::-webkit-scrollbar {
  width: 4px;
}

.certificate-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 2px;
}

.certificate-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.certificate-list::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .certificate-item {
    flex-direction: column;
    align-items: stretch;
  }
  
  .certificate-actions {
    margin-left: 0;
    margin-top: 8px;
    text-align: right;
  }
  
  .certificate-meta {
    flex-direction: column;
  }
}
</style>