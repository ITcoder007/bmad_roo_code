<template>
  <div class="monitoring-logs">
    <el-card>
      <template #header>
        <span>监控日志</span>
      </template>
      
      <el-table 
        :data="logs" 
        v-loading="loading" 
        style="width: 100%"
      >
        <el-table-column prop="logTime" label="时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.logTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="certificateName" label="证书名称" />
        <el-table-column prop="domain" label="域名" />
        <el-table-column prop="logType" label="日志类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getLogTypeTag(row.logType)">
              {{ getLogTypeText(row.logType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="日志内容" />
        <el-table-column prop="daysToExpiry" label="剩余天数" width="100">
          <template #default="{ row }">
            <span v-if="row.daysToExpiry !== null">{{ row.daysToExpiry }} 天</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { monitoringApi } from '@/api/monitoring'
import { formatDateTime, getLogTypeText } from '@/utils/format'

const loading = ref(false)
const logs = ref([])
const total = ref(0)

const pagination = reactive({
  page: 1,
  size: 20
})

const fetchLogs = async () => {
  loading.value = true
  try {
    const res = await monitoringApi.getLogs(pagination)
    if (res.success) {
      logs.value = res.data.content
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const getLogTypeTag = (type) => {
  const map = {
    'MONITORING': 'info',
    'ALERT_EMAIL': 'warning',
    'ALERT_SMS': 'danger'
  }
  return map[type] || 'info'
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchLogs()
}

const handlePageChange = (page) => {
  pagination.page = page
  fetchLogs()
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.monitoring-logs {
  padding: 0;
}
</style>