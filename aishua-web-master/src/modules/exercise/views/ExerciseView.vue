<template>
  <div class="exercise-container">
    <!-- 练习模式选择页面 -->
    <div v-if="!exerciseStarted" class="mode-select-page">
      <van-nav-bar
        title="选择练习模式"
        left-arrow
        @click-left="goBack"
      />
      
      <div class="mode-select-content">
        <h2>选择练习模式</h2>
        
        <div v-if="!isLoggedIn" class="guest-notice">
          <p>您当前是游客模式，可以免费练习，但练习记录不会保存。</p>
          <van-button type="primary" size="small" @click="goToLogin">立即登录</van-button>
        </div>
        
        <div class="mode-options">
          <van-card 
            v-for="mode in exerciseModes" 
            :key="mode.value"
            :class="{ active: selectedMode === mode.value, 'require-login': mode.requireLogin }"
            @click="selectMode(mode.value)"
          >
            <template #title>
              <h3>{{ mode.label }}</h3>
            </template>
            <template #desc>
              <p>{{ mode.description }}</p>
              <span v-if="mode.requireLogin" class="login-required">需登录</span>
            </template>
          </van-card>
        </div>
        
        <div class="config-section" v-if="selectedMode">
          <van-form>
            <van-field label="题目数量">
              <template #input>
                <van-slider 
                  v-model="configForm.count" 
                  :min="5" 
                  :max="50" 
                  :step="5"
                />
              </template>
            </van-field>
            
            <van-field label="难度等级">
              <template #input>
                <van-dropdown-menu>
                  <van-dropdown-item 
                    v-model="configForm.difficulty" 
                    :options="difficultyOptions"
                    placeholder="选择难度"
                  />
                </van-dropdown-menu>
              </template>
            </van-field>
            
            <van-field label="学科">
              <template #input>
                <van-dropdown-menu>
                  <van-dropdown-item 
                    v-model="configForm.subjectId" 
                    :options="subjectOptions"
                    placeholder="选择学科"
                  />
                </van-dropdown-menu>
              </template>
            </van-field>
            
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
          
          <van-button type="primary" block @click="startExercise" :loading="loading">
            开始练习
          </van-button>
        </div>
      </div>
    </div>
    
    <!-- 练习页面 -->
    <div v-else class="exercise-page">
      <van-nav-bar
        title="刷题练习"
        left-arrow
        @click-left="goBack"
      />
      
      <div class="progress-container">
        <van-progress 
          :percentage="progress" 
          stroke-width="8"
          color="linear-gradient(135deg, #409eff 0%, #2a7fff 100%)"
          :show-pivot="false"
        />
        <div class="progress-info">
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
          @click="skipQuestion"
          :disabled="isLastQuestion || !currentQuestion"
        >
          跳过
        </van-button>
        <van-button 
          type="primary" 
          size="large"
          @click="submitAnswer"
          :disabled="!canSubmitCurrent || isSubmitting || !currentQuestion"
          :loading="isSubmitting"
        >
          {{ isLastQuestion ? '提交练习' : '下一题' }}
        </van-button>
      </div>
      
      <!-- 练习完成结果弹窗 -->
      <van-dialog
        v-model:show="showResult"
        title="练习完成！"
        show-cancel-button
        confirm-button-text="返回首页"
        cancel-button-text="继续练习"
        @confirm="goToHome"
        @cancel="startNewExercise"
      >
        <div class="result-dialog-content">
          <p>您已完成本次练习</p>
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
          </div>
          <van-button type="primary" block @click="viewStats" style="margin-top: 20px;">查看统计</van-button>
        </div>
      </van-dialog>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { getRandomQuestions, getAllCategories, getAllSubjects, submitAnswer as submitAnswerApi, batchSubmitAnswers as batchSubmitAnswersApi, getUserStats } from '../api/exercise';
import { showToast } from 'vant';

export default {
  name: 'ExerciseView',
  setup() {
    const router = useRouter();
    
    // 练习模式选择相关
    const exerciseStarted = ref(false);
    const selectedMode = ref(null);
    const loading = ref(false);
    const isLoggedIn = ref(!!localStorage.getItem('token'));
    
    // 检查登录状态
    const checkLoginStatus = () => {
      const token = localStorage.getItem('token');
      const userId = localStorage.getItem('userId');
      isLoggedIn.value = !!token;
      console.log('登录状态:', isLoggedIn.value);
      console.log('Token:', token);
      console.log('UserId:', userId);
    };
    const configForm = reactive({
      count: 10,
      difficulty: null,
      subjectId: null,
      categoryId: null
    });
    
    // 练习模式选项
    const exerciseModes = [
      {
        value: 1,
        label: '顺序练习',
        description: '按顺序答题，可查看答案解析'
      },
      {
        value: 2,
        label: '随机练习',
        description: '随机抽取题目，提高应变能力'
      },
      {
        value: 3,
        label: '错题重练',
        description: '专门练习之前的错题',
        requireLogin: true
      },
      {
        value: 4,
        label: '限时练习',
        description: '在规定时间内完成题目',
        requireLogin: true
      }
    ];
    
    // 难度等级选项
    const difficultyOptions = [
      { text: '简单', value: 1 },
      { text: '中等', value: 2 },
      { text: '困难', value: 3 }
    ];
    
    // 题目分类选项
    const categoryOptions = ref([]);
    
    // 学科选项
    const subjectOptions = ref([]);
    
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
    
    const currentQuestion = computed(() => {
      return questions.value[currentQuestionIndex.value] || null;
    });
    
    const totalQuestions = computed(() => questions.value.length);
    const progress = computed(() => {
      return Math.round(((currentQuestionIndex.value + 1) / totalQuestions.value) * 100);
    });
    const isLastQuestion = computed(() => {
      return currentQuestionIndex.value >= totalQuestions.value - 1;
    });
    
    const getQuestionTypeText = (type) => {
      switch (type) {
        case 1: return '单选题';
        case 2: return '多选题';
        case 3: return '判断题';
        case 4: return '填空题';
        case 5: return '简答题';
        default: return '未知题型';
      }
    };

    const canSubmitCurrent = computed(() => {
      if (!currentQuestion.value) return false;
      const type = currentQuestion.value.type;
      if (type === 2) return Array.isArray(selectedAnswers.value) && selectedAnswers.value.length > 0;
      if (type === 4 || type === 5) return typeof textAnswer.value === 'string' && textAnswer.value.trim().length > 0;
      return !!selectedAnswer.value;
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
    
    // 加载学科
    const loadSubjects = async () => {
      try {
        const response = await getAllSubjects();
        if (response && response.code === 200) {
          subjectOptions.value = (response.data || []).map(subject => ({
            text: subject.name,
            value: subject.id
          }));
        }
      } catch (error) {
        console.error('加载学科失败:', error);
        showToast('加载学科失败');
      }
    };
    
    // 从后端获取题目
    const loadQuestions = async (params) => {
      try {
        loading.value = true;
        console.log('获取题目参数:', params);
        const response = await getRandomQuestions(params || { count: 10 });
        console.log('获取题目响应:', response);
        
        if (response.code === 200) {
          if (Array.isArray(response.data)) {
            // 转换后端返回的题目数据为前端所需格式
            questions.value = response.data.map(q => {
              // 处理选项数据，确保格式正确
              let options = [];

              // 尝试从不同的可能字段获取选项
              if (q.options) {
                let rawOptions = q.options;

                // 后端字段通常是 JSON 字符串，这里优先做解析
                if (typeof rawOptions === 'string') {
                  try {
                    rawOptions = JSON.parse(rawOptions);
                  } catch (e) {
                    console.error('解析题目选项失败，使用空选项:', e, q);
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
                } else {
                  options = [];
                }
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
              } else {
                // 如果没有找到选项，创建一个空数组
                options = [];
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
      const modeConfig = exerciseModes.find(m => m.value === mode);
      if (modeConfig && modeConfig.requireLogin && !isLoggedIn.value) {
        showToast('此功能需要登录才能使用');
        return;
      }
      selectedMode.value = mode;
    };
    
    const startExercise = async () => {
      if (!selectedMode.value) {
        showToast('请选择练习模式');
        return;
      }

      loading.value = true;
      try {
        const params = {
          count: configForm.count,
          exerciseMode: selectedMode.value
        };

        if (configForm.difficulty) {
          params.difficulty = configForm.difficulty;
        }
        if (configForm.subjectId) {
          params.subjectId = configForm.subjectId;
        }
        if (configForm.categoryId) {
          params.categoryId = configForm.categoryId;
        }

        // 调用后端API获取题目
        await loadQuestions(params);
        
        if (questions.value.length > 0) {
          // 开始练习
          exerciseStarted.value = true;
          // 重置练习状态
          currentQuestionIndex.value = 0;
          selectedAnswer.value = '';
          selectedAnswers.value = [];
          textAnswer.value = '';
          userAnswers.value = [];
          correctCount.value = 0;
        } else {
          showToast('获取题目失败，请重试');
        }
      } catch (error) {
        console.error('开始练习失败:', error);
        showToast('开始练习失败，请重试');
      } finally {
        loading.value = false;
      }
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

    const getCurrentSubmitAnswer = () => {
      const q = currentQuestion.value;
      if (!q) return '';
      if (q.type === 2) return [...selectedAnswers.value].sort().join('');
      if (q.type === 4 || q.type === 5) return textAnswer.value.trim();
      return selectedAnswer.value;
    };
    
    const submitAnswer = async () => {
      if (!currentQuestion.value) return;
      if (!canSubmitCurrent.value) return;
      
      isSubmitting.value = true;
      
      try {
        const submitAns = getCurrentSubmitAnswer();

        // 保存用户答案
        userAnswers.value.push({
          questionId: currentQuestion.value.id,
          answer: submitAns,
          isCorrect: submitAns === currentQuestion.value.answer
        });
        
        if (submitAns === currentQuestion.value.answer) {
          correctCount.value++;
        }
        
        // 检查登录状态
        checkLoginStatus();
        
        // 调用后端API提交答案
        const requestData = {
          questionId: currentQuestion.value.id,
          userAnswer: submitAns
        };
        
        console.log('提交答案请求数据:', requestData);
        console.log('登录状态:', isLoggedIn.value);
        console.log('Token:', localStorage.getItem('token'));
        
        const response = await submitAnswerApi(requestData);
        console.log('提交答案响应:', response);
        
        if (isLastQuestion.value) {
          // 练习完成，批量提交所有答案
          if (userAnswers.value.length > 0) {
            const batchData = userAnswers.value.map(item => ({
              questionId: item.questionId,
              userAnswer: item.answer
            }));
            
            console.log('批量提交答案请求数据:', batchData);
            try {
              const batchResponse = await batchSubmitAnswersApi(batchData);
              console.log('批量提交答案响应:', batchResponse);
            } catch (error) {
              console.error('批量提交答案失败:', error);
            }
          }
          
          // 显示结果
          console.log('显示结果弹窗');
          showResult.value = true;
          
          // 5秒后自动返回个人中心，给用户足够时间查看结果
          setTimeout(() => {
            showResult.value = false;
            router.push('/dashboard');
          }, 5000);
        } else {
          // 下一题
          currentQuestionIndex.value++;
          selectedAnswer.value = '';
          selectedAnswers.value = [];
          textAnswer.value = '';
        }
      } catch (error) {
        console.error('提交答案时发生错误:', error);
        showToast('提交答案失败，请重试');
      } finally {
        isSubmitting.value = false;
      }
    };
    
    const skipQuestion = () => {
      if (isLastQuestion.value) return;
      currentQuestionIndex.value++;
      selectedAnswer.value = '';
      selectedAnswers.value = [];
      textAnswer.value = '';
    };
    
    const goBack = () => {
      if (exerciseStarted.value) {
        // 如果已经开始练习，询问是否确认返回
        if (confirm('确定要退出练习吗？当前进度将会丢失。')) {
          router.go(-1);
        }
      } else {
        router.go(-1);
      }
    };
    
    const goToLogin = () => {
      router.push('/login');
    };
    
    const viewStats = () => {
      showResult.value = false;
      router.push('/exercise/stats');
    };
    
    const startNewExercise = () => {
      showResult.value = false;
      exerciseStarted.value = false;
      selectedMode.value = null;
      configForm.count = 10;
      configForm.difficulty = null;
      configForm.categoryId = null;
    };
    
    const goToHome = () => {
      showResult.value = false;
      router.push('/dashboard');
    };

    watch(currentQuestionIndex, () => {
      selectedAnswer.value = '';
      selectedAnswers.value = [];
      textAnswer.value = '';
    });
    
    onMounted(async () => {
      // 检查登录状态
      checkLoginStatus();
      // 加载题目分类
      await loadCategories();
      // 加载学科
      await loadSubjects();
      // 测试获取用户统计，验证登录状态
      if (isLoggedIn.value) {
        try {
          const statsResponse = await getUserStats();
          console.log('获取用户统计响应:', statsResponse);
        } catch (error) {
          console.error('获取用户统计失败:', error);
        }
      }
    });
    
    return {
      // 练习模式选择相关
      exerciseStarted,
      selectedMode,
      loading,
      isLoggedIn,
      configForm,
      exerciseModes,
      difficultyOptions,
      subjectOptions,
      categoryOptions,
      selectMode,
      startExercise,
      goToLogin,
      checkLoginStatus,
      
      // 练习相关
      currentQuestion,
      currentQuestionIndex,
      selectedAnswer,
      selectedAnswers,
      textAnswer,
      totalQuestions,
      progress,
      isLastQuestion,
      isSubmitting,
      showResult,
      correctCount,
      getQuestionTypeText,
      canSubmitCurrent,
      selectOption,
      onAnswerSelect,
      onAnswersSelect,
      toggleMultiOption,
      submitAnswer,
      skipQuestion,
      goBack,
      viewStats,
      startNewExercise,
      goToHome
    };
  }
};
</script>

<style scoped>
.exercise-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding-bottom: 80px;
}

/* 练习模式选择页面样式 */
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

.van-card.require-login .van-card__content {
  opacity: 0.7;
}

.login-required {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #ee0a24;
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 3px;
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

/* 练习页面样式 */
.exercise-page {
  min-height: 100vh;
}

.progress-container {
  padding: 20px 16px;
}

.progress-info {
  text-align: center;
  margin-top: 8px;
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

.result-container {
  padding: 24px 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.result-header {
  text-align: center;
  margin-bottom: 32px;
}

.result-header h3 {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
}

.result-header p {
  color: #666;
  font-size: 14px;
  margin: 0;
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

.result-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-actions .van-button {
  height: 48px;
  font-weight: 600;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
}
</style>