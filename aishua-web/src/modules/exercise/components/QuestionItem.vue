<template>
  <div class="question-item">
    <div class="question-header">
      <span class="question-number">第 {{ questionIndex + 1 }} 题</span>
      <span class="question-difficulty" :class="getDifficultyClass(question.difficulty)">
        {{ getDifficultyText(question.difficulty) }}
      </span>
    </div>
    
    <div class="question-content">
      <h3>{{ question.title }}</h3>
      <div v-if="question.content" class="question-description" v-html="question.content"></div>
    </div>
    
    <div class="question-options" v-if="question.type === 1 || question.type === 2">
      <el-radio-group 
        v-if="question.type === 1" 
        :model-value="selectedAnswer"
        @update:model-value="onAnswerChange"
      >
        <el-radio 
          v-for="(option, index) in parsedOptions" 
          :key="index"
          :label="String.fromCharCode(65 + index)"
          class="option-item"
        >
          <span class="option-letter">{{ String.fromCharCode(65 + index) }}.</span>
          <span class="option-content">{{ option }}</span>
        </el-radio>
      </el-radio-group>
      
      <el-checkbox-group 
        v-if="question.type === 2" 
        :model-value="selectedAnswer"
        @update:model-value="onAnswerChange"
      >
        <el-checkbox 
          v-for="(option, index) in parsedOptions" 
          :key="index"
          :label="String.fromCharCode(65 + index)"
          class="option-item"
        >
          <span class="option-letter">{{ String.fromCharCode(65 + index) }}.</span>
          <span class="option-content">{{ option }}</span>
        </el-checkbox>
      </el-checkbox-group>
    </div>
    
    <!-- 其他类型的题目保持不变 -->
    <div class="question-input" v-else-if="question.type === 3">
      <el-radio-group 
        :model-value="selectedAnswer" 
        @update:model-value="onAnswerChange"
      >
        <el-radio :label="'A'" class="option-item">正确</el-radio>
        <el-radio :label="'B'" class="option-item">错误</el-radio>
      </el-radio-group>
    </div>
    
    <div class="question-input" v-else-if="question.type === 4 || question.type === 5">
      <el-input 
        :model-value="selectedAnswer" 
        @update:model-value="onAnswerChange"
        type="textarea" 
        :placeholder="question.type === 4 ? '请输入答案' : '请简要回答'"
        :rows="question.type === 4 ? 1 : 3"
      />
    </div>
    
    <!-- 答案解析 -->
    <div v-if="showAnalysis" class="answer-analysis">
      <el-divider>答案解析</el-divider>
      <div v-html="question.analysis"></div>
      <div class="correct-answer">
        <strong>正确答案：</strong>
        <span>{{ getCorrectAnswerText() }}</span>
      </div>
    </div>
    
    <div class="question-actions">
      <el-button 
        v-if="!showAnalysis" 
        @click="toggleAnalysis"
        size="small"
      >
        查看解析
      </el-button>
      <el-button 
        v-else 
        @click="toggleAnalysis"
        size="small"
      >
        隐藏解析
      </el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'QuestionItem',
  props: {
    question: {
      type: Object,
      required: true
    },
    questionIndex: {
      type: Number,
      required: true
    },
    modelValue: {
      type: [String, Array],
      default: null
    },
    showAnalysis: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:modelValue', 'update:showAnalysis'],
  data() {
    return {
      selectedAnswer: this.modelValue || (this.question.type === 2 ? [] : '')
    };
  },
  computed: {
    parsedOptions() {
      try {
        if (this.question.options) {
          // 后端存储的是JSON字符串，需要解析
          const optionsObj = JSON.parse(this.question.options);
          // 转换为数组格式，按A、B、C、D顺序
          const keys = Object.keys(optionsObj).sort();
          return keys.map(key => {
            // 只返回value字段的值，而不是整个对象
            return optionsObj[key].value;
          });
        }
      } catch (e) {
        console.error('解析选项失败:', e);
        // 如果解析失败，尝试直接作为数组处理
        if (Array.isArray(this.question.options)) {
          return this.question.options;
        }
        // 如果还是不行，返回空数组
        return [];
      }
      return [];
    }
  },
  watch: {
    modelValue(newVal) {
      this.selectedAnswer = newVal || (this.question.type === 2 ? [] : '');
    }
  },
  methods: {
    onAnswerChange(value) {
      this.selectedAnswer = value;
      this.$emit('update:modelValue', value);
    },
    toggleAnalysis() {
      this.$emit('update:show-analysis', !this.showAnalysis);
    },
    getDifficultyClass(difficulty) {
      if (difficulty === 1) return 'easy';
      if (difficulty === 2) return 'medium';
      if (difficulty === 3) return 'hard';
      return '';
    },
    getDifficultyText(difficulty) {
      if (difficulty === 1) return '简单';
      if (difficulty === 2) return '中等';
      if (difficulty === 3) return '困难';
      return '未知';
    },
    getCorrectAnswerText() {
      const correctAnswer = this.question.answer;
      if (this.question.type === 1 || this.question.type === 2) {
        // 选择题
        try {
          const options = this.parsedOptions;
          if (!options || options.length === 0) {
            return correctAnswer;
          }
          
          // 处理多选题答案（如 "ABC"）
          if (typeof correctAnswer === 'string' && correctAnswer.length > 1) {
            return correctAnswer.split('').map(ans => {
              const optionIndex = ans.charCodeAt(0) - 65;
              if (optionIndex >= 0 && optionIndex < options.length) {
                return `${ans}.${options[optionIndex]}`;
              }
              return ans;
            }).join(', ');
          } else {
            // 单选题
            const optionIndex = correctAnswer.charCodeAt(0) - 65;
            if (optionIndex >= 0 && optionIndex < options.length) {
              return `${correctAnswer}.${options[optionIndex]}`;
            }
            return correctAnswer;
          }
        } catch (e) {
          return correctAnswer;
        }
      } else if (this.question.type === 3) {
        // 判断题
        return correctAnswer === 'A' ? '正确' : '错误';
      }
      return correctAnswer;
    }
  }
};
</script>

<style scoped>
.question-item {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.question-number {
  font-weight: bold;
  color: #333;
}

.question-difficulty {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.question-difficulty.easy {
  background-color: #e6f7ff;
  color: #1890ff;
}

.question-difficulty.medium {
  background-color: #fff7e6;
  color: #fa8c16;
}

.question-difficulty.hard {
  background-color: #fff1f0;
  color: #ff4d4f;
}

.question-content h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  line-height: 1.5;
}

.question-description {
  margin-bottom: 15px;
  color: #666;
  line-height: 1.6;
}

.option-item {
  display: block;
  margin: 8px 0;
  padding: 10px;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  transition: all 0.2s;
}

.option-item:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.option-letter {
  font-weight: bold;
  margin-right: 8px;
  color: #333;
}

.option-content {
  color: #666;
}

.question-input {
  margin: 15px 0;
}

.answer-analysis {
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.answer-analysis div {
  margin-bottom: 10px;
  line-height: 1.6;
}

.correct-answer {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #e8e8e8;
}

.question-actions {
  margin-top: 15px;
  text-align: right;
}
</style>