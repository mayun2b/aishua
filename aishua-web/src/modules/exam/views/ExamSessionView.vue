<template>
  <div class="exam-session-page">
    <section v-if="loading" class="state-card">正在加载考试信息...</section>

    <section v-else-if="!examSession" class="state-card">
      <p>考试会话已失效，请返回考试中心重新开考。</p>
      <router-link class="ghost-link" to="/exercise/exam">返回考试中心</router-link>
    </section>

    <template v-else>
      <section class="summary-card">
        <div>
          <p class="eyebrow">Exam Session</p>
          <h1>{{ examSession.paperName }}</h1>
          <p class="description">
            {{ examSession.subjectName || '-' }} · {{ questions.length }} 题 · 总分
            {{ examSession.totalScore || 0 }} 分
          </p>
        </div>

        <div class="meta-grid">
          <div>
            <span>开始时间</span>
            <strong>{{ formatDateTime(examSession.startTime) }}</strong>
          </div>
          <div>
            <span>考试时长</span>
            <strong>{{ examDurationMinutes }} 分钟</strong>
          </div>
          <div>
            <span>剩余时间</span>
            <strong :class="timeClass">{{ displayRemainingTime }}</strong>
          </div>
          <div>
            <span>当前进度</span>
            <strong>{{ currentIndex + 1 }} / {{ questions.length }}</strong>
          </div>
        </div>
      </section>

      <section v-if="submittedResult" class="result-card">
        <div class="result-head">
          <h2>已完成交卷</h2>
          <span class="status-chip done">成绩已生成</span>
        </div>
        <div class="result-grid">
          <div>
            <span>总题数</span>
            <strong>{{ submittedResult.totalQuestions || 0 }} 题</strong>
          </div>
          <div>
            <span>答对题数</span>
            <strong>{{ submittedResult.correctQuestions || 0 }} 题</strong>
          </div>
          <div>
            <span>考试得分</span>
            <strong>{{ formatScore(submittedResult.score) }} 分</strong>
          </div>
          <div>
            <span>用时</span>
            <strong>{{ submittedResult.duration || 0 }} 分钟</strong>
          </div>
        </div>
        <div class="result-actions">
          <router-link class="ghost-link" :to="`/exercise/exam/records/${recordId}`">查看试卷详情</router-link>
          <router-link class="ghost-link" to="/exercise/exam/records">返回考试记录</router-link>
          <router-link class="ghost-link" to="/exercise/exam">继续考试</router-link>
        </div>
      </section>

      <template v-else>
        <section class="nav-card">
          <div class="card-head">
            <h2>答题卡</h2>
            <span>已作答 {{ answeredCount }} / {{ questions.length }}</span>
          </div>
          <div class="nav-grid">
            <button
              v-for="(question, index) in questions"
              :key="question.questionId"
              type="button"
              :class="[
                'nav-chip',
                index === currentIndex ? 'active' : '',
                isAnswered(question.questionId) ? 'answered' : ''
              ]"
              @click="goToQuestion(index)"
            >
              {{ index + 1 }}
            </button>
          </div>
        </section>

        <section v-if="currentQuestion" class="question-card">
          <div class="card-head">
            <h2>第 {{ currentIndex + 1 }} 题</h2>
            <div class="meta-pills">
              <span class="pill">{{ resolveTypeLabel(currentQuestion.type) }}</span>
              <span class="pill">{{ resolveDifficultyLabel(currentQuestion.difficulty) }}</span>
              <span class="pill">分值 {{ currentQuestion.score || 0 }}</span>
            </div>
          </div>

          <h3>{{ currentQuestion.title }}</h3>
          <p v-if="currentQuestion.content" class="content">{{ currentQuestion.content }}</p>

          <div v-if="isChoiceQuestion(currentQuestion.type)" class="option-list">
            <button
              v-for="option in currentOptions"
              :key="option.optionKey"
              type="button"
              :class="[
                'option-item',
                isOptionActive(currentQuestion, option) ? 'active' : ''
              ]"
              @click="toggleOption(currentQuestion, option)"
            >
              <span class="option-key">{{ option.optionKey }}</span>
              <span>{{ option.optionText }}</span>
            </button>
          </div>

          <div v-else class="text-answer">
            <textarea
              :value="textAnswerValue"
              rows="6"
              maxlength="10000"
              placeholder="请输入你的答案"
              @input="setTextAnswer($event.target.value)"
            ></textarea>
          </div>

          <div class="question-actions">
            <button type="button" class="ghost-button" :disabled="currentIndex === 0" @click="goToPrevious">
              上一题
            </button>
            <button
              type="button"
              class="ghost-button"
              :disabled="currentIndex >= questions.length - 1"
              @click="goToNext"
            >
              下一题
            </button>
            <button type="button" :disabled="submitting" @click="submitExam">
              {{ submitting ? '交卷中...' : '提交试卷' }}
            </button>
          </div>
        </section>
      </template>
    </template>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { onBeforeRouteLeave, useRoute } from 'vue-router'
import { showToast } from 'vant'
import examApi from '../api/exam'
import {
  buildSessionCacheKey,
  buildSessionStateKey,
  formatDateTime,
  formatScore,
  isLikelyBooleanText,
  parseQuestionOptions,
  resolveDifficultyLabel,
  resolveTypeLabel
} from '../utils/examHelpers'

const route = useRoute()

const loading = ref(true)
const submitting = ref(false)
const examSession = ref(null)
const questions = ref([])
const currentIndex = ref(0)
const answers = ref({})
const answerTimeSeconds = ref({})
const enteredAt = ref(0)
const remainingSeconds = ref(0)
const submittedResult = ref(null)
const countdownTimer = ref(null)
const autoSubmitted = ref(false)

const recordId = computed(() => Number(route.params.recordId || 0))
const sessionCacheKey = computed(() => buildSessionCacheKey(recordId.value))
const stateCacheKey = computed(() => buildSessionStateKey(recordId.value))

const examDurationMinutes = computed(() => {
  const duration = Number(examSession.value?.duration || 0)
  return duration > 0 ? duration : 120
})

const totalDurationSeconds = computed(() => examDurationMinutes.value * 60)

const currentQuestion = computed(() => questions.value[currentIndex.value] || null)
const currentOptions = computed(() => parseQuestionOptions(currentQuestion.value?.options))

const answeredCount = computed(() => {
  let count = 0
  for (const question of questions.value) {
    if (isAnswered(question.questionId)) {
      count++
    }
  }
  return count
})

const displayRemainingTime = computed(() => {
  const safe = Math.max(remainingSeconds.value, 0)
  const minutes = Math.floor(safe / 60)
  const seconds = safe % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

const timeClass = computed(() => {
  if (remainingSeconds.value <= 60) {
    return 'danger-text'
  }
  if (remainingSeconds.value <= 300) {
    return 'warn-text'
  }
  return ''
})

const textAnswerValue = computed(() => {
  if (!currentQuestion.value) {
    return ''
  }
  const raw = answers.value[currentQuestion.value.questionId]
  return typeof raw === 'string' ? raw : ''
})

const loadSessionFromCache = () => {
  const raw = sessionStorage.getItem(sessionCacheKey.value)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw)
  } catch (error) {
    return null
  }
}

const loadClientState = () => {
  const raw = sessionStorage.getItem(stateCacheKey.value)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw)
  } catch (error) {
    return null
  }
}

const saveClientState = () => {
  if (!recordId.value || submittedResult.value) {
    return
  }
  const payload = {
    currentIndex: currentIndex.value,
    answers: answers.value,
    answerTimeSeconds: answerTimeSeconds.value
  }
  sessionStorage.setItem(stateCacheKey.value, JSON.stringify(payload))
}

const clearClientState = () => {
  sessionStorage.removeItem(sessionCacheKey.value)
  sessionStorage.removeItem(stateCacheKey.value)
}

const calcRemainingSeconds = (startTime) => {
  const startAt = new Date(startTime).getTime()
  if (!startAt || Number.isNaN(startAt)) {
    return totalDurationSeconds.value
  }
  const elapsed = Math.max(Math.floor((Date.now() - startAt) / 1000), 0)
  return Math.max(totalDurationSeconds.value - elapsed, 0)
}

const initializeSession = () => {
  const payload = loadSessionFromCache()
  if (!payload?.examRecordId || payload.examRecordId !== recordId.value) {
    loading.value = false
    examSession.value = null
    return
  }

  examSession.value = payload
  questions.value = [...(payload.questions || [])]
    .filter((item) => item && item.questionId)
    .sort((left, right) => {
      const sortLeft = Number(left.sort || 0)
      const sortRight = Number(right.sort || 0)
      if (sortLeft === sortRight) {
        return Number(left.questionId) - Number(right.questionId)
      }
      return sortLeft - sortRight
    })

  const state = loadClientState()
  if (state) {
    answers.value = state.answers || {}
    answerTimeSeconds.value = state.answerTimeSeconds || {}
    currentIndex.value = Math.min(
      Math.max(Number(state.currentIndex || 0), 0),
      Math.max(questions.value.length - 1, 0)
    )
  }

  remainingSeconds.value = calcRemainingSeconds(payload.startTime)
  loading.value = false
  beginCurrentQuestionTiming()
}

const clearTimer = () => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
    countdownTimer.value = null
  }
}

const syncCurrentQuestionTime = () => {
  if (!currentQuestion.value || !enteredAt.value) {
    return
  }
  const delta = Math.max(Math.round((Date.now() - enteredAt.value) / 1000), 0)
  if (delta <= 0) {
    return
  }
  const questionId = currentQuestion.value.questionId
  const current = Number(answerTimeSeconds.value[questionId] || 0)
  answerTimeSeconds.value = {
    ...answerTimeSeconds.value,
    [questionId]: current + delta
  }
  enteredAt.value = Date.now()
}

const beginCurrentQuestionTiming = () => {
  enteredAt.value = Date.now()
}

const startTimer = () => {
  clearTimer()
  countdownTimer.value = setInterval(() => {
    if (remainingSeconds.value <= 0) {
      clearTimer()
      if (!submitting.value && !submittedResult.value && !autoSubmitted.value) {
        autoSubmitted.value = true
        showToast('考试时间已到，正在自动交卷')
        submitExam()
      }
      return
    }

    remainingSeconds.value -= 1
  }, 1000)
}

const isChoiceQuestion = (type) => type === 1 || type === 2 || type === 3

const isAnswered = (questionId) => {
  const value = answers.value[questionId]
  if (Array.isArray(value)) {
    return value.length > 0
  }
  if (typeof value === 'string') {
    return value.trim() !== ''
  }
  return false
}

const isOptionActive = (question, option) => {
  const current = answers.value[question.questionId]
  if (question.type === 2) {
    return Array.isArray(current) ? current.includes(option.optionKey) : false
  }
  return String(current || '') === option.optionKey
}

const toggleOption = (question, option) => {
  if (question.type === 2) {
    const list = Array.isArray(answers.value[question.questionId])
      ? [...answers.value[question.questionId]]
      : []
    const index = list.indexOf(option.optionKey)
    if (index >= 0) {
      list.splice(index, 1)
    } else {
      list.push(option.optionKey)
    }
    list.sort()
    answers.value = {
      ...answers.value,
      [question.questionId]: list
    }
    return
  }

  answers.value = {
    ...answers.value,
    [question.questionId]: option.optionKey
  }
}

const setTextAnswer = (value) => {
  if (!currentQuestion.value) {
    return
  }
  answers.value = {
    ...answers.value,
    [currentQuestion.value.questionId]: String(value || '')
  }
}

const goToQuestion = (index) => {
  if (index < 0 || index >= questions.value.length || index === currentIndex.value) {
    return
  }
  syncCurrentQuestionTime()
  currentIndex.value = index
  beginCurrentQuestionTiming()
}

const goToPrevious = () => {
  goToQuestion(currentIndex.value - 1)
}

const goToNext = () => {
  goToQuestion(currentIndex.value + 1)
}

const resolveJudgementAnswer = (question, optionKey) => {
  const options = parseQuestionOptions(question.options)
  const option = options.find((item) => item.optionKey === optionKey)
  if (!option) {
    return optionKey || ''
  }
  const text = String(option.optionText || '').trim()
  if (isLikelyBooleanText(text)) {
    return text
  }
  return option.optionKey || text
}

const buildUserAnswer = (question) => {
  const raw = answers.value[question.questionId]
  if (question.type === 2) {
    return Array.isArray(raw) ? raw.slice().sort().join(',') : ''
  }
  if (question.type === 3) {
    return resolveJudgementAnswer(question, String(raw || ''))
  }
  if (question.type === 4 || question.type === 5) {
    return typeof raw === 'string' ? raw.trim() : ''
  }
  return typeof raw === 'string' ? raw : ''
}

const buildAnswerTime = (questionId) => {
  const raw = Number(answerTimeSeconds.value[questionId] || 0)
  return raw > 0 ? raw : 0
}

const submitExam = async () => {
  if (!recordId.value || submitting.value || submittedResult.value || !questions.value.length) {
    return
  }

  syncCurrentQuestionTime()
  submitting.value = true

  try {
    const usedSeconds = totalDurationSeconds.value - Math.max(remainingSeconds.value, 0)
    const payload = {
      answers: questions.value.map((question) => ({
        questionId: question.questionId,
        userAnswer: buildUserAnswer(question),
        answerTime: buildAnswerTime(question.questionId)
      })),
      duration: Math.max(Math.round(usedSeconds / 60), 0)
    }

    const response = await examApi.submit(recordId.value, payload)
    submittedResult.value = response.data
    clearTimer()
    clearClientState()
    showToast('交卷成功')
  } catch (error) {
    showToast(error.message || '交卷失败')
  } finally {
    submitting.value = false
  }
}

watch(
  answers,
  () => {
    saveClientState()
  },
  { deep: true }
)

watch(
  answerTimeSeconds,
  () => {
    saveClientState()
  },
  { deep: true }
)

watch(currentIndex, () => {
  saveClientState()
})

onBeforeRouteLeave((to, from, next) => {
  if (submittedResult.value || !examSession.value) {
    next()
    return
  }

  const confirmed = window.confirm('离开作答页面后将中断本次答题，确认离开吗？')
  if (!confirmed) {
    next(false)
    return
  }

  syncCurrentQuestionTime()
  saveClientState()
  next()
})

onMounted(() => {
  initializeSession()
  if (examSession.value && !submittedResult.value) {
    startTimer()
  }
})

onBeforeUnmount(() => {
  syncCurrentQuestionTime()
  saveClientState()
  clearTimer()
})
</script>

<style scoped>
.exam-session-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background: linear-gradient(135deg, #eaf0ff 0%, #e8f7f1 100%);
}

.summary-card,
.nav-card,
.question-card,
.result-card,
.state-card {
  max-width: 1180px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 28px;
  box-shadow: 0 24px 60px rgba(29, 57, 84, 0.12);
}

.summary-card,
.nav-card,
.question-card,
.result-card {
  padding: 26px;
}

.state-card {
  padding: 34px;
  text-align: center;
  color: #5f7184;
}

.eyebrow {
  margin: 0;
  color: #7a8899;
  font-size: 12px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
}

.summary-card h1 {
  margin: 10px 0 0;
  color: #17324d;
  font-size: 30px;
}

.description {
  margin: 12px 0 0;
  color: #637487;
}

.meta-grid {
  margin-top: 18px;
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.meta-grid div {
  background: #f4f8ff;
  border-radius: 12px;
  padding: 10px 12px;
  display: grid;
  gap: 4px;
}

.meta-grid span {
  color: #5f7287;
  font-size: 12px;
}

.meta-grid strong {
  color: #17324d;
  font-size: 15px;
}

.warn-text {
  color: #c57a1a;
}

.danger-text {
  color: #cf3131;
}

.nav-card,
.question-card,
.result-card {
  margin-top: 18px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.card-head h2 {
  margin: 0;
  color: #17324d;
}

.nav-grid {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.nav-chip {
  width: 40px;
  height: 40px;
  border: 1px solid #d3deea;
  border-radius: 10px;
  background: #fff;
  color: #17324d;
  font-weight: 600;
  cursor: pointer;
}

.nav-chip.answered {
  border-color: #87b695;
  background: #f1fbf4;
}

.nav-chip.active {
  border-color: #17324d;
  background: #17324d;
  color: #fff;
}

.question-card h3 {
  margin: 16px 0 0;
  color: #17324d;
  line-height: 1.6;
}

.content {
  margin-top: 10px;
  color: #607285;
  line-height: 1.75;
}

.meta-pills {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pill {
  background: #f4f8ff;
  border-radius: 999px;
  padding: 4px 10px;
  color: #42566d;
  font-size: 12px;
}

.option-list {
  margin-top: 16px;
  display: grid;
  gap: 10px;
}

.option-item {
  border: 1px solid #d3deea;
  border-radius: 12px;
  padding: 12px;
  background: #fff;
  color: #17324d;
  display: flex;
  gap: 10px;
  text-align: left;
}

.option-item.active {
  border-color: #17324d;
  background: #eef5ff;
}

.option-key {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: #e2ebf7;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}

.text-answer {
  margin-top: 16px;
}

textarea {
  width: 100%;
  border-radius: 12px;
  border: 1px solid #d3deea;
  padding: 12px;
  box-sizing: border-box;
  font-size: 14px;
  resize: vertical;
}

.question-actions {
  margin-top: 18px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

button,
.ghost-link {
  border: 0;
  border-radius: 12px;
  padding: 10px 14px;
  font-size: 14px;
  text-decoration: none;
  cursor: pointer;
}

button {
  background: #17324d;
  color: #fff;
}

button:disabled {
  opacity: 0.68;
  cursor: not-allowed;
}

.ghost-button,
.ghost-link {
  background: rgba(23, 50, 77, 0.08);
  color: #17324d;
}

.result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.result-head h2 {
  margin: 0;
  color: #17324d;
}

.status-chip {
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 600;
}

.status-chip.done {
  color: #2d8a44;
  background: #e7f8ed;
}

.result-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.result-grid div {
  background: #f4f8ff;
  border-radius: 12px;
  padding: 12px;
  display: grid;
  gap: 4px;
}

.result-grid span {
  font-size: 12px;
  color: #5f7287;
}

.result-grid strong {
  color: #17324d;
  font-size: 18px;
}

.result-actions {
  margin-top: 16px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .exam-session-page {
    padding: 20px 14px 32px;
  }

  .summary-card h1 {
    font-size: 24px;
  }
}
</style>
