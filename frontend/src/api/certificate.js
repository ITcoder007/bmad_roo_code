import request from './request'

// 证书相关API
export const certificateApi = {
  // 获取证书列表
  getList(params) {
    return request({
      url: '/certificates',
      method: 'get',
      params
    })
  },
  
  // 获取证书详情
  getById(id) {
    return request({
      url: `/certificates/${id}`,
      method: 'get'
    })
  },
  
  // 创建证书
  create(data) {
    return request({
      url: '/certificates',
      method: 'post',
      data
    })
  },
  
  // 更新证书
  update(id, data) {
    return request({
      url: `/certificates/${id}`,
      method: 'put',
      data
    })
  },
  
  // 删除证书
  delete(id) {
    return request({
      url: `/certificates/${id}`,
      method: 'delete'
    })
  }
}