/**
 * 认证状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getCurrentUser } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)
  const isLoading = ref(false)

  // 计算属性
  const isAuthenticated = computed(() => !!token.value)
  const userInfo = computed(() => user.value)

  // 获取当前用户信息
  const fetchUserInfo = async () => {
    if (!token.value) return null
    
    try {
      const userInfo = await getCurrentUser()
      user.value = userInfo
      return userInfo
    } catch (error) {
      console.error('获取用户信息失败:', error)
      await handleLogout()
      return null
    }
  }

  // 登录
  const handleLogin = async (credentials) => {
    try {
      isLoading.value = true
      const result = await login(credentials)
      
      token.value = result.token
      user.value = result.user
      
      // 存储到本地存储
      localStorage.setItem('token', result.token)
      
      return result
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 登出
  const handleLogout = async () => {
    try {
      if (token.value) {
        await logout()
      }
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      // 清除本地状态
      token.value = ''
      user.value = null
      localStorage.removeItem('token')
    }
  }

  // 检查认证状态
  const checkAuth = async () => {
    if (token.value && !user.value) {
      await fetchUserInfo()
    }
    return isAuthenticated.value
  }

  // 更新用户信息
  const updateUserInfo = (newUserInfo) => {
    user.value = { ...user.value, ...newUserInfo }
  }

  return {
    // 状态
    token,
    user,
    isLoading,
    
    // 计算属性
    isAuthenticated,
    userInfo,
    
    // 方法
    handleLogin,
    handleLogout,
    fetchUserInfo,
    checkAuth,
    updateUserInfo
  }
})