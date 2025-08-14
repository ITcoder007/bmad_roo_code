import { defineStore } from 'pinia'
import { certificateApi } from '@/api/certificate'

export const useCertificateStore = defineStore('certificate', {
  state: () => ({
    certificates: [],
    currentCertificate: null,
    loading: false,
    total: 0,
    queryParams: {
      page: 1,
      size: 20,
      keyword: '',
      status: ''
    }
  }),
  
  actions: {
    // 获取证书列表
    async fetchCertificates() {
      this.loading = true
      try {
        const res = await certificateApi.getList(this.queryParams)
        if (res.success) {
          this.certificates = res.data.content
          this.total = res.data.total
        }
      } finally {
        this.loading = false
      }
    },
    
    // 获取证书详情
    async fetchCertificateById(id) {
      this.loading = true
      try {
        const res = await certificateApi.getById(id)
        if (res.success) {
          this.currentCertificate = res.data
        }
        return res.data
      } finally {
        this.loading = false
      }
    },
    
    // 创建证书
    async createCertificate(data) {
      const res = await certificateApi.create(data)
      if (res.success) {
        await this.fetchCertificates()
      }
      return res
    },
    
    // 更新证书
    async updateCertificate(id, data) {
      const res = await certificateApi.update(id, data)
      if (res.success) {
        await this.fetchCertificates()
      }
      return res
    },
    
    // 删除证书
    async deleteCertificate(id) {
      const res = await certificateApi.delete(id)
      if (res.success) {
        await this.fetchCertificates()
      }
      return res
    },
    
    // 设置查询参数
    setQueryParams(params) {
      this.queryParams = { ...this.queryParams, ...params }
    }
  },
  
  getters: {
    // 即将过期的证书
    expiringCertificates: (state) => {
      return state.certificates.filter(c => c.status === 'EXPIRING_SOON')
    },
    
    // 已过期的证书
    expiredCertificates: (state) => {
      return state.certificates.filter(c => c.status === 'EXPIRED')
    }
  }
})