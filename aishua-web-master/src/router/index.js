import { createRouter, createWebHistory } from 'vue-router';
import authRoutes from '../modules/auth/router';
import exerciseRoutes from '../modules/exercise/router';

// 合并所有模块的路由
const routes = [
  ...authRoutes,
  ...exerciseRoutes,
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue')
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 全局前置守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  
  if (to.meta.requiresAuth && !token) {
    // 需要认证但没有token，重定向到登录页
    next('/login');
  } else {
    next();
  }
});

export default router;