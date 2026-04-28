<template>
  <div class="detail-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">Practice Session</p>
        <h1>本次练习作答明细</h1>
        <p class="description">
          这里展示该次练习下每一道题的作答结果、答案对比和耗时，适合按单次练习做复盘。
        </p>
      </div>

      <div class="hero-actions">
        <router-link class="ghost" :to="backLink">返回练习记录</router-link>
        <router-link class="ghost" to="/wrong-questions">查看错题记录</router-link>
      </div>
    </section>

    <section v-if="session" class="summary-card">
      <div class="summary-item">
        <span>学科</span>
        <strong>{{ session.subjectName || '-' }}</strong>
      </div>
      <div class="summary-item">
        <span>模式</span>
        <strong>{{ resolveModeLabel(session.practiceMode) }}</strong>
      </div>
      <div class="summary-item">
        <span>答题情况</span>
        <strong>{{ session.answeredCount || 0 }} / {{ session.questionCount || 0 }}</strong>
      </div>
      <div class="summary-item">
        <span>正确率</span>
        <strong>{{ formatRate(session.correctRate) }}</strong>
      </div>
      <div class="summary-item">
        <span>总耗时</span>
        <strong>{{ session.totalTimeCost || 0 }} 秒</strong>
      </div>
      <div class="summary-item">
        <span>练习时间</span>
        <strong>{{ formatDateTime(session.startedAt) }}</strong>
      </div>
    </section>

    <section class="table-card">
      <div class="table-head">
        <h2>题目明细</h2>
        <span>{{ records.length }} 题</span>
      </div>

      <div v-if="loading" class="empty-state">正在加载本次练习明细...</div>
      <div v-else-if="!session || !records.length" class="empty-state">当前练习暂无可展示的作答记录。</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>题目</th>
              <th>结果</th>
              <th>答案对比</th>
              <th>耗时</th>
              <th>提交时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="record in pagedRecords" :key="record.recordId">
              <td>
                <div class="title-cell">
                  <strong>{{ record.questionTitle || '题目已删除' }}</strong>
                  <span>{{ resolveTypeLabel(record.questionType) }}</span>
                </div>
              </td>
              <td>
                <span :class="['status-chip', record.isCorrect === 1 ? 'success' : 'danger']">
                  {{ record.isCorrect === 1 ? '答对' : '答错' }}
                </span>
              </td>
              <td>
                <div class="answer-cell">
                  <span>你的答案：{{ formatAnswerDisplay(record.userAnswer) }}</span>
                  <span>标准答案：{{ formatAnswerDisplay(record.correctAnswer) }}</span>
                </div>
              </td>
              <td>{{ record.timeCost || 0 }} 秒</td>
              <td>{{ formatDateTime(record.exerciseTime) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <BasePagination v-if="!loading && records.length" v-model="currentPage" :total="records.length" />
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import BasePagination from '@/components/BasePagination.vue'
import useClientPagination from '@/composables/useClientPagination'
import practiceApi from '../api/practice'

const route = useRoute()

const loading = ref(false)
const session = ref(null)
const records = ref([])
const { currentPage, pagedItems: pagedRecords, resetPage } = useClientPagination(records)

const sessionId = computed(() => Number(route.params.sessionId || 0))
const backLink = computed(() => {
  const subjectId = session.value?.subjectId
  return subjectId ? `/practice-records?subjectId=${subjectId}` : '/practice-records'
})

const loadSessionDetail = async () => {
  if (!sessionId.value) {
    showToast('练习会话不存在')
    return
  }

  loading.value = true
  try {
    const response = await practiceApi.getSessionDetail(sessionId.value)
    session.value = response.data || null
    records.value = response.data?.records || []
    resetPage()
  } catch (error) {
    showToast(error.message || '加载练习明细失败')
  } finally {
    loading.value = false
  }
}

const resolveModeLabel = (mode) => {
  switch (mode) {
    case 1:
      return '顺序练习'
    case 2:
      return '随机练习'
    default:
      return mode ? `模式 ${mode}` : '-'
  }
}

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
      return type ? `题型 ${type}` : '-'
  }
}

const formatRate = (value) => {
  if (value == null || value === '') {
    return '0%'
  }
  return `${value}%`
}

const formatDateTime = (value) => {
  if (!value) {
    return '暂无记录'
  }
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

const formatAnswerDisplay = (value) => {
  if (value == null || String(value).trim() === '') {
    return '未作答'
  }

  const normalized = String(value).trim()
  if (normalized.startsWith('[') && normalized.endsWith(']')) {
    try {
      const parsed = JSON.parse(normalized)
      if (Array.isArray(parsed)) {
        return parsed.join(', ')
      }
    } catch (error) {
      return normalized
    }
  }
  return normalized
}

onMounted(() => {
  loadSessionDetail()
})
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background: linear-gradient(135deg, #f2f7eb 0%, #dbe7f8 100%);
}

.hero-card,
.summary-card,
.table-card {
  max-width: 1180px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.94);
  border-radius: 28px;
  box-shadow: 0 24px 60px rgba(38, 57, 77, 0.12);
}

.hero-card,
.table-card {
  padding: 30px;
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
  color: #5e6d7c;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.summary-card,
.table-card {
  margin-top: 18px;
}

.summary-card {
  padding: 20px 24px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
}

.summary-item {
  display: grid;
  gap: 6px;
}

.summary-item span {
  color: #6c7a8d;
  font-size: 13px;
}

.summary-item strong {
  color: #17324d;
  font-size: 18px;
}

.table-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.table-head h2 {
  margin: 0;
  color: #17324d;
  font-size: 24px;
}

.table-head span {
  color: #6c7a8d;
}

.table-wrap {
  margin-top: 18px;
  overflow: auto;
}

table {
  width: 100%;
  min-width: 980px;
  border-collapse: collapse;
}

th,
td {
  padding: 14px 12px;
  text-align: left;
  border-bottom: 1px solid #e5edf5;
  vertical-align: top;
}

.title-cell,
.answer-cell {
  display: grid;
  gap: 6px;
}

.title-cell strong {
  color: #17324d;
}

.title-cell span,
.answer-cell span {
  color: #5e6d7c;
  font-size: 13px;
  line-height: 1.6;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
}

.status-chip.success {
  background: #ddf5e9;
  color: #0f7a43;
}

.status-chip.danger {
  background: #fce7e7;
  color: #b42318;
}

.empty-state {
  margin-top: 18px;
  padding: 34px;
  text-align: center;
  color: #6c7a8d;
  background: #f3f7fb;
  border-radius: 18px;
}

.ghost {
  border: 0;
  border-radius: 14px;
  padding: 12px 18px;
  font-size: 14px;
  text-decoration: none;
  cursor: pointer;
  background: #e7eef6;
  color: #17324d;
}

@media (max-width: 768px) {
  .detail-page {
    padding: 18px 14px 30px;
  }

  .hero-card,
  .summary-card,
  .table-card {
    border-radius: 22px;
    padding: 20px;
  }

  .hero-card h1 {
    font-size: 28px;
  }
}
</style>
