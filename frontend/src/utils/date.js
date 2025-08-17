/**
 * 日期格式化工具函数
 */

/**
 * 格式化日期为 YYYY-MM-DD 格式
 * @param {string|Date} date 日期
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date) {
  if (!date) return ''
  
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).replace(/\//g, '-')
}

/**
 * 格式化日期时间为 YYYY-MM-DD HH:mm:ss 格式
 * @param {string|Date} date 日期时间
 * @returns {string} 格式化后的日期时间字符串
 */
export function formatDateTime(date) {
  if (!date) return ''
  
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  }).replace(/\//g, '-')
}

/**
 * 计算距离今天的天数
 * @param {string|Date} date 目标日期
 * @returns {number} 天数差（正数表示未来，负数表示过去）
 */
export function getDaysFromToday(date) {
  if (!date) return 0
  
  const d = new Date(date)
  if (isNaN(d.getTime())) return 0
  
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  d.setHours(0, 0, 0, 0)
  
  return Math.ceil((d.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
}

/**
 * 格式化相对时间（如：3天前，5天后）
 * @param {string|Date} date 日期
 * @returns {string} 相对时间字符串
 */
export function formatRelativeTime(date) {
  const days = getDaysFromToday(date)
  
  if (days === 0) return '今天'
  if (days === 1) return '明天'
  if (days === -1) return '昨天'
  if (days > 0) return `${days}天后`
  return `${Math.abs(days)}天前`
}