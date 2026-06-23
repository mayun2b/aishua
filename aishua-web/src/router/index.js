import { createRouter, createWebHistory } from 'vue-router'
import store from '../store'
import Home from '../views/Home.vue'
import NotFound from '../views/NotFound.vue'
import { navigationState } from './navigationState'

const APP_NAME = 'AI刷题'
const DEFAULT_TITLE = `${APP_NAME} - 智能知识点强化系统`

const withMeta = (title, meta = {}) => ({ title, ...meta })

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: withMeta('首页')
  },
  {
    path: '/login',
    name: 'UserLogin',
    component: () => import('../modules/auth/views/UserLoginView.vue'),
    meta: withMeta('账号登录', { guestOnly: true })
  },
  {
    path: '/register',
    name: 'UserRegister',
    component: () => import('../modules/auth/views/UserRegisterView.vue'),
    meta: withMeta('用户注册', { guestOnly: true })
  },
  {
    path: '/admin-login',
    name: 'AdminLogin',
    redirect: (to) => ({
      path: '/login',
      query: to.query,
      hash: to.hash
    })
  },
  {
    path: '/dashboard',
    name: 'UserDashboard',
    component: () => import('../modules/dashboard/views/UserDashboardView.vue'),
    meta: withMeta('学习工作台', { requiresAuth: true })
  },
  {
    path: '/dashboard/screen',
    name: 'StudyCommandCenter',
    component: () => import('../modules/dashboard/views/StudyCommandCenterView.vue'),
    meta: withMeta('学情战情大屏', { requiresAuth: true })
  },
  {
    path: '/subjects',
    name: 'SubjectCatalog',
    component: () => import('../modules/subject/views/SubjectCatalogView.vue'),
    meta: withMeta('学科列表', { requiresAuth: true })
  },
  {
    path: '/my-subjects',
    name: 'MySubjects',
    component: () => import('../modules/subject/views/MySubjectsView.vue'),
    meta: withMeta('我的学科', { requiresAuth: true })
  },
  {
    path: '/subjects/:subjectId/directories',
    name: 'SubjectDirectories',
    component: () => import('../modules/subject/views/SubjectDirectoryView.vue'),
    meta: withMeta('目录与考点', { requiresAuth: true })
  },
  {
    path: '/practice',
    name: 'Practice',
    component: () => import('../modules/practice/views/PracticeView.vue'),
    meta: withMeta('练习配置', { requiresAuth: true })
  },
  {
    path: '/practice/session/:sessionId',
    name: 'PracticeSession',
    component: () => import('../modules/practice/views/PracticeSessionView.vue'),
    meta: withMeta('练习作答', { requiresAuth: true, shell: false })
  },
  {
    path: '/practice-records',
    name: 'PracticeRecords',
    component: () => import('../modules/practice/views/PracticeRecordsView.vue'),
    meta: withMeta('练习记录', { requiresAuth: true })
  },
  {
    path: '/practice-records/:sessionId',
    name: 'PracticeRecordDetail',
    component: () => import('../modules/practice/views/PracticeRecordDetailView.vue'),
    meta: withMeta('练习详情', { requiresAuth: true })
  },
  {
    path: '/wrong-questions',
    name: 'WrongQuestions',
    component: () => import('../modules/practice/views/WrongQuestionsView.vue'),
    meta: withMeta('错题记录', { requiresAuth: true })
  },
  {
    path: '/weak-points',
    name: 'WeakKnowledgePoints',
    component: () => import('../modules/practice/views/WeakKnowledgePointsView.vue'),
    meta: withMeta('薄弱知识点', { requiresAuth: true })
  },
  {
    path: '/exercise/exam',
    name: 'ExamCenter',
    component: () => import('../modules/exam/views/ExamCenterView.vue'),
    meta: withMeta('模拟考试', { requiresAuth: true })
  },
  {
    path: '/exercise/exam/session/:recordId',
    name: 'ExamSession',
    component: () => import('../modules/exam/views/ExamSessionView.vue'),
    meta: withMeta('考试作答', { requiresAuth: true, shell: false })
  },
  {
    path: '/exercise/exam/records',
    name: 'ExamRecords',
    component: () => import('../modules/exam/views/ExamRecordListView.vue'),
    meta: withMeta('考试记录', { requiresAuth: true })
  },
  {
    path: '/exercise/exam/records/:recordId',
    name: 'ExamRecordDetail',
    component: () => import('../modules/exam/views/ExamRecordDetailView.vue'),
    meta: withMeta('考试详情', { requiresAuth: true })
  },
  {
    path: '/learning-analysis',
    name: 'LearningAnalysis',
    component: () => import('../modules/ai/views/LearningAnalysisView.vue'),
    meta: withMeta('学情分析', { requiresAuth: true })
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('../modules/admin/views/AdminDashboardView.vue'),
    meta: withMeta('管理台总览', { requiresAuth: true, requiresAdmin: true })
  },
  {
    path: '/admin/subjects',
    name: 'AdminSubjectManagement',
    component: () => import('../modules/admin/views/AdminSubjectManagementView.vue'),
    meta: withMeta('管理台 / 学科管理', { requiresAuth: true, requiresAdmin: true })
  },
  {
    path: '/admin/directories',
    name: 'AdminDirectoryManagement',
    component: () => import('../modules/admin/views/AdminDirectoryManagementView.vue'),
    meta: withMeta('管理台 / 目录管理', { requiresAuth: true, requiresAdmin: true })
  },
  {
    path: '/admin/tags',
    name: 'AdminTagManagement',
    component: () => import('../modules/admin/views/AdminTagManagementView.vue'),
    meta: withMeta('管理台 / 标签管理', { requiresAuth: true, requiresAdmin: true })
  },
  {
    path: '/admin/directory-tags',
    name: 'AdminDirectoryTagRelations',
    component: () => import('../modules/admin/views/AdminDirectoryTagRelationView.vue'),
    meta: withMeta('管理台 / 目录-考点关系', { requiresAuth: true, requiresAdmin: true })
  },
  {
    path: '/admin/questions',
    name: 'AdminQuestionManagement',
    component: () => import('../modules/admin/views/AdminQuestionManagementView.vue'),
    meta: withMeta('管理台 / 题目管理', { requiresAuth: true, requiresAdmin: true })
  },
  {
    path: '/admin/exams',
    name: 'AdminExamManagement',
    component: () => import('../modules/admin/views/AdminExamManagementView.vue'),
    meta: withMeta('管理台 / 试卷管理', { requiresAuth: true, requiresAdmin: true })
  },
  {
    path: '/admin/exams/:paperId/questions',
    name: 'AdminExamQuestionConfig',
    component: () => import('../modules/admin/views/AdminExamQuestionConfigView.vue'),
    meta: withMeta('管理台 / 配置试题', { requiresAuth: true, requiresAdmin: true })
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound,
    meta: withMeta('页面未找到')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }

    if (to.hash) {
      return {
        el: to.hash,
        top: 80,
        behavior: 'smooth'
      }
    }

    return {
      left: 0,
      top: 0,
      behavior: from.name ? 'smooth' : 'auto'
    }
  }
})

function resolveAuthenticatedHome() {
  return store.getters['auth/isAdmin'] ? '/admin' : '/dashboard'
}

router.beforeEach((to, from, next) => {
  navigationState.pending = true

  const isAuthenticated = store.getters['auth/isAuthenticated']
  const isAdmin = store.getters['auth/isAdmin']

  if (to.meta.requiresAuth && !isAuthenticated) {
    next({
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    })
    return
  }

  if (to.meta.requiresAdmin && !isAdmin) {
    next(resolveAuthenticatedHome())
    return
  }

  next()
})

router.afterEach((to) => {
  const pageTitle = typeof to.meta?.title === 'string' && to.meta.title
    ? `${to.meta.title} - ${APP_NAME}`
    : DEFAULT_TITLE
  document.title = pageTitle
  navigationState.pending = false
})

router.onError(() => {
  navigationState.pending = false
})

export default router
