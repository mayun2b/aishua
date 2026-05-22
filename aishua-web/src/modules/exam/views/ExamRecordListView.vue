<template>
  <div class="exam-record-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">Exam Records</p>
        <h1>我的考试记录</h1>
        <p class="description">
          可查看历史考试成绩。仅“已完成”记录支持复盘详情，进行中的记录需要回到原作答页继续交卷。
        </p>
      </div>

      <div class="hero-actions">
        <router-link class="ghost" to="/exercise/exam">去考试中心</router-link>
        <router-link class="ghost" to="/dashboard">返回工作台</router-link>
      </div>
    </section>

    <section class="filter-card">
      <div class="table-head">
        <h2>筛选条件</h2>
      </div>
      <div class="filter-grid">
        <label>
          <span>学科</span>
          <select v-model="filters.subjectId">
            <option value="">全部学科</option>
            <option v-for="subject in subjectOptions" :key="subject.id" :value="String(subject.id)">
              {{ subject.name }}
            </option>
          </select>
        </label>

        <label>
          <span>状态</span>
          <select v-model="filters.status">
            <option value="">全部状态</option>
            <option value="2">已完成</option>
            <option value="1">进行中</option>
          </select>
        </label>
      </div>
      <div class="filter-actions">
        <button type="button" class="ghost" @click="resetFilters">重置</button>
        <button type="button" @click="loadRecords">刷新</button>
      </div>
    </section>

    <section class="table-card">
      <div class="table-head">
        <h2>记录列表</h2>
        <span>{{ filteredRecords.length }} 条</span>
      </div>

      <div v-if="loading" class="empty-state">正在加载考试记录...</div>
      <div v-else-if="!filteredRecords.length" class="empty-state">暂无考试记录</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>考试名称</th>
              <th>学科</th>
              <th>成绩</th>
              <th>正确题数</th>
              <th>用时</th>
              <th>开考时间</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="record in filteredRecords" :key="record.id">
              <td>{{ record.examName || '-' }}</td>
              <td>{{ record.subjectName || '-' }}</td>
              <td>{{ formatScore(record.score) }}</td>
              <td>{{ record.correctQuestions || 0 }} / {{ record.totalQuestions || 0 }}</td>
              <td>{{ formatDurationMinutes(record.duration) }}</td>
              <td>{{ formatDateTime(record.startTime) }}</td>
              <td>
                <span :class="['status-chip', Number(record.status) === 2 ? 'done' : 'ongoing']">
                  {{ resolveStatusLabel(record.status) }}
                </span>
              </td>
              <td>
                <div class="row-actions">
                  <router-link
                    v-if="Number(record.status) === 2"
                    class="small ghost"
                    :to="`/exercise/exam/records/${record.id}`"
                  >
                    详情
                  </router-link>
                  <router-link
                    v-else-if="hasSessionCache(record.id)"
                    class="small ghost"
                    :to="`/exercise/exam/session/${record.id}`"
                  >
                    继续作答
                  </router-link>
                  <span v-else class="tip">作答页已丢失</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { showToast } from 'vant'
import examApi from '../api/exam'
import {
  buildSessionCacheKey,
  formatDateTime,
  formatDurationMinutes,
  formatScore,
  resolveStatusLabel
} from '../utils/examHelpers'

const loading = ref(false)
const records = ref([])

const filters = reactive({
  subjectId: '',
  status: ''
})

const subjectOptions = computed(() => {
  const map = new Map()
  for (const item of records.value) {
    if (!item?.subjectId) {
      continue
    }
    if (!map.has(item.subjectId)) {
      map.set(item.subjectId, {
        id: item.subjectId,
        name: item.subjectName || `学科 ${item.subjectId}`
      })
    }
  }
  return [...map.values()]
})

const filteredRecords = computed(() => {
  return records.value.filter((item) => {
    const subjectMatched = !filters.subjectId || String(item.subjectId) === filters.subjectId
    const statusMatched = !filters.status || String(item.status) === filters.status
    return subjectMatched && statusMatched
  })
})

const hasSessionCache = (recordId) => {
  return Boolean(sessionStorage.getItem(buildSessionCacheKey(recordId)))
}

const loadRecords = async () => {
  loading.value = true
  try {
    const response = await examApi.listMyRecords()
    records.value = response.data || []
  } catch (error) {
    showToast(error.message || '加载考试记录失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.subjectId = ''
  filters.status = ''
}

onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.exam-record-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background: linear-gradient(135deg, #eef5ff 0%, #eaf8ef 100%);
}

.hero-card,
.filter-card,
.table-card {
  max-width: 1180px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 28px;
  box-shadow: 0 24px 60px rgba(29, 57, 84, 0.12);
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
  color: #78879a;
  font-size: 12px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
}

.hero-card h1 {
  margin: 12px 0 0;
  font-size: 34px;
  color: #17324d;
}

.description {
  margin: 12px 0 0;
  max-width: 760px;
  color: #607184;
  line-height: 1.75;
}

.hero-actions {
  display: flex;
  gap: 10px;
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
  color: #6f7f93;
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
  color: #536579;
  font-size: 13px;
}

select {
  width: 100%;
  border: 1px solid #d0dbe9;
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

.table-wrap {
  margin-top: 14px;
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  text-align: left;
  padding: 12px 10px;
  border-bottom: 1px solid #e5ebf3;
  color: #233a52;
  font-size: 14px;
  white-space: nowrap;
}

th {
  color: #607286;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.status-chip {
  border-radius: 999px;
  padding: 4px 9px;
  font-size: 12px;
  font-weight: 600;
}

.status-chip.done {
  color: #2d8a44;
  background: #e7f8ed;
}

.status-chip.ongoing {
  color: #bd7b17;
  background: #fff4dd;
}

.row-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tip {
  color: #9aa7b8;
  font-size: 12px;
}

button,
.ghost {
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

.ghost {
  background: rgba(23, 50, 77, 0.08);
  color: #17324d;
}

.small {
  padding: 6px 10px;
  border-radius: 10px;
  font-size: 12px;
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
  .exam-record-page {
    padding: 20px 14px 32px;
  }

  .hero-card h1 {
    font-size: 28px;
  }
}
</style>
