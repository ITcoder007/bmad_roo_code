import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/monitoring/Dashboard.vue'),
    meta: { title: '仪表板', requiresAuth: true }
  },
  {
    path: '/certificates',
    name: 'Certificates',
    component: () => import('../views/certificate/CertificateList.vue'),
    meta: { title: '证书管理', requiresAuth: true }
  },
  {
    path: '/certificates/:id',
    name: 'CertificateDetail',
    component: () => import('../views/certificate/CertificateDetail.vue'),
    meta: { title: '证书详情', requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/error/NotFound.vue'),
    meta: { title: '页面未找到' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || '证书生命周期管理系统'}`
  
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    if (!token) {
      next('/login')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router