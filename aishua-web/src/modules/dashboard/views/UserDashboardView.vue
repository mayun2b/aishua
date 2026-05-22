<template>
  <div class="dashboard-page">
    <header class="page-head">
      <div>
        <p class="eyebrow">Learning Stats</p>
        <h1>{{ greeting }}</h1>
      </div>

      <div class="head-actions">
        <router-link class="ghost" to="/my-subjects">开始练习</router-link>
        <router-link class="ghost" to="/practice-records">练习记录</router-link>
        <router-link class="ghost" to="/exercise/exam">模拟考试</router-link>
        <router-link class="ghost" to="/exercise/exam/records">考试记录</router-link>
        <router-link class="ghost" to="/wrong-questions">错题记录</router-link>
        <router-link v-if="user?.isAdmin === 1" class="ghost" to="/admin">管理台</router-link>
        <button type="button" @click="handleLogout">退出登录</button>
      </div>
    </header>

    <main class="dashboard-shell">
      <section class="toolbar">
        <div class="profile-line">
          <span>{{ user?.nickname || '同学' }}</span>
          <strong>{{ user?.phone || user?.id || '-' }}</strong>
        </div>

        <div class="range-control" aria-label="统计周期">
          <button
            v-for="option in dayOptions"
            :key="option"
            type="button"
            :class="{ active: selectedDays === option }"
            @click="changeDays(option)"
          >
            {{ option }} 天
          </button>
        </div>
      </section>

      <section v-if="loading" class="state-block">正在加载学习统计...</section>

      <template v-else>
        <section class="metric-grid">
          <article v-for="item in overviewItems" :key="item.label" class="metric-card">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.meta }}</small>
          </article>
        </section>

        <section class="panel">
          <div class="panel-head">
            <h2>近 {{ selectedDays }} 天趋势</h2>
            <span>{{ todaySummary }}</span>
          </div>

          <div v-if="trendItems.length" class="trend-chart">
            <div
              v-for="(item, index) in trendItems"
              :key="item.statDate || index"
              class="trend-column"
              :title="`${formatDate(item.statDate)}：${item.doCount || 0} 题，正确率 ${formatRate(item.correctRate)}`"
            >
              <div class="trend-track">
                <span class="trend-fill" :style="{ height: trendBarHeight(item) }"></span>
              </div>
              <span class="trend-label">{{ trendDayLabel(index, item.statDate) }}</span>
            </div>
          </div>
          <div v-else class="empty-state">暂无趋势数据</div>
        </section>

        <section class="split-grid">
          <section class="panel">
            <div class="panel-head">
              <h2>学科表现</h2>
              <router-link to="/my-subjects">我的学科</router-link>
            </div>

            <div v-if="subjectItems.length" class="subject-list">
              <article v-for="subject in subjectItems" :key="subject.subjectId || subject.subjectName" class="subject-row">
                <div class="row-main">
                  <strong>{{ subject.subjectName || '-' }}</strong>
                  <span>{{ subject.totalCount || 0 }} 题 · {{ formatTime(subject.totalTimeCost) }}</span>
                </div>
                <div class="rate-bar">
                  <span :style="{ width: rateWidth(subject.correctRate) }"></span>
                </div>
                <em>{{ formatRate(subject.correctRate) }}</em>
              </article>
            </div>
            <div v-else class="empty-state">暂无学科统计</div>
          </section>

          <section class="panel">
            <div class="panel-head">
              <h2>薄弱知识点</h2>
              <router-link to="/wrong-questions">错题记录</router-link>
            </div>

            <div v-if="weakItems.length" class="weak-list">
              <article v-for="item in weakItems" :key="`${item.subjectId}-${item.tagId}`" class="weak-row">
                <div>
                  <strong>{{ item.tagName || '-' }}</strong>
                  <span>{{ item.subjectName || '-' }}</span>
                </div>
                <div class="weak-meta">
                  <span :class="['level-chip', masteryClass(item.masteryLevel)]">{{ masteryLabel(item.masteryLevel) }}</span>
                  <em>{{ formatRate(item.correctRate) }}</em>
                </div>
              </article>
            </div>
            <div v-else class="empty-state">暂无薄弱点</div>
          </section>
        </section>

        <section class="panel">
          <div class="panel-head">
            <h2>知识点掌握度</h2>
            <span>展示最近 30 条</span>
          </div>

          <div v-if="masteryItems.length" class="mastery-table-wrap">
            <table class="mastery-table">
              <thead>
                <tr>
                  <th>考点</th>
                  <th>学科</th>
                  <th>作答</th>
                  <th>正确率</th>
                  <th>掌握等级</th>
                  <th>更新时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in masteryItems" :key="`${item.subjectId}-${item.tagId}`">
                  <td>{{ item.tagName || '-' }}</td>
                  <td>{{ item.subjectName || '-' }}</td>
                  <td>{{ item.correctCount || 0 }} / {{ item.totalCount || 0 }}</td>
                  <td>{{ formatRate(item.correctRate) }}</td>
                  <td>
                    <span :class="['level-chip', masteryClass(item.masteryLevel)]">
                      {{ masteryLabel(item.masteryLevel) }}
                    </span>
                  </td>
                  <td>{{ formatDateTime(item.updateTime) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-else class="empty-state">暂无知识点掌握度数据</div>
        </section>

        <section class="panel">
          <div class="panel-head">
            <h2>最近练习</h2>
            <router-link to="/practice-records">全部记录</router-link>
          </div>

          <div v-if="recentSessions.length" class="session-table-wrap">
            <table>
              <thead>
                <tr>
                  <th>时间</th>
                  <th>学科</th>
                  <th>答题</th>
                  <th>正确率</th>
                  <th>耗时</th>
                  <th>状态</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="session in recentSessions" :key="session.sessionId">
                  <td>{{ formatDateTime(session.startedAt) }}</td>
                  <td>{{ session.subjectName || '-' }}</td>
                  <td>{{ session.answeredCount || 0 }} / {{ session.questionCount || 0 }}</td>
                  <td>{{ formatRate(session.correctRate) }}</td>
                  <td>{{ formatTime(session.totalTimeCost) }}</td>
                  <td>
                    <span :class="['status-chip', session.status === 2 ? 'done' : 'active']">
                      {{ session.status === 2 ? '已完成' : '进行中' }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-else class="empty-state">暂无练习记录</div>
        </section>
      </template>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { showToast } from 'vant'
import practiceApi from '../../practice/api/practice'

const router = useRouter()
const store = useStore()

const dayOptions = [7, 30, 90]
const selectedDays = ref(30)
const loading = ref(false)

const emptyStats = () => ({
  overview: {},
  dailyTrends: [],
  subjectStats: [],
  weakPoints: [],
  recentSessions: []
})

const stats = ref(emptyStats())

const user = computed(() => store.getters['auth/currentUser'])
const greeting = computed(() => `${user.value?.nickname || '同学'}，你的练习闭环`)
const overview = computed(() => stats.value.overview || {})
const trendItems = computed(() => stats.value.dailyTrends || [])
const subjectItems = computed(() => (stats.value.subjectStats || []).slice(0, 6))
const weakItems = computed(() => (stats.value.weakPoints || []).slice(0, 8))
const masteryItems = computed(() => (stats.value.knowledgeMasteries || []).slice(0, 30))
const recentSessions = computed(() => stats.value.recentSessions || [])
const trendMax = computed(() => Math.max(...trendItems.value.map((item) => Number(item.doCount || 0)), 1))

const overviewItems = computed(() => [
  {
    label: '累计答题',
    value: formatNumber(overview.value.totalCount),
    meta: `答对 ${formatNumber(overview.value.correctCount)} 题`
  },
  {
    label: '整体正确率',
    value: formatRate(overview.value.correctRate),
    meta: `错题 ${formatNumber(overview.value.wrongCount)} 题`
  },
  {
    label: '今日练习',
    value: `${formatNumber(overview.value.todayCount)} 题`,
    meta: formatTime(overview.value.todayTimeCost)
  },
  {
    label: '连续练习',
    value: `${formatNumber(overview.value.continuousDays)} 天`,
    meta: `最高 ${formatNumber(overview.value.maxContinuousDays)} 天`
  },
  {
    label: '错题沉淀',
    value: `${formatNumber(overview.value.wrongQuestionCount)} 题`,
    meta: `已掌握 ${formatNumber(overview.value.masteredWrongQuestionCount)} 题`
  },
  {
    label: '练习场次',
    value: `${formatNumber(overview.value.finishedSessionCount)} 次`,
    meta: `累计用时 ${formatTime(overview.value.totalTimeCost)}`
  },
  {
    label: '最近学习',
    value: formatDate(overview.value.lastExerciseDate),
    meta: `活跃 ${formatNumber(overview.value.activeDays)} 天`
  }
])

const todaySummary = computed(() => {
  return `今日 ${formatNumber(overview.value.todayCount)} 题 · ${formatTime(overview.value.todayTimeCost)}`
})

const loadStats = async () => {
  loading.value = true
  try {
    const response = await practiceApi.getStats({ days: selectedDays.value })
    stats.value = {
      ...emptyStats(),
      ...(response.data || {})
    }
  } catch (error) {
    showToast(error.message || '加载学习统计失败')
  } finally {
    loading.value = false
  }
}

const changeDays = async (days) => {
  if (selectedDays.value === days) {
    return
  }
  selectedDays.value = days
  await loadStats()
}

const handleLogout = () => {
  store.dispatch('auth/logout')
  router.push('/login')
}

const formatNumber = (value) => {
  return Number(value || 0).toLocaleString('zh-CN')
}

const formatRate = (value) => {
  const rate = Number(value || 0)
  const rounded = Math.round(rate * 10) / 10
  return `${Number.isInteger(rounded) ? rounded.toFixed(0) : rounded.toFixed(1)}%`
}

const formatTime = (value) => {
  const seconds = Number(value || 0)
  if (seconds <= 0) {
    return '0 秒'
  }

  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  if (hours > 0) {
    return `${hours} 小时 ${minutes} 分钟`
  }
  if (minutes > 0) {
    return `${minutes} 分钟`
  }
  return `${seconds} 秒`
}

const formatDate = (value) => {
  if (!value) {
    return '-'
  }
  return String(value).slice(0, 10)
}

const formatDateTime = (value) => {
  if (!value) {
    return '-'
  }
  return String(value).replace('T', ' ').slice(0, 16)
}

const trendBarHeight = (item) => {
  const count = Number(item?.doCount || 0)
  if (count <= 0) {
    return '0'
  }
  return `${Math.max(8, Math.round((count / trendMax.value) * 100))}%`
}

const trendDayLabel = (index, value) => {
  const total = trendItems.value.length
  const step = Math.max(1, Math.ceil(total / 5))
  if (total <= 10 || index === 0 || index === total - 1 || index % step === 0) {
    return formatDate(value).slice(5)
  }
  return ''
}

const rateWidth = (value) => {
  return `${Math.min(Math.max(Number(value || 0), 0), 100)}%`
}

const masteryLabel = (level) => {
  switch (Number(level || 0)) {
    case 3:
      return '稳固'
    case 2:
      return '熟悉'
    case 1:
      return '起步'
    default:
      return '薄弱'
  }
}

const masteryClass = (level) => {
  return `level-${Math.min(Math.max(Number(level || 0), 0), 3)}`
}

onMounted(loadStats)
</script>

<style scoped>
.dashboard-page {
  min-height: 100vh;
  padding: 28px 24px 48px;
  background: #f4f6f8;
  color: #1f2933;
}

.page-head,
.dashboard-shell {
  max-width: 1180px;
  margin: 0 auto;
}

.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.eyebrow {
  margin: 0;
  color: #667085;
  font-size: 12px;
  letter-spacing: 0;
  text-transform: uppercase;
}

.page-head h1 {
  margin: 8px 0 0;
  font-size: 30px;
  line-height: 1.25;
  color: #111827;
}

.head-actions,
.range-control {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.head-actions button,
.head-actions .ghost,
.range-control button {
  min-height: 40px;
  border: 1px solid #d0d5dd;
  border-radius: 8px;
  padding: 0 14px;
  background: #fff;
  color: #344054;
  font-size: 14px;
  text-decoration: none;
  cursor: pointer;
}

.head-actions button {
  border-color: #1f2937;
  background: #1f2937;
  color: #fff;
}

.dashboard-shell {
  margin-top: 24px;
}

.toolbar,
.panel,
.metric-card {
  border: 1px solid #e4e7ec;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
}

.toolbar {
  min-height: 72px;
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.profile-line {
  display: grid;
  gap: 4px;
}

.profile-line span,
.metric-card span,
.subject-row span,
.weak-row span,
.panel-head span,
.metric-card small {
  color: #667085;
  font-size: 13px;
}

.profile-line strong {
  color: #111827;
  font-size: 16px;
}

.range-control {
  padding: 3px;
  border: 1px solid #d0d5dd;
  border-radius: 8px;
  background: #f9fafb;
}

.range-control button {
  border: 0;
  min-width: 74px;
  background: transparent;
}

.range-control button.active {
  background: #2563eb;
  color: #fff;
}

.state-block,
.empty-state {
  padding: 28px;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  background: #fff;
  color: #667085;
  text-align: center;
}

.state-block {
  margin-top: 18px;
}

.metric-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.metric-card {
  min-height: 126px;
  padding: 18px;
  display: grid;
  align-content: space-between;
}

.metric-card strong {
  margin-top: 10px;
  color: #111827;
  font-size: 28px;
  line-height: 1.15;
}

.panel {
  margin-top: 18px;
  padding: 20px;
}

.panel-head {
  min-height: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.panel-head h2 {
  margin: 0;
  color: #111827;
  font-size: 18px;
}

.panel-head a {
  color: #2563eb;
  font-size: 14px;
  text-decoration: none;
}

.trend-chart {
  height: 210px;
  margin-top: 18px;
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: minmax(12px, 1fr);
  gap: 6px;
  align-items: end;
  overflow-x: auto;
  padding-bottom: 4px;
}

.trend-column {
  min-width: 0;
  height: 100%;
  display: grid;
  grid-template-rows: 1fr 20px;
  gap: 6px;
}

.trend-track {
  position: relative;
  height: 100%;
  border-radius: 6px;
  background: #eef2f7;
  overflow: hidden;
}

.trend-fill {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 6px 6px 0 0;
  background: #22a06b;
}

.trend-label {
  min-height: 20px;
  color: #667085;
  font-size: 11px;
  text-align: center;
  white-space: nowrap;
}

.split-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 1fr);
  gap: 18px;
}

.subject-list,
.weak-list {
  margin-top: 14px;
  display: grid;
  gap: 12px;
}

.subject-row,
.weak-row {
  min-height: 64px;
  padding: 12px;
  border: 1px solid #edf0f5;
  border-radius: 8px;
  background: #fcfcfd;
}

.subject-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(120px, 180px) 56px;
  gap: 14px;
  align-items: center;
}

.row-main,
.weak-row > div:first-child {
  display: grid;
  gap: 5px;
  min-width: 0;
}

.row-main strong,
.weak-row strong {
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rate-bar {
  height: 8px;
  border-radius: 999px;
  background: #eef2f7;
  overflow: hidden;
}

.rate-bar span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: #2563eb;
}

.subject-row em,
.weak-meta em {
  color: #111827;
  font-style: normal;
  font-weight: 700;
}

.weak-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.weak-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.level-chip,
.status-chip {
  min-width: 54px;
  border-radius: 999px;
  padding: 5px 9px;
  font-size: 12px;
  text-align: center;
}

.level-0 {
  background: #fff1f2;
  color: #be123c;
}

.level-1 {
  background: #fff7ed;
  color: #c2410c;
}

.level-2 {
  background: #eff6ff;
  color: #1d4ed8;
}

.level-3 {
  background: #ecfdf3;
  color: #027a48;
}

.session-table-wrap {
  margin-top: 14px;
  overflow-x: auto;
}

.mastery-table-wrap {
  margin-top: 14px;
  overflow-x: auto;
}

table {
  width: 100%;
  min-width: 720px;
  border-collapse: collapse;
}

th,
td {
  padding: 13px 10px;
  border-bottom: 1px solid #edf0f5;
  color: #344054;
  font-size: 14px;
  text-align: left;
  white-space: nowrap;
}

th {
  color: #667085;
  font-weight: 600;
  background: #f9fafb;
}

.status-chip.done {
  background: #ecfdf3;
  color: #027a48;
}

.status-chip.active {
  background: #eff6ff;
  color: #1d4ed8;
}

@media (max-width: 900px) {
  .page-head {
    display: grid;
  }

  .metric-grid,
  .split-grid {
    grid-template-columns: 1fr;
  }

  .subject-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  .dashboard-page {
    padding: 20px 14px 36px;
  }

  .page-head h1 {
    font-size: 24px;
  }

  .head-actions,
  .head-actions .ghost,
  .head-actions button,
  .range-control,
  .range-control button {
    width: 100%;
  }

  .trend-chart {
    gap: 4px;
  }

  .weak-row {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
