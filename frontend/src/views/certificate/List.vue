<template>
  <div class="certificate-list">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="证书名称/域名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="全部" value="" />
            <el-option label="正常" value="NORMAL" />
            <el-option label="即将过期" value="EXPIRING_SOON" />
            <el-option label="已过期" value="EXPIRED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right;">
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            添加证书
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 数据表格 -->
    <el-card>
      <el-table 
        :data="certificates" 
        v-loading="loading" 
        style="width: 100%"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="name" label="证书名称" min-width="150" />
        <el-table-column prop="domain" label="域名" min-width="150" />
        <el-table-column prop="issuer" label="颁发机构" width="120" />
        <el-table-column prop="certificateType" label="证书类型" width="100" />
        <el-table-column prop="expiryDate" label="到期日期" width="120" sortable>
          <template #default="{ row }">
            {{ formatDate(row.expiryDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="daysUntilExpiry" label="剩余天数" width="100">
          <template #default="{ row }">
            <span v-if="row.daysUntilExpiry < 0" class="text-danger">
              已过期
            </span>
            <span v-else :class="getDaysClass(row.daysUntilExpiry)">
              {{ row.daysUntilExpiry }} 天
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
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
import { useRouter } from 'vue-router'
import { useCertificateStore } from '@/stores/certificate'
import { formatDate, getStatusType, getStatusText } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'

const router = useRouter()
const certificateStore = useCertificateStore()

const loading = ref(false)
const certificates = ref([])
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  size: 20
})

// 获取证书列表
const fetchCertificates = async () => {
  loading.value = true
  try {
    certificateStore.setQueryParams({
      ...pagination,
      ...searchForm
    })
    await certificateStore.fetchCertificates()
    certificates.value = certificateStore.certificates
    total.value = certificateStore.total
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchCertificates()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  pagination.page = 1
  fetchCertificates()
}

// 创建证书
const handleCreate = () => {
  router.push('/certificates-create')
}

// 查看详情
const handleView = (row) => {
  router.push(`/certificates/${row.id}`)
}

// 编辑证书
const handleEdit = (row) => {
  router.push(`/certificates-edit/${row.id}`)
}

// 删除证书
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除证书 "${row.name}" 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await certificateStore.deleteCertificate(row.id)
    if (res.success) {
      ElMessage.success('删除成功')
      fetchCertificates()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

// 分页大小改变
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchCertificates()
}

// 页码改变
const handlePageChange = (page) => {
  pagination.page = page
  fetchCertificates()
}

// 排序改变
const handleSortChange = ({ column, prop, order }) => {
  // 可以根据需要实现排序逻辑
  fetchCertificates()
}

// 获取剩余天数样式
const getDaysClass = (days) => {
  if (days <= 7) return 'text-danger'
  if (days <= 15) return 'text-warning'
  if (days <= 30) return 'text-info'
  return ''
}

onMounted(() => {
  fetchCertificates()
})
</script>

<style scoped>
.certificate-list {
  padding: 0;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
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