import { describe, it, expect } from 'vitest'
import certificateRoutes from '@/router/modules/certificate'
import monitoringRoutes from '@/router/modules/monitoring'
import systemRoutes from '@/router/modules/system'

describe('Router Modules', () => {
  describe('Certificate Routes', () => {
    it('exports an array of routes', () => {
      expect(Array.isArray(certificateRoutes)).toBe(true)
      expect(certificateRoutes.length).toBeGreaterThan(0)
    })

    it('contains certificate list route', () => {
      const listRoute = certificateRoutes.find(route => route.name === 'CertificateList')
      
      expect(listRoute).toBeDefined()
      expect(listRoute.path).toBe('certificates')
      expect(listRoute.meta.title).toBe('证书列表')
      expect(listRoute.meta.permissions).toContain('certificate:view')
    })

    it('contains certificate detail route', () => {
      const detailRoute = certificateRoutes.find(route => route.name === 'CertificateDetail')
      
      expect(detailRoute).toBeDefined()
      expect(detailRoute.path).toBe('certificates/:id')
      expect(detailRoute.meta.hideInMenu).toBe(true)
    })

    it('contains certificate form routes', () => {
      const addRoute = certificateRoutes.find(route => route.name === 'CertificateAdd')
      const editRoute = certificateRoutes.find(route => route.name === 'CertificateEdit')
      
      expect(addRoute).toBeDefined()
      expect(addRoute.path).toBe('certificates/add')
      expect(addRoute.meta.permissions).toContain('certificate:create')
      
      expect(editRoute).toBeDefined()
      expect(editRoute.path).toBe('certificates/:id/edit')
      expect(editRoute.meta.permissions).toContain('certificate:update')
    })
  })

  describe('Monitoring Routes', () => {
    it('exports an array of routes', () => {
      expect(Array.isArray(monitoringRoutes)).toBe(true)
      expect(monitoringRoutes.length).toBeGreaterThan(0)
    })

    it('contains monitoring dashboard route', () => {
      const dashboardRoute = monitoringRoutes.find(route => route.name === 'MonitoringDashboard')
      
      expect(dashboardRoute).toBeDefined()
      expect(dashboardRoute.path).toBe('monitoring/dashboard')
      expect(dashboardRoute.meta.title).toBe('监控仪表板')
    })

    it('contains alerts and logs routes', () => {
      const alertsRoute = monitoringRoutes.find(route => route.name === 'MonitoringAlerts')
      const logsRoute = monitoringRoutes.find(route => route.name === 'MonitoringLogs')
      
      expect(alertsRoute).toBeDefined()
      expect(alertsRoute.path).toBe('monitoring/alerts')
      
      expect(logsRoute).toBeDefined()
      expect(logsRoute.path).toBe('monitoring/logs')
    })
  })

  describe('System Routes', () => {
    it('exports an array of routes', () => {
      expect(Array.isArray(systemRoutes)).toBe(true)
      expect(systemRoutes.length).toBeGreaterThan(0)
    })

    it('contains user management route', () => {
      const usersRoute = systemRoutes.find(route => route.name === 'SystemUsers')
      
      expect(usersRoute).toBeDefined()
      expect(usersRoute.path).toBe('system/users')
      expect(usersRoute.meta.title).toBe('用户管理')
      expect(usersRoute.meta.permissions).toContain('system:user:view')
    })

    it('contains role and settings routes', () => {
      const rolesRoute = systemRoutes.find(route => route.name === 'SystemRoles')
      const settingsRoute = systemRoutes.find(route => route.name === 'SystemSettings')
      
      expect(rolesRoute).toBeDefined()
      expect(rolesRoute.path).toBe('system/roles')
      
      expect(settingsRoute).toBeDefined()
      expect(settingsRoute.path).toBe('system/settings')
    })

    it('all routes have required meta properties', () => {
      systemRoutes.forEach(route => {
        expect(route.meta).toBeDefined()
        expect(route.meta.title).toBeDefined()
        expect(route.meta.icon).toBeDefined()
        expect(route.meta.permissions).toBeDefined()
        expect(Array.isArray(route.meta.permissions)).toBe(true)
      })
    })
  })
})