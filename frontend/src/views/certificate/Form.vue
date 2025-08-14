<template>
  <div class="certificate-form">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑证书' : '添加证书' }}</span>
      </template>
      
      <el-form 
        ref="formRef"
        :model="form" 
        :rules="rules" 
        label-width="120px"
        style="max-width: 600px"
      >
        <el-form-item label="证书名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入证书名称" />
        </el-form-item>
        
        <el-form-item label="域名" prop="domain">
          <el-input v-model="form.domain" placeholder="请输入域名" />
        </el-form-item>
        
        <el-form-item label="颁发机构" prop="issuer">
          <el-input v-model="form.issuer" placeholder="请输入颁发机构" />
        </el-form-item>
        
        <el-form-item label="证书类型" prop="certificateType">
          <el-select v-model="form.certificateType" placeholder="请选择证书类型">
            <el-option label="SSL" value="SSL" />
            <el-option label="Code Signing" value="Code Signing" />
            <el-option label="Email" value="Email" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="颁发日期" prop="issueDate">
          <el-date-picker 
            v-model="form.issueDate" 
            type="date" 
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item label="到期日期" prop="expiryDate">
          <el-date-picker 
            v-model="form.expiryDate" 
            type="date" 
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item label="备注">
          <el-input 
            v-model="form.notes" 
            type="textarea" 
            :rows="3"
            placeholder="请输入备注信息" 
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">保存</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCertificateStore } from '@/stores/certificate'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const certificateStore = useCertificateStore()

const formRef = ref()
const isEdit = ref(false)
const form = reactive({
  name: '',
  domain: '',
  issuer: '',
  certificateType: 'SSL',
  issueDate: '',
  expiryDate: '',
  notes: ''
})

const rules = {
  name: [
    { required: true, message: '请输入证书名称', trigger: 'blur' }
  ],
  domain: [
    { required: true, message: '请输入域名', trigger: 'blur' }
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

const loadCertificate = async () => {
  const id = route.params.id
  if (id) {
    isEdit.value = true
    const data = await certificateStore.fetchCertificateById(id)
    Object.assign(form, data)
  }
}

const handleSubmit = async () => {
  await formRef.value.validate()
  
  try {
    let res
    if (isEdit.value) {
      res = await certificateStore.updateCertificate(route.params.id, form)
    } else {
      res = await certificateStore.createCertificate(form)
    }
    
    if (res.success) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      router.push('/certificates')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleCancel = () => {
  router.push('/certificates')
}

onMounted(() => {
  loadCertificate()
})
</script>

<style scoped>
.certificate-form {
  padding: 0;
}
</style>