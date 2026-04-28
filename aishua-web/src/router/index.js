import { createRouter, createWebHistory } from 'vue-router'
import store from '../store'
import Home from '../views/Home.vue'
import UserLoginView from '../modules/auth/views/UserLoginView.vue'
import UserRegisterView from '../modules/auth/views/UserRegisterView.vue'
import UserDashboardView from '../modules/dashboard/views/UserDashboardView.vue'
import AdminDashboardView from '../modules/admin/views/AdminDashboardView.vue'
import AdminSubjectManagementView from '../modules/admin/views/AdminSubjectManagementView.vue'
import AdminDirectoryManagementView from '../modules/admin/views/AdminDirectoryManagementView.vue'
import AdminTagManagementView from '../modules/admin/views/AdminTagManagementView.vue'
import AdminQuestionManagementView from '../modules/admin/views/AdminQuestionManagementView.vue'
import SubjectCatalogView from '../modules/subject/views/SubjectCatalogView.vue'
import MySubjectsView from '../modules/subject/views/MySubjectsView.vue'
import PracticeView from '../modules/practice/views/PracticeView.vue'
import PracticeRecordsView from '../modules/practice/views/PracticeRecordsView.vue'
import PracticeRecordDetailView from '../modules/practice/views/PracticeRecordDetailView.vue'
import WrongQuestionsView from '../modules/practice/views/WrongQuestionsView.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/login',
    name: 'UserLogin',
    component: UserLoginView,
    meta: {
      guestOnly: true
    }
  },
  {
    path: '/register',
    name: 'UserRegister',
    component: UserRegisterView,
    meta: {
      guestOnly: true
    }
  },
  {
    path: '/admin-login',
    name: 'AdminLogin',
    redirect: '/login'
  },
  {
    path: '/dashboard',
    name: 'UserDashboard',
    component: UserDashboardView,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/subjects',
    name: 'SubjectCatalog',
    component: SubjectCatalogView,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/my-subjects',
    name: 'MySubjects',
    component: MySubjectsView,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/practice',
    name: 'Practice',
    component: PracticeView,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/practice-records',
    name: 'PracticeRecords',
    component: PracticeRecordsView,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/practice-records/:sessionId',
    name: 'PracticeRecordDetail',
    component: PracticeRecordDetailView,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/wrong-questions',
    name: 'WrongQuestions',
    component: WrongQuestionsView,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboardView,
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/admin/subjects',
    name: 'AdminSubjectManagement',
    component: AdminSubjectManagementView,
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/admin/directories',
    name: 'AdminDirectoryManagement',
    component: AdminDirectoryManagementView,
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/admin/tags',
    name: 'AdminTagManagement',
    component: AdminTagManagementView,
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/admin/questions',
    name: 'AdminQuestionManagement',
    component: AdminQuestionManagementView,
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 已登录用户访问访客页时，按角色直接回到对应工作台
function resolveAuthenticatedHome() {
  return store.getters['auth/isAdmin'] ? '/admin' : '/dashboard'
}

router.beforeEach((to, from, next) => {
  const isAuthenticated = store.getters['auth/isAuthenticated']
  const isAdmin = store.getters['auth/isAdmin']

  if (to.meta.guestOnly && isAuthenticated) {
    next(resolveAuthenticatedHome())
    return
  }

  if (to.meta.requiresAuth && !isAuthenticated) {
    // 记录原始目标地址，登录后可以回跳
    next({
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    })
    return
  }

  if (to.meta.requiresAdmin && !isAdmin) {
    next('/dashboard')
    return
  }

  next()
})

export default router
