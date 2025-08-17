<template>
  <div class="certificate-form">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">
        {{ isEdit ? '编辑证书' : '添加证书' }}
      </h1>
      <p class="page-description">
        {{ isEdit ? '修改现有证书信息' : '创建新的证书记录' }}
      </p>
    </div>

    <!-- 表单内容 -->
    <div class="content-section">
      <el-card>
        <BaseForm
          v-model="formData"
          :rules="formRules"
          :submit-loading="submitLoading"
          label-width="120px"
          :show-cancel="true"
          @submit="handleSubmit"
          @cancel="handleCancel"
        >
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item
                label="证书名称"
                prop="name"
              >
                <el-input
                  v-model="formData.name"
                  placeholder="请输入证书名称"
                  clearable
                />
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item
                label="域名"
                prop="domain"
              >
                <el-input
                  v-model="formData.domain"
                  placeholder="请输入域名"
                  clearable
                />
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item
                label="颁发机构"
                prop="issuer"
              >
                <el-input
                  v-model="formData.issuer"
                  placeholder="请输入颁发机构"
                  clearable
                />
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item
                label="证书类型"
                prop="certificateType"
              >
                <el-select
                  v-model="formData.certificateType"
                  placeholder="请选择证书类型"
                  style="width: 100%"
                >
                  <el-option
                    label="SSL/TLS"
                    value="SSL"
                  />
                  <el-option
                    label="代码签名"
                    value="CODE_SIGNING"
                  />
                  <el-option
                    label="邮件加密"
                    value="EMAIL"
                  />
                  <el-option
                    label="客户端认证"
                    value="CLIENT_AUTH"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item
                label="颁发日期"
                prop="issueDate"
              >
                <el-date-picker
                  v-model="formData.issueDate"
                  type="date"
                  placeholder="选择颁发日期"
                  style="width: 100%"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item
                label="过期日期"
                prop="expiryDate"
              >
                <el-date-picker
                  v-model="formData.expiryDate"
                  type="date"
                  placeholder="选择过期日期"
                  style="width: 100%"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  :disabled-date="disabledDate"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </BaseForm>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import BaseForm from '@/components/common/BaseForm.vue'
import { getCertificateById, createCertificate, updateCertificate } from '@/api/certificate'

const route = useRoute()
const router = useRouter()

// 响应式数据
const submitLoading = ref(false)
const formData = reactive({
  name: '',
  domain: '',
  issuer: '',
  certificateType: '',
  issueDate: '',
  expiryDate: ''
})

// 计算属性
const isEdit = computed(() => !!route.params.id)

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入证书名称', trigger: 'blur' },
    { min: 2, max: 100, message: '证书名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  domain: [
    { required: true, message: '请输入域名', trigger: 'blur' },
    { 
      pattern: /^[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/,
      message: '请输入有效的域名格式',
      trigger: 'blur'
    }
  ],
  issuer: [
    { required: true, message: '请输入颁发机构', trigger: 'blur' },
    { min: 2, max: 100, message: '颁发机构长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  certificateType: [
    { required: true, message: '请选择证书类型', trigger: 'change' }
  ],
  issueDate: [
    { required: true, message: '请选择颁发日期', trigger: 'change' }
  ],
  expiryDate: [
    { required: true, message: '请选择过期日期', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value && formData.issueDate && new Date(value) <= new Date(formData.issueDate)) {
          callback(new Error('过期日期必须晚于颁发日期'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 禁用日期函数
const disabledDate = (time) => {
  // 禁用早于颁发日期的日期
  if (formData.issueDate) {
    return time.getTime() <= new Date(formData.issueDate).getTime()
  }
  return false
}

// 加载证书详情（编辑模式）
const loadCertificateDetail = async () => {
  if (!isEdit.value) return
  
  try {
    const id = route.params.id
    const certificate = await getCertificateById(id)
    
    // 填充表单数据
    Object.assign(formData, {
      name: certificate.name,
      domain: certificate.domain,
      issuer: certificate.issuer,
      certificateType: certificate.certificateType,
      issueDate: certificate.issueDate,
      expiryDate: certificate.expiryDate
    })
  } catch (error) {
    console.error('加载证书详情失败:', error)
    ElMessage.error('加载证书详情失败')
    router.back()
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    submitLoading.value = true
    
    if (isEdit.value) {
      // 编辑模式
      const id = route.params.id
      await updateCertificate(id, formData)
      ElMessage.success('证书更新成功')
    } else {
      // 新增模式
      await createCertificate(formData)
      ElMessage.success('证书创建成功')
    }
    
    // 返回列表页面
    router.push('/certificates')
  } catch (error) {
    console.error('保存证书失败:', error)
    ElMessage.error(isEdit.value ? '证书更新失败' : '证书创建失败')
  } finally {
    submitLoading.value = false
  }
}

// 取消操作
const handleCancel = () => {
  router.back()
}

// 生命周期
onMounted(() => {
  loadCertificateDetail()
})
</script>

<style scoped>
.certificate-form {
  padding: 0;
}

.content-section {
  background: #fff;
  border-radius: 6px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .certificate-form :deep(.el-col-12) {
    flex: 0 0 100%;
    max-width: 100%;
  }
}
</style>