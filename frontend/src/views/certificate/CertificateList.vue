<template>
  <div class="certificate-list">
    <!-- 搜索和筛选区域 -->
    <div class="filter-section">
      <el-row :gutter="16">
        <!-- 搜索组件 -->
        <el-col
          :xs="24"
          :sm="24"
          :md="12"
          :lg="8"
        >
          <CertificateSearchBox
            v-model="searchKeyword"
            :loading="searchLoading"
            @search="handleSearch"
            @clear="handleClearSearch"
          />
        </el-col>
        <el-col
          :xs="24"
          :sm="12"
          :md="6"
          :lg="4"
        >
          <el-select 
            v-model="statusFilter" 
            placeholder="筛选状态"
            clearable
            @change="handleStatusChange"
          >
            <el-option
              label="全部"
              value=""
            />
            <el-option
              label="正常"
              value="NORMAL"
            />
            <el-option
              label="即将过期"
              value="EXPIRING_SOON"
            />
            <el-option
              label="已过期"
              value="EXPIRED"
            />
          </el-select>
        </el-col>
        <el-col
          :xs="24"
          :sm="12"
          :md="6"
          :lg="4"
        >
          <el-button
            type="primary"
            @click="handleRefresh"
          >
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </el-col>
      </el-row>
      
      <!-- 搜索结果信息 -->
      <div
        v-if="hasSearchQuery || searchLoading"
        class="search-result-info"
      >
        <div
          v-if="searchLoading"
          class="loading-info"
        >
          <el-icon class="is-loading">
            <Loading />
          </el-icon>
          <span>正在搜索...</span>
        </div>
        <div
          v-else-if="hasSearchQuery"
          class="result-info"
        >
          <el-tag
            type="info"
            size="small"
          >
            搜索"{{ searchKeyword }}"，找到 {{ filteredCertificates.length }} 个结果
          </el-tag>
          <el-button 
            type="text" 
            size="small" 
            @click="handleClearSearch"
          >
            清除搜索
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 表格区域 -->
    <BaseTable
      :data="paginatedCertificates"
      :loading="loading || searchLoading"
      border
      stripe
      @sort-change="handleSortChange"
    >
      <!-- 空状态 -->
      <template #empty>
        <div class="empty-state">
          <div
            v-if="hasSearchQuery"
            class="search-empty"
          >
            <el-icon
              size="48"
              color="#c0c4cc"
            >
              <Search />
            </el-icon>
            <p>未找到匹配"{{ searchKeyword }}"的证书</p>
            <el-button
              type="text"
              @click="handleClearSearch"
            >
              清除搜索条件
            </el-button>
          </div>
          <div
            v-else
            class="data-empty"
          >
            <el-icon
              size="48"
              color="#c0c4cc"
            >
              <Document />
            </el-icon>
            <p>暂无证书数据</p>
          </div>
        </div>
      </template>
      <el-table-column
        prop="name"
        label="证书名称"
        sortable
        min-width="150"
      >
        <template #default="{ row }">
          <el-link 
            type="primary" 
            :underline="false"
            @click="viewDetails(row.id)"
          >
            <span 
              v-if="hasSearchQuery"
              class="highlighted-text"
              v-html="getHighlightedName(row.name)"
            />
            <span v-else>{{ row.name }}</span>
          </el-link>
        </template>
      </el-table-column>
      
      <el-table-column
        prop="domain"
        label="域名"
        min-width="200"
      >
        <template #default="{ row }">
          <span 
            v-if="hasSearchQuery"
            class="highlighted-text"
            v-html="getHighlightedDomain(row.domain)"
          />
          <span v-else>{{ row.domain }}</span>
        </template>
      </el-table-column>
      
      <el-table-column
        prop="issuer"
        label="颁发机构"
        min-width="150"
      />
      
      <el-table-column
        prop="expiryDate"
        label="到期日期"
        sortable
        min-width="120"
      >
        <template #default="{ row }">
          {{ formatDate(row.expiryDate) }}
        </template>
      </el-table-column>
      
      <el-table-column
        prop="status"
        label="状态"
        sortable
        width="100"
      >
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column
        label="操作"
        width="180"
        fixed="right"
      >
        <template #default="{ row }">
          <el-button 
            size="small" 
            data-testid="view-details"
            @click="viewDetails(row.id)"
          >
            查看
          </el-button>
          <el-button 
            size="small" 
            type="primary" 
            @click="editCertificate(row.id)"
          >
            编辑
          </el-button>
          <el-button 
            size="small" 
            type="danger" 
            @click="deleteCertificate(row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </BaseTable>
    
    <!-- 分页 -->
    <el-pagination
      v-model:current-page="currentPage"
      :page-size="pageSize"
      :total="totalCertificates"
      layout="total, prev, pager, next"
      @current-change="handlePageChange"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Loading, Search, Document } from '@element-plus/icons-vue'
import { useCertificateStore } from '@/stores/modules/certificate'
import BaseTable from '@/components/common/BaseTable.vue'
import CertificateSearchBox from '@/components/business/CertificateSearchBox.vue'
import { smartHighlight } from '@/utils/highlight'
import { useSearchUrlSync } from '@/composables/useUrlSync'

const router = useRouter()
const certificateStore = useCertificateStore()

// URL 同步
const {
  searchQuery: searchKeyword,
  statusFilter,
  currentPage,
  updateSearch: urlUpdateSearch,
  updateStatus: urlUpdateStatus,
  updatePage: urlUpdatePage,
  clearSearch: urlClearSearch
} = useSearchUrlSync()

// 响应式数据
const loading = ref(false)
const searchLoading = ref(false)
const pageSize = ref(20)

// 计算属性
const hasSearchQuery = computed(() => searchKeyword.value.trim() !== '')

const filteredCertificates = computed(() => {
  let certificates = certificateStore.certificates
  
  // 搜索过滤
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    certificates = certificates.filter(cert => {
      return (
        (cert.name && cert.name.toLowerCase().includes(keyword)) ||
        (cert.domain && cert.domain.toLowerCase().includes(keyword))
      )
    })
  }
  
  // 状态过滤
  if (statusFilter.value) {
    certificates = certificates.filter(cert => cert.status === statusFilter.value)
  }
  
  return certificates
})

const sortedCertificates = computed(() => {
  let result = [...filteredCertificates.value]
  
  if (certificateStore.sortConfig.prop) {
    result = result.sort((a, b) => {
      const aVal = a[certificateStore.sortConfig.prop]
      const bVal = b[certificateStore.sortConfig.prop]
      const order = certificateStore.sortConfig.order === 'ascending' ? 1 : -1
      
      if (certificateStore.sortConfig.prop === 'expiryDate') {
        return (new Date(aVal) - new Date(bVal)) * order
      } else if (certificateStore.sortConfig.prop === 'status') {
        const statusOrder = { 'EXPIRED': 3, 'EXPIRING_SOON': 2, 'NORMAL': 1 }
        return (statusOrder[aVal] - statusOrder[bVal]) * order
      } else {
        return aVal.localeCompare(bVal) * order
      }
    })
  }
  
  return result
})

const totalCertificates = computed(() => sortedCertificates.value.length)

const paginatedCertificates = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return sortedCertificates.value.slice(start, end)
})

// 方法
const loadCertificates = async () => {
  loading.value = true
  try {
    await certificateStore.fetchCertificateList()
  } catch (error) {
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

// 高亮显示文本
const highlightSearchText = (text, keyword) => {
  if (!keyword || !text) return text
  return smartHighlight(text, keyword, 'search-highlight')
}

// 获取高亮后的证书名称
const getHighlightedName = (name) => {
  return highlightSearchText(name, searchKeyword.value)
}

// 获取高亮后的域名
const getHighlightedDomain = (domain) => {
  return highlightSearchText(domain, searchKeyword.value)
}

// 事件处理
const handleRefresh = () => {
  loadCertificates()
}

const handleSortChange = ({ prop, order }) => {
  certificateStore.setSortConfig({ prop, order })
}

const viewDetails = (id) => {
  router.push(`/certificates/${id}`)
}

const editCertificate = (id) => {
  router.push(`/certificates/${id}/edit`)
}

const deleteCertificate = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除此证书？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await certificateStore.removeExistingCertificate(id)
    ElMessage.success('删除成功')
    await loadCertificates()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSearch = async (keyword) => {
  urlUpdateSearch(keyword)
  try {
    searchLoading.value = true
    await certificateStore.searchCertificates(keyword)
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    searchLoading.value = false
  }
}

const handleClearSearch = () => {
  urlClearSearch()
  certificateStore.clearSearch()
}

const handleStatusChange = () => {
  urlUpdateStatus(statusFilter.value)
}

const handlePageChange = (page) => {
  urlUpdatePage(page)
}

// 生命周期
onMounted(() => {
  loadCertificates()
})
</script>

<style scoped>
.certificate-list {
  padding: 20px;
}

.filter-section {
  margin-bottom: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.search-result-info {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 搜索高亮样式 */
:deep(.search-highlight) {
  background-color: #fff3cd;
  color: #856404;
  padding: 1px 3px;
  border-radius: 3px;
  font-weight: 600;
  box-shadow: 0 1px 2px rgba(255, 193, 7, 0.2);
}

.highlighted-text {
  line-height: 1.4;
}

/* 加载和搜索状态样式 */
.loading-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
}

.result-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
}

.search-empty,
.data-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.search-empty p,
.data-empty p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.el-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 16px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .certificate-list {
    padding: 12px;
  }
  
  .filter-section {
    margin-bottom: 16px;
    padding: 12px;
  }
  
  .el-pagination {
    margin-top: 16px;
    padding: 12px 0;
  }
  
  .el-pagination :deep(.el-pager) {
    flex-wrap: wrap;
  }
}

@media (max-width: 480px) {
  .certificate-list {
    padding: 8px;
  }
  
  .filter-section {
    padding: 8px;
  }
  
  /* 操作按钮在小屏幕上垂直堆叠 */
  :deep(.el-table__fixed-right) {
    .el-button {
      display: block;
      margin: 2px 0;
      width: 100%;
    }
  }
}
</style>