<template>
  <div class="exercise-container">
    <!-- 练习模式选择页面 -->
    <div v-if="!exerciseStarted" class="mode-select-page">
      <div class="page-header">
        <button class="back-button" @click="goBack">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M19 12H5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M12 19L5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          返回
        </button>
        <h1>选择练习模式</h1>
        <div class="header-right"></div>
      </div>
      
      <div class="mode-select-content">
        <div v-if="!isLoggedIn" class="guest-notice">
          <p>您当前是游客模式，可以免费练习，但练习记录不会保存。</p>
          <button class="login-button" @click="goToLogin">立即登录</button>
        </div>
        
        <div class="mode-options">
          <div 
            v-for="mode in exerciseModes" 
            :key="mode.value"
            :class="{ active: selectedMode === mode.value, 'require-login': mode.requireLogin }"
            @click="selectMode(mode.value)"
            class="mode-card"
          >
            <h3>{{ mode.label }}</h3>
            <p>{{ mode.description }}</p>
            <span v-if="mode.requireLogin" class="login-required">需登录</span>
          </div>
        </div>
        
        <!-- 知识点列表页面 -->
        <div v-if="selectedMode === 1 && showKnowledgePoints" class="knowledge-points-section">
          <div class="knowledge-header">
            <h2>{{ subjectOptions.find(s => s.value === configForm.subjectId)?.text || '当前学科' }} 的知识点</h2>
            <button class="back-button-small" @click="showKnowledgePoints = false">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M19 12H5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 19L5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              返回
            </button>
          </div>
          
          <div v-if="loading" class="loading-container">
            <div class="loading-spinner"></div>
            <p>加载知识点中...</p>
          </div>
          
          <div v-else-if="knowledgePoints.length === 0" class="empty-state">
            <p>暂无知识点数据</p>
          </div>
          
          <div v-else class="knowledge-points-list">
            <div 
              v-for="point in knowledgePoints" 
              :key="point.id"
              class="knowledge-point-item"
              @click="selectKnowledgePoint(point)"
            >
              <div class="knowledge-point-info">
                <h3>{{ point.name }}</h3>
                <div class="knowledge-progress">
                  <div class="progress-bar">
                    <div class="progress-fill" :style="{ width: point.progress + '%' }"></div>
                  </div>
                  <span class="progress-text">{{ point.progress }}%</span>
                </div>
                <div class="knowledge-stats">
                  <span>{{ point.completedQuestions }}/{{ point.totalQuestions }} 题</span>
                </div>
              </div>
              <div class="knowledge-point-action">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 其他模式的配置页面 -->
        <div v-else-if="selectedMode" class="config-section">
          <div class="config-grid">
            <div class="config-item">
              <label>题目数量</label>
              <div class="slider-container">
                <input 
                  type="range" 
                  v-model="configForm.count" 
                  min="5" 
                  max="50" 
                  step="5"
                  class="config-slider"
                />
                <span class="slider-value">{{ configForm.count }}题</span>
              </div>
            </div>
            
            <div class="config-item">
              <label>难度等级</label>
              <select v-model="configForm.difficulty" class="config-select">
                <option value="">选择难度</option>
                <option v-for="option in difficultyOptions" :key="option.value" :value="option.value">
                  {{ option.text }}
                </option>
              </select>
            </div>
            
            <!-- 如果没有选中学科，显示学科选择 -->
            <div v-if="!hasSelectedSubject" class="config-item">
              <label>学科</label>
              <select v-model="configForm.subjectId" class="config-select" @change="handleSubjectChange">
                <option value="">选择学科</option>
                <option v-for="option in subjectOptions" :key="option.value" :value="option.value">
                  {{ option.text }}
                </option>
              </select>
            </div>
            
            <!-- 如果有选中学科，显示已选择的学科信息 -->
            <div v-else class="config-item">
              <label>已选择学科</label>
              <div class="selected-subject-info">
                {{ subjectOptions.find(s => s.value === configForm.subjectId)?.text || '未知学科' }}
              </div>
            </div>
            
            <div class="config-item">
              <label>知识点分类</label>
              <select v-model="configForm.categoryId" class="config-select">
                <option value="">选择知识点</option>
                <option v-for="option in categoryOptions" :key="option.value" :value="option.value">
                  {{ option.text }}
                </option>
              </select>
            </div>
          </div>
          
          <button class="start-button" @click="startExercise" :disabled="loading">
            {{ loading ? '加载中...' : '开始练习' }}
          </button>
        </div>
      </div>
    </div>
    
    <!-- 练习页面 -->
    <div v-else class="exercise-page">
      <div class="page-header">
        <button class="back-button" @click="goBack">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M19 12H5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M12 19L5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          返回
        </button>
        <h1>刷题练习</h1>
        <div class="header-right">
          <span class="question-progress">第 {{ currentQuestionIndex + 1 }} 题 / 共 {{ totalQuestions }} 题</span>
        </div>
      </div>
      
      <div class="progress-container">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progress + '%' }"></div>
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
          <div v-if="currentQuestion.type === 1" class="option-group">
            <div
              v-for="(option, index) in currentQuestion.options"
              :key="index"
              :class="{ selected: selectedAnswer === option.label }"
              @click="selectOption(option.label)"
              class="option-item"
            >
              <div class="option-radio">
                <div class="radio-circle" :class="{ checked: selectedAnswer === option.label }"></div>
              </div>
              <div class="option-content">
                <span class="option-label">{{ option.label }}. </span>
                <span class="option-value">{{ option.value }}</span>
              </div>
            </div>
          </div>

          <!-- 多选题 -->
          <div v-else-if="currentQuestion.type === 2" class="option-group">
            <div
              v-for="(option, index) in currentQuestion.options"
              :key="index"
              :class="{ selected: selectedAnswers.includes(option.label) }"
              @click="toggleMultiOption(option.label)"
              class="option-item"
            >
              <div class="option-checkbox">
                <div class="checkbox-square" :class="{ checked: selectedAnswers.includes(option.label) }">
                  <svg v-if="selectedAnswers.includes(option.label)" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M20 6L9 17L4 12" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
              </div>
              <div class="option-content">
                <span class="option-label">{{ option.label }}. </span>
                <span class="option-value">{{ option.value }}</span>
              </div>
            </div>
          </div>

          <!-- 判断题：约定 A=正确，B=错误 -->
          <div v-else-if="currentQuestion.type === 3" class="option-group">
            <div
              :class="{ selected: selectedAnswer === 'A' }"
              @click="selectOption('A')"
              class="option-item"
            >
              <div class="option-radio">
                <div class="radio-circle" :class="{ checked: selectedAnswer === 'A' }"></div>
              </div>
              <div class="option-content">
                <span class="option-label">A. </span>
                <span class="option-value">正确</span>
              </div>
            </div>
            <div
              :class="{ selected: selectedAnswer === 'B' }"
              @click="selectOption('B')"
              class="option-item"
            >
              <div class="option-radio">
                <div class="radio-circle" :class="{ checked: selectedAnswer === 'B' }"></div>
              </div>
              <div class="option-content">
                <span class="option-label">B. </span>
                <span class="option-value">错误</span>
              </div>
            </div>
          </div>

          <!-- 填空题 -->
          <div v-else-if="currentQuestion.type === 4" class="text-answer">
            <input
              v-model="textAnswer"
              type="text"
              placeholder="请输入答案"
              class="text-input"
            />
          </div>

          <!-- 简答题 -->
          <div v-else-if="currentQuestion.type === 5" class="text-answer">
            <textarea
              v-model="textAnswer"
              rows="5"
              placeholder="请输入答案"
              class="text-area"
            ></textarea>
          </div>

          <div v-else class="no-question">
            <p>未知题型，无法作答</p>
          </div>
        </div>
      </div>
      
      <div class="loading-container" v-else>
        <div class="loading-spinner"></div>
        <p>加载题目中...</p>
      </div>
      
      <div class="action-buttons">
        <button 
          class="action-button secondary"
          @click="skipQuestion"
          :disabled="isLastQuestion || !currentQuestion"
        >
          跳过
        </button>
        <button 
          class="action-button primary"
          @click="submitAnswer"
          :disabled="!canSubmitCurrent || isSubmitting || !currentQuestion"
        >
          <span v-if="isSubmitting" class="loading-dots">提交中...</span>
          <span v-else>{{ isLastQuestion ? '提交练习' : '下一题' }}</span>
        </button>
      </div>
      
      <!-- 练习完成结果弹窗 -->
      <div v-if="showResult" class="result-modal">
        <div class="modal-overlay" @click="showResult = false"></div>
        <div class="modal-content">
          <div class="modal-header">
            <h3>练习完成！</h3>
            <button class="close-button" @click="showResult = false">×</button>
          </div>
          <div class="modal-body">
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
            
            <!-- 查看作答详情按钮 -->
            <button class="modal-button secondary" @click="toggleAnswerDetails">
              {{ showAnswerDetails ? '收起作答详情' : '查看作答详情' }}
            </button>
            
            <!-- 题目作答详情（默认隐藏） -->
            <div v-if="showAnswerDetails" class="question-details">
              <h4>作答详情</h4>
              <div v-if="!userAnswers || !Array.isArray(userAnswers) || userAnswers.length === 0" class="empty-state">
                <p>暂无作答记录</p>
              </div>
              <div v-else class="question-list">
                <div 
                  v-for="(item, index) in userAnswers" 
                  :key="item?.questionId || index"
                  class="question-item"
                >
                  <div class="question-header" @click="toggleQuestion(index)">
                    <div class="question-info">
                      <span class="question-number">{{ index + 1 }}.</span>
                      <span class="question-title">{{ item.question?.title || '未知题目' }}</span>
                      <span 
                        :class="['result-badge', item.isCorrect ? 'correct' : 'wrong']"
                      >
                        {{ item.isCorrect ? '正确' : '错误' }}
                      </span>
                    </div>
                    <div class="question-toggle">
                      <svg 
                        width="16" 
                        height="16" 
                        viewBox="0 0 24 24" 
                        fill="none" 
                        xmlns="http://www.w3.org/2000/svg"
                        :class="{ rotated: expandedQuestions[index] }"
                      >
                        <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                      </svg>
                    </div>
                  </div>
                  
                  <div v-if="expandedQuestions[index]" class="question-content">
                    <div class="answer-section">
                      <div class="answer-item">
                        <span class="answer-label">你的答案：</span>
                        <span :class="['answer-value', item.isCorrect ? 'correct' : 'wrong']">
                          {{ item.userAnswer || '未作答' }}
                        </span>
                      </div>
                      <div class="answer-item">
                        <span class="answer-label">正确答案：</span>
                        <span class="answer-value correct">{{ item.correctAnswer || '未知' }}</span>
                      </div>
                    </div>
                    
                    <!-- 题目选项（如果有） -->
                    <div v-if="item.question && item.question.options && Array.isArray(item.question.options) && item.question.options.length > 0" class="options-section">
                      <h5>选项：</h5>
                      <div class="options-list">
                        <div 
                          v-for="option in item.question.options" 
                          :key="option.label"
                          :class="[
                            'option-detail',
                            option.label === item.correctAnswer ? 'correct-option' : '',
                            option.label === item.userAnswer && !item.isCorrect ? 'wrong-option' : ''
                          ]"
                        >
                          <span class="option-label">{{ option.label }}.</span>
                          <span class="option-value">{{ option.value }}</span>
                        </div>
                      </div>
                    </div>
                    
                    <!-- 答案解析 -->
                    <div v-if="item.analysis" class="analysis-section">
                      <h5>解析：</h5>
                      <div class="analysis-content">{{ item.analysis }}</div>
                    </div>
                    <div v-else class="analysis-section">
                      <h5>解析：</h5>
                      <div class="analysis-content">暂无解析</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <button class="modal-button primary" @click="viewStats">查看统计</button>
          </div>
          <div class="modal-footer">
            <button class="modal-button secondary" @click="startNewExercise">继续练习</button>
            <button class="modal-button primary" @click="goToHome">返回首页</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { getRandomQuestions, getAllCategories, getCategoriesBySubjectId, getKnowledgePointProgress, getAllSubjects, submitAnswer as submitAnswerApi, batchSubmitAnswers as batchSubmitAnswersApi, getUserStats, getRandomWrongQuestions } from '../api/exercise';
import { parseQuestionOptions, getQuestionTypeText, calculateTimeCost, getCurrentSubmitAnswer } from '../utils/questionUtils';

// 简单的提示函数
const showToast = (message) => {
  alert(message);
};

export default {
  name: 'ExerciseView',
  setup() {
    const router = useRouter();
    
    // 练习模式选择相关
    const exerciseStarted = ref(false);
    const selectedMode = ref(null);
    const loading = ref(false);
    const isLoggedIn = ref(!!localStorage.getItem('token'));
    const showKnowledgePoints = ref(false);
    const knowledgePoints = ref([]);
    
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
        label: '按知识点练习',
        description: '只刷对应知识点的题，针对性练习'
      },
      {
        value: 2,
        label: '随机练习',
        description: '将所有知识点题目随机抽取，组合练习'
      },
      {
        value: 3,
        label: '错题重练',
        description: '只练本学科的错题，巩固薄弱知识点',
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
    const questionStartTime = ref(null); // 记录当前题目的开始时间
    const expandedQuestions = ref({}); // 控制题目展开/收起状态
    const showAnswerDetails = ref(false); // 控制是否显示作答详情
    
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
    
    // 检查是否有选中学科
    const hasSelectedSubject = computed(() => {
      return !!configForm.subjectId;
    });
    


    const canSubmitCurrent = computed(() => {
      if (!currentQuestion.value) return false;
      const type = currentQuestion.value.type;
      if (type === 2) return Array.isArray(selectedAnswers.value) && selectedAnswers.value.length > 0;
      if (type === 4 || type === 5) return typeof textAnswer.value === 'string' && textAnswer.value.trim().length > 0;
      return !!selectedAnswer.value;
    });
    
    // 加载题目分类
    const loadCategories = async (subjectId = null) => {
      try {
        let response;
        if (subjectId) {
          // 如果指定了学科ID，只加载该学科的分类
          response = await getCategoriesBySubjectId(subjectId);
        } else {
          // 否则加载所有分类
          response = await getAllCategories();
        }
        
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
    
    const loadKnowledgePoints = async () => {
      try {
        loading.value = true;
        if (configForm.subjectId) {
          // 获取当前用户ID
          const userId = localStorage.getItem('userId');
          
          // 调用后端API获取知识点进度
          const response = await getKnowledgePointProgress({
            subjectId: configForm.subjectId,
            userId: userId ? parseInt(userId) : null
          });
          
          if (response && response.code === 200) {
            knowledgePoints.value = response.data || [];
          } else {
            // 如果API调用失败，使用默认数据
            await loadCategories(configForm.subjectId);
            knowledgePoints.value = categoryOptions.value.map(category => ({
              id: category.value,
              name: category.text,
              progress: 0,
              totalQuestions: 0,
              completedQuestions: 0
            }));
          }
        } else {
          // 如果没有选中学科，先加载所有学科
          await loadSubjects();
          // 默认选择第一个学科
          if (subjectOptions.value.length > 0) {
            configForm.subjectId = subjectOptions.value[0].value;
            await loadKnowledgePoints();
          }
        }
      } catch (error) {
        console.error('加载知识点失败:', error);
        showToast('加载知识点失败');
        // 失败时使用默认数据
        if (configForm.subjectId) {
          await loadCategories(configForm.subjectId);
          knowledgePoints.value = categoryOptions.value.map(category => ({
            id: category.value,
            name: category.text,
            progress: 0,
            totalQuestions: 0,
            completedQuestions: 0
          }));
        }
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
      
      // 如果选择按知识点练习，直接显示知识点列表
      if (mode === 1) {
        loadKnowledgePoints();
        showKnowledgePoints.value = true;
      } else {
        showKnowledgePoints.value = false;
      }
    };

    const selectKnowledgePoint = async (point) => {
      // 设置选择的知识点
      configForm.categoryId = point.id;
      // 开始练习
      await startExercise();
    };
    
    // 学科变更时加载对应分类
    const handleSubjectChange = async (subjectId) => {
      if (subjectId) {
        // 如果当前是按知识点练习模式，调用API获取知识点进度
        if (selectedMode.value === 1) {
          await loadKnowledgePoints();
        } else {
          await loadCategories(subjectId);
        }
      } else {
        await loadCategories();
      }
    };
    
    const startExercise = async () => {
      if (!selectedMode.value) {
        showToast('请选择练习模式');
        return;
      }

      loading.value = true;
      try {
        if (selectedMode.value === 3) {
          // 错题重练模式
          if (!isLoggedIn.value) {
            showToast('错题重练需要登录');
            loading.value = false;
            return;
          }
          
          const params = {
            count: configForm.count
          };
          
          // 如果选择了学科，只获取该学科的错题
          if (configForm.subjectId) {
            params.subjectId = configForm.subjectId;
          }
          
          // 如果选择了知识点，只获取该知识点的错题
          if (configForm.categoryId) {
            params.categoryId = configForm.categoryId;
          }
          
          // 调用错题重练API
          const response = await getRandomWrongQuestions(configForm.count);
          console.log('错题重练API响应:', response);
          
          if (response.code === 200 && Array.isArray(response.data)) {
            questions.value = response.data.map(q => {
              let options = [];
              if (q.options) {
                options = parseQuestionOptions(q.options);
              } else if (q.optionA) {
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
          } else {
            questions.value = [];
          }
        } else {
          // 按知识点练习或随机练习模式
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
          
          // 按知识点练习时必须选择知识点
          if (selectedMode.value === 1 && !configForm.categoryId) {
            showToast('按知识点练习请选择知识点');
            loading.value = false;
            return;
          }
          
          if (configForm.categoryId) {
            params.categoryId = configForm.categoryId;
          }

          // 调用后端API获取题目
          await loadQuestions(params);
        }
        
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
          expandedQuestions.value = {}; // 重置展开状态
          showAnswerDetails.value = false; // 重置作答详情显示状态
          // 初始化第一个题目的开始时间
          questionStartTime.value = Date.now();
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

    const toggleQuestion = (index) => {
      expandedQuestions.value = {
        ...expandedQuestions.value,
        [index]: !expandedQuestions.value[index]
      };
    };

    const toggleAnswerDetails = () => {
      showAnswerDetails.value = !showAnswerDetails.value;
    };


    
    const submitAnswer = async () => {
      if (!currentQuestion.value) return;
      if (!canSubmitCurrent.value) return;
      
      isSubmitting.value = true;
      
      try {
        const submitAns = getCurrentSubmitAnswer(currentQuestion.value, selectedAnswer.value, selectedAnswers.value, textAnswer.value);

        // 计算答题时间（秒）
        const timeCost = calculateTimeCost(questionStartTime.value);
        
        // 保存用户答案和完整题目信息
        const answerData = {
          questionId: currentQuestion.value.id,
          question: currentQuestion.value, // 保存完整题目信息
          userAnswer: submitAns,
          correctAnswer: currentQuestion.value.answer,
          isCorrect: submitAns === currentQuestion.value.answer,
          timeCost: timeCost,
          analysis: currentQuestion.value.analysis // 保存解析信息
        };
        userAnswers.value.push(answerData);
        
        if (submitAns === currentQuestion.value.answer) {
          correctCount.value++;
        }
        
        // 检查登录状态
        checkLoginStatus();
        
        // 调用后端API提交答案
        const requestData = {
          questionId: currentQuestion.value.id,
          userAnswer: submitAns,
          timeCost: timeCost
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
              userAnswer: item.userAnswer,
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
          
          // 显示结果
          showResult.value = true;
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
      // 记录新题目的开始时间
      questionStartTime.value = Date.now();
    });
    
    onMounted(async () => {
      // 检查登录状态
      checkLoginStatus();
      
      // 检查本地存储中是否有选中学科
      const selectedSubjectId = localStorage.getItem('selectedSubjectId');
      if (selectedSubjectId) {
        // 设置学科ID
        configForm.subjectId = parseInt(selectedSubjectId);
        
        // 加载该学科的分类
        await loadCategories(configForm.subjectId);
      } else {
        // 如果没有选中学科，加载所有分类
        await loadCategories();
      }
      
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
      hasSelectedSubject,
      selectMode,
      handleSubjectChange,
      startExercise,
      goToLogin,
      checkLoginStatus,
      // 知识点练习相关
      showKnowledgePoints,
      knowledgePoints,
      selectKnowledgePoint,
      
      // 练习相关
      currentQuestion,
      currentQuestionIndex,
      selectedAnswer,
      selectedAnswers,
      textAnswer,
      userAnswers,
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
      toggleQuestion,
      toggleAnswerDetails,
      expandedQuestions,
      showAnswerDetails,
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
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
  color: white;
  padding: 1rem 2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.page-header h1 {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 0;
}

.back-button {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  transition: all 0.3s;
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.header-right {
  display: flex;
  align-items: center;
}

.question-progress {
  background: rgba(255, 255, 255, 0.2);
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
}

/* 练习模式选择页面 */
.mode-select-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.guest-notice {
  background: #fef0f0;
  border: 1px solid #fecaca;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.guest-notice p {
  margin: 0;
  color: #dc2626;
  font-weight: 500;
}

.login-button {
  background: #dc2626;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
}

.login-button:hover {
  background: #b91c1c;
  transform: translateY(-2px);
}

/* 模式选择卡片 */
.mode-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

/* 知识点列表页面 */
.knowledge-points-section {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.knowledge-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.knowledge-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
}

.back-button-small {
  background: #f3f4f6;
  color: #374151;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s;
}

.back-button-small:hover {
  background: #e5e7eb;
  transform: translateY(-1px);
}

.knowledge-points-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.knowledge-point-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
}

.knowledge-point-item:hover {
  border-color: #409eff;
  background: #f9fafb;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
}

.knowledge-point-info {
  flex: 1;
}

.knowledge-point-info h3 {
  margin: 0 0 0.75rem 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #1f2937;
}

.knowledge-progress {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.5rem;
}

.knowledge-progress .progress-bar {
  flex: 1;
  height: 6px;
  background: #e5e7eb;
  border-radius: 3px;
  overflow: hidden;
}

.knowledge-progress .progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #409eff 0%, #2a7fff 100%);
  transition: width 0.3s ease;
}

.progress-text {
  font-weight: 600;
  color: #409eff;
  min-width: 50px;
  text-align: right;
}

.knowledge-stats {
  font-size: 0.875rem;
  color: #6b7280;
}

.knowledge-point-action {
  color: #6b7280;
  transition: color 0.3s;
}

.knowledge-point-item:hover .knowledge-point-action {
  color: #409eff;
}

.mode-card {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.mode-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.mode-card.active {
  border-color: #409eff;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
}

.mode-card.require-login {
  opacity: 0.7;
}

.mode-card h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
}

.mode-card p {
  margin: 0 0 1rem 0;
  color: #6b7280;
  line-height: 1.5;
}

.login-required {
  background: #fefce8;
  color: #d97706;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

/* 配置区域 */
.config-section {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.config-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.config-item {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.config-item label {
  font-weight: 500;
  color: #374151;
  font-size: 0.9rem;
}

/* 滑块样式 */
.slider-container {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.config-slider {
  flex: 1;
  height: 6px;
  border-radius: 3px;
  background: #e5e7eb;
  outline: none;
  -webkit-appearance: none;
}

.config-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #409eff;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.4);
}

.slider-value {
  background: #409eff;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-weight: 500;
  min-width: 60px;
  text-align: center;
}

/* 选择框样式 */
.config-select {
  padding: 0.75rem 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 1rem;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
}

.config-select:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

/* 已选择学科样式 */
.selected-subject-info {
  padding: 0.75rem 1rem;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 8px;
  font-weight: 500;
  border: 1px solid #c6e2ff;
}

/* 开始按钮 */
.start-button {
  width: 100%;
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
  color: white;
  border: none;
  padding: 1rem;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.start-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.4);
}

.start-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 进度条 */
.progress-container {
  padding: 1rem 2rem;
  background: white;
}

.progress-bar {
  height: 8px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
  transition: width 0.3s ease;
}

/* 题目容器 */
.question-container {
  max-width: 800px;
  margin: 2rem auto;
  padding: 0 2rem;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
}

.question-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.6;
}

.question-type {
  background: #ecf5ff;
  color: #409eff;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 500;
}

/* 选项组 */
.options-container {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.option-group {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.option-item:hover {
  border-color: #409eff;
  background: #f9fafb;
}

.option-item.selected {
  border-color: #409eff;
  background: #f0f9ff;
}

/* 单选按钮 */
.option-radio {
  display: flex;
  align-items: center;
}

.radio-circle {
  width: 20px;
  height: 20px;
  border: 2px solid #d1d5db;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.radio-circle.checked {
  border-color: #409eff;
  background: #409eff;
}

.radio-circle.checked::after {
  content: '';
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
}

/* 复选框 */
.option-checkbox {
  display: flex;
  align-items: center;
}

.checkbox-square {
  width: 20px;
  height: 20px;
  border: 2px solid #d1d5db;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.checkbox-square.checked {
  border-color: #409eff;
  background: #409eff;
}

.option-content {
  flex: 1;
}

.option-label {
  font-weight: 600;
  color: #374151;
  margin-right: 0.5rem;
}

.option-value {
  color: #4b5563;
  line-height: 1.5;
}

/* 文本输入 */
.text-answer {
  margin-top: 2rem;
}

.text-input,
.text-area {
  width: 100%;
  padding: 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 1rem;
  font-family: inherit;
  transition: all 0.3s;
}

.text-input:focus,
.text-area:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.text-area {
  resize: vertical;
  min-height: 120px;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem;
  gap: 1rem;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 1rem;
  padding: 2rem;
  background: white;
  position: sticky;
  bottom: 0;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
}

.action-button {
  flex: 1;
  padding: 1rem;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.action-button.primary {
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
  color: white;
}

.action-button.secondary {
  background: #f3f4f6;
  color: #374151;
}

.action-button:hover:not(:disabled) {
  transform: translateY(-2px);
}

.action-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-dots {
  display: inline-block;
}

/* 结果弹窗 */
.result-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  z-index: 1001;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6b7280;
  padding: 0.25rem;
  border-radius: 4px;
}

.close-button:hover {
  background: #f3f4f6;
}

.modal-body {
  padding: 2rem;
  text-align: center;
}

.modal-body p {
  margin: 0 0 2rem 0;
  color: #374151;
  font-size: 1.1rem;
}

.result-stats {
  display: flex;
  justify-content: center;
  gap: 3rem;
  margin-bottom: 2rem;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 2rem;
  font-weight: 700;
  color: #409eff;
}

.stat-label {
  font-size: 0.875rem;
  color: #6b7280;
  margin-top: 0.25rem;
}

.modal-footer {
  display: flex;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.modal-button {
  flex: 1;
  padding: 0.75rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.modal-button.primary {
  background: linear-gradient(135deg, #409eff 0%, #2a7fff 100%);
  color: white;
}

.modal-button.secondary {
  background: #f3f4f6;
  color: #374151;
}

.modal-button:hover {
  transform: translateY(-2px);
}

/* 题目详情样式 */
.question-details {
  margin-top: 2rem;
  border-top: 1px solid #e5e7eb;
  padding-top: 1.5rem;
}

.question-details h4 {
  margin: 0 0 1rem 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #374151;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.question-item {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  background: white;
}

.question-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  background: #f9fafb;
  cursor: pointer;
  transition: background-color 0.3s;
}

.question-header:hover {
  background: #f3f4f6;
}

.question-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
}

.question-number {
  font-weight: 600;
  color: #409eff;
  min-width: 24px;
}

.question-title {
  flex: 1;
  color: #374151;
  font-weight: 500;
  line-height: 1.4;
}

.result-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
}

.result-badge.correct {
  background: #d1fae5;
  color: #065f46;
}

.result-badge.wrong {
  background: #fee2e2;
  color: #991b1b;
}

.question-toggle {
  color: #6b7280;
  transition: color 0.3s, transform 0.3s;
}

.question-toggle svg.rotated {
  transform: rotate(180deg);
}

.question-content {
  padding: 1rem;
  border-top: 1px solid #e5e7eb;
}

.answer-section {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.answer-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.answer-label {
  font-weight: 600;
  color: #374151;
  min-width: 70px;
}

.answer-value {
  font-weight: 500;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
}

.answer-value.correct {
  background: #d1fae5;
  color: #065f46;
}

.answer-value.wrong {
  background: #fee2e2;
  color: #991b1b;
}

.options-section {
  margin-bottom: 1rem;
}

.options-section h5 {
  margin: 0 0 0.75rem 0;
  font-size: 0.9rem;
  font-weight: 600;
  color: #374151;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.option-detail {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  padding: 0.5rem;
  border-radius: 4px;
  border: 1px solid #e5e7eb;
}

.option-detail.correct-option {
  background: #d1fae5;
  border-color: #22c55e;
}

.option-detail.wrong-option {
  background: #fee2e2;
  border-color: #ef4444;
}

.analysis-section {
  background: #f8fafc;
  padding: 1rem;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.analysis-section h5 {
  margin: 0 0 0.5rem 0;
  font-size: 0.9rem;
  font-weight: 600;
  color: #374151;
}

.analysis-content {
  color: #4b5563;
  line-height: 1.5;
  font-size: 0.9rem;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 2rem;
  color: #6b7280;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px dashed #e5e7eb;
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