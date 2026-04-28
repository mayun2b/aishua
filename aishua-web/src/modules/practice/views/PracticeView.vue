<template>
  <div class="practice-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">Practice Center</p>
        <h1>整套作答后统一提交</h1>
        <p class="description">
          当前练习流程会在开始时一次性加载本次题目，用户可以自由切题、完成整套作答，最后统一提交并查看判分结果。
        </p>
      </div>

      <div class="hero-actions">
        <router-link class="ghost" to="/dashboard">返回工作台</router-link>
        <router-link class="ghost" to="/my-subjects">我的学科</router-link>
      </div>
    </section>

    <section class="config-card">
      <div class="section-head">
        <h2>练习配置</h2>
        <span v-if="session">进行中会话：#{{ session.sessionId }}</span>
      </div>

      <div v-if="loadingSubjects" class="empty-state">正在加载学科列表...</div>
      <div v-else-if="!subjects.length" class="empty-state">
        你还没有加入任何学科，请先前往
        <router-link to="/subjects">学科列表</router-link>
        选择学习方向。
      </div>
      <div v-else class="config-grid">
        <label>
          <span>学科</span>
          <select v-model="selectedSubjectId" :disabled="hasActiveSession">
            <option v-for="subject in subjects" :key="subject.subjectId" :value="String(subject.subjectId)">
              {{ subject.name }}（{{ subject.code }}）
            </option>
          </select>
        </label>

        <label>
          <span>模式</span>
          <select v-model.number="practiceMode" :disabled="hasActiveSession">
            <option :value="1">顺序练习</option>
            <option :value="2">随机练习</option>
          </select>
        </label>

        <label>
          <span>题量</span>
          <input v-model.number="questionCount" type="number" min="1" max="50" :disabled="hasActiveSession" />
        </label>
      </div>

      <div v-if="subjects.length" class="config-actions">
        <button type="button" :disabled="starting || !selectedSubjectId || hasActiveSession" @click="startPractice">
          {{ starting ? '正在开始...' : hasActiveSession ? '练习已开始' : '开始练习' }}
        </button>
      </div>
    </section>

    <section v-if="session" class="summary-card">
      <div class="summary-item">
        <span>学科</span>
        <strong>{{ session.subjectName }}</strong>
      </div>
      <div class="summary-item">
        <span>模式</span>
        <strong>{{ resolveModeLabel(session.practiceMode) }}</strong>
      </div>
      <div class="summary-item">
        <span>题目数量</span>
        <strong>{{ questions.length }}</strong>
      </div>
      <div class="summary-item">
        <span>作答进度</span>
        <strong>{{ progressText }}</strong>
      </div>
    </section>

    <section v-if="session" class="sheet-card">
      <div class="section-head">
        <h2>答题导航</h2>
        <span>{{ submitResult ? '已提交，可查看结果' : `未作答 ${unansweredCount} 题` }}</span>
      </div>

      <div v-if="loadingSheet" class="empty-state">正在加载本次题目...</div>
      <div v-else-if="!questions.length" class="empty-state">当前练习没有可作答题目。</div>
      <div v-else class="sheet-body">
        <div class="question-nav">
          <button
            v-for="(question, index) in questions"
            :key="question.questionId"
            type="button"
            :class="[
              'nav-chip',
              index === currentIndex ? 'active' : '',
              isAnswered(question.questionId) ? 'answered' : '',
              getQuestionResult(question.questionId)?.isCorrect === 1 ? 'correct' : '',
              getQuestionResult(question.questionId)?.isCorrect === 0 ? 'wrong' : ''
            ]"
            @click="goToQuestion(index)"
          >
            {{ index + 1 }}
          </button>
        </div>

        <div v-if="currentQuestion" class="question-panel">
          <div class="question-head">
            <div class="question-meta">
              <span class="chip">{{ resolveTypeLabel(currentQuestion.type) }}</span>
              <span class="chip ghost-chip">难度 {{ currentQuestion.difficulty ?? 1 }}</span>
              <span class="chip ghost-chip">第 {{ currentIndex + 1 }} / {{ questions.length }} 题</span>
            </div>
            <span class="answer-state">
              {{ isAnswered(currentQuestion.questionId) ? '已作答' : '未作答' }}
            </span>
          </div>

          <h3>{{ currentQuestion.title }}</h3>
          <p v-if="currentQuestion.content" class="question-content">{{ currentQuestion.content }}</p>

          <div v-if="currentQuestion.type === 1 || currentQuestion.type === 2" class="options-list">
            <label v-for="option in parsedOptions" :key="option.optionKey" class="option-item">
              <input
                v-if="currentQuestion.type === 2"
                v-model="multiChoiceAnswer"
                type="checkbox"
                :value="option.optionValue"
                :disabled="isSubmitted"
              />
              <input
                v-else
                v-model="singleChoiceAnswer"
                type="radio"
                :value="option.optionValue"
                :disabled="isSubmitted"
              />
              <span class="option-marker">{{ option.optionLabel }}</span>
              <span class="option-text">{{ option.optionText }}</span>
            </label>
          </div>

          <div v-else-if="currentQuestion.type === 3" class="options-list">
            <label class="option-item">
              <input v-model="singleChoiceAnswer" type="radio" value="正确" :disabled="isSubmitted" />
              <span>正确</span>
            </label>
            <label class="option-item">
              <input v-model="singleChoiceAnswer" type="radio" value="错误" :disabled="isSubmitted" />
              <span>错误</span>
            </label>
          </div>

          <div v-else class="answer-block">
            <textarea
              v-model.trim="textAnswer"
              rows="5"
              maxlength="2000"
              :disabled="isSubmitted"
              :placeholder="currentQuestion.type === 4 ? '请输入填空答案' : '请输入你的作答内容'"
            ></textarea>
          </div>

          <div class="question-actions">
            <button type="button" class="secondary-button" :disabled="currentIndex === 0" @click="goToPreviousQuestion">
              上一题
            </button>
            <button
              type="button"
              class="secondary-button"
              :disabled="currentIndex >= questions.length - 1"
              @click="goToNextQuestion"
            >
              下一题
            </button>
            <button type="button" :disabled="submitting || !canSubmitAll" @click="submitAllAnswers">
              {{ submitting ? '正在提交...' : '统一提交' }}
            </button>
          </div>

          <div v-if="currentResult" class="analysis-card">
            <div class="section-head compact">
              <h2>当前题判分</h2>
              <span :class="['result-badge', currentResult.isCorrect === 1 ? 'success' : 'danger']">
                {{ currentResult.isCorrect === 1 ? '回答正确' : '回答错误' }}
              </span>
            </div>

            <div class="result-grid">
              <div class="result-item">
                <span>你的答案</span>
                <strong>{{ formatAnswerDisplay(currentResult.userAnswer) }}</strong>
              </div>
              <div class="result-item">
                <span>标准答案</span>
                <strong>{{ formatAnswerDisplay(currentResult.correctAnswer) }}</strong>
              </div>
              <div class="result-item">
                <span>本题耗时</span>
                <strong>{{ currentResult.timeCost }} 秒</strong>
              </div>
            </div>

            <p v-if="currentResult.analysis" class="analysis-text">{{ currentResult.analysis }}</p>
          </div>
        </div>
      </div>
    </section>

    <section v-if="submitResult" class="result-card">
      <div class="section-head">
        <h2>提交结果</h2>
        <span class="result-badge success">已完成</span>
      </div>

      <div class="result-grid">
        <div class="result-item">
          <span>总题数</span>
          <strong>{{ submitResult.questionCount }}</strong>
        </div>
        <div class="result-item">
          <span>答对</span>
          <strong>{{ submitResult.correctCount }}</strong>
        </div>
        <div class="result-item">
          <span>答错</span>
          <strong>{{ submitResult.wrongCount }}</strong>
        </div>
        <div class="result-item">
          <span>正确率</span>
          <strong>{{ submitResult.correctRate }}%</strong>
        </div>
      </div>

      <div class="config-actions">
        <button type="button" @click="resetPractice">重新开始</button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import practiceApi from '../api/practice'
import subjectApi from '../../subject/api/subject'

const route = useRoute()

const loadingSubjects = ref(false)
const starting = ref(false)
const loadingSheet = ref(false)
const submitting = ref(false)

const subjects = ref([])
const selectedSubjectId = ref('')
const practiceMode = ref(1)
const questionCount = ref(10)

const session = ref(null)
const questionSheet = ref(null)
const submitResult = ref(null)
const currentIndex = ref(0)
const currentQuestionEnteredAt = ref(0)

const answers = ref({})
const timeSpentMs = ref({})

const hasActiveSession = computed(() => Boolean(session.value))
const questions = computed(() => questionSheet.value?.questions || [])
const currentQuestion = computed(() => questions.value[currentIndex.value] || null)
const isSubmitted = computed(() => Boolean(submitResult.value))

const answeredCount = computed(() => questions.value.filter((question) => isAnswered(question.questionId)).length)
const unansweredCount = computed(() => Math.max(questions.value.length - answeredCount.value, 0))
const canSubmitAll = computed(() => hasActiveSession.value && questions.value.length > 0 && unansweredCount.value === 0 && !isSubmitted.value)
const progressText = computed(() => `${answeredCount.value} / ${questions.value.length}`)

const resultMap = computed(() => {
  const entries = (submitResult.value?.results || []).map((item) => [item.questionId, item])
  return Object.fromEntries(entries)
})

const currentResult = computed(() => {
  if (!currentQuestion.value) {
    return null
  }
  return resultMap.value[currentQuestion.value.questionId] || null
})

const parsedOptions = computed(() => {
  if (!currentQuestion.value?.options) {
    return []
  }

  try {
    const parsed = JSON.parse(currentQuestion.value.options)
    if (!Array.isArray(parsed)) {
      return []
    }
    return parsed.map((item, index) => {
      const label = resolveOptionLabel(item, index)
      const value = resolveOptionValue(item, label)
      const text = resolveOptionText(item, label, value)
      return {
        optionKey: `${label}-${value}-${text}-${index}`,
        optionValue: value,
        optionLabel: label,
        optionText: text
      }
    })
  } catch (error) {
    return []
  }
})

const normalizeOptionField = (value) => {
  if (value == null) {
    return ''
  }
  return String(value).trim()
}

const isSameOptionText = (left, right) => {
  return normalizeOptionField(left).toUpperCase() === normalizeOptionField(right).toUpperCase()
}

const resolveOptionLabel = (item, index) => {
  const explicitLabel = normalizeOptionField(item?.label)
  if (explicitLabel) {
    return explicitLabel
  }
  return String.fromCharCode(65 + index)
}

const resolveOptionValue = (item, fallbackLabel) => {
  const candidates = [
    item?.value,
    item?.key,
    item?.optionValue,
    typeof item === 'string' ? item : null
  ]
  for (const candidate of candidates) {
    const normalized = normalizeOptionField(candidate)
    if (normalized) {
      return normalized
    }
  }
  return fallbackLabel
}

const resolveOptionText = (item, label, value) => {
  const textCandidates = [
    item?.text,
    item?.content,
    item?.title,
    item?.desc,
    item?.description
  ]
  for (const candidate of textCandidates) {
    const normalized = normalizeOptionField(candidate)
    if (normalized && !isSameOptionText(normalized, label)) {
      return normalized
    }
  }

  if (!isSameOptionText(value, label)) {
    return value
  }
  return label
}

const singleChoiceAnswer = computed({
  get() {
    if (!currentQuestion.value) {
      return ''
    }
    const answer = answers.value[currentQuestion.value.questionId]
    return typeof answer === 'string' ? answer : ''
  },
  set(value) {
    if (!currentQuestion.value || isSubmitted.value) {
      return
    }
    answers.value = {
      ...answers.value,
      [currentQuestion.value.questionId]: String(value)
    }
  }
})

const multiChoiceAnswer = computed({
  get() {
    if (!currentQuestion.value) {
      return []
    }
    const answer = answers.value[currentQuestion.value.questionId]
    return Array.isArray(answer) ? answer : []
  },
  set(value) {
    if (!currentQuestion.value || isSubmitted.value) {
      return
    }
    answers.value = {
      ...answers.value,
      [currentQuestion.value.questionId]: Array.isArray(value) ? [...value] : []
    }
  }
})

const textAnswer = computed({
  get() {
    if (!currentQuestion.value) {
      return ''
    }
    const answer = answers.value[currentQuestion.value.questionId]
    return typeof answer === 'string' ? answer : ''
  },
  set(value) {
    if (!currentQuestion.value || isSubmitted.value) {
      return
    }
    answers.value = {
      ...answers.value,
      [currentQuestion.value.questionId]: value == null ? '' : String(value)
    }
  }
})

const resolveTypeLabel = (type) => {
  switch (type) {
    case 1:
      return '单选题'
    case 2:
      return '多选题'
    case 3:
      return '判断题'
    case 4:
      return '填空题'
    case 5:
      return '简答题'
    default:
      return `题型 ${type}`
  }
}

const resolveModeLabel = (mode) => {
  return mode === 2 ? '随机练习' : '顺序练习'
}

const formatAnswerDisplay = (value) => {
  if (value == null || String(value).trim() === '') {
    return '未作答'
  }
  return String(value)
}

const isAnswered = (questionId) => {
  const answer = answers.value[questionId]
  if (Array.isArray(answer)) {
    return answer.length > 0
  }
  return typeof answer === 'string' ? answer.trim().length > 0 : false
}

const getQuestionResult = (questionId) => {
  return resultMap.value[questionId] || null
}

const initializeSessionState = () => {
  questionSheet.value = null
  submitResult.value = null
  currentIndex.value = 0
  currentQuestionEnteredAt.value = 0
  answers.value = {}
  timeSpentMs.value = {}
}

const loadSubjects = async () => {
  loadingSubjects.value = true
  try {
    const response = await subjectApi.listMySubjects()
    subjects.value = response.data || []

    const routeSubjectId = route.query.subjectId ? String(route.query.subjectId) : ''
    if (routeSubjectId && subjects.value.some((subject) => String(subject.subjectId) === routeSubjectId)) {
      selectedSubjectId.value = routeSubjectId
    } else if (subjects.value[0]) {
      selectedSubjectId.value = String(subjects.value[0].subjectId)
    }
  } catch (error) {
    showToast(error.message || '加载学科失败')
  } finally {
    loadingSubjects.value = false
  }
}

const beginQuestionTimer = () => {
  if (!currentQuestion.value || isSubmitted.value) {
    currentQuestionEnteredAt.value = 0
    return
  }
  currentQuestionEnteredAt.value = Date.now()
}

const syncCurrentQuestionTime = () => {
  if (!currentQuestion.value || !currentQuestionEnteredAt.value || isSubmitted.value) {
    return
  }

  const elapsedMs = Math.max(Date.now() - currentQuestionEnteredAt.value, 0)
  if (elapsedMs > 0) {
    const questionId = currentQuestion.value.questionId
    timeSpentMs.value = {
      ...timeSpentMs.value,
      [questionId]: (timeSpentMs.value[questionId] || 0) + elapsedMs
    }
  }
  currentQuestionEnteredAt.value = Date.now()
}

const loadQuestionSheet = async () => {
  if (!session.value?.sessionId) {
    return
  }

  loadingSheet.value = true
  try {
    const response = await practiceApi.getQuestions(session.value.sessionId)
    questionSheet.value = response.data
    currentIndex.value = 0
    currentQuestionEnteredAt.value = 0
    beginQuestionTimer()
  } catch (error) {
    showToast(error.message || '加载题目失败')
  } finally {
    loadingSheet.value = false
  }
}

const startPractice = async () => {
  if (!selectedSubjectId.value) {
    showToast('请先选择学科')
    return
  }

  starting.value = true
  try {
    initializeSessionState()
    const response = await practiceApi.start({
      subjectId: Number(selectedSubjectId.value),
      practiceMode: Number(practiceMode.value),
      questionCount: Number(questionCount.value)
    })
    session.value = response.data
    await loadQuestionSheet()
  } catch (error) {
    showToast(error.message || '开始练习失败')
  } finally {
    starting.value = false
  }
}

const goToQuestion = (index) => {
  if (index < 0 || index >= questions.value.length || index === currentIndex.value) {
    return
  }
  syncCurrentQuestionTime()
  currentIndex.value = index
  beginQuestionTimer()
}

const goToPreviousQuestion = () => {
  goToQuestion(currentIndex.value - 1)
}

const goToNextQuestion = () => {
  goToQuestion(currentIndex.value + 1)
}

const buildUserAnswer = (question) => {
  const answer = answers.value[question.questionId]
  if (Array.isArray(answer)) {
    return JSON.stringify(answer)
  }
  if (typeof answer === 'string') {
    return answer.trim()
  }
  return ''
}

const buildTimeCost = (questionId) => {
  const ms = timeSpentMs.value[questionId] || 0
  return Math.max(Math.round(ms / 1000), 0)
}

const submitAllAnswers = async () => {
  if (!session.value?.sessionId || !canSubmitAll.value) {
    if (unansweredCount.value > 0) {
      showToast('还有未作答题目，完成后才能提交')
    }
    return
  }

  syncCurrentQuestionTime()
  submitting.value = true
  try {
    const payload = {
      answers: questions.value.map((question) => ({
        questionId: question.questionId,
        userAnswer: buildUserAnswer(question),
        timeCost: buildTimeCost(question.questionId)
      }))
    }
    const response = await practiceApi.submitAll(session.value.sessionId, payload)
    submitResult.value = response.data
    session.value = {
      ...session.value,
      answeredCount: response.data.answeredCount,
      correctCount: response.data.correctCount,
      wrongCount: response.data.wrongCount,
      status: response.data.finished ? 2 : 1
    }
    currentQuestionEnteredAt.value = 0
    showToast('提交成功')
  } catch (error) {
    showToast(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const resetPractice = () => {
  syncCurrentQuestionTime()
  session.value = null
  initializeSessionState()
}

watch(
  () => route.query.subjectId,
  (subjectId) => {
    if (!session.value && subjectId) {
      selectedSubjectId.value = String(subjectId)
    }
  }
)

onMounted(() => {
  loadSubjects()
})

onBeforeUnmount(() => {
  syncCurrentQuestionTime()
})
</script>

<style scoped>
.practice-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background:
    radial-gradient(circle at top left, rgba(244, 208, 111, 0.18), transparent 28%),
    radial-gradient(circle at top right, rgba(31, 78, 121, 0.14), transparent 32%),
    linear-gradient(135deg, #f4efe4 0%, #e3edf5 100%);
}

.hero-card,
.config-card,
.summary-card,
.sheet-card,
.result-card {
  max-width: 1180px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 28px;
  box-shadow: 0 24px 60px rgba(38, 57, 77, 0.12);
}

.hero-card,
.config-card,
.sheet-card,
.result-card {
  padding: 28px;
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}

.eyebrow {
  margin: 0;
  color: #7a8793;
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

.hero-card h1 {
  margin: 12px 0 0;
  color: #17324d;
  font-size: 34px;
}

.description {
  margin: 14px 0 0;
  max-width: 760px;
  color: #5e6d7c;
  line-height: 1.7;
}

.hero-actions,
.config-actions,
.question-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.config-card,
.summary-card,
.sheet-card,
.result-card {
  margin-top: 18px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.section-head.compact h2 {
  font-size: 20px;
}

.section-head h2 {
  margin: 0;
  color: #17324d;
  font-size: 24px;
}

.section-head span {
  color: #6c7a8d;
}

.config-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.config-grid label {
  display: grid;
  gap: 8px;
  color: #17324d;
  font-weight: 600;
}

.config-grid select,
.config-grid input,
.answer-block textarea {
  width: 100%;
  padding: 13px 14px;
  border: 1px solid #cad7e3;
  border-radius: 14px;
  font-size: 14px;
  box-sizing: border-box;
  resize: vertical;
  background: #fff;
}

.config-actions {
  margin-top: 18px;
}

.summary-card {
  padding: 20px 24px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
}

.summary-item,
.result-item {
  display: grid;
  gap: 6px;
}

.summary-item span,
.result-item span {
  color: #6c7a8d;
  font-size: 13px;
}

.summary-item strong,
.result-item strong {
  color: #17324d;
  font-size: 18px;
}

.empty-state {
  margin-top: 18px;
  padding: 34px;
  text-align: center;
  color: #6c7a8d;
  background: #f3f7fb;
  border-radius: 18px;
}

.sheet-body {
  margin-top: 18px;
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 20px;
}

.question-nav {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  align-content: start;
}

.nav-chip {
  min-width: 0;
  padding: 12px 0;
  border-radius: 16px;
  border: 1px solid #d7e1eb;
  background: #f7fafc;
  color: #17324d;
}

.nav-chip.answered {
  background: #ecf7f1;
  border-color: #bde3ce;
}

.nav-chip.active {
  background: #17324d;
  border-color: #17324d;
  color: #fff;
}

.nav-chip.correct {
  background: #dff3e7;
  border-color: #98d1af;
}

.nav-chip.wrong {
  background: #fce9e7;
  border-color: #efb9b3;
}

.question-panel {
  padding: 8px 0 0;
}

.question-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.question-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.chip,
.result-badge,
.answer-state {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
}

.chip {
  background: #ddf5e9;
  color: #0f7a43;
}

.ghost-chip {
  background: #e8edf6;
  color: #17324d;
}

.answer-state {
  background: #f3f7fb;
  color: #526173;
}

.question-panel h3 {
  margin: 18px 0 0;
  color: #17324d;
  line-height: 1.6;
  font-size: 24px;
}

.question-content {
  margin: 14px 0 0;
  color: #5e6d7c;
  line-height: 1.7;
  white-space: pre-wrap;
}

.options-list {
  margin-top: 20px;
  display: grid;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid #d9e3ee;
  background: #f8fbff;
  color: #17324d;
  line-height: 1.6;
}

.option-marker {
  flex: 0 0 auto;
  min-width: 32px;
  font-weight: 700;
  color: #17324d;
}

.option-text {
  flex: 1;
  min-width: 0;
  color: #17324d;
  word-break: break-word;
}

.answer-block {
  margin-top: 20px;
}

.question-actions {
  margin-top: 22px;
}

.analysis-card {
  margin-top: 24px;
  padding: 20px;
  border-radius: 20px;
  background: #f7fafc;
}

.result-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.analysis-text {
  margin: 18px 0 0;
  padding: 16px 18px;
  border-radius: 16px;
  background: #edf3f8;
  color: #4d6074;
  line-height: 1.7;
  white-space: pre-wrap;
}

.result-badge.success {
  background: #ddf5e9;
  color: #0f7a43;
}

.result-badge.danger {
  background: #fce7e7;
  color: #b42318;
}

button,
.ghost {
  border: 0;
  border-radius: 14px;
  padding: 12px 18px;
  font-size: 14px;
  text-decoration: none;
  cursor: pointer;
  transition: transform 0.18s ease, opacity 0.18s ease, background-color 0.18s ease;
}

button:hover:not(:disabled),
.ghost:hover {
  transform: translateY(-1px);
}

button {
  background: #17324d;
  color: #fff;
}

.secondary-button,
.ghost {
  background: #e7eef6;
  color: #17324d;
}

button:disabled {
  opacity: 0.65;
  cursor: not-allowed;
  transform: none;
}

@media (max-width: 900px) {
  .sheet-body {
    grid-template-columns: 1fr;
  }

  .question-nav {
    grid-template-columns: repeat(5, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .practice-page {
    padding: 18px 14px 30px;
  }

  .hero-card,
  .config-card,
  .summary-card,
  .sheet-card,
  .result-card {
    border-radius: 22px;
    padding: 20px;
  }

  .hero-card h1 {
    font-size: 28px;
  }

  .question-nav {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}
</style>
