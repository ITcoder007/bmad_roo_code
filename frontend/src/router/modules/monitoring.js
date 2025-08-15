/**
 * 监控相关路由配置
 */
export default [
  {
    path: 'monitoring/dashboard',
    name: 'MonitoringDashboard',
    component: () => import('@/views/monitoring/Dashboard.vue'),
    meta: { 
      title: '监控仪表板',
      icon: 'DataAnalysis',
      permissions: ['monitoring:view']
    }
  },
  {
    path: 'monitoring/alerts',
    name: 'MonitoringAlerts',
    component: () => import('@/views/monitoring/Alerts.vue'),
    meta: { 
      title: '预警信息',
      icon: 'Warning',
      permissions: ['monitoring:alert:view']
    }
  },
  {
    path: 'monitoring/logs',
    name: 'MonitoringLogs',
    component: () => import('@/views/monitoring/Logs.vue'),
    meta: { 
      title: '监控日志',
      icon: 'DocumentCopy',
      permissions: ['monitoring:log:view']
    }
  }
]