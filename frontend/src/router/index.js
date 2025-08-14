import { createRouter, createWebHistory } from 'vue-router'

// 路由配置
const routes = [
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '仪表板' }
      },
      {
        path: 'certificates',
        name: 'CertificateList',
        component: () => import('@/views/certificate/List.vue'),
        meta: { title: '证书列表' }
      },
      {
        path: 'certificates/:id',
        name: 'CertificateDetail',
        component: () => import('@/views/certificate/Detail.vue'),
        meta: { title: '证书详情' }
      },
      {
        path: 'certificates-create',
        name: 'CertificateCreate',
        component: () => import('@/views/certificate/Form.vue'),
        meta: { title: '创建证书', mode: 'create' }
      },
      {
        path: 'certificates-edit/:id',
        name: 'CertificateEdit',
        component: () => import('@/views/certificate/Form.vue'),
        meta: { title: '编辑证书', mode: 'edit' }
      },
      {
        path: 'monitoring-logs',
        name: 'MonitoringLogs',
        component: () => import('@/views/monitoring/Logs.vue'),
        meta: { title: '监控日志' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 证书管理系统`
  }
  next()
})

export default router