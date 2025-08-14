import request from './request'

// 系统相关API
export const systemApi = {
  // 获取系统状态
  getStatus() {
    return request({
      url: '/system/status',
      method: 'get'
    })
  }
}