<template>
  <div class="certificate-detail">
    <!-- 页面标题和导航 -->
    <div class="page-header">
      <el-breadcrumb>
        <el-breadcrumb-item to="/certificates">证书列表</el-breadcrumb-item>
        <el-breadcrumb-item>证书详情</el-breadcrumb-item>
      </el-breadcrumb>
      <h1>{{ certificate?.name || '证书详情' }}</h1>
    </div>
    
    <!-- 加载状态 -->
    <Loading v-if="loading" />
    
    <!-- 证书详情内容 -->
    <div v-else-if="certificate" class="detail-content">
      <!-- 证书状态展示 -->
      <el-card class="status-card">
        <template #header>
          <span>证书状态</span>
        </template>
        <CertificateStatusBadge 
          :status="certificate.status" 
          :expiry-date="certificate.expiryDate" 
        />
      </el-card>
      
      <!-- 编辑模式/查看模式 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>证书信息</span>
            <div class="card-actions">
              <el-button 
                v-if="!editMode" 
                type="primary" 
                @click="toggleEditMode"
                data-testid="edit-button"
              >
                编辑
              </el-button>
              <el-button v-else @click="cancelEdit">取消</el-button>
              <el-button 
                v-if="editMode" 
                type="primary" 
                @click="saveChanges"
                :loading="saving"
              >
                保存
              </el-button>
            </div>
          </div>
        </template>
        
        <!-- 查看模式 -->
        <div v-if="!editMode" class="view-mode">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="证书名称">{{ certificate.name }}</el-descriptions-item>
            <el-descriptions-item label="域名">{{ certificate.domain }}</el-descriptions-item>
            <el-descriptions-item label="颁发机构">{{ certificate.issuer }}</el-descriptions-item>
            <el-descriptions-item label="证书类型">{{ formatCertificateType(certificate.certificateType) }}</el-descriptions-item>
            <el-descriptions-item label="颁发日期">{{ formatDate(certificate.issueDate) }}</el-descriptions-item>
            <el-descriptions-item label="到期日期">{{ formatDate(certificate.expiryDate) }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(certificate.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDateTime(certificate.updatedAt) }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <!-- 编辑模式 -->
        <el-form 
          v-else 
          ref="formRef" 
          :model="editForm" 
          :rules="formRules" 
          label-width="120px"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="证书名称" prop="name">
                <el-input v-model="editForm.name" placeholder="请输入证书名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="域名" prop="domain">
                <el-input v-model="editForm.domain" placeholder="请输入域名" />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="颁发机构" prop="issuer">
                <el-input v-model="editForm.issuer" placeholder="请输入颁发机构" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="证书类型" prop="certificateType">
                <el-select v-model="editForm.certificateType" placeholder="请选择证书类型">
                  <el-option label="SSL/TLS" value="SSL/TLS" />
                  <el-option label="代码签名" value="CODE_SIGNING" />
                  <el-option label="邮件加密" value="EMAIL" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="颁发日期" prop="issueDate">
                <el-date-picker 
                  v-model="editForm.issueDate" 
                  type="date" 
                  placeholder="请选择颁发日期"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="到期日期" prop="expiryDate">
                <el-date-picker 
                  v-model="editForm.expiryDate" 
                  type="date" 
                  placeholder="请选择到期日期"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>
      
      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button @click="goBack">返回列表</el-button>
        <el-button 
          type="danger" 
          @click="handleDelete"
          data-testid="delete-button"
        >
          删除证书
        </el-button>
      </div>
    </div>
    
    <!-- 错误状态 -->
    <div v-else class="error-state">
      <el-empty description="证书不存在或加载失败">
        <el-button type="primary" @click="goBack">返回列表</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCertificateStore } from '@/stores/modules/certificate'
import { ElMessage, ElMessageBox } from 'element-plus'
import CertificateStatusBadge from '@/components/business/CertificateStatusBadge.vue'
import Loading from '@/components/common/Loading.vue'
import { formatDate, formatDateTime } from '@/utils/date'

const route = useRoute()
const router = useRouter()
const certificateStore = useCertificateStore()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const editMode = ref(false)
const formRef = ref(null)
const editForm = ref({})

// 计算属性
const certificate = computed(() => certificateStore.currentCertificate)

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入证书名称', trigger: 'blur' },
    { min: 2, max: 100, message: '证书名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  domain: [
    { required: true, message: '请输入域名', trigger: 'blur' },
    { 
      pattern: /^[a-zA-Z0-9]([a-zA-Z0-9\-]*[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9\-]*[a-zA-Z0-9])?)*$/, 
      message: '请输入有效的域名', 
      trigger: 'blur' 
    }
  ],
  issuer: [
    { required: true, message: '请输入颁发机构', trigger: 'blur' }
  ],
  certificateType: [
    { required: true, message: '请选择证书类型', trigger: 'change' }
  ],
  issueDate: [
    { required: true, message: '请选择颁发日期', trigger: 'change' }
  ],
  expiryDate: [
    { required: true, message: '请选择到期日期', trigger: 'change' }
  ]
}

// 方法
const loadCertificate = async () => {
  loading.value = true
  try {
    const certificateId = route.params.id
    await certificateStore.fetchCertificateDetail(certificateId)
  } catch (error) {
    ElMessage.error('获取证书详情失败')
    // 注意：这里不立即跳转，让用户看到错误状态
  } finally {
    loading.value = false
  }
}

const toggleEditMode = () => {
  editMode.value = true
  // 复制证书数据到编辑表单，格式化日期
  editForm.value = {
    ...certificate.value,
    issueDate: certificate.value.issueDate ? formatDate(certificate.value.issueDate) : '',
    expiryDate: certificate.value.expiryDate ? formatDate(certificate.value.expiryDate) : ''
  }
}

const cancelEdit = () => {
  editMode.value = false
  editForm.value = {}
}

const saveChanges = async () => {
  try {
    await formRef.value.validate()
    saving.value = true
    
    await certificateStore.updateExistingCertificate(certificate.value.id, editForm.value)
    editMode.value = false
    ElMessage.success('证书信息更新成功')
  } catch (error) {
    if (error.message) {
      ElMessage.error('证书信息更新失败: ' + error.message)
    } else {
      ElMessage.error('证书信息更新失败')
    }
  } finally {
    saving.value = false
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      '确认删除此证书？删除后无法恢复。', 
      '警告', 
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await certificateStore.removeExistingCertificate(certificate.value.id)
    ElMessage.success('证书删除成功')
    router.push('/certificates')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('证书删除失败')
    }
  }
}

const goBack = () => {
  router.push('/certificates')
}

const formatCertificateType = (type) => {
  const typeMap = {
    'SSL/TLS': 'SSL/TLS证书',
    'CODE_SIGNING': '代码签名证书',
    'EMAIL': '邮件加密证书'
  }
  return typeMap[type] || type
}

// 生命周期
onMounted(() => {
  loadCertificate()
})
</script>

<style scoped>
.certificate-detail {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 10px 0;
  font-size: 24px;
  font-weight: 500;
}

.detail-content .el-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-actions {
  display: flex;
  gap: 10px;
}

.action-buttons {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}

.error-state {
  text-align: center;
  padding: 40px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .certificate-detail {
    padding: 10px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons .el-button {
    width: 100%;
  }
}
</style>