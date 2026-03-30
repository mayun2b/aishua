// 练习模块路由
export default [
  {
    path: '/exercise/:subjectId?',
    name: 'Exercise',
    component: () => import('./views/ExerciseView.vue'),
    meta: { requiresAuth: true, layout: 'default' }
  },
  {
    path: '/exercise/exam/:subjectId?',
    name: 'Exam',
    component: () => import('./views/ExamView.vue'),
    meta: { requiresAuth: true, layout: 'default' }
  },
  {
    path: '/exercise/records/:subjectId?',
    name: 'ExamRecords',
    component: () => import('./views/ExamRecordView.vue'),
    meta: { requiresAuth: true, layout: 'default' }
  },
  {
    path: '/exercise/stats/:subjectId?',
    name: 'ExerciseStats',
    component: () => import('./views/ExerciseStatsView.vue'),
    meta: { requiresAuth: true, layout: 'default' }
  },
  {
    path: '/exercise/wrong/:subjectId?',
    name: 'WrongQuestions',
    component: () => import('./views/WrongQuestionView.vue'),
    meta: { requiresAuth: true, layout: 'default' }
  },
  {
    path: '/exercise/records/detail/:id',
    name: 'ExamDetail',
    component: () => import('./views/ExamDetailView.vue'),
    meta: { requiresAuth: true, layout: 'default' }
  },
  {
    path: '/exercise/questions',
    name: 'QuestionManagement',
    component: () => import('./views/QuestionManagementView.vue'),
    meta: { requiresAuth: true, layout: 'default' }
  },
  {
    path: '/exercise/questions/:id',
    name: 'QuestionDetail',
    component: () => import('./views/QuestionDetailView.vue'),
    meta: { requiresAuth: true, layout: 'default' }
  },
  {
    path: '/exercise/subjects',
    name: 'SubjectManagement',
    component: () => import('./views/SubjectManagementView.vue'),
    meta: { requiresAuth: true, layout: 'default' }
  },
  {
    path: '/exercise/subjects/:id',
    name: 'SubjectDetail',
    component: () => import('./views/SubjectDetailView.vue'),
    meta: { requiresAuth: true, layout: 'default' }
  },
  {
    path: '/exercise/ai-questions/:subjectId?',
    name: 'AiGeneratedQuestions',
    component: () => import('./views/AiGeneratedQuestionsView.vue'),
    meta: { requiresAuth: true, layout: 'default' }
  },
  {
    path: '/exercise/ai-practice/:subjectId?',
    name: 'AiQuestionPractice',
    component: () => import('./views/AiQuestionPracticeView.vue'),
    meta: { requiresAuth: true, layout: 'default' }
  }
];