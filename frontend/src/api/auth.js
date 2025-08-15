/**
 * 认证相关 API
 */
import { http } from '@/utils/request'

const API_PREFIX = '/auth'

/**
 * 用户登录
 * @param {object} credentials 登录凭据
 * @param {string} credentials.username 用户名
 * @param {string} credentials.password 密码
 * @returns {Promise} 登录结果
 */
export function login(credentials) {
  return http.post(`${API_PREFIX}/login`, credentials)
}

/**
 * 用户登出
 * @returns {Promise} 登出结果
 */
export function logout() {
  return http.post(`${API_PREFIX}/logout`)
}

/**
 * 刷新令牌
 * @returns {Promise} 新令牌
 */
export function refreshToken() {
  return http.post(`${API_PREFIX}/refresh`)
}

/**
 * 获取当前用户信息
 * @returns {Promise} 用户信息
 */
export function getCurrentUser() {
  return http.get(`${API_PREFIX}/me`)
}

/**
 * 修改密码
 * @param {object} data 密码数据
 * @param {string} data.oldPassword 旧密码
 * @param {string} data.newPassword 新密码
 * @returns {Promise} 修改结果
 */
export function changePassword(data) {
  return http.post(`${API_PREFIX}/change-password`, data)
}