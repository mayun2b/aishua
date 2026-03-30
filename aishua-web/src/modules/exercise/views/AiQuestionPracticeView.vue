<template>
  <div class="ai-question-practice">
    <h2>AI题目练习</h2>
    
    <div class="action-buttons">
      <button @click="loadAiQuestions" class="refresh-button" :disabled="loading">{{ loading ? '加载中...' : '加载AI题目' }}</button>
    </div>
    
    <div v-if="aiQuestions.length === 0 && !loading" class="empty-state">
      <p>暂无AI生成的题目，请先在练习统计页面生成AI题目</p>
    </div>
    
    <div v-else-if="loading" class="loading-state">
      <p>加载中...</p>
    </div>
    
    <div v-else class="questions-container">
      <div 
        v-for="(question, index) in aiQuestions" 
        :key="question.id" 
        class="question-card"
        :class="{ 'answered': question.userAnswer !== null }"
      >
        <div class="question-header">
          <span class="question-number">{{ index + 1 }}</span>
          <span class="question-type">{{ getQuestionType(question.type) }}</span>
          <span class="question-difficulty">{{ getDifficultyText(question.difficulty) }}</span>
        </div>
        
        <div class="question-content">
          <h3>{{ question.title }}</h3>
          <div v-if="question.content" class="question-body">{{ question.content }}</div>
        </div>
        
        <!-- 选择题选项 -->
        <div v-if="question.type === 1 || question.type === 2" class="options">
          <div 
            v-for="option in parseOptions(question.options)" 
            :key="option.key" 
            class="option-item"
            :class="{ 
              'selected': question.userAnswer === option.key,
              'correct': question.userAnswer === question.answer && option.key === question.answer,
              'wrong': question.userAnswer !== null && question.userAnswer !== question.answer && option.key === question.userAnswer
            }"
            @click="selectOption(question, option.key)"
          >
            <span class="option-key">{{ option.key }}</span>
            <span class="option-value">{{ option.value }}</span>
          </div>
        </div>
        
        <!-- 判断题选项 -->
        <div v-if="question.type === 3" class="options">
          <div 
            class="option-item"
            :class="{ 
              'selected': question.userAnswer === '正确',
              'correct': question.userAnswer === question.answer && question.answer === '正确',
              'wrong': question.userAnswer !== null && question.userAnswer !== question.answer && question.userAnswer === '正确'
            }"
            @click="selectOption(question, '正确')"
          >
            <span class="option-key">正确</span>
          </div>
          <div 
            class="option-item"
            :class="{ 
              'selected': question.userAnswer === '错误',
              'correct': question.userAnswer === question.answer && question.answer === '错误',
              'wrong': question.userAnswer !== null && question.userAnswer !== question.answer && question.userAnswer === '错误'
            }"
            @click="selectOption(question, '错误')"
          >
            <span class="option-key">错误</span>
          </div>
        </div>
        
        <!-- 填空题输入 -->
        <div v-if="question.type === 4" class="fill-blank">
          <input 
            type="text" 
            v-model="question.userAnswer" 
            placeholder="请输入答案"
            class="fill-blank-input"
          >
        </div>
        
        <!-- 简答题输入 -->
        <div v-if="question.type === 5" class="essay">
          <textarea 
            v-model="question.userAnswer" 
            placeholder="请输入答案"
            class="essay-input"
            rows="4"
          ></textarea>
        </div>
        
        <!-- 答案解析 -->
        <div v-if="question.userAnswer !== null" class="answer-analysis">
          <h4>答案解析</h4>
          <div class="answer-info">
            <span class="correct-answer">正确答案：{{ question.answer }}</span>
            <span v-if="question.userAnswer === question.answer" class="result correct">回答正确</span>
            <span v-else class="result wrong">回答错误</span>
          </div>
          <div v-if="question.analysis" class="analysis-content">{{ question.analysis }}</div>
        </div>
      </div>
      
      <!-- 统计信息 -->
      <div class="stats-summary">
        <div class="stat-item">
          <span class="stat-label">总题数：</span>
          <span class="stat-value">{{ aiQuestions.length }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">已答题数：</span>
          <span class="stat-value">{{ answeredCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">正确数：</span>
          <span class="stat-value correct">{{ correctCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">错误数：</span>
          <span class="stat-value wrong">{{ wrongCount }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">正确率：</span>
          <span class="stat-value">{{ accuracyRate }}%</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { aiApi } from '@/modules/exercise/api/exercise';

export default {
  name: 'AiQuestionPracticeView',
  setup() {
    const aiQuestions = ref([]);
    const loading = ref(false);
    
    // 计算属性
    const answeredCount = computed(() => {
      return aiQuestions.value.filter(q => q.userAnswer !== null).length;
    });
    
    const correctCount = computed(() => {
      return aiQuestions.value.filter(q => q.userAnswer !== null && q.userAnswer === q.answer).length;
    });
    
    const wrongCount = computed(() => {
      return aiQuestions.value.filter(q => q.userAnswer !== null && q.userAnswer !== q.answer).length;
    });
    
    const accuracyRate = computed(() => {
      if (answeredCount.value === 0) return 0;
      return ((correctCount.value / answeredCount.value) * 100).toFixed(1);
    });
    
    // 获取题目类型文本
    const getQuestionType = (type) => {
      const types = {
        1: '单选题',
        2: '多选题',
        3: '判断题',
        4: '填空题',
        5: '简答题'
      };
      return types[type] || '未知题型';
    };
    
    // 获取难度文本
    const getDifficultyText = (difficulty) => {
      const difficulties = {
        1: '简单',
        2: '中等',
        3: '困难'
      };
      return difficulties[difficulty] || '未知难度';
    };
    
    // 解析选项JSON
    const parseOptions = (options) => {
      try {
        return JSON.parse(options);
      } catch (e) {
        return [];
      }
    };
    
    // 选择选项
    const selectOption = (question, optionKey) => {
      question.userAnswer = optionKey;
    };
    
    // 加载AI题目
    const loadAiQuestions = async () => {
      loading.value = true;
      try {
        // 获取当前用户ID（实际应该从登录状态中获取）
        const userId = 1; // 临时值
        const response = await aiApi.getAiGeneratedQuestions(userId);
        
        if (response.code === 200) {
          // 为每个题目添加userAnswer属性，用于存储用户答案
          aiQuestions.value = response.data.map(q => ({
            ...q,
            userAnswer: null
          }));
        }
      } catch (error) {
        console.error('加载AI题目失败:', error);
      } finally {
        loading.value = false;
      }
    };
    
    // 页面加载时加载题目
    onMounted(() => {
      loadAiQuestions();
    });
    
    return {
      aiQuestions,
      loading,
      answeredCount,
      correctCount,
      wrongCount,
      accuracyRate,
      getQuestionType,
      getDifficultyText,
      parseOptions,
      selectOption,
      loadAiQuestions
    };
  }
};
</script>

<style scoped>
.ai-question-practice {
  padding: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin: 20px 0;
}

h2 {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 24px;
  border-left: 4px solid #409eff;
  padding-left: 12px;
}

.action-buttons {
  margin-bottom: 24px;
}

.refresh-button {
  padding: 10px 20px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.refresh-button:hover:not(:disabled) {
  background-color: #66b1ff;
}

.refresh-button:disabled {
  background-color: #c0c4cc;
  cursor: not-allowed;
}

.empty-state,
.loading-state {
  text-align: center;
  padding: 60px 0;
  color: #909399;
  font-size: 16px;
}

.questions-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.question-card {
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.question-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.question-card.answered {
  border-color: #409eff;
}

.question-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.question-number {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.question-type {
  background-color: #ecf5ff;
  color: #409eff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.question-difficulty {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.question-difficulty:nth-child(3):contains('简单') {
  background-color: #f0f9eb;
  color: #67c23a;
}

.question-difficulty:nth-child(3):contains('中等') {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.question-difficulty:nth-child(3):contains('困难') {
  background-color: #fef0f0;
  color: #f56c6c;
}

.question-content h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.question-body {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 16px;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background-color: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.option-item:hover {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.option-item.selected {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.option-item.correct {
  border-color: #67c23a;
  background-color: #f0f9eb;
}

.option-item.wrong {
  border-color: #f56c6c;
  background-color: #fef0f0;
}

.option-key {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #dcdfe6;
  color: #606266;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.option-item.selected .option-key,
.option-item.correct .option-key,
.option-item.wrong .option-key {
  background-color: white;
}

.option-item.selected .option-key {
  color: #409eff;
}

.option-item.correct .option-key {
  color: #67c23a;
}

.option-item.wrong .option-key {
  color: #f56c6c;
}

.option-value {
  flex: 1;
  color: #303133;
}

.fill-blank {
  margin-bottom: 16px;
}

.fill-blank-input {
  width: 100%;
  padding: 10px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
}

.fill-blank-input:focus {
  outline: none;
  border-color: #409eff;
}

.essay {
  margin-bottom: 16px;
}

.essay-input {
  width: 100%;
  padding: 10px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  resize: vertical;
}

.essay-input:focus {
  outline: none;
  border-color: #409eff;
}

.answer-analysis {
  background-color: white;
  border-radius: 4px;
  padding: 16px;
  border-left: 4px solid #409eff;
}

.answer-analysis h4 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.answer-info {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 12px;
}

.correct-answer {
  color: #606266;
}

.result {
  font-weight: 600;
}

.result.correct {
  color: #67c23a;
}

.result.wrong {
  color: #f56c6c;
}

.analysis-content {
  color: #606266;
  line-height: 1.6;
}

.stats-summary {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-label {
  color: #606266;
  font-weight: 500;
}

.stat-value {
  font-weight: 600;
  color: #303133;
}

.stat-value.correct {
  color: #67c23a;
}

.stat-value.wrong {
  color: #f56c6c;
}
</style>