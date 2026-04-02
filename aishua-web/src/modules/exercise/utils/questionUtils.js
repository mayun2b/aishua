// 题目相关工具函数

/**
 * 解析题目选项数据
 * @param {any} optionsData - 原始选项数据
 * @returns {Array} 格式化后的选项数组
 */
export const parseQuestionOptions = (optionsData) => {
  let options = [];

  if (optionsData) {
    let rawOptions = optionsData;

    // 后端字段通常是 JSON 字符串，这里优先做解析
    if (typeof rawOptions === 'string') {
      try {
        rawOptions = JSON.parse(rawOptions);
      } catch (e) {
        console.error('解析题目选项失败，使用空选项:', e);
        rawOptions = null;
      }
    }

    if (Array.isArray(rawOptions)) {
      // 已经是数组：["xxx","yyy"] 或 [{label,value}] 或 [{key,value}]
      options = rawOptions.map((opt, idx) => {
        if (opt && typeof opt === 'object') {
          if ('label' in opt && 'value' in opt) return opt;
          if ('key' in opt && 'value' in opt) {
            return { label: opt.key, value: opt.value };
          }
        }
        return {
          label: String.fromCharCode(65 + idx),
          value: typeof opt === 'string' ? opt : JSON.stringify(opt)
        };
      });
    } else if (rawOptions && typeof rawOptions === 'object') {
      // 对象结构：可能是 { "A": "xxx" } 或 { "A": { "value": "xxx" } }
      const keys = Object.keys(rawOptions).sort();
      options = keys.map((key, idx) => {
        const item = rawOptions[key];
        if (item && typeof item === 'object' && 'value' in item) {
          return {
            label: key || String.fromCharCode(65 + idx),
            value: item.value
          };
        }
        return {
          label: key || String.fromCharCode(65 + idx),
          value: item
        };
      });
    }
  }
  
  return options;
};

/**
 * 从单独的选项字段解析选项
 * @param {Object} question - 题目对象
 * @returns {Array} 格式化后的选项数组
 */
export const parseQuestionOptionsFromFields = (question) => {
  let options = [];
  const labels = ['A', 'B', 'C', 'D', 'E', 'F'];
  const optionKeys = ['optionA', 'optionB', 'optionC', 'optionD', 'optionE', 'optionF'];
  
  options = optionKeys
    .filter(key => question[key] !== undefined && question[key] !== null)
    .map((key, idx) => ({
      label: labels[idx],
      value: question[key]
    }));
  
  return options;
};

/**
 * 获取题目类型文本
 * @param {number} type - 题目类型
 * @returns {string} 题目类型文本
 */
export const getQuestionTypeText = (type) => {
  switch (type) {
    case 1: return '单选题';
    case 2: return '多选题';
    case 3: return '判断题';
    case 4: return '填空题';
    case 5: return '简答题';
    default: return '未知题型';
  }
};

/**
 * 格式化时间（秒）为 MM:SS 格式
 * @param {number} seconds - 秒数
 * @returns {string} 格式化后的时间字符串
 */
export const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
};

/**
 * 计算答题时间
 * @param {number} startTime - 开始时间戳
 * @param {number} endTime - 结束时间戳
 * @returns {number} 答题时间（秒）
 */
export const calculateTimeCost = (startTime, endTime = Date.now()) => {
  return Math.round((endTime - startTime) / 1000);
};

/**
 * 获取当前提交的答案
 * @param {Object} question - 当前题目
 * @param {string} selectedAnswer - 单选题答案
 * @param {Array} selectedAnswers - 多选题答案
 * @param {string} textAnswer - 文本答案
 * @returns {string} 格式化后的答案
 */
export const getCurrentSubmitAnswer = (question, selectedAnswer, selectedAnswers, textAnswer) => {
  if (!question) return '';
  if (question.type === 2) return [...selectedAnswers].sort().join('');
  if (question.type === 4 || question.type === 5) return textAnswer.trim();
  // 判断题特殊处理：根据后端存储格式返回对应答案
  if (question.type === 3) {
    // 检查后端答案格式
    if (question.answer === '正确' || question.answer === '错误') {
      // 后端存储的是文本格式
      return selectedAnswer === 'A' ? '正确' : '错误';
    } else {
      // 后端存储的是数字格式
      return selectedAnswer === 'A' ? '1' : '0';
    }
  }
  return selectedAnswer;
};

/**
 * 格式化日期时间
 * @param {Date} date - 日期对象
 * @returns {string} 格式化后的日期时间字符串
 */
export const formatDateTime = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

/**
 * 获取成绩等级
 * @param {number} score - 分数
 * @returns {Object} 包含等级文本和样式类的对象
 */
export const getGradeInfo = (score) => {
  if (score >= 90) {
    return { text: '优秀', className: 'grade-excellent' };
  } else if (score >= 80) {
    return { text: '良好', className: 'grade-good' };
  } else if (score >= 60) {
    return { text: '及格', className: 'grade-pass' };
  } else {
    return { text: '不及格', className: 'grade-fail' };
  }
};
