<template>
  <div class="records-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">Practice Records</p>
        <h1>练习记录</h1>
        <p class="description">
          这里按每一次练习会话展示你的记录。你可以先找到某一次练习，再进入查看那次练习下每道题的作答情况。
        </p>
      </div>

      <div class="hero-actions">
        <router-link class="ghost" to="/dashboard">返回工作台</router-link>
        <router-link class="ghost" to="/wrong-questions">查看错题记录</router-link>
      </div>
    </section>

    <section class="filter-card">
      <div class="filter-grid">
        <label>
          <span>学科</span>
          <select v-model="filters.subjectId">
            <option value="">全部学科</option>
            <option v-for="subject in subjects" :key="subject.subjectId" :value="String(subject.subjectId)">
              {{ subject.name }}
            </option>
          </select>
        </label>
      </div>

      <div class="filter-actions">
        <button type="button" class="ghost" @click="resetFilters">重置</button>
        <button type="button" @click="loadSessions">查询</button>
      </div>
    </section>

    <section class="table-card">
      <div class="table-head">
        <h2>练习会话</h2>
        <span>{{ sessions.length }} 次</span>
      </div>

      <div v-if="loading" class="empty-state">正在加载练习会话...</div>
      <div v-else-if="!sessions.length" class="empty-state">暂无练习记录。</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>开始时间</th>
              <th>学科 / 模式</th>
              <th>答题情况</th>
              <th>正确率</th>
              <th>总耗时</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="session in pagedSessions" :key="session.sessionId">
              <td>
                <div class="cell-stack">
                  <strong>{{ formatDateTime(session.startedAt) }}</strong>
                  <span>结束：{{ formatDateTime(session.endedAt) }}</span>
                </div>
              </td>
              <td>
                <div class="cell-stack">
                  <strong>{{ session.subjectName || '-' }}</strong>
                  <span>{{ resolveModeLabel(session.practiceMode) }}</span>
                </div>
              </td>
              <td>
                <div class="cell-stack">
                  <strong>{{ session.answeredCount || 0 }} / {{ session.questionCount || 0 }}</strong>
                  <span>答对 {{ session.correctCount || 0 }}，答错 {{ session.wrongCount || 0 }}</span>
                </div>
              </td>
              <td>{{ formatRate(session.correctRate) }}</td>
              <td>{{ session.totalTimeCost || 0 }} 秒</td>
              <td>
                <span :class="['status-chip', session.status === 2 ? 'success' : 'warning']">
                  {{ session.status === 2 ? '已完成' : '进行中' }}
                </span>
              </td>
              <td>
                <router-link class="detail-link" :to="`/practice-records/${session.sessionId}`">
                  查看作答
                </router-link>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <BasePagination v-if="!loading && sessions.length" v-model="currentPage" :total="sessions.length" />
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import BasePagination from '@/components/BasePagination.vue'
import useClientPagination from '@/composables/useClientPagination'
import subjectApi from '../../subject/api/subject'
import practiceApi from '../api/practice'

const route = useRoute()

const loading = ref(false)
const subjects = ref([])
const sessions = ref([])
const { currentPage, pagedItems: pagedSessions, resetPage } = useClientPagination(sessions)

const filters = reactive({
  subjectId: ''
})

const resolveRouteSubjectId = () => {
  return route.query.subjectId ? String(route.query.subjectId) : ''
}

const loadSubjects = async () => {
  try {
    const response = await subjectApi.listMySubjects()
    subjects.value = response.data || []
  } catch (error) {
    showToast(error.message || '加载学科失败')
  }
}

const loadSessions = async () => {
  loading.value = true
  try {
    const response = await practiceApi.listSessions({
      subjectId: filters.subjectId ? Number(filters.subjectId) : undefined
    })
    sessions.value = response.data || []
    resetPage()
  } catch (error) {
    showToast(error.message || '加载练习记录失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = async () => {
  filters.subjectId = resolveRouteSubjectId()
  await loadSessions()
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

const formatDateTime = (value) => {
  if (!value) {
    return '暂无记录'
  }
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

const formatRate = (value) => {
  if (value == null || value === '') {
    return '0%'
  }
  return `${value}%`
}

watch(
  () => route.query.subjectId,
  async () => {
    filters.subjectId = resolveRouteSubjectId()
    await loadSessions()
  }
)

onMounted(async () => {
  filters.subjectId = resolveRouteSubjectId()
  await loadSubjects()
  await loadSessions()
})
</script>

<style scoped>
.records-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background: linear-gradient(135deg, #f0f6eb 0%, #dde8f8 100%);
}

.hero-card,
.filter-card,
.table-card {
  max-width: 1180px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.94);
  border-radius: 28px;
  box-shadow: 0 24px 60px rgba(38, 57, 77, 0.12);
}

.hero-card {
  padding: 30px;
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

.hero-actions,
.filter-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-card,
.table-card {
  margin-top: 18px;
  padding: 24px;
}

.filter-card {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  flex: 1;
}

.filter-grid label {
  display: grid;
  gap: 8px;
  color: #17324d;
  font-weight: 600;
}

.filter-grid select {
  width: 100%;
  padding: 13px 14px;
  border: 1px solid #cad7e3;
  border-radius: 14px;
  font-size: 14px;
  box-sizing: border-box;
  background: #fff;
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

th {
  color: #6c7a8d;
  font-size: 13px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.cell-stack {
  display: grid;
  gap: 6px;
}

.cell-stack strong {
  color: #17324d;
}

.cell-stack span {
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

.status-chip.warning {
  background: #fff4e5;
  color: #b54708;
}

.detail-link {
  display: inline-flex;
  align-items: center;
  border-radius: 12px;
  padding: 8px 12px;
  background: #17324d;
  color: #fff;
  text-decoration: none;
  font-size: 13px;
}

.empty-state {
  margin-top: 18px;
  padding: 34px;
  text-align: center;
  color: #6c7a8d;
  background: #f3f7fb;
  border-radius: 18px;
}

button,
.ghost {
  border: 0;
  border-radius: 14px;
  padding: 12px 18px;
  font-size: 14px;
  text-decoration: none;
  cursor: pointer;
}

button {
  background: #17324d;
  color: #fff;
}

.ghost {
  background: #e7eef6;
  color: #17324d;
}

@media (max-width: 768px) {
  .records-page {
    padding: 18px 14px 30px;
  }

  .hero-card,
  .filter-card,
  .table-card {
    border-radius: 22px;
    padding: 20px;
  }

  .hero-card h1 {
    font-size: 28px;
  }
}
</style>
