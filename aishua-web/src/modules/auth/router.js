// 认证模块路由
export default [
  {
    path: '/login',
    name: 'Login',
    component: () => import('./views/LoginView.vue'),
    meta: { layout: 'empty' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('./views/RegisterView.vue'),
    meta: { layout: 'empty' }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('./views/DashboardView.vue'),
    meta: { requiresAuth: true, layout: 'default' }
  },
  {
    path: '/user-management',
    name: 'UserManagement',
    component: () => import('./views/UserManagementView.vue'),
    meta: { requiresAuth: true, layout: 'default' }
  }
];