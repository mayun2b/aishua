<template>
  <div class="question-detail">
    <h1>{{ isEditMode ? '编辑题目' : '添加题目' }}</h1>
    
    <form @submit.prevent="saveQuestion">
      <!-- 基本信息 -->
      <div class="form-section">
        <h2>基本信息</h2>
        
        <div class="form-item">
          <label for="title">题目标题：</label>
          <input 
            type="text" 
            id="title" 
            v-model="formData.title" 
            required 
            placeholder="请输入题目标题"
          />
        </div>
        
        <div class="form-item">
          <label for="content">题目内容：</label>
          <textarea 
            id="content" 
            v-model="formData.content" 
            required 
            placeholder="请输入题目内容"
            rows="4"
          ></textarea>
        </div>
        
        <div class="form-item">
          <label for="type">题型：</label>
          <select id="type" v-model="formData.type" required @change="onTypeChange">
            <option value="">请选择题型</option>
            <option value="1">单选题</option>
            <option value="2">多选题</option>
            <option value="3">判断题</option>
            <option value="4">填空题</option>
            <option value="5">简答题</option>
          </select>
        </div>
        
        <div class="form-item">
          <label for="subjectId">学科：</label>
          <select id="subjectId" v-model="formData.subjectId" required>
            <option value="">请选择学科</option>
            <option v-for="subject in subjects" :key="subject.id" :value="subject.id">
              {{ subject.name }}
            </option>
          </select>
        </div>
        
        <div class="form-item">
          <label for="categoryId">分类：</label>
          <select id="categoryId" v-model="formData.categoryId" required>
            <option value="">请选择分类</option>
            <option v-for="category in categories" :key="category.id" :value="category.id">
              {{ category.name }}
            </option>
          </select>
        </div>
        
        <div class="form-item">
          <label for="difficulty">难度：</label>
          <select id="difficulty" v-model="formData.difficulty" required>
            <option value="">请选择难度</option>
            <option value="1">简单</option>
            <option value="2">中等</option>
            <option value="3">困难</option>
          </select>
        </div>
        
        <div class="form-item">
          <label for="tags">知识点标签：</label>
          <input 
            type="text" 
            id="tags" 
            v-model="formData.tags" 
            placeholder="请输入知识点标签，用逗号分隔"
          />
        </div>
      </div>
      
      <!-- 选项（选择题和判断题） -->
      <div v-if="[1, 2, 3].includes(Number(formData.type))" class="form-section">
        <h2>选项</h2>
        
        <div v-if="formData.type == 3" class="form-item">
          <!-- 判断题 -->
          <div class="option-item">
            <label>
              <input type="radio" v-model="formData.answer" value="正确" />
              正确
            </label>
            <label>
              <input type="radio" v-model="formData.answer" value="错误" />
              错误
            </label>
          </div>
        </div>
        
        <div v-else class="options-list">
          <!-- 单选题和多选题 -->
          <div 
            v-for="(option, index) in options" 
            :key="index" 
            class="option-item"
          >
            <input 
              type="text" 
              v-model="option.text" 
              placeholder="选项内容"
              @input="updateOptions"
            />
            <button 
              type="button" 
              class="btn-remove" 
              @click="removeOption(index)"
              :disabled="options.length <= 2"
            >
              删除
            </button>
          </div>
          <button 
            type="button" 
            class="btn-add" 
            @click="addOption"
            :disabled="options.length >= 6"
          >
            添加选项
          </button>
        </div>
      </div>
      
      <!-- 答案和解析 -->
      <div class="form-section">
        <h2>答案和解析</h2>
        
        <div class="form-item" v-if="[4, 5].includes(Number(formData.type))">
          <label for="answer">正确答案：</label>
          <textarea 
            id="answer" 
            v-model="formData.answer" 
            required 
            placeholder="请输入正确答案"
            rows="2"
          ></textarea>
        </div>
        
        <div class="form-item" v-else-if="formData.type == 1">
          <!-- 单选题 -->
          <label>正确答案：</label>
          <div class="answer-options">
            <label v-for="(option, index) in options" :key="index">
              <input 
                type="radio" 
                name="answer"
                :value="String.fromCharCode(65 + index)"
                v-model="selectedAnswer"
                @change="updateAnswer"
              />
              {{ String.fromCharCode(65 + index) }}. {{ option.text }}
            </label>
          </div>
        </div>
        <div class="form-item" v-else-if="formData.type == 2">
          <!-- 多选题 -->
          <label>正确答案：</label>
          <div class="answer-options">
            <label v-for="(option, index) in options" :key="index">
              <input 
                type="checkbox" 
                name="answers"
                :value="String.fromCharCode(65 + index)"
                v-model="selectedAnswers"
                @change="updateAnswer"
              />
              {{ String.fromCharCode(65 + index) }}. {{ option.text }}
            </label>
          </div>
        </div>
        
        <div class="form-item">
          <label for="analysis">答案解析：</label>
          <textarea 
            id="analysis" 
            v-model="formData.analysis" 
            required 
            placeholder="请输入答案解析"
            rows="4"
          ></textarea>
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="form-actions">
        <button type="button" class="btn-cancel" @click="goBack">取消</button>
        <button type="submit" class="btn-submit">保存</button>
      </div>
    </form>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { questionApi } from '../api/exercise';

export default {
  name: 'QuestionDetailView',
  setup() {
    const router = useRouter();
    const route = useRoute();
    const questionId = route.params.id;
    const isEditMode = questionId !== 'new';
    
    const categories = ref([]);
    const subjects = ref([]);
    const formData = ref({
      title: '',
      content: '',
      type: '',
      subjectId: '',
      categoryId: '',
      difficulty: '',
      options: '',
      answer: '',
      analysis: '',
      tags: ''
    });
    
    const options = ref([
      { text: '' },
      { text: '' }
    ]);
    
    const selectedAnswers = ref([]);
    const selectedAnswer = ref('');
    
    // 加载分类列表
    const loadCategories = async () => {
      try {
        const response = await questionApi.getAllCategories();
        categories.value = response.data;
      } catch (error) {
        console.error('加载分类失败:', error);
      }
    };
    
    // 加载学科列表
    const loadSubjects = async () => {
      try {
        const response = await questionApi.getAllSubjects();
        subjects.value = response.data;
      } catch (error) {
        console.error('加载学科失败:', error);
      }
    };
    
    // 加载题目详情
    const loadQuestion = async () => {
      if (!isEditMode) return;
      
      try {
        const response = await questionApi.getQuestionById(questionId);
        const question = response.data;
        
        // 先设置除type以外的字段
        formData.value = {
          title: question.title,
          content: question.content,
          type: '', // 暂时设置为空，避免触发onTypeChange
          subjectId: question.subjectId ? String(question.subjectId) : '',
          categoryId: String(question.categoryId),
          difficulty: String(question.difficulty),
          options: question.options,
          answer: question.answer,
          analysis: question.analysis,
          tags: question.tags
        };
        
        // 解析选项
        if (question.options) {
          try {
            const parsedOptions = JSON.parse(question.options);
            // 转换格式：如果是key-value格式，转换为text格式
            if (parsedOptions.length > 0 && 'key' in parsedOptions[0] && 'value' in parsedOptions[0]) {
              options.value = parsedOptions.map(opt => ({ text: opt.value }));
            } else {
              options.value = parsedOptions;
            }
          } catch (e) {
            console.error('解析选项失败:', e);
          }
        }
        
        // 解析答案
        if (question.type == 1) {
          // 单选题
          selectedAnswer.value = question.answer;
        } else if (question.type == 2) {
          // 多选题
          selectedAnswers.value = question.answer.split('');
        }
        
        // 最后设置type字段，触发onTypeChange
        formData.value.type = String(question.type);
      } catch (error) {
        console.error('加载题目失败:', error);
      }
    };
    
    // 题型变化时重置选项
    const onTypeChange = () => {
      // 只有当用户主动改变题型时才重置选项和答案
      // 避免在加载题目时重置已经解析的选项和答案
      if (!isEditMode) {
        if ([1, 2].includes(Number(formData.value.type))) {
          options.value = [
            { text: '' },
            { text: '' }
          ];
          if (formData.value.type == 1) {
            // 单选题
            selectedAnswer.value = '';
          } else {
            // 多选题
            selectedAnswers.value = [];
          }
          formData.value.answer = '';
        } else if (formData.value.type == 3) {
          formData.value.answer = '';
        }
      }
    };
    
    // 添加选项
    const addOption = () => {
      options.value.push({ text: '' });
    };
    
    // 删除选项
    const removeOption = (index) => {
      options.value.splice(index, 1);
      updateOptions();
    };
    
    // 更新选项
    const updateOptions = () => {
      // 转换为后端期望的格式：[{"key": "A", "value": "选项内容"}, ...]
      const formattedOptions = options.value.map((option, index) => ({
        key: String.fromCharCode(65 + index),
        value: option.text
      }));
      formData.value.options = JSON.stringify(formattedOptions);
    };
    
    // 更新答案
    const updateAnswer = () => {
      if (formData.value.type == 1) {
        // 单选题
        formData.value.answer = selectedAnswer.value;
      } else if (formData.value.type == 2) {
        // 多选题
        if (Array.isArray(selectedAnswers.value)) {
          formData.value.answer = selectedAnswers.value.sort().join('');
        } else {
          formData.value.answer = '';
        }
      }
    };
    
    // 保存题目
    const saveQuestion = async () => {
      try {
        // 确保选项格式正确
        if ([1, 2].includes(Number(formData.value.type))) {
          updateOptions();
        }
        
        // 验证必填字段
        if (!formData.value.title.trim()) {
          alert('题目标题不能为空');
          return;
        }
        if (!formData.value.type) {
          alert('题型不能为空');
          return;
        }
        if (!formData.value.answer.trim()) {
          alert('答案不能为空');
          return;
        }
        
        // 转换字段类型
        const submitData = {
          ...formData.value,
          type: formData.value.type ? Number(formData.value.type) : null,
          subjectId: formData.value.subjectId ? Number(formData.value.subjectId) : null,
          categoryId: formData.value.categoryId ? Number(formData.value.categoryId) : null,
          difficulty: formData.value.difficulty ? Number(formData.value.difficulty) : null,
          title: formData.value.title.trim(),
          answer: formData.value.answer.trim()
        };
        
        console.log('提交数据:', submitData);
        
        if (isEditMode) {
          await questionApi.updateQuestion(questionId, submitData);
        } else {
          await questionApi.createQuestion(submitData);
        }
        router.push('/exercise/questions');
      } catch (error) {
        console.error('保存题目失败:', error);
        console.error('错误详情:', error.response?.data);
        console.error('错误状态:', error.response?.status);
        console.error('错误头信息:', error.response?.headers);
      }
    };
    
    // 返回列表
    const goBack = () => {
      router.push('/exercise/questions');
    };
    
    onMounted(() => {
      loadCategories();
      loadSubjects();
      loadQuestion();
    });
    
    watch(() => formData.value.type, onTypeChange);
    
    return {
      isEditMode,
      categories,
      subjects,
      formData,
      options,
      selectedAnswers,
      selectedAnswer,
      onTypeChange,
      addOption,
      removeOption,
      updateOptions,
      updateAnswer,
      saveQuestion,
      goBack
    };
  }
};
</script>

<style scoped>
.question-detail {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

h1 {
  margin-bottom: 20px;
  color: #333;
}

h2 {
  margin: 20px 0 15px;
  color: #606266;
  font-size: 16px;
  font-weight: 600;
}

.form-section {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.form-item {
  margin-bottom: 15px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.form-item input,
.form-item textarea,
.form-item select {
  width: 100%;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-item textarea {
  resize: vertical;
}

.options-list {
  margin-top: 10px;
}

.option-item {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  align-items: center;
}

.option-item input {
  flex: 1;
}

.btn-remove {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  white-space: nowrap;
}

.btn-remove:disabled {
  background-color: #fbc4c4;
  cursor: not-allowed;
}

.btn-add {
  background-color: #67c23a;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  margin-top: 10px;
}

.btn-add:disabled {
  background-color: #b3e19d;
  cursor: not-allowed;
}

.answer-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.answer-options label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: normal;
  cursor: pointer;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 30px;
}

.btn-cancel {
  background-color: #ffffff;
  color: #606266;
  border: 1px solid #dcdfe6;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-submit {
  background-color: #409EFF;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-submit:hover {
  background-color: #66b1ff;
}
</style>