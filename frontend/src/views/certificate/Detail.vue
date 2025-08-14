<template>
  <div class="certificate-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>证书详情</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>
      
      <el-descriptions :column="2" border v-if="certificate">
        <el-descriptions-item label="证书名称">{{ certificate.name }}</el-descriptions-item>
        <el-descriptions-item label="域名">{{ certificate.domain }}</el-descriptions-item>
        <el-descriptions-item label="颁发机构">{{ certificate.issuer }}</el-descriptions-item>
        <el-descriptions-item label="证书类型">{{ certificate.certificateType }}</el-descriptions-item>
        <el-descriptions-item label="创建日期">{{ formatDate(certificate.issueDate) }}</el-descriptions-item>
        <el-descriptions-item label="到期日期">{{ formatDate(certificate.expiryDate) }}</el-descriptions-item>
        <el-descriptions-item label="剩余天数">
          <span :class="getDaysClass(certificate.daysUntilExpiry)">
            {{ certificate.daysUntilExpiry }} 天
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(certificate.status)">
            {{ getStatusText(certificate.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ certificate.notes || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCertificateStore } from '@/stores/certificate'
import { formatDate, getStatusType, getStatusText } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const certificateStore = useCertificateStore()

const loading = ref(false)
const certificate = ref(null)

const fetchCertificate = async () => {
  loading.value = true
  try {
    const data = await certificateStore.fetchCertificateById(route.params.id)
    certificate.value = data
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/certificates')
}

const getDaysClass = (days) => {
  if (days <= 7) return 'text-danger'
  if (days <= 15) return 'text-warning'
  if (days <= 30) return 'text-info'
  return ''
}

onMounted(() => {
  fetchCertificate()
})
</script>

<style scoped>
.certificate-detail {
  padding: 0;
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

.text-info {
  color: #409EFF;
}
</style>