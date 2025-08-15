/**
 * 监控相关 API
 */
import { http } from '@/utils/request'

const API_PREFIX = '/monitoring'

/**
 * 获取监控仪表板数据
 * @returns {Promise} 仪表板数据
 */
export function getDashboardData() {
  return http.get(`${API_PREFIX}/dashboard`)
}

/**
 * 获取预警信息列表
 * @param {object} params 查询参数
 * @returns {Promise} 预警信息列表
 */
export function getAlertList(params = {}) {
  return http.get(`${API_PREFIX}/alerts`, params)
}

/**
 * 获取监控日志列表
 * @param {object} params 查询参数
 * @returns {Promise} 监控日志列表
 */
export function getMonitoringLogs(params = {}) {
  return http.get(`${API_PREFIX}/logs`, params)
}

/**
 * 手动触发监控检查
 * @returns {Promise} 触发结果
 */
export function triggerMonitoring() {
  return http.post(`${API_PREFIX}/trigger`)
}

/**
 * 获取系统健康状态
 * @returns {Promise} 健康状态
 */
export function getSystemHealth() {
  return http.get(`${API_PREFIX}/health`)
}