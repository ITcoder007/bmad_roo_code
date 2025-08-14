import request from './request'

// 监控日志相关API
export const monitoringApi = {
  // 获取监控日志列表
  getLogs(params) {
    return request({
      url: '/monitoring-logs',
      method: 'get',
      params
    })
  }
}