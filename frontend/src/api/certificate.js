/**
 * 证书管理相关 API
 * 包含日期格式自动转换
 */
import { http } from '@/utils/request'

const API_PREFIX = '/certificates'

/**
 * 获取证书列表
 * @param {object} params 查询参数
 * @param {number} params.page 页码
 * @param {number} params.size 每页大小
 * @param {string} params.keyword 关键词搜索（向后兼容）
 * @param {string} params.search 搜索关键词（按证书名称和域名搜索）
 * @param {string} params.status 证书状态
 * @param {string} params.sort 排序字段
 * @returns {Promise} 证书列表
 */
export function getCertificateList(params = {}) {
  return http.get(API_PREFIX, params)
}

/**
 * 获取证书详情
 * @param {number} id 证书ID
 * @returns {Promise} 证书详情
 */
export function getCertificateById(id) {
  return http.get(`${API_PREFIX}/${id}`)
}

/**
 * 创建证书
 * @param {object} data 证书数据
 * @param {string} data.name 证书名称
 * @param {string} data.domain 域名
 * @param {string} data.issuer 颁发机构
 * @param {string} data.issueDate 颁发日期
 * @param {string} data.expiryDate 过期日期
 * @param {string} data.certificateType 证书类型
 * @returns {Promise} 创建结果
 */
export function createCertificate(data) {
  // 转换日期格式为 ISO 8601
  const formattedData = {
    ...data,
    issueDate: data.issueDate ? new Date(data.issueDate).toISOString() : null,
    expiryDate: data.expiryDate ? new Date(data.expiryDate).toISOString() : null
  }
  return http.post(API_PREFIX, formattedData)
}

/**
 * 更新证书
 * @param {number} id 证书ID
 * @param {object} data 更新数据
 * @returns {Promise} 更新结果
 */
export function updateCertificate(id, data) {
  // 转换日期格式为 ISO 8601
  const formattedData = {
    ...data,
    issueDate: data.issueDate ? new Date(data.issueDate).toISOString() : null,
    expiryDate: data.expiryDate ? new Date(data.expiryDate).toISOString() : null
  }
  return http.put(`${API_PREFIX}/${id}`, formattedData)
}

/**
 * 删除证书
 * @param {number} id 证书ID
 * @returns {Promise} 删除结果
 */
export function deleteCertificate(id) {
  return http.delete(`${API_PREFIX}/${id}`)
}

/**
 * 批量删除证书
 * @param {Array<number>} ids 证书ID数组
 * @returns {Promise} 删除结果
 */
export function batchDeleteCertificates(ids) {
  return http.delete(`${API_PREFIX}/batch`, {
    data: { ids }
  })
}

/**
 * 手动更新证书状态
 * @param {number} id 证书ID
 * @returns {Promise} 更新结果
 */
export function updateCertificateStatus(id) {
  return http.post(`${API_PREFIX}/${id}/status`)
}

/**
 * 批量更新证书状态
 * @returns {Promise} 更新结果
 */
export function batchUpdateCertificateStatus() {
  return http.post(`${API_PREFIX}/batch/status`)
}

/**
 * 导出证书数据
 * @param {object} params 导出参数
 * @param {string} params.format 导出格式 (excel, csv, pdf)
 * @param {Array<number>} params.ids 要导出的证书ID (可选，不传则导出全部)
 * @returns {Promise} 导出结果
 */
export function exportCertificates(params = {}) {
  const { format = 'excel', ids } = params
  
  return http.download(
    `${API_PREFIX}/export`,
    `certificates.${format}`,
    { format, ids: ids ? ids.join(',') : undefined }
  )
}

/**
 * 导入证书数据
 * @param {File} file 导入文件
 * @param {function} onProgress 上传进度回调
 * @returns {Promise} 导入结果
 */
export function importCertificates(file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)
  
  return http.upload(`${API_PREFIX}/import`, formData, onProgress)
}

/**
 * 获取证书统计信息
 * @returns {Promise} 统计信息
 */
export function getCertificateStatistics() {
  return http.get(`${API_PREFIX}/statistics`)
}

/**
 * 获取即将过期的证书
 * @param {number} days 天数阈值，默认30天
 * @returns {Promise} 即将过期的证书列表
 */
export function getExpiringCertificates(days = 30) {
  return http.get(`${API_PREFIX}/expiring`, { days })
}

/**
 * 验证证书信息
 * @param {object} data 证书数据
 * @returns {Promise} 验证结果
 */
export function validateCertificate(data) {
  return http.post(`${API_PREFIX}/validate`, data)
}

/**
 * 搜索证书
 * @param {string} query 搜索查询
 * @param {object} options 搜索选项
 * @param {number} options.page 页码
 * @param {number} options.size 每页大小
 * @param {string} options.status 状态筛选
 * @param {string} options.sort 排序字段
 * @returns {Promise} 搜索结果
 */
export function searchCertificates(query, options = {}) {
  const { page = 1, size = 20, status, sort } = options
  
  return http.get(API_PREFIX, {
    search: query,
    page,
    size,
    status,
    sort
  })
}

/**
 * 获取搜索建议
 * @param {string} query 搜索查询
 * @param {number} limit 结果限制
 * @returns {Promise} 搜索建议列表
 */
export function getCertificateSearchSuggestions(query, limit = 5) {
  return http.get(`${API_PREFIX}/suggestions`, {
    q: query,
    limit
  })
}

/**
 * 获取仪表板统计信息
 * @returns {Promise} 仪表板统计数据
 */
export function getDashboardStats() {
  return http.get(`${API_PREFIX}/dashboard/stats`)
}

/**
 * 获取即将过期的证书（仪表板专用）
 * @param {number} days 天数阈值，默认7天
 * @returns {Promise} 即将过期的证书列表
 */
export function getExpiringCertificatesForDashboard(days = 7) {
  return http.get(`${API_PREFIX}/expiring`, { days })
}

/**
 * 获取最近添加的证书
 * @param {number} limit 数量限制，默认5个
 * @returns {Promise} 最近添加的证书列表
 */
export function getRecentCertificates(limit = 5) {
  return http.get(`${API_PREFIX}/recent`, { limit })
}