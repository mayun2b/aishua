import request from '@/utils/request';

// 练习API
// 注意：游客模式下部分功能受限
export const exerciseApi = {
  // 开始练习
  // 游客模式可使用，但练习记录不会持久化
  startExercise(params) {
    console.log('调用startExercise API:', params);
    return request({
      url: '/exercise/start',
      method: 'get',
      params
    });
  },

  // 提交答案
  // 游客模式可提交，但答案不会保存到用户历史记录
  submitAnswer(data) {
    console.log('调用submitAnswer API:', data);
    return request({
      url: '/exercise/submit',
      method: 'post',
      data
    });
  },

  // 批量提交答案
  // 游客模式可使用，适用于完成整个练习后批量提交
  batchSubmitAnswers(data) {
    console.log('调用batchSubmitAnswers API:', data);
    return request({
      url: '/exercise/batch-submit',
      method: 'post',
      data
    });
  },

  // 获取用户统计
  // 注意：游客模式下返回空数据或默认值
  getUserStats() {
    console.log('调用getUserStats API');
    return request({
      url: '/exercise/stats',
      method: 'get'
    });
  }
};

// 题目API
export const questionApi = {
  // 获取随机题目
  getRandomQuestions(params) {
    return request({
      url: '/question/random',
      method: 'get',
      params
    });
  },

  // 根据ID获取题目详情
  getQuestionById(id) {
    return request({
      url: `/question/${id}`,
      method: 'get'
    });
  },

  // 获取所有题目分类
  getAllCategories() {
    return request({
      url: '/question/categories',
      method: 'get'
    });
  }
};

// 错题API
// 注意：以下所有错题相关功能在游客模式下不可用或受限
export const wrongQuestionApi = {
  // 分页查询错题列表
  // 游客模式下返回空列表
  getWrongQuestions(pageNum, pageSize) {
    return request({
      url: '/wrong/page',
      method: 'get',
      params: {
        pageNum,
        pageSize
      }
    });
  },

  // 获取错题数量
  // 游客模式下返回0
  getWrongCount() {
    return request({
      url: '/wrong/count',
      method: 'get'
    });
  },

  // 标记为已掌握
  // 游客模式下调用会失败
  markAsMastered(questionId) {
    return request({
      url: `/wrong/${questionId}/mastered`,
      method: 'put'
    });
  },

  // 移除错题
  // 游客模式下调用会失败
  removeFromWrong(questionId) {
    return request({
      url: `/wrong/${questionId}`,
      method: 'delete'
    });
  },

  // 随机获取错题（用于错题重练）
  // 游客模式下返回空数组
  getRandomWrongQuestions(count) {
    return request({
      url: '/wrong/random',
      method: 'get',
      params: {
        count
      }
    });
  }
};

// 考试记录API
export const examRecordApi = {
  // 获取用户考试记录列表
  getExamRecords(userId) {
    return request({
      url: `/exam/records/user/${userId}`,
      method: 'get'
    });
  },

  // 根据考试模式获取记录
  getExamRecordsByMode(userId, mode) {
    return request({
      url: `/exam/records/user/${userId}/mode/${mode}`,
      method: 'get'
    });
  },

  // 根据日期范围获取记录
  getExamRecordsByDateRange(userId, startDate, endDate) {
    return request({
      url: `/exam/records/user/${userId}/date`,
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
      url: `/exam/records/${id}`,
      method: 'get'
    });
  },

  // 保存考试记录
  saveExamRecord(data) {
    return request({
      url: '/exam/records/save',
      method: 'post',
      data
    });
  },

  // 获取考试记录题目
  getExamRecordQuestions(examRecordId) {
    return request({
      url: `/exam/records/${examRecordId}/questions`,
      method: 'get'
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
  getAllCategories
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