import request from '@/utils/request';

// 练习API
// 注意：游客模式下部分功能受限
export const exerciseApi = {
  // 开始练习
  // 游客模式可使用，但练习记录不会持久化
  startExercise(params) {
    console.log('调用startExercise API:', params);
    return request({
      url: '/api/exercise/start',
      method: 'get',
      params
    });
  },

  // 提交答案
  // 游客模式可提交，但答案不会保存到用户历史记录
  submitAnswer(data) {
    console.log('调用submitAnswer API:', data);
    return request({
      url: '/api/exercise/submit',
      method: 'post',
      data
    });
  },

  // 批量提交答案
  // 游客模式可使用，适用于完成整个练习后批量提交
  batchSubmitAnswers(data) {
    console.log('调用batchSubmitAnswers API:', data);
    return request({
      url: '/api/exercise/batch-submit',
      method: 'post',
      data
    });
  },

  // 获取用户统计
  // 注意：游客模式下返回空数据或默认值
  getUserStats(params) {
    console.log('调用getUserStats API:', params);
    return request({
      url: '/api/exercise/stats',
      method: 'get',
      params
    });
  }
};

// 题目API
export const questionApi = {
  // 获取随机题目
  getRandomQuestions(params) {
    return request({
      url: '/api/question/random',
      method: 'get',
      params
    });
  },

  // 根据ID获取题目详情
  getQuestionById(id) {
    return request({
      url: `/api/question/${id}`,
      method: 'get'
    });
  },

  // 获取所有题目分类
  getAllCategories() {
    return request({
      url: '/api/question/categories',
      method: 'get'
    });
  },

  // 根据学科 ID 获取题目分类
  getCategoriesBySubjectId(subjectId) {
    return request({
      url: `/api/question/categories/${subjectId}`,
      method: 'get'
    });
  },

  // 获取知识点进度
  getKnowledgePointProgress(params) {
    return request({
      url: '/api/question/knowledge-points/progress',
      method: 'get',
      params
    });
  },

  // 获取所有学科
  getAllSubjects() {
    return request({
      url: '/api/question/subjects',
      method: 'get'
    });
  },

  // 根据ID获取学科详情
  getSubjectById(id) {
    return request({
      url: `/api/subject/${id}`,
      method: 'get'
    });
  },

  // 创建学科
  createSubject(data) {
    return request({
      url: '/api/subject',
      method: 'post',
      data
    });
  },

  // 更新学科
  updateSubject(id, data) {
    return request({
      url: `/api/subject/${id}`,
      method: 'put',
      data
    });
  },

  // 删除学科
  deleteSubject(id) {
    return request({
      url: `/api/subject/${id}`,
      method: 'delete'
    });
  },

  // 获取题目列表（分页）
  getQuestions(params) {
    return request({
      url: '/api/question/page',
      method: 'get',
      params
    });
  },

  // 创建题目
  createQuestion(data) {
    return request({
      url: '/api/question',
      method: 'post',
      data
    });
  },

  // 更新题目
  updateQuestion(id, data) {
    return request({
      url: `/api/question/${id}`,
      method: 'put',
      data
    });
  },

  // 删除题目
  deleteQuestion(id) {
    return request({
      url: `/api/question/${id}`,
      method: 'delete'
    });
  },

  // 批量修改学科
  batchUpdateSubject(data) {
    return request({
      url: '/api/question/batch/update-subject',
      method: 'post',
      data
    });
  },

  // 批量修改分类
  batchUpdateCategory(data) {
    return request({
      url: '/api/question/batch/update-category',
      method: 'post',
      data
    });
  },

  // 批量删除题目
  batchDeleteQuestions(questionIds) {
    return request({
      url: '/api/question/batch/delete',
      method: 'post',
      data: { questionIds }
    });
  }
};

// 错题API
// 注意：以下所有错题相关功能在游客模式下不可用或受限
export const wrongQuestionApi = {
  // 分页查询错题列表
  // 游客模式下返回空列表
  getWrongQuestions(params) {
    return request({
      url: '/api/wrong/page',
      method: 'get',
      params
    });
  },

  // 获取错题数量
  // 游客模式下返回0
  getWrongCount(params) {
    return request({
      url: '/api/wrong/count',
      method: 'get',
      params
    });
  },

  // 标记为已掌握
  // 游客模式下调用会失败
  markAsMastered(questionId) {
    return request({
      url: `/api/wrong/${questionId}/mastered`,
      method: 'put'
    });
  },

  // 移除错题
  // 游客模式下调用会失败
  removeFromWrong(questionId) {
    return request({
      url: `/api/wrong/${questionId}`,
      method: 'delete'
    });
  },

  // 随机获取错题（用于错题重练）
  // 游客模式下返回空数组
  getRandomWrongQuestions(params) {
    return request({
      url: '/api/wrong/random',
      method: 'get',
      params
    });
  }
};

// 考试记录API
export const examRecordApi = {
  // 获取用户考试记录列表
  getExamRecords(userId) {
    return request({
      url: `/api/exam/records/user/${userId}`,
      method: 'get'
    });
  },

  // 根据考试模式获取记录
  getExamRecordsByMode(userId, mode) {
    return request({
      url: `/api/exam/records/user/${userId}/mode/${mode}`,
      method: 'get'
    });
  },

  // 根据日期范围获取记录
  getExamRecordsByDateRange(userId, startDate, endDate) {
    return request({
      url: `/api/exam/records/user/${userId}/date`,
      method: 'get',
      params: {
        startDate,
        endDate
      }
    });
  },

  // 获取考试记录详情
  getExamRecordById(id) {
    return request({
      url: `/api/exam/records/${id}`,
      method: 'get'
    });
  },

  // 保存考试记录
  saveExamRecord(data) {
    return request({
      url: '/api/exam/records/save',
      method: 'post',
      data
    });
  },

  // 获取考试记录题目
  getExamRecordQuestions(examRecordId) {
    return request({
      url: `/api/exam/records/${examRecordId}/questions`,
      method: 'get'
    });
  }
};

// AI题目生成API
export const aiApi = {
  // 生成AI题目
  generateQuestions(data) {
    return request({
      url: '/api/ai/generate-questions',
      method: 'post',
      data
    });
  },
  // 分析用户错题
  analyzeWrongQuestions() {
    return request({
      url: '/api/ai/analyze-wrong-questions',
      method: 'get'
    });
  },
  // 分析用户练习记录
  analyzeExerciseRecords() {
    return request({
      url: '/api/ai/analyze-exercise-records',
      method: 'get'
    });
  },
  // AI生成题目
  generateAiQuestion(data) {
    return request({
      url: '/api/ai/question/generate',
      method: 'post',
      data
    });
  },
  // 获取用户的AI生成题目列表
  getAiGeneratedQuestions(userId) {
    return request({
      url: `/api/ai/question/user/${userId}`,
      method: 'get'
    });
  },
  // 根据学科获取AI生成题目列表
  getAiGeneratedQuestionsBySubject(userId, subjectId) {
    return request({
      url: `/api/ai/question/user/${userId}/subject/${subjectId}`,
      method: 'get'
    });
  },
  // 更新题目练习状态
  updateAiQuestionPracticeStatus(id, isPracticed) {
    return request({
      url: `/api/ai/question/${id}/practice`,
      method: 'put',
      params: { isPracticed }
    });
  }
};

// 导出便捷方法
export const {
  startExercise,
  submitAnswer,
  batchSubmitAnswers,
  getUserStats
} = exerciseApi;

export const {
  getRandomQuestions,
  getQuestionById,
  getAllCategories,
  getCategoriesBySubjectId,
  getKnowledgePointProgress,
  getAllSubjects,
  getSubjectById,
  createSubject,
  updateSubject,
  deleteSubject,
  getQuestions,
  createQuestion,
  updateQuestion,
  deleteQuestion,
  batchUpdateSubject,
  batchUpdateCategory,
  batchDeleteQuestions
} = questionApi;

export const {
  getWrongQuestions,
  getWrongCount,
  markAsMastered,
  removeFromWrong,
  getRandomWrongQuestions
} = wrongQuestionApi;

export const {
  getExamRecords,
  getExamRecordsByMode,
  getExamRecordsByDateRange,
  getExamRecordById,
  saveExamRecord,
  getExamRecordQuestions
} = examRecordApi;

export const {
  generateQuestions,
  analyzeWrongQuestions,
  analyzeExerciseRecords,
  generateAiQuestion,
  getAiGeneratedQuestions,
  getAiGeneratedQuestionsBySubject,
  updateAiQuestionPracticeStatus
} = aiApi;