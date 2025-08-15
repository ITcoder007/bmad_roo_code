/**
 * 系统管理相关 API
 */
import { http } from '@/utils/request'

const API_PREFIX = '/system'

/**
 * 用户管理 API
 */
export const users = {
  /**
   * 获取用户列表
   * @param {object} params 查询参数
   * @returns {Promise} 用户列表
   */
  getList(params = {}) {
    return http.get(`${API_PREFIX}/users`, params)
  },

  /**
   * 获取用户详情
   * @param {number} id 用户ID
   * @returns {Promise} 用户详情
   */
  getById(id) {
    return http.get(`${API_PREFIX}/users/${id}`)
  },

  /**
   * 创建用户
   * @param {object} data 用户数据
   * @returns {Promise} 创建结果
   */
  create(data) {
    return http.post(`${API_PREFIX}/users`, data)
  },

  /**
   * 更新用户
   * @param {number} id 用户ID
   * @param {object} data 更新数据
   * @returns {Promise} 更新结果
   */
  update(id, data) {
    return http.put(`${API_PREFIX}/users/${id}`, data)
  },

  /**
   * 删除用户
   * @param {number} id 用户ID
   * @returns {Promise} 删除结果
   */
  delete(id) {
    return http.delete(`${API_PREFIX}/users/${id}`)
  }
}

/**
 * 角色管理 API
 */
export const roles = {
  /**
   * 获取角色列表
   * @param {object} params 查询参数
   * @returns {Promise} 角色列表
   */
  getList(params = {}) {
    return http.get(`${API_PREFIX}/roles`, params)
  },

  /**
   * 获取角色详情
   * @param {number} id 角色ID
   * @returns {Promise} 角色详情
   */
  getById(id) {
    return http.get(`${API_PREFIX}/roles/${id}`)
  },

  /**
   * 创建角色
   * @param {object} data 角色数据
   * @returns {Promise} 创建结果
   */
  create(data) {
    return http.post(`${API_PREFIX}/roles`, data)
  },

  /**
   * 更新角色
   * @param {number} id 角色ID
   * @param {object} data 更新数据
   * @returns {Promise} 更新结果
   */
  update(id, data) {
    return http.put(`${API_PREFIX}/roles/${id}`, data)
  },

  /**
   * 删除角色
   * @param {number} id 角色ID
   * @returns {Promise} 删除结果
   */
  delete(id) {
    return http.delete(`${API_PREFIX}/roles/${id}`)
  }
}

/**
 * 系统设置 API
 */
export const settings = {
  /**
   * 获取系统设置
   * @returns {Promise} 系统设置
   */
  get() {
    return http.get(`${API_PREFIX}/settings`)
  },

  /**
   * 更新系统设置
   * @param {object} data 设置数据
   * @returns {Promise} 更新结果
   */
  update(data) {
    return http.put(`${API_PREFIX}/settings`, data)
  },

  /**
   * 重置系统设置
   * @returns {Promise} 重置结果
   */
  reset() {
    return http.post(`${API_PREFIX}/settings/reset`)
  }
}