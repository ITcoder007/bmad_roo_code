/**
 * 证书相关路由配置
 */
export default [
  {
    path: 'certificates',
    name: 'CertificateList',
    component: () => import('@/views/certificate/CertificateList.vue'),
    meta: { 
      title: '证书列表',
      icon: 'Document',
      permissions: ['certificate:view']
    }
  },
  {
    path: 'certificates/add',
    name: 'CertificateAdd',
    component: () => import('@/views/certificate/CertificateForm.vue'),
    meta: { 
      title: '添加证书',
      icon: 'Plus',
      permissions: ['certificate:create'],
      hideInMenu: true
    }
  },
  {
    path: 'certificates/:id',
    name: 'CertificateDetail',
    component: () => import('@/views/certificate/CertificateDetail.vue'),
    meta: { 
      title: '证书详情',
      icon: 'View',
      permissions: ['certificate:view'],
      hideInMenu: true
    }
  },
  {
    path: 'certificates/:id/edit',
    name: 'CertificateEdit',
    component: () => import('@/views/certificate/CertificateForm.vue'),
    meta: { 
      title: '编辑证书',
      icon: 'Edit',
      permissions: ['certificate:update'],
      hideInMenu: true
    }
  }
]