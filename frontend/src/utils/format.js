import dayjs from 'dayjs'

// 格式化日期
export function formatDate(date, format = 'YYYY-MM-DD') {
  if (!date) return '-'
  return dayjs(date).format(format)
}

// 格式化日期时间
export function formatDateTime(date) {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 获取状态标签类型
export function getStatusType(status) {
  const map = {
    'NORMAL': 'success',
    'EXPIRING_SOON': 'warning',
    'EXPIRED': 'danger'
  }
  return map[status] || 'info'
}

// 获取状态文本
export function getStatusText(status) {
  const map = {
    'NORMAL': '正常',
    'EXPIRING_SOON': '即将过期',
    'EXPIRED': '已过期'
  }
  return map[status] || status
}

// 获取日志类型文本
export function getLogTypeText(type) {
  const map = {
    'MONITORING': '监控检查',
    'ALERT_EMAIL': '邮件预警',
    'ALERT_SMS': '短信预警'
  }
  return map[type] || type
}