<template>
  <div class="ai-questions-container">
    <div class="page-header">
      <h2>AI智能题目生成</h2>
      <p>基于您的错题自动分析并生成相关知识点的题目</p>
    </div>

    <div class="form-section">
      <h3>错题分析</h3>
      <p class="section-description">点击下方按钮，系统将自动分析您的错题并生成个性化的学习建议</p>
      
      <button 
        class="btn-primary" 
        :disabled="loading" 
        @click="analyzeUserWrongQuestions"
      >
        <span v-if="loading">分析中...</span>
        <span v-else>分析我的错题</span>
      </button>
    </div>

    <div v-if="analysisResult" class="analysis-section">
      <h3>错题分析结果</h3>
      <div class="analysis-card">
        <div class="analysis-item">
          <h4>总体情况</h4>
          <p>{{ analysisResult.summary }}</p>
        </div>
        
        <div class="analysis-item">
          <h4>薄弱知识点</h4>
          <ul class="weak-points">
            <li v-for="(point, index) in analysisResult.weakPoints" :key="index">
              {{ point }}
            </li>
          </ul>
        </div>
        
        <div class="analysis-item">
          <h4>学习建议</h4>
          <p>{{ analysisResult.suggestions }}</p>
        </div>
      </div>
      
      <div class="action-buttons">
        <button 
          class="btn-primary" 
          :disabled="loading" 
          @click="generateQuestionsBasedOnAnalysis"
        >
          <span v-if="loading">生成中...</span>
          <span v-else>基于分析生成题目</span>
        </button>
      </div>
    </div>

    <div v-if="generatedQuestions" class="results-section">
      <h3>生成的题目</h3>
      <div class="questions-list">
        <div v-for="(question, index) in generatedQuestions" :key="index" class="question-card">
          <div class="question-header">
            <h4>题目 {{ index + 1 }}</h4>
          </div>
          <div class="question-content">
            <p class="question-text">{{ question.title }}</p>
            <div class="options">
              <div v-for="(option, optionIndex) in question.options" :key="optionIndex" class="option">
                <input 
                  type="radio" 
                  :id="`q${index}-option${optionIndex}`" 
                  :name="`question${index}`"
                  :value="option.key"
                  v-model="userAnswers[index]"
                />
                <label :for="`q${index}-option${optionIndex}`">
                  {{ option.key }}. {{ option.value }}
                </label>
              </div>
            </div>
            <div class="answer-section" v-if="showAnswers">
              <div class="correct-answer">
                <strong>正确答案：</strong>{{ question.correctAnswer }}
              </div>
              <div class="explanation">
                <strong>解析：</strong>{{ question.explanation }}
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="action-buttons">
        <button class="btn-outline" @click="checkAnswers">查看答案</button>
        <button class="btn-outline" @click="resetForm">重新分析</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { analyzeWrongQuestions, generateQuestions } from '../api/exercise'

const loading = ref(false)
const analysisResult = ref(null)
const generatedQuestions = ref(null)
const userAnswers = ref([])
const showAnswers = ref(false)

const analyzeUserWrongQuestions = async () => {
  loading.value = true
  try {
    const response = await analyzeWrongQuestions()
    
    if (response.code === 200) {
      analysisResult.value = response.data
      generatedQuestions.value = null
      userAnswers.value = []
      showAnswers.value = false
    } else {
      alert('分析错题失败：' + response.message)
    }
  } catch (error) {
    console.error('分析错题失败:', error)
    alert('分析错题失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const generateQuestionsBasedOnAnalysis = async () => {
  if (!analysisResult.value) {
    alert('请先分析错题')
    return
  }
  
  loading.value = true
  try {
    const response = await generateQuestions({
      wrongQuestion: '基于错题分析',
      answer: '自动生成',
      knowledgePoints: analysisResult.value.weakPoints.join(','),
      questionCount: 3
    })
    
    if (response.code === 200) {
      const rawContent = response.data.rawContent
      generatedQuestions.value = parseQuestions(rawContent)
      userAnswers.value = Array(generatedQuestions.value.length).fill('')
      showAnswers.value = false
    } else {
      alert('生成题目失败：' + response.message)
    }
  } catch (error) {
    console.error('生成题目失败:', error)
    alert('生成题目失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const parseQuestions = (rawContent) => {
  const questions = []
  const questionBlocks = rawContent.split('题目')
  
  for (let i = 1; i < questionBlocks.length; i++) {
    const block = questionBlocks[i]
    const lines = block.split('\n')
    
    const question = {
      title: '',
      options: [],
      correctAnswer: '',
      explanation: ''
    }
    
    for (const line of lines) {
      const trimmedLine = line.trim()
      if (trimmedLine === '') continue
      
      if (trimmedLine.startsWith('：')) {
        question.title = trimmedLine.substring(1).trim()
      } else if (trimmedLine.startsWith('选项：')) {
        const optionsStr = trimmedLine.substring(3).trim()
        const optionParts = optionsStr.split(' ')
        for (const part of optionParts) {
          if (part.includes('.')) {
            const [key, ...valueParts] = part.split('.')
            question.options.push({
              key: key,
              value: valueParts.join('.').trim()
            })
          }
        }
      } else if (trimmedLine.startsWith('答案：')) {
        question.correctAnswer = trimmedLine.substring(3).trim()
      } else if (trimmedLine.startsWith('解析：')) {
        question.explanation = trimmedLine.substring(3).trim()
      }
    }
    
    if (question.title) {
      questions.push(question)
    }
  }
  
  return questions
}

const checkAnswers = () => {
  showAnswers.value = true
}

const resetForm = () => {
  analysisResult.value = null
  generatedQuestions.value = null
  userAnswers.value = []
  showAnswers.value = false
}
</script>

<style scoped>
.ai-questions-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.page-header {
  text-align: center;
  margin-bottom: 2rem;
}

.page-header h2 {
  font-size: 1.75rem;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.page-header p {
  color: var(--text-secondary);
  font-size: 1rem;
}

.form-section {
  background-color: var(--card-background);
  padding: 2rem;
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-sm);
  margin-bottom: 2rem;
}

.form-section h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1.5rem;
}

.section-description {
  color: var(--text-secondary);
  margin-bottom: 2rem;
  line-height: 1.6;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: var(--text-primary);
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  font-size: 1rem;
  transition: var(--transition);
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-group textarea {
  resize: vertical;
  min-height: 100px;
}

.btn-primary {
  background: var(--gradient-primary);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: var(--border-radius);
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
  width: 100%;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.analysis-section {
  background-color: var(--card-background);
  padding: 2rem;
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-sm);
  margin-bottom: 2rem;
}

.analysis-section h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1.5rem;
}

.analysis-card {
  background-color: white;
  padding: 1.5rem;
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-sm);
  margin-bottom: 2rem;
  border: 1px solid var(--border-color);
}

.analysis-item {
  margin-bottom: 1.5rem;
}

.analysis-item h4 {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.75rem;
}

.analysis-item p {
  color: var(--text-secondary);
  line-height: 1.6;
}

.weak-points {
  list-style-type: disc;
  padding-left: 1.5rem;
  color: var(--text-secondary);
  line-height: 1.6;
}

.weak-points li {
  margin-bottom: 0.5rem;
}

.results-section {
  background-color: var(--card-background);
  padding: 2rem;
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-sm);
}

.results-section h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1.5rem;
}

.questions-list {
  margin-bottom: 2rem;
}

.question-card {
  background-color: white;
  padding: 1.5rem;
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-sm);
  margin-bottom: 1.5rem;
  border: 1px solid var(--border-color);
  transition: var(--transition);
}

.question-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--primary-light);
}

.question-header {
  margin-bottom: 1rem;
}

.question-header h4 {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
}

.question-text {
  margin-bottom: 1rem;
  line-height: 1.6;
  color: var(--text-primary);
}

.options {
  margin-bottom: 1rem;
}

.option {
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
}

.option input[type="radio"] {
  margin-right: 0.5rem;
}

.option label {
  cursor: pointer;
  color: var(--text-primary);
}

.answer-section {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-color);
}

.correct-answer {
  margin-bottom: 0.75rem;
  color: var(--success-color);
}

.explanation {
  color: var(--text-secondary);
  line-height: 1.6;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.btn-outline {
  background-color: white;
  color: var(--primary-color);
  border: 1px solid var(--primary-color);
  padding: 0.75rem 1.5rem;
  border-radius: var(--border-radius);
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
}

.btn-outline:hover {
  background-color: var(--primary-color);
  color: white;
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

@media (max-width: 768px) {
  .ai-questions-container {
    padding: 1rem;
  }
  
  .form-section,
  .analysis-section,
  .results-section {
    padding: 1.5rem;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons button {
    width: 100%;
  }
}
</style>