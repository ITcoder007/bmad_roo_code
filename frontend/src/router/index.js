import { createRouter, createWebHistory } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: DefaultLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/monitoring/Dashboard.vue'),
        meta: { title: '仪表板' }
      },
      {
        path: 'certificates',
        name: 'CertificateList',
        component: () => import('@/views/certificate/CertificateList.vue'),
        meta: { title: '证书列表' }
      },
      {
        path: 'certificates/add',
        name: 'CertificateAdd',
        component: () => import('@/views/certificate/CertificateForm.vue'),
        meta: { title: '添加证书' }
      },
      {
        path: 'certificates/:id',
        name: 'CertificateDetail',
        component: () => import('@/views/certificate/CertificateDetail.vue'),
        meta: { title: '证书详情' }
      },
      {
        path: 'certificates/:id/edit',
        name: 'CertificateEdit',
        component: () => import('@/views/certificate/CertificateForm.vue'),
        meta: { title: '编辑证书' }
      },
      {
        path: 'monitoring/dashboard',
        name: 'MonitoringDashboard',
        component: () => import('@/views/monitoring/Dashboard.vue'),
        meta: { title: '监控仪表板' }
      },
      {
        path: 'monitoring/alerts',
        name: 'MonitoringAlerts',
        component: () => import('@/views/monitoring/Alerts.vue'),
        meta: { title: '预警信息' }
      },
      {
        path: 'monitoring/logs',
        name: 'MonitoringLogs',
        component: () => import('@/views/monitoring/Logs.vue'),
        meta: { title: '监控日志' }
      },
      {
        path: 'system/users',
        name: 'SystemUsers',
        component: () => import('@/views/system/Users.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'system/roles',
        name: 'SystemRoles',
        component: () => import('@/views/system/Roles.vue'),
        meta: { title: '角色管理' }
      },
      {
        path: 'system/settings',
        name: 'SystemSettings',
        component: () => import('@/views/system/Settings.vue'),
        meta: { title: '系统设置' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { title: '页面未找到' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 导入路由守卫
import { 
  authGuard, 
  permissionGuard, 
  titleGuard, 
  progressGuard,
  progressDoneGuard 
} from './guards'

// 全局前置守卫
router.beforeEach((to, from, next) => {
  progressGuard(to, from, next)
})

router.beforeEach((to, from, next) => {
  titleGuard(to, from, next)
})

router.beforeEach((to, from, next) => {
  authGuard(to, from, next)
})

router.beforeEach((to, from, next) => {
  permissionGuard(to, from, next)
})

// 全局后置守卫
router.afterEach((to, from) => {
  progressDoneGuard(to, from)
})

export default router