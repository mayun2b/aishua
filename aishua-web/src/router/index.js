import { createRouter, createWebHistory } from 'vue-router'
import store from '../store'
import Home from '../views/Home.vue'
import NotFound from '../views/NotFound.vue'
import UserLoginView from '../modules/auth/views/UserLoginView.vue'
import UserRegisterView from '../modules/auth/views/UserRegisterView.vue'
import UserDashboardView from '../modules/dashboard/views/UserDashboardView.vue'
import AdminDashboardView from '../modules/admin/views/AdminDashboardView.vue'
import AdminSubjectManagementView from '../modules/admin/views/AdminSubjectManagementView.vue'
import AdminDirectoryManagementView from '../modules/admin/views/AdminDirectoryManagementView.vue'
import AdminTagManagementView from '../modules/admin/views/AdminTagManagementView.vue'
import AdminQuestionManagementView from '../modules/admin/views/AdminQuestionManagementView.vue'
import AdminExamManagementView from '../modules/admin/views/AdminExamManagementView.vue'
import AdminExamQuestionConfigView from '../modules/admin/views/AdminExamQuestionConfigView.vue'
import SubjectCatalogView from '../modules/subject/views/SubjectCatalogView.vue'
import MySubjectsView from '../modules/subject/views/MySubjectsView.vue'
import PracticeView from '../modules/practice/views/PracticeView.vue'
import PracticeSessionView from '../modules/practice/views/PracticeSessionView.vue'
import PracticeRecordsView from '../modules/practice/views/PracticeRecordsView.vue'
import PracticeRecordDetailView from '../modules/practice/views/PracticeRecordDetailView.vue'
import WrongQuestionsView from '../modules/practice/views/WrongQuestionsView.vue'
import ExamCenterView from '../modules/exam/views/ExamCenterView.vue'
import ExamSessionView from '../modules/exam/views/ExamSessionView.vue'
import ExamRecordListView from '../modules/exam/views/ExamRecordListView.vue'
import ExamRecordDetailView from '../modules/exam/views/ExamRecordDetailView.vue'
import LearningAnalysisView from '../modules/ai/views/LearningAnalysisView.vue'

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
    path: '/practice/session/:sessionId',
    name: 'PracticeSession',
    component: PracticeSessionView,
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
    path: '/exercise/exam',
    name: 'ExamCenter',
    component: ExamCenterView,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/exercise/exam/session/:recordId',
    name: 'ExamSession',
    component: ExamSessionView,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/exercise/exam/records',
    name: 'ExamRecords',
    component: ExamRecordListView,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/exercise/exam/records/:recordId',
    name: 'ExamRecordDetail',
    component: ExamRecordDetailView,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/learning-analysis',
    name: 'LearningAnalysis',
    component: LearningAnalysisView,
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
  },
  {
    path: '/admin/exams',
    name: 'AdminExamManagement',
    component: AdminExamManagementView,
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/admin/exams/:paperId/questions',
    name: 'AdminExamQuestionConfig',
    component: AdminExamQuestionConfigView,
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 已登录用户访问访客页时，按角色直接回到对应工作台。
function resolveAuthenticatedHome() {
  return store.getters['auth/isAdmin'] ? '/admin' : '/dashboard'
}

// 全局前置守卫：统一处理登录态、访客页和管理员权限。
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
