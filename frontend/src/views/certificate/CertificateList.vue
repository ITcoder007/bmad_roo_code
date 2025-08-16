<template>
  <div class="certificate-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">证书列表</h1>
      <p class="page-description">管理和监控所有证书的状态和信息</p>
    </div>

    <!-- 操作区域 -->
    <div class="page-actions">
      <div class="actions-left">
        <!-- 搜索框 -->
        <el-input
          v-model="searchKeyword"
          placeholder="搜索证书名称或域名"
          style="width: 300px"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <!-- 状态筛选 -->
        <el-select
          v-model="selectedStatus"
          placeholder="选择状态"
          style="width: 150px"
          clearable
        >
          <el-option label="全部" value="" />
          <el-option label="正常" value="NORMAL" />
          <el-option label="即将过期" value="EXPIRING_SOON" />
          <el-option label="已过期" value="EXPIRED" />
        </el-select>
      </div>

      <div class="actions-right">
        <!-- 刷新按钮 -->
        <el-button type="default" @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>

        <!-- 导出按钮 -->
        <el-button type="primary" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>

        <!-- 添加证书按钮 -->
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加证书
        </el-button>
      </div>
    </div>

    <!-- 表格区域 -->
    <div class="content-section">
      <el-table
        :data="certificateList"
        :loading="loading"
        border
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="name" label="证书名称" min-width="150">
          <template #default="{ row }">
            <el-link 
              type="primary" 
              @click="handleDetail(row)"
              :underline="false"
            >
              {{ row.name }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column prop="domain" label="域名" min-width="200" />

        <el-table-column prop="issuer" label="颁发机构" min-width="150" />

        <el-table-column prop="expiryDate" label="过期时间" min-width="120">
          <template #default="{ row }">
            {{ formatDate(row.expiryDate) }}
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="剩余天数" width="100">
          <template #default="{ row }">
            <span :class="getDaysColor(row.daysUntilExpiry)">
              {{ row.daysUntilExpiry }} 天
            </span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleDetail(row)">
              查看
            </el-button>
            <el-button size="small" type="primary" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Download, Plus } from '@element-plus/icons-vue'
import { getCertificateList, deleteCertificate } from '@/api/certificate'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const searchKeyword = ref('')
const selectedStatus = ref('')
const certificateList = ref([])
const selectedRows = ref([])

// 分页数据
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 获取证书列表
const loadCertificateList = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page,
      size: pagination.size,
      keyword: searchKeyword.value,
      status: selectedStatus.value
    }
    
    const result = await getCertificateList(params)
    // 修复：后端返回的数据在 records 字段中
    certificateList.value = result.records || []
    pagination.total = result.total || 0
  } catch (error) {
    console.error('获取证书列表失败:', error)
    ElMessage.error('获取证书列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString()
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'NORMAL': 'success',
    'EXPIRING_SOON': 'warning',
    'EXPIRED': 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'NORMAL': '正常',
    'EXPIRING_SOON': '即将过期',
    'EXPIRED': '已过期'
  }
  return textMap[status] || '未知'
}

// 获取剩余天数颜色
const getDaysColor = (days) => {
  if (days < 0) return 'text-danger'
  if (days <= 7) return 'text-danger'
  if (days <= 30) return 'text-warning'
  return 'text-success'
}

// 事件处理
const handleRefresh = () => {
  loadCertificateList()
}

const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

const handleAdd = () => {
  router.push('/certificates/add')
}

const handleDetail = (row) => {
  router.push(`/certificates/${row.id}`)
}

const handleEdit = (row) => {
  router.push(`/certificates/${row.id}/edit`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除证书 "${row.name}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteCertificate(row.id)
    ElMessage.success('删除成功')
    loadCertificateList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除证书失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadCertificateList()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadCertificateList()
}

// 监听搜索条件变化
const searchDebounce = ref(null)
const handleSearch = () => {
  if (searchDebounce.value) {
    clearTimeout(searchDebounce.value)
  }
  searchDebounce.value = setTimeout(() => {
    pagination.page = 1
    loadCertificateList()
  }, 500)
}

// 监听状态筛选变化
const handleStatusChange = () => {
  pagination.page = 1
  loadCertificateList()
}

// 生命周期
onMounted(() => {
  loadCertificateList()
})
</script>

<style scoped>
.certificate-list {
  padding: 0;
}

.page-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
}

.actions-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.actions-right {
  display: flex;
  gap: 8px;
}

.content-section {
  background: #fff;
  border-radius: 6px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.text-success {
  color: #67c23a;
}

.text-warning {
  color: #e6a23c;
}

.text-danger {
  color: #f56c6c;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .actions-left {
    justify-content: space-between;
  }
  
  .actions-right {
    justify-content: center;
  }
}
</style>