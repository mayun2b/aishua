<template>
  <div class="exercise-page">
    <NavBar 
      :title="isGuestMode ? '游客模式 - 刷题练习' : '刷题练习'" 
      left-text="返回选择" 
      left-arrow 
      @click-left="goBack"
    >
      <template #right>
        <span>当前进度: {{ currentQuestionIndex + 1 }} / {{ questions.length }}</span>
      </template>
    </NavBar>

    <transition name="slide-fade">
      <div v-if="currentQuestion" key="question" class="exercise-content">
        <CellGroup>
          <Cell :title="'第 ' + (currentQuestionIndex + 1) + ' 题'" :label="currentQuestion.title" />
          
          <div v-if="currentQuestion.type <= 3" class="question-options">
            <div 
              v-for="(option, key) in parsedOptions" 
              :key="key"
              class="option-item"
            >
              <Radio 
                v-if="currentQuestion.type === 1 || currentQuestion.type === 3" 
                :name="key"
                :disabled="isGuestMode && showAnalysis[currentQuestionIndex]"
              >
                <div class="option-content">
                  <strong>{{ key }}.</strong> {{ option }}
                </div>
              </Radio>
              
              <Checkbox 
                v-else-if="currentQuestion.type === 2"
                :name="key"
                :disabled="isGuestMode && showAnalysis[currentQuestionIndex]"
              >
                <div class="option-content">
                  <strong>{{ key }}.</strong> {{ option }}
                </div>
              </Checkbox>
            </div>
          </div>
          
          <div v-else-if="currentQuestion.type === 4" class="fill-blank">
            <Field 
              v-model="userAnswers[currentQuestionIndex]" 
              rows="1" 
              autosize 
              type="textarea" 
              placeholder="请输入答案"
              :readonly="isGuestMode && showAnalysis[currentQuestionIndex]"
            />
          </div>
          
          <div v-else-if="currentQuestion.type === 5" class="short-answer">
            <Field 
              v-model="userAnswers[currentQuestionIndex]" 
              rows="3" 
              autosize 
              type="textarea" 
              placeholder="请输入答案"
              :readonly="isGuestMode && showAnalysis[currentQuestionIndex]"
            />
          </div>
        </CellGroup>

        <div class="question-analysis" v-if="showAnalysis[currentQuestionIndex]">
          <Divider>答案解析</Divider>
          <div class="analysis-content">
            <p><strong>正确答案：</strong>{{ currentQuestion.answer }}</p>
            <p><strong>解析：</strong>{{ currentQuestion.analysis || '暂无解析' }}</p>
          </div>
        </div>

        <div class="navigation-buttons">
          <Button 
            v-if="currentQuestionIndex > 0" 
            type="default" 
            @click="prevQuestion"
            :disabled="isGuestMode && showAnalysis[currentQuestionIndex]"
          >
            上一题
          </Button>
          
          <Button 
            v-if="currentQuestionIndex < questions.length - 1" 
            type="primary" 
            @click="nextQuestion"
            :disabled="isGuestMode && showAnalysis[currentQuestionIndex]"
          >
            下一题
          </Button>
          
          <Button 
            v-else 
            type="success" 
            @click="submitAll"
            :loading="submitting"
            :disabled="isGuestMode && showAnalysis[currentQuestionIndex]"
          >
            提交练习
          </Button>
        </div>
      </div>
    </transition>
    
    <!-- 提交结果弹窗 -->
    <Popup 
      v-if="submitResult" 
      v-model="showResultDialog" 
      position="center" 
      :style="{ width: '80%', height: '60%', padding: '20px' }"
    >
      <div class="result-content">
        <h3 v-if="isGuestMode" class="guest-warning">
          注意：您正在使用游客模式，本次练习不会保存到您的账户
        </h3>
        <div class="result-summary">
          <p>总题数: {{ submitResult.totalCount }}</p>
          <p>正确数: {{ submitResult.correctCount }}</p>
          <p>正确率: {{ (submitResult.correctRate * 100).toFixed(1) }}%</p>
        </div>
        <div class="result-actions">
          <Button @click="restartExercise" type="default" style="margin-right: 10px;">重新练习</Button>
          <Button type="primary" @click="goToDashboard">返回首页</Button>
        </div>
      </div>
    </Popup>
  </div>
</template>

<script>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { exerciseApi } from '@/modules/exercise/api/exercise';
// 重新添加Toast导入以解决ESLint报错和运行时引用问题
import { Toast, NavBar, CellGroup, Cell, Radio, Checkbox, Button, Field, Divider, Popup } from 'vant';

export default {
  name: 'ExercisePage',
  components: {
    NavBar,
    CellGroup,
    Cell,
    Radio,
    Checkbox,
    Button,
    Field,
    Divider,
    Popup
  },
  props: {
    exerciseData: {
      type: Object,
      required: true
    }
  },
  setup(props) {
    const router = useRouter();
    const questions = ref(props.exerciseData.questions || []);
    const currentQuestionIndex = ref(0);
    const userAnswers = ref({});
    const showAnalysis = ref({});
    const submitting = ref(false);
    const submitResult = ref(null);
    const showResultDialog = ref(false);
    const exerciseMode = ref(props.exerciseData.mode || 1);
    const isGuestMode = ref(!localStorage.getItem('token'));
    
    const currentQuestion = computed(() => questions.value[currentQuestionIndex.value]);
    const parsedOptions = computed(() => {
      try {
        if (currentQuestion.value.options) {
          return JSON.parse(currentQuestion.value.options);
        }
      } catch (e) {
        console.error('解析选项失败:', e);
      }
      return {};
    });

    const onAnswerChange = (answer) => {
      userAnswers.value[currentQuestionIndex.value] = answer;
    };

    const prevQuestion = () => {
      if (currentQuestionIndex.value > 0) {
        currentQuestionIndex.value--;
      }
    };

    const nextQuestion = () => {
      if (currentQuestionIndex.value < questions.value.length - 1) {
        currentQuestionIndex.value++;
      }
    };

    const handleSubmitError = (error) => {
      console.error('提交失败:', error);
      
      let errorMessage = '提交失败，请重试';
      if (error.response) {
        switch(error.response.status) {
          case 400:
            errorMessage = '请求数据格式错误，请检查后重试';
            break;
          case 401:
            errorMessage = '登录已过期，请重新登录';
            break;
          case 403:
            errorMessage = '没有权限提交答案';
            break;
          case 404:
            errorMessage = '服务器接口未找到';
            break;
          case 500:
            errorMessage = '服务器内部错误，请稍后再试';
            break;
          default:
            errorMessage = error.response.data?.message || errorMessage;
        }
      } else if (error.request) {
        errorMessage = '网络连接失败，请检查网络状态';
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      Toast.fail(errorMessage);
    };

    const submitAll = async () => {
      if (!validateAnswers()) {
        Toast.fail('请完成所有题目后再提交');
        return;
      }
      
      submitting.value = true;
      try {
        const requests = questions.value.map((question, index) => ({
          questionId: question.id,
          userAnswer: formatUserAnswer(userAnswers.value[index]),
          exerciseMode: exerciseMode.value,
          sessionId: localStorage.getItem('sessionId') || `session-${Date.now()}`,
          ...(question.type === 1 || question.type === 2 ? { questionType: question.type } : {}),
          timeCost: Math.max(5, Math.floor(45 * (0.5 + Math.random())))
        }));
        
        const response = await exerciseApi.batchSubmitAnswers(requests);
        
        if (response && response.code === 200 && response.data) {
          const results = Array.isArray(response.data) ? response.data : [response.data];
          
          submitResult.value = {
            totalCount: questions.value.length,
            correctCount: results.filter(r => r.isCorrect === true).length,
            correctRate: results.filter(r => r.isCorrect === true).length / questions.value.length,
            details: results
          };
          
          showResultDialog.value = true;
          
          questions.value.forEach((_, index) => {
            showAnalysis.value[index] = true;
          });
        } else {
          throw new Error(response?.message || 'Invalid response format');
        }
      } catch (error) {
        handleSubmitError(error);
      } finally {
        submitting.value = false;
      }
    };

    const formatUserAnswer = (answer) => {
      if (Array.isArray(answer)) {
        return answer.sort().join('');
      }
      return answer || '';
    };

    const validateAnswers = () => {
      if (questions.value.length === 0) {
        Toast('没有可提交的题目');
        return false;
      }
      
      return questions.value.every((question, index) => {
        const answer = userAnswers.value[index];
        
        if (answer === undefined || answer === null) {
          Toast(`第${index + 1}题尚未作答`);
          return false;
        }
        
        if (answer === '') {
          Toast(`第${index + 1}题答案不能为空`);
          return false;
        }
        
        if (Array.isArray(answer) && answer.length === 0) {
          Toast(`第${index + 1}题请选择至少一个选项`);
          return false;
        }
        
        return true;
      });
    };

    const goBack = () => {
      router.go(-1);
    };

    const restartExercise = () => {
      showResultDialog.value = false;
      submitResult.value = null;
      currentQuestionIndex.value = 0;
      userAnswers.value = {};
      showAnalysis.value = {};
    };

    const goToDashboard = () => {
      router.push('/exercise');
    };

    return {
      questions,
      currentQuestionIndex,
      userAnswers,
      showAnalysis,
      submitting,
      submitResult,
      showResultDialog,
      exerciseMode,
      isGuestMode,
      currentQuestion,
      parsedOptions,
      onAnswerChange,
      prevQuestion,
      nextQuestion,
      submitAll,
      handleSubmitError,
      goBack,
      restartExercise,
      goToDashboard
    };
  }
};
</script>

<style scoped>
.exercise-page {
  padding: 20px 0;
}

.option-item {
  padding: 10px 0;
}

.option-content {
  display: flex;
  align-items: center;
}

.question-options {
  margin: 15px 0;
}

.question-analysis {
  margin-top: 20px;
}

.analysis-content {
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 5px;
}

.analysis-content p {
  margin: 10px 0;
}

.navigation-buttons {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
  padding: 20px 0;
}

.result-content {
  text-align: center;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.guest-warning {
  color: #e6a23c;
  font-size: 14px;
  text-align: center;
  margin-bottom: 20px;
}

.result-summary {
  margin: 20px 0;
  font-size: 16px;
  line-height: 1.8;
}

.result-summary p {
  margin: 8px 0;
}

.result-actions {
  margin-top: 20px;
}
</style>