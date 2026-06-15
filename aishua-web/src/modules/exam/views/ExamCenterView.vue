<template>
  <div class="exam-center-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">Exam Center</p>
        <h1>学生端模拟考试</h1>
        <p class="description">
          选择试卷后将立即开考。开考时系统会创建考试记录并锁定计时，请在规定时长内完成交卷。
        </p>
      </div>

      <div class="hero-actions">
        <router-link class="ghost" to="/dashboard">返回工作台</router-link>
        <router-link class="ghost" to="/exercise/exam/records">我的考试记录</router-link>
      </div>
    </section>

    <section class="filter-card">
      <div class="table-head">
        <h2>试卷筛选</h2>
      </div>
      <div class="filter-grid">
        <label>
          <span>学科</span>
          <select v-model="selectedSubjectId">
            <option value="">全部学科</option>
            <option
              v-for="subject in subjectOptions"
              :key="subject.subjectId"
              :value="String(subject.subjectId)"
            >
              {{ subject.subjectName }}
            </option>
          </select>
        </label>
      </div>
      <div class="filter-actions">
        <button type="button" class="ghost" @click="resetFilter">重置</button>
      </div>
    </section>

    <section class="table-card">
      <div class="table-head">
        <h2>可用试卷</h2>
        <span>{{ filteredPapers.length }} 张</span>
      </div>

      <div v-if="loading" class="empty-state">正在加载试卷...</div>
      <div v-else-if="!filteredPapers.length" class="empty-state">
        当前条件下没有可用试卷，请稍后再试
      </div>
      <div v-else class="paper-grid">
        <article v-for="paper in filteredPapers" :key="paper.id" class="paper-card">
          <header>
            <h3>{{ paper.paperName }}</h3>
            <span>{{ paper.subjectName || '-' }}</span>
          </header>

          <div class="paper-meta">
            <div>
              <span>题量</span>
              <strong>{{ paper.totalQuestions || 0 }} 题</strong>
            </div>
            <div>
              <span>总分</span>
              <strong>{{ paper.totalScore || 0 }} 分</strong>
            </div>
            <div>
              <span>时长</span>
              <strong>{{ paper.duration || 0 }} 分钟</strong>
            </div>
          </div>

          <footer>
            <span class="update-time">更新于 {{ formatDateTime(paper.updateTime) }}</span>
            <button
              type="button"
              :disabled="startingPaperId === paper.id"
              @click="startExam(paper)"
            >
              {{ startingPaperId === paper.id ? '开考中...' : '开始考试' }}
            </button>
          </footer>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import examApi from '../api/exam'
import { buildSessionCacheKey, formatDateTime } from '../utils/examHelpers'

const router = useRouter()

const loading = ref(false)
const papers = ref([])
const selectedSubjectId = ref('')
const startingPaperId = ref(null)

const subjectOptions = computed(() => {
  const map = new Map()
  for (const paper of papers.value) {
    if (!paper?.subjectId) {
      continue
    }
    if (!map.has(paper.subjectId)) {
      map.set(paper.subjectId, {
        subjectId: paper.subjectId,
        subjectName: paper.subjectName || `学科 ${paper.subjectId}`
      })
    }
  }
  return [...map.values()]
})

const filteredPapers = computed(() => {
  if (!selectedSubjectId.value) {
    return papers.value
  }
  return papers.value.filter((item) => String(item.subjectId) === selectedSubjectId.value)
})

const loadPapers = async () => {
  loading.value = true
  try {
    const response = await examApi.listPapers()
    papers.value = response.data || []
  } catch (error) {
    showToast(error.message || '加载试卷失败')
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  selectedSubjectId.value = ''
}

const startExam = async (paper) => {
  if (!paper?.id) {
    return
  }

  startingPaperId.value = paper.id
  try {
    const response = await examApi.start({ paperId: paper.id })
    const payload = response.data
    if (!payload?.examRecordId || !Array.isArray(payload.questions) || !payload.questions.length) {
      showToast('试卷暂无可用题目')
      return
    }

    const cacheKey = buildSessionCacheKey(payload.examRecordId)
    sessionStorage.setItem(cacheKey, JSON.stringify(payload))
    await router.push(`/exercise/exam/session/${payload.examRecordId}`)
  } catch (error) {
    showToast(error.message || '开始考试失败')
  } finally {
    startingPaperId.value = null
  }
}

onMounted(() => {
  loadPapers()
})
</script>

<style scoped>
.exam-center-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background: linear-gradient(135deg, #edf3ff 0%, #dff2f4 100%);
}

.hero-card,
.filter-card,
.table-card {
  max-width: 1180px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 28px;
  box-shadow: 0 24px 60px rgba(30, 62, 98, 0.12);
}

.hero-card {
  padding: 30px;
  display: flex;
  justify-content: space-between;
  gap: 18px;
  flex-wrap: wrap;
}

.eyebrow {
  margin: 0;
  color: #77879b;
  font-size: 12px;
  letter-spacing: 0.2em;
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
  color: #5f6f80;
  line-height: 1.75;
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-self: flex-start;
}

.filter-card,
.table-card {
  margin-top: 18px;
  padding: 24px;
}

.table-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.table-head h2 {
  margin: 0;
  color: #17324d;
}

.table-head span {
  color: #6e7e91;
  font-size: 14px;
}

.filter-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
}

label {
  display: grid;
  gap: 8px;
}

label span {
  color: #536479;
  font-size: 13px;
}

select {
  width: 100%;
  border: 1px solid #cfdae8;
  border-radius: 12px;
  padding: 11px 12px;
  font-size: 14px;
  background: #fff;
}

.filter-actions {
  margin-top: 14px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.paper-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.paper-card {
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  padding: 18px;
  background: #fff;
  display: grid;
  gap: 16px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.paper-card:hover {
  border-color: #bdd0e7;
  box-shadow: 0 18px 34px rgba(30, 62, 98, 0.1);
  transform: translateY(-2px);
}

.paper-card header {
  display: grid;
  gap: 6px;
}

.paper-card h3 {
  margin: 0;
  color: #17324d;
  font-size: 18px;
}

.paper-card header span {
  color: #63768b;
  font-size: 13px;
}

.paper-meta {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.paper-meta div {
  background: #f4f8ff;
  border-radius: 10px;
  padding: 10px;
  display: grid;
  gap: 4px;
}

.paper-meta span {
  color: #63768b;
  font-size: 12px;
}

.paper-meta strong {
  color: #1e3a5f;
  font-size: 14px;
  font-variant-numeric: tabular-nums;
}

.paper-card footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.update-time {
  color: #73849a;
  font-size: 12px;
}

button,
.ghost {
  border: 0;
  border-radius: 12px;
  min-height: 40px;
  padding: 10px 14px;
  font-size: 14px;
  cursor: pointer;
  text-decoration: none;
  transition: background-color 0.2s ease, box-shadow 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

button {
  background: #17324d;
  color: #fff;
}

button:hover:not(:disabled),
.ghost:hover {
  box-shadow: 0 10px 20px rgba(23, 50, 77, 0.12);
  transform: translateY(-1px);
}

button:focus-visible,
.ghost:focus-visible {
  outline: 3px solid rgba(37, 99, 235, 0.18);
  outline-offset: 3px;
}

button:disabled {
  opacity: 0.68;
  cursor: not-allowed;
}

.ghost {
  background: rgba(23, 50, 77, 0.08);
  color: #17324d;
}

.empty-state {
  margin-top: 14px;
  border: 1px dashed #cfdae8;
  border-radius: 16px;
  padding: 26px;
  color: #63768b;
  text-align: center;
}

@media (max-width: 768px) {
  .exam-center-page {
    padding: 20px 14px 32px;
  }

  .hero-card h1 {
    font-size: 28px;
  }
}
</style>
