/**
 * 系统管理相关路由配置
 */
export default [
  {
    path: 'system/users',
    name: 'SystemUsers',
    component: () => import('@/views/system/Users.vue'),
    meta: { 
      title: '用户管理',
      icon: 'User',
      permissions: ['system:user:view']
    }
  },
  {
    path: 'system/roles',
    name: 'SystemRoles',
    component: () => import('@/views/system/Roles.vue'),
    meta: { 
      title: '角色管理',
      icon: 'UserFilled',
      permissions: ['system:role:view']
    }
  },
  {
    path: 'system/settings',
    name: 'SystemSettings',
    component: () => import('@/views/system/Settings.vue'),
    meta: { 
      title: '系统设置',
      icon: 'Tools',
      permissions: ['system:setting:view']
    }
  }
]