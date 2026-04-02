<template>
  <div class="exam-container">
    <!-- 考试模式选择页面 -->
    <div v-if="!examStarted" class="mode-select-page">
      <van-nav-bar
        title="选择考试模式"
        left-arrow
        @click-left="goBack"
      />
      
      <div class="mode-select-content">
        <h2>选择考试模式</h2>
        
        <div v-if="!isLoggedIn" class="guest-notice">
          <p>您当前是游客模式，无法参加考试，请先登录。</p>
          <van-button type="primary" size="small" @click="goToLogin">立即登录</van-button>
        </div>
        
        <div class="mode-options">
          <van-card 
            v-for="mode in examModes" 
            :key="mode.value"
            :class="{ active: selectedMode === mode.value }"
            @click="selectMode(mode.value)"
          >
            <template #title>
              <h3>{{ mode.label }}</h3>
            </template>
            <template #desc>
              <p>{{ mode.description }}</p>
              <p class="exam-info">时间: {{ mode.time }}分钟 | 题目: {{ mode.questionCount }}题</p>
            </template>
          </van-card>
        </div>
        
        <div class="config-section" v-if="selectedMode">
          <van-form>
            <van-field label="题目分类">
              <template #input>
                <van-dropdown-menu>
                  <van-dropdown-item 
                    v-model="configForm.categoryId" 
                    :options="categoryOptions"
                    placeholder="选择分类"
                  />
                </van-dropdown-menu>
              </template>
            </van-field>
          </van-form>
          
          <van-button type="primary" block @click="startExam" :loading="loading" :disabled="!isLoggedIn">
            开始考试
          </van-button>
        </div>
      </div>
    </div>
    
    <!-- 考试页面 -->
    <div v-else class="exam-page">
      <van-nav-bar
        title="考试中"
        left-arrow
        @click-left="confirmExit"
      />
      
      <div class="top-bar">
        <div class="time-container">
          <van-icon name="clock-o" size="16" />
          <span class="time-text" :class="{ warning: remainingTime < 300, danger: remainingTime < 60 }">
            剩余时间: {{ formatTime(remainingTime) }}
          </span>
        </div>
        <div class="question-progress">
          <span>第 {{ currentQuestionIndex + 1 }} 题 / 共 {{ totalQuestions }} 题</span>
        </div>
      </div>
      
      <div class="question-container" v-if="currentQuestion">
        <div class="question-header">
          <h3 class="question-title">{{ currentQuestion.title }}</h3>
          <div class="question-type" v-if="currentQuestion.type">
            {{ getQuestionTypeText(currentQuestion.type) }}
          </div>
        </div>
        
        <div class="options-container">
          <!-- 单选题 -->
          <van-radio-group
            v-if="currentQuestion.type === 1"
            v-model="selectedAnswer"
            @change="onAnswerSelect"
          >
            <van-cell-group inset v-if="currentQuestion.options && currentQuestion.options.length > 0">
              <van-cell
                v-for="(option, index) in currentQuestion.options"
                :key="index"
                clickable
                @click="selectOption(option.label)"
              >
                <template #icon>
                  <van-radio :name="option.label" />
                </template>
                <template #title>
                  <span class="option-label">{{ option.label }}. </span>
                  <span class="option-value">{{ option.value }}</span>
                </template>
              </van-cell>
            </van-cell-group>
            <div v-else>
              <p>暂无选项</p>
            </div>
          </van-radio-group>

          <!-- 多选题 -->
          <van-checkbox-group
            v-else-if="currentQuestion.type === 2"
            v-model="selectedAnswers"
            @change="onAnswersSelect"
          >
            <van-cell-group inset v-if="currentQuestion.options && currentQuestion.options.length > 0">
              <van-cell
                v-for="(option, index) in currentQuestion.options"
                :key="index"
                clickable
                @click="toggleMultiOption(option.label)"
              >
                <template #icon>
                  <van-checkbox :name="option.label" />
                </template>
                <template #title>
                  <span class="option-label">{{ option.label }}. </span>
                  <span class="option-value">{{ option.value }}</span>
                </template>
              </van-cell>
            </van-cell-group>
            <div v-else>
              <p>暂无选项</p>
            </div>
          </van-checkbox-group>

          <!-- 判断题：约定 A=正确，B=错误 -->
          <van-radio-group
            v-else-if="currentQuestion.type === 3"
            v-model="selectedAnswer"
            @change="onAnswerSelect"
          >
            <van-cell-group inset>
              <van-cell clickable @click="selectOption('A')">
                <template #icon>
                  <van-radio name="A" />
                </template>
                <template #title>
                  <span class="option-label">A. </span>
                  <span class="option-value">正确</span>
                </template>
              </van-cell>
              <van-cell clickable @click="selectOption('B')">
                <template #icon>
                  <van-radio name="B" />
                </template>
                <template #title>
                  <span class="option-label">B. </span>
                  <span class="option-value">错误</span>
                </template>
              </van-cell>
            </van-cell-group>
          </van-radio-group>

          <!-- 填空题 -->
          <div v-else-if="currentQuestion.type === 4" class="text-answer">
            <van-field
              v-model="textAnswer"
              rows="1"
              autosize
              type="textarea"
              placeholder="请输入答案"
            />
          </div>

          <!-- 简答题 -->
          <div v-else-if="currentQuestion.type === 5" class="text-answer">
            <van-field
              v-model="textAnswer"
              rows="3"
              autosize
              type="textarea"
              placeholder="请输入答案"
            />
          </div>

          <div v-else>
            <p>未知题型，无法作答</p>
          </div>
        </div>
      </div>
      
      <div class="loading" v-else>
        <van-loading>加载题目中...</van-loading>
      </div>
      
      <div class="action-buttons">
        <van-button 
          type="default" 
          size="large"
          @click="prevQuestion"
          :disabled="currentQuestionIndex === 0 || !currentQuestion"
        >
          上一题
        </van-button>
        <van-button 
          type="primary" 
          size="large"
          @click="nextQuestion"
          :disabled="isLastQuestion || !currentQuestion"
        >
          下一题
        </van-button>
        <van-button 
          type="danger" 
          size="large"
          @click="submitExam"
          :loading="isSubmitting"
        >
          交卷
        </van-button>
      </div>
      
      <!-- 考试完成结果弹窗 -->
      <van-dialog
        v-model:show="showResult"
        title="考试完成！"
        show-cancel-button
        confirm-button-text="查看详情"
        cancel-button-text="返回首页"
        @confirm="viewExamDetails"
        @cancel="goToHome"
      >
        <div class="result-dialog-content">
          <p>您已完成本次考试</p>
          <div class="result-stats">
            <div class="stat-item">
              <div class="stat-number">{{ correctCount }}</div>
              <div class="stat-label">答对</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ totalQuestions - correctCount }}</div>
              <div class="stat-label">答错</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ Math.round((correctCount / totalQuestions) * 100) }}%</div>
              <div class="stat-label">正确率</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ score }}</div>
              <div class="stat-label">得分</div>
            </div>
          </div>
          <div class="exam-grade" :class="getGradeInfo(score).className">
            {{ getGradeInfo(score).text }}
          </div>
        </div>
      </van-dialog>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, watch, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { getRandomQuestions, getAllCategories, batchSubmitAnswers as batchSubmitAnswersApi, saveExamRecord } from '../api/exercise';
import { showToast, Dialog } from 'vant';
import { parseQuestionOptions, getQuestionTypeText, calculateTimeCost, getCurrentSubmitAnswer, formatDateTime, getGradeInfo, formatTime } from '../utils/questionUtils';

export default {
  name: 'ExamView',
  setup() {
    const router = useRouter();
    
    // 考试模式选择相关
    const examStarted = ref(false);
    const selectedMode = ref(null);
    const loading = ref(false);
    const isLoggedIn = ref(!!localStorage.getItem('token'));
    
    // 检查登录状态
    const checkLoginStatus = () => {
      const token = localStorage.getItem('token');
      isLoggedIn.value = !!token;
    };
    const configForm = reactive({
      categoryId: null
    });
    
    // 考试模式选项
    const examModes = [
      {
        value: 1,
        label: '基础考试',
        description: '适合初学者的基础测试',
        time: 30,
        questionCount: 20
      },
      {
        value: 2,
        label: '中级考试',
        description: '适合有一定基础的学习者',
        time: 45,
        questionCount: 30
      },
      {
        value: 3,
        label: '高级考试',
        description: '适合高级学习者的综合测试',
        time: 60,
        questionCount: 40
      }
    ];
    
    // 题目分类选项
    const categoryOptions = ref([]);
    
    // 从后端获取的真实数据
    const questions = ref([]);
    const currentQuestionIndex = ref(0);
    const selectedAnswer = ref('');
    const selectedAnswers = ref([]);
    const textAnswer = ref('');
    const userAnswers = ref([]);
    const isSubmitting = ref(false);
    const showResult = ref(false);
    const correctCount = ref(0);
    const score = ref(0);
    const examTime = ref(1800); // 默认30分钟
    const remainingTime = ref(1800);
    const timer = ref(null);
    const questionStartTime = ref(null); // 记录当前题目的开始时间
    
    const currentQuestion = computed(() => {
      return questions.value[currentQuestionIndex.value] || null;
    });
    
    const totalQuestions = computed(() => questions.value.length);
    const isLastQuestion = computed(() => {
      return currentQuestionIndex.value >= totalQuestions.value - 1;
    });
    


    // 加载题目分类
    const loadCategories = async () => {
      try {
        const response = await getAllCategories();
        if (response && response.code === 200) {
          categoryOptions.value = (response.data || []).map(category => ({
            text: category.name,
            value: category.id
          }));
        }
      } catch (error) {
        console.error('加载题目分类失败:', error);
        showToast('加载题目分类失败');
      }
    };
    
    // 从后端获取题目
    const loadQuestions = async (params) => {
      try {
        loading.value = true;
        console.log('获取题目参数:', params);
        const response = await getRandomQuestions(params);
        console.log('获取题目响应:', response);
        
        if (response.code === 200) {
          if (Array.isArray(response.data)) {
            // 转换后端返回的题目数据为前端所需格式
            questions.value = response.data.map(q => {
              // 处理选项数据，确保格式正确
              let options = [];

              // 尝试从不同的可能字段获取选项
              if (q.options) {
                options = parseQuestionOptions(q.options);
              } else if (q.optionA) {
                // 如果后端使用单独的选项字段
                const labels = ['A', 'B', 'C', 'D', 'E', 'F'];
                const optionKeys = ['optionA', 'optionB', 'optionC', 'optionD', 'optionE', 'optionF'];
                
                options = optionKeys
                  .filter(key => q[key] !== undefined && q[key] !== null)
                  .map((key, idx) => ({
                    label: labels[idx],
                    value: q[key]
                  }));
              }
              
              return {
                ...q,
                options: options
              };
            });
            console.log('处理后的题目数据:', questions.value);
          } else {
            console.error('获取题目失败: 响应数据不是数组', response.data);
            questions.value = [];
          }
        } else {
          console.error('获取题目失败:', response.message);
          // 如果获取失败，使用默认数据
          questions.value = [];
        }
      } catch (error) {
        console.error('获取题目时发生错误:', error);
        // 发生错误时使用默认数据
        questions.value = [];
      } finally {
        loading.value = false;
      }
    };
    
    const selectMode = (mode) => {
      selectedMode.value = mode;
    };
    
    const startExam = async () => {
      if (!selectedMode.value) {
        showToast('请选择考试模式');
        return;
      }

      if (!isLoggedIn.value) {
        showToast('请先登录');
        return;
      }

      loading.value = true;
      try {
        const modeConfig = examModes.find(m => m.value === selectedMode.value);
        const params = {
          count: modeConfig.questionCount,
          exerciseMode: 2 // 随机模式
        };

        if (configForm.categoryId) {
          params.categoryId = configForm.categoryId;
        }

        // 调用后端API获取题目
        await loadQuestions(params);
        
        if (questions.value.length > 0) {
          // 开始考试
          examStarted.value = true;
          // 重置考试状态
          currentQuestionIndex.value = 0;
          selectedAnswer.value = '';
          selectedAnswers.value = [];
          textAnswer.value = '';
          userAnswers.value = [];
          correctCount.value = 0;
          score.value = 0;
          
          // 设置考试时间
          examTime.value = modeConfig.time * 60;
          remainingTime.value = modeConfig.time * 60;
          
          // 初始化第一个题目的开始时间
          questionStartTime.value = Date.now();
          
          // 开始倒计时
          startTimer();
        } else {
          showToast('获取题目失败，请重试');
        }
      } catch (error) {
        console.error('开始考试失败:', error);
        showToast('开始考试失败，请重试');
      } finally {
        loading.value = false;
      }
    };
    
    const startTimer = () => {
      if (timer.value) {
        clearInterval(timer.value);
      }
      
      timer.value = setInterval(() => {
        if (remainingTime.value > 0) {
          remainingTime.value--;
        } else {
          // 时间到，自动交卷
          clearInterval(timer.value);
          submitExam();
        }
      }, 1000);
    };
    

    
    const selectOption = (value) => {
      selectedAnswer.value = value;
    };
    
    const onAnswerSelect = (value) => {
      selectedAnswer.value = value;
    };

    const onAnswersSelect = (values) => {
      selectedAnswers.value = values;
    };

    const toggleMultiOption = (label) => {
      const list = Array.isArray(selectedAnswers.value) ? [...selectedAnswers.value] : [];
      const idx = list.indexOf(label);
      if (idx >= 0) list.splice(idx, 1);
      else list.push(label);
      selectedAnswers.value = list;
    };


    
    const saveCurrentAnswer = () => {
      const q = currentQuestion.value;
      if (!q) return;
      
      const submitAns = getCurrentSubmitAnswer(q, selectedAnswer.value, selectedAnswers.value, textAnswer.value);
      
      // 计算答题时间（秒）
      const timeCost = calculateTimeCost(questionStartTime.value);
      
      // 保存用户答案
      const existingIndex = userAnswers.value.findIndex(item => item.questionId === q.id);
      if (existingIndex >= 0) {
        userAnswers.value[existingIndex] = {
          questionId: q.id,
          answer: submitAns,
          isCorrect: submitAns === q.answer,
          timeCost: timeCost
        };
      } else {
        userAnswers.value.push({
          questionId: q.id,
          answer: submitAns,
          isCorrect: submitAns === q.answer,
          timeCost: timeCost
        });
      }
    };
    
    const prevQuestion = () => {
      if (currentQuestionIndex.value > 0) {
        saveCurrentAnswer();
        currentQuestionIndex.value--;
        loadCurrentAnswer();
        // 记录新题目的开始时间
        questionStartTime.value = Date.now();
      }
    };
    
    const nextQuestion = () => {
      if (currentQuestionIndex.value < totalQuestions.value - 1) {
        saveCurrentAnswer();
        currentQuestionIndex.value++;
        loadCurrentAnswer();
        // 记录新题目的开始时间
        questionStartTime.value = Date.now();
      }
    };
    
    const loadCurrentAnswer = () => {
      const q = currentQuestion.value;
      if (!q) return;
      
      const savedAnswer = userAnswers.value.find(item => item.questionId === q.id);
      if (savedAnswer) {
        if (q.type === 2) {
          selectedAnswers.value = savedAnswer.answer.split('');
          selectedAnswer.value = '';
          textAnswer.value = '';
        } else if (q.type === 4 || q.type === 5) {
          textAnswer.value = savedAnswer.answer;
          selectedAnswer.value = '';
          selectedAnswers.value = [];
        } else {
          selectedAnswer.value = savedAnswer.answer;
          selectedAnswers.value = [];
          textAnswer.value = '';
        }
      } else {
        selectedAnswer.value = '';
        selectedAnswers.value = [];
        textAnswer.value = '';
      }
    };
    
    const submitExam = async () => {
      isSubmitting.value = true;
      
      try {
        // 保存当前题目答案
        saveCurrentAnswer();
        
        // 计算正确答案数量
        correctCount.value = userAnswers.value.filter(item => item.isCorrect).length;
        
        // 计算得分
        score.value = Math.round((correctCount.value / totalQuestions.value) * 100);
        
        // 调用后端API提交答案
        if (userAnswers.value.length > 0) {
          const batchData = userAnswers.value.map(item => ({
              questionId: item.questionId,
              userAnswer: item.answer,
              timeCost: item.timeCost
            }));
          
          console.log('批量提交答案请求数据:', batchData);
          try {
            const batchResponse = await batchSubmitAnswersApi(batchData);
            console.log('批量提交答案响应:', batchResponse);
          } catch (error) {
            console.error('批量提交答案失败:', error);
          }
        }
        
        // 保存考试记录到后端
        console.log('开始保存考试记录');
        const userId = localStorage.getItem('userId');
        console.log('用户ID:', userId);
        
        if (userId) {
          console.log('用户ID存在，准备保存考试记录');
          const actualDurationSeconds = examTime.value - remainingTime.value;
          const actualDurationMinutes = Math.round(actualDurationSeconds / 60);
          
          // 准备考试题目数据
          const examQuestions = userAnswers.value.map(item => ({
            questionId: item.questionId,
            userAnswer: item.answer,
            isCorrect: item.isCorrect ? 1 : 0,
            answerTime: item.timeCost // 使用真实的答题时间
          }));
          
          const examRecordData = {
            userId: parseInt(userId),
            examName: examModes.find(m => m.value === selectedMode.value).label,
            examMode: selectedMode.value,
            totalQuestions: totalQuestions.value,
            correctQuestions: correctCount.value,
            score: score.value,
            duration: actualDurationMinutes, // 实际作答时长（分钟）
            startTime: formatDateTime(new Date(Date.now() - actualDurationSeconds * 1000)),
            endTime: formatDateTime(new Date()),
            status: 1, // 已完成
            questions: examQuestions
          };
          
          console.log('保存考试记录请求数据:', examRecordData);
          try {
            console.log('调用 saveExamRecord 函数');
            const recordResponse = await saveExamRecord(examRecordData);
            console.log('保存考试记录响应:', recordResponse);
            if (recordResponse && recordResponse.code === 200) {
              console.log('考试记录保存成功');
            } else {
              console.error('保存考试记录失败:', recordResponse?.message || '未知错误');
              showToast('保存考试记录失败: ' + (recordResponse?.message || '未知错误'));
            }
          } catch (error) {
            console.error('保存考试记录失败:', error);
            showToast('保存考试记录失败: ' + (error.message || '网络错误'));
          }
        } else {
          console.log('用户ID不存在，跳过保存考试记录');
        }
        
        // 停止倒计时
        if (timer.value) {
          clearInterval(timer.value);
        }
        
        // 显示结果
        console.log('显示结果弹窗');
        showResult.value = true;
      } catch (error) {
        console.error('提交考试时发生错误:', error);
        showToast('提交考试失败，请重试');
      } finally {
        isSubmitting.value = false;
      }
    };
    
    const confirmExit = () => {
      Dialog.confirm({
        title: '退出考试',
        message: '确定要退出考试吗？当前进度将会丢失。',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(() => {
        // 停止倒计时
        if (timer.value) {
          clearInterval(timer.value);
        }
        router.go(-1);
      }).catch(() => {
        // 取消退出
      });
    };
    
    const goBack = () => {
      router.go(-1);
    };
    
    const goToLogin = () => {
      router.push('/login');
    };
    
    const viewExamDetails = () => {
      showResult.value = false;
      // 跳转到考试详情页面
      router.push('/exercise/stats');
    };
    
    const goToHome = () => {
      showResult.value = false;
      router.push('/exercise');
    };
    


    watch(currentQuestionIndex, () => {
      loadCurrentAnswer();
    });
    
    onMounted(async () => {
      // 检查登录状态
      checkLoginStatus();
      // 加载题目分类
      await loadCategories();
    });
    
    onUnmounted(() => {
      // 清理定时器
      if (timer.value) {
        clearInterval(timer.value);
      }
    });
    
    return {
      // 考试模式选择相关
      examStarted,
      selectedMode,
      loading,
      isLoggedIn,
      configForm,
      examModes,
      categoryOptions,
      selectMode,
      startExam,
      goToLogin,
      checkLoginStatus,
      
      // 考试相关
      currentQuestion,
      currentQuestionIndex,
      selectedAnswer,
      selectedAnswers,
      textAnswer,
      totalQuestions,
      isLastQuestion,
      isSubmitting,
      showResult,
      correctCount,
      score,
      remainingTime,
      getQuestionTypeText,
      selectOption,
      onAnswerSelect,
      onAnswersSelect,
      toggleMultiOption,
      prevQuestion,
      nextQuestion,
      submitExam,
      confirmExit,
      goBack,
      viewExamDetails,
      goToHome,
      formatTime,
      getGradeInfo
    };
  }
};
</script>

<style scoped>
.exam-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding-bottom: 100px;
}

/* 考试模式选择页面样式 */
.mode-select-page {
  min-height: 100vh;
}

.mode-select-content {
  padding: 20px;
}

.mode-select-content h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
  font-weight: 700;
}

.guest-notice {
  background-color: #f4f4f5;
  color: #909399;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mode-options {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin: 20px 0;
}

.van-card {
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.van-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.van-card.active {
  border: 2px solid #1989fa;
}

.exam-info {
  font-size: 14px;
  color: #666;
  margin-top: 8px;
}

.config-section {
  margin-top: 30px;
  padding: 20px;
  border: 1px solid #ebedf0;
  border-radius: 4px;
  background-color: white;
}

.config-section .van-button {
  margin-top: 20px;
  height: 48px;
  font-weight: 600;
}

/* 考试页面样式 */
.exam-page {
  min-height: 100vh;
}

.top-bar {
  background: white;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.time-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-text {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.time-text.warning {
  color: #faad14;
}

.time-text.danger {
  color: #f5222d;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

.question-progress {
  font-size: 14px;
  color: #666;
}

.question-container {
  padding: 0 16px;
  margin: 20px 0;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.question-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  line-height: 1.5;
  flex: 1;
  margin-right: 12px;
}

.question-type {
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  align-self: flex-start;
}

.options-container {
  margin-top: 16px;
}

.option-label {
  font-weight: 600;
  margin-right: 8px;
  color: #409eff;
}

.option-value {
  color: #333;
}

.action-buttons {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  background: white;
  display: flex;
  gap: 12px;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.action-buttons .van-button {
  flex: 1;
  height: 48px;
  font-weight: 600;
}

.result-dialog-content {
  padding: 16px;
  text-align: center;
}

.result-dialog-content p {
  color: #666;
  font-size: 14px;
  margin: 0 0 24px 0;
}

.result-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 24px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

.exam-grade {
  font-size: 20px;
  font-weight: 700;
  margin-top: 16px;
  padding: 8px 0;
  border-radius: 4px;
}

.grade-excellent {
  color: #52c41a;
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
}

.grade-good {
  color: #1890ff;
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
}

.grade-pass {
  color: #faad14;
  background-color: #fffbe6;
  border: 1px solid #ffe58f;
}

.grade-fail {
  color: #f5222d;
  background-color: #fff1f0;
  border: 1px solid #ffccc7;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
}
</style>