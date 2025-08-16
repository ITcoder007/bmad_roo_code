<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>证书生命周期管理系统</h2>
      <el-alert
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <template #title>
          默认账号：admin / admin123
        </template>
      </el-alert>
      <el-form
        :model="loginForm"
        @submit.prevent="handleLogin"
      >
        <el-form-item>
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            style="width: 100%"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const loginForm = reactive({
      username: 'admin',  // 默认填充用户名
      password: ''
    })

    const handleLogin = async () => {
      if (!loginForm.username || !loginForm.password) {
        ElMessage.error('请输入用户名和密码')
        return
      }
      
      loading.value = true
      try {
        // 由于后端使用 Basic Auth，我们将凭据编码存储
        const credentials = btoa(`${loginForm.username}:${loginForm.password}`)
        
        // 测试认证是否有效 - 尝试获取证书列表
        const response = await fetch('/api/api/v1/certificates?page=1&size=1', {
          headers: {
            'Authorization': `Basic ${credentials}`
          }
        })
        
        if (response.ok) {
          // 认证成功，存储凭据
          localStorage.setItem('token', credentials)
          localStorage.setItem('username', loginForm.username)
          ElMessage.success('登录成功')
          router.push('/dashboard')
        } else if (response.status === 401) {
          ElMessage.error('用户名或密码错误')
        } else {
          ElMessage.error('登录失败，请稍后重试')
        }
      } catch (error) {
        console.error('Login error:', error)
        ElMessage.error('网络错误，请检查服务器连接')
      } finally {
        loading.value = false
      }
    }

    return {
      loginForm,
      loading,
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
  padding: 20px;
}

.login-card h2 {
  text-align: center;
  margin-bottom: 30px;
}
</style>