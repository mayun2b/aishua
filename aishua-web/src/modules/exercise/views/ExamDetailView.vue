<template>
  <div class="exam-detail-container">
    <van-nav-bar
      title="考试详情"
      left-arrow
      @click-left="goBack"
    />
    
    <div class="detail-content">
      <div class="loading" v-if="loading">
        <van-loading>加载中...</van-loading>
      </div>
      
      <div v-else-if="examRecord">
        <!-- 考试基本信息 -->
        <div class="exam-info">
          <h2>{{ examRecord.examName }}</h2>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">考试时间:</span>
              <span class="value">{{ examRecord.date }}</span>
            </div>
            <div class="info-item">
              <span class="label">得分:</span>
              <span class="value score">{{ examRecord.score }}分</span>
            </div>
            <div class="info-item">
              <span class="label">正确率:</span>
              <span class="value">{{ Math.round(examRecord.correctRate * 100) }}%</span>
            </div>
            <div class="info-item">
              <span class="label">用时:</span>
              <span class="value">{{ examRecord.duration }}分钟</span>
            </div>
            <div class="info-item">
              <span class="label">题目数量:</span>
              <span class="value">{{ examRecord.questionCount }}题</span>
            </div>
            <div class="info-item">
              <span class="label">答对题数:</span>
              <span class="value">{{ examRecord.correctCount }}题</span>
            </div>
            <div class="info-item">
              <span class="label">等级:</span>
              <span class="value grade" :class="getGradeClass(examRecord.score)">
                {{ getGradeText(examRecord.score) }}
              </span>
            </div>
          </div>
        </div>
        
        <!-- 题目详情 -->
        <div class="questions-section">
          <h3>题目详情</h3>
          
          <div class="loading-questions" v-if="loadingQuestions">
            <van-loading>加载题目中...</van-loading>
          </div>
          
          <div v-else-if="examQuestions.length > 0">
            <div 
              v-for="(question, index) in examQuestions" 
              :key="question.id"
              class="question-item"
              :class="{ 'correct': question.isCorrect === 1, 'incorrect': question.isCorrect === 0 }"
            >
              <div class="question-header">
                <span class="question-number">第{{ index + 1 }}题</span>
                <span class="question-status" :class="{ 'correct': question.isCorrect === 1, 'incorrect': question.isCorrect === 0 }">
                  {{ question.isCorrect === 1 ? '正确' : '错误' }}
                </span>
              </div>
              
              <div class="question-content" v-if="question.questionInfo">
                <h4>{{ question.questionInfo.title }}</h4>
                
                <!-- 选项 -->
                <div class="options" v-if="question.questionInfo.options && question.questionInfo.type <= 3">
                  <div 
                    v-for="option in processedOptions(question.questionInfo.options)" 
                    :key="option.label"
                    class="option"
                    :class="{
                      'selected': isOptionSelected(option.label, question.userAnswer),
                      'correct': isOptionCorrect(option.label, question.questionInfo.answer),
                      'incorrect': isOptionIncorrect(option.label, question.userAnswer, question.questionInfo.answer)
                    }"
                  >
                    <span class="option-label">{{ option.label }}.</span>
                    <span class="option-value">{{ option.value }}</span>
                  </div>
                </div>
                
                <!-- 填空题/简答题 -->
                <div class="text-answer" v-else-if="question.questionInfo.type >= 4">
                  <div class="user-answer-block">
                    <strong>您的答案：</strong>
                    <p>{{ question.userAnswer }}</p>
                  </div>
                </div>
                
                <!-- 正确答案 -->
                <div class="correct-answer">
                  <strong>正确答案：</strong>{{ question.questionInfo.answer }}
                </div>
                
                <!-- 解析 -->
                <div class="analysis" v-if="question.questionInfo.analysis">
                  <strong>解析：</strong>{{ question.questionInfo.analysis }}
                </div>
                
                <!-- 答题时间 -->
                <div class="answer-time" v-if="question.answerTime">
                  <strong>答题时间：</strong>{{ question.answerTime }}秒
                </div>
              </div>
              
              <div class="question-content" v-else>
                <p class="no-question-info">题目信息加载失败</p>
              </div>
            </div>
          </div>
          
          <div class="no-questions" v-else-if="!loadingQuestions">
            <van-icon name="document-text-o" size="64" color="#c8c9cc" />
            <p>暂无题目详情</p>
          </div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="action-buttons">
          <van-button type="primary" block @click="goToExam">再次考试</van-button>
          <van-button block @click="goBack">返回列表</van-button>
        </div>
      </div>
      
      <div class="error-state" v-else>
        <van-icon name="warning-o" size="64" color="#faad14" />
        <p>考试记录不存在</p>
        <van-button type="primary" block @click="goBack">返回列表</van-button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { showToast } from 'vant';
import { getExamRecordById, getExamRecordQuestions } from '../api/exercise';
import { getQuestionById } from '../api/exercise';

export default {
  name: 'ExamDetailView',
  setup() {
    const router = useRouter();
    const route = useRoute();
    const examRecordId = route.params.id;
    
    const loading = ref(true);
    const loadingQuestions = ref(false);
    const examRecord = ref(null);
    const examQuestions = ref([]);
    
    // 加载考试记录
    const loadExamRecord = async () => {
      loading.value = true;
      try {
        const response = await getExamRecordById(examRecordId);
        if (response.code === 200) {
          examRecord.value = {
            ...response.data,
            date: new Date(response.data.startTime).toLocaleString('zh-CN'),
            examMode: response.data.examMode === 1 ? '基础考试' : response.data.examMode === 2 ? '中级考试' : '高级考试',
            correctRate: response.data.correctQuestions / response.data.totalQuestions,
            questionCount: response.data.totalQuestions,
            correctCount: response.data.correctQuestions
          };
        } else {
          showToast('获取考试记录失败');
        }
      } catch (error) {
        console.error('加载考试记录失败:', error);
        showToast('加载考试记录失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 加载考试题目详情
    const loadExamQuestions = async () => {
      loadingQuestions.value = true;
      try {
        const response = await getExamRecordQuestions(examRecordId);
        if (response.code === 200) {
          const questions = response.data;
          
          // 为每个题目加载详细信息
          for (const question of questions) {
            try {
              const questionResponse = await getQuestionById(question.questionId);
              if (questionResponse.code === 200) {
                question.questionInfo = questionResponse.data;
              }
            } catch (error) {
              console.error('加载题目详情失败:', error);
            }
          }
          
          examQuestions.value = questions;
        } else {
          showToast('获取题目详情失败');
        }
      } catch (error) {
        console.error('加载考试题目失败:', error);
        showToast('加载题目详情失败');
      } finally {
        loadingQuestions.value = false;
      }
    };
    
    // 判断选项是否被选中
    const isOptionSelected = (optionKey, userAnswer) => {
      return userAnswer.includes(optionKey);
    };
    
    // 判断选项是否正确
    const isOptionCorrect = (optionKey, correctAnswer) => {
      return correctAnswer.includes(optionKey);
    };
    
    // 判断选项是否错误（被选中但不是正确答案）
    const isOptionIncorrect = (optionKey, userAnswer, correctAnswer) => {
      return userAnswer.includes(optionKey) && !correctAnswer.includes(optionKey);
    };
    
    // 处理选项数据
    const processedOptions = (options) => {
      if (!options) return [];
      
      let processed = [];
      
      // 后端字段通常是 JSON 字符串，这里优先做解析
      let rawOptions = options;
      if (typeof rawOptions === 'string') {
        try {
          rawOptions = JSON.parse(rawOptions);
        } catch (e) {
          console.error('解析题目选项失败:', e);
          return [];
        }
      }

      if (Array.isArray(rawOptions)) {
        // 已经是数组：["xxx","yyy"] 或 [{label,value}] 或 [{key,value}]
        processed = rawOptions.map((opt, idx) => {
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
        processed = keys.map((key, idx) => {
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
      
      return processed;
    };
    
    // 获取等级样式
    const getGradeClass = (score) => {
      if (score >= 90) return 'grade-excellent';
      if (score >= 80) return 'grade-good';
      if (score >= 60) return 'grade-pass';
      return 'grade-fail';
    };
    
    // 获取等级文本
    const getGradeText = (score) => {
      if (score >= 90) return '优秀';
      if (score >= 80) return '良好';
      if (score >= 60) return '及格';
      return '不及格';
    };
    
    // 跳转到考试页面
    const goToExam = () => {
      router.push('/exercise/exam');
    };
    
    // 返回上一页
    const goBack = () => {
      router.push('/exercise/records');
    };
    
    onMounted(async () => {
      await loadExamRecord();
      if (examRecord.value) {
        await loadExamQuestions();
      }
    });
    
    return {
      loading,
      loadingQuestions,
      examRecord,
      examQuestions,
      isOptionSelected,
      isOptionCorrect,
      isOptionIncorrect,
      processedOptions,
      getGradeClass,
      getGradeText,
      goToExam,
      goBack
    };
  }
};
</script>

<style scoped>
.exam-detail-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding-bottom: 30px;
}

.detail-content {
  padding: 20px;
}

.loading,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  text-align: center;
}

.error-state p {
  margin: 20px 0;
  color: #666;
  font-size: 16px;
}

.exam-info {
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.exam-info h2 {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 700;
  color: #333;
  text-align: center;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px;
  background-color: #f9f9f9;
  border-radius: 6px;
}

.info-item .label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.info-item .value {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.info-item .value.score {
  color: #409eff;
  font-size: 18px;
}

.info-item .value.grade {
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 12px;
}

.grade-excellent {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.grade-good {
  background-color: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.grade-pass {
  background-color: #fffbe6;
  color: #faad14;
  border: 1px solid #ffe58f;
}

.grade-fail {
  background-color: #fff1f0;
  color: #f5222d;
  border: 1px solid #ffccc7;
}

.questions-section {
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.questions-section h3 {
  margin: 0 0 24px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.loading-questions {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
}

.no-questions {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  text-align: center;
  color: #999;
}

.no-questions p {
  margin: 20px 0;
  font-size: 16px;
}

.question-item {
  padding: 20px;
  border-radius: 8px;
  background-color: #f9f9f9;
  border-left: 4px solid #ddd;
  margin-bottom: 20px;
}

.question-item.correct {
  border-left-color: #52c41a;
  background-color: #f6ffed;
}

.question-item.incorrect {
  border-left-color: #f5222d;
  background-color: #fff1f0;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.question-number {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.question-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.question-status.correct {
  background-color: #52c41a;
  color: white;
}

.question-status.incorrect {
  background-color: #f5222d;
  color: white;
}

.question-content {
  font-size: 14px;
  line-height: 1.6;
}

.question-content h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.options {
  margin-bottom: 16px;
}

.option {
  padding: 10px 12px;
  margin-bottom: 8px;
  border-radius: 6px;
  background-color: #f0f0f0;
  display: flex;
  align-items: center;
  transition: all 0.3s;
}

.option:hover {
  background-color: #e0e0e0;
}

.option.selected {
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
}

.option.correct {
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
}

.option.incorrect {
  background-color: #fff1f0;
  border: 1px solid #ffccc7;
}

.option-label {
  font-weight: 600;
  margin-right: 8px;
  color: #333;
}

.option-value {
  flex: 1;
  color: #666;
}

.text-answer {
  margin-bottom: 16px;
}

.user-answer-block {
  padding: 12px;
  background-color: #f0f0f0;
  border-radius: 6px;
  margin-bottom: 12px;
}

.user-answer-block p {
  margin: 8px 0 0 0;
  color: #666;
}

.correct-answer {
  margin: 12px 0;
  padding: 12px;
  background-color: #f6ffed;
  border-radius: 6px;
  color: #52c41a;
}

.analysis {
  margin: 12px 0;
  padding: 12px;
  background-color: #f0f9ff;
  border-radius: 6px;
  color: #1890ff;
  line-height: 1.5;
}

.answer-time {
  margin-top: 12px;
  font-size: 12px;
  color: #999;
}

.no-question-info {
  color: #999;
  text-align: center;
  padding: 20px 0;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-buttons .van-button {
  height: 48px;
  font-weight: 600;
}
</style>
