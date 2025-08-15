/**
 * 路由守卫配置
 */

/**
 * 权限检查
 * @param {Array} requiredPermissions 需要的权限数组
 * @param {Array} userPermissions 用户权限数组
 * @returns {boolean} 是否有权限
 */
function hasPermission(requiredPermissions, userPermissions) {
  if (!requiredPermissions || requiredPermissions.length === 0) {
    return true
  }
  
  if (!userPermissions || userPermissions.length === 0) {
    return false
  }
  
  return requiredPermissions.some(permission => userPermissions.includes(permission))
}

/**
 * 获取用户权限（模拟，实际应该从状态管理中获取）
 * @returns {Array} 用户权限数组
 */
function getUserPermissions() {
  // TODO: 实际应该从 Pinia store 中获取用户权限
  return [
    'certificate:view',
    'certificate:create',
    'certificate:update',
    'certificate:delete',
    'monitoring:view',
    'monitoring:alert:view',
    'monitoring:log:view',
    'system:user:view',
    'system:role:view',
    'system:setting:view'
  ]
}

/**
 * 认证检查守卫
 */
export function authGuard(to, from, next) {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
    return
  }
  
  next()
}

/**
 * 权限检查守卫
 */
export function permissionGuard(to, from, next) {
  const requiredPermissions = to.meta.permissions
  const userPermissions = getUserPermissions()
  
  if (!hasPermission(requiredPermissions, userPermissions)) {
    // 没有权限，重定向到 403 页面或首页
    next('/dashboard')
    return
  }
  
  next()
}

/**
 * 标题设置守卫
 */
export function titleGuard(to, from, next) {
  const title = to.meta.title
  const appName = import.meta.env.VITE_APP_NAME || '证书生命周期管理系统'
  
  if (title) {
    document.title = `${title} - ${appName}`
  } else {
    document.title = appName
  }
  
  next()
}

/**
 * 页面加载进度守卫
 */
export function progressGuard(to, from, next) {
  // 这里可以集成 nprogress 或类似的进度条
  // NProgress.start()
  next()
}

/**
 * 页面加载完成守卫
 */
export function progressDoneGuard(to, from) {
  // 这里可以结束进度条
  // NProgress.done()
}