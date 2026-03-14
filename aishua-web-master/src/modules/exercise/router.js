// 练习模块路由
export default [
  {
    path: '/exercise',
    name: 'Exercise',
    component: () => import('./views/ExerciseView.vue'),
    meta: { requiresAuth: true, layout: 'default' }
  },
  {
    path: '/exercise/exam',
    name: 'Exam',
    component: () => import('./views/ExamView.vue'),
    meta: { requiresAuth: true, layout: 'default' }
  },
  {
    path: '/exercise/records',
    name: 'ExamRecords',
    component: () => import('./views/ExamRecordView.vue'),
    meta: { requiresAuth: true, layout: 'default' }
  },
  {
    path: '/exercise/stats',
    name: 'ExerciseStats',
    component: () => import('./views/ExerciseStatsView.vue'),
    meta: { requiresAuth: true, layout: 'default' }
  },
  {
    path: '/exercise/wrong',
    name: 'WrongQuestions',
    component: () => import('./views/WrongQuestionView.vue'),
    meta: { requiresAuth: true, layout: 'default' }
  },
  {
    path: '/exercise/records/:id',
    name: 'ExamDetail',
    component: () => import('./views/ExamDetailView.vue'),
    meta: { requiresAuth: true, layout: 'default' }
  }
];