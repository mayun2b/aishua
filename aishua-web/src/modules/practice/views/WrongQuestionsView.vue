<template>
  <div class="wrong-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">Wrong Questions</p>
        <h1>错题记录</h1>
        <p class="description">
          这里集中展示你在练习中答错过的题目，方便按学科回顾、定位薄弱点并继续练习。
        </p>
      </div>

      <div class="hero-actions">
        <router-link class="ghost" to="/dashboard">返回工作台</router-link>
        <router-link class="ghost" to="/practice-records">查看练习记录</router-link>
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
        <button type="button" @click="loadWrongQuestions">查询</button>
      </div>
    </section>

    <section class="table-card">
      <div class="table-head">
        <h2>错题列表</h2>
        <span>{{ wrongQuestions.length }} 条</span>
      </div>

      <div v-if="loading" class="empty-state">正在加载错题记录...</div>
      <div v-else-if="!wrongQuestions.length" class="empty-state">暂无错题记录。</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>题目</th>
              <th>学科</th>
              <th>标签</th>
              <th>标准答案</th>
              <th>错误次数</th>
              <th>最近出错</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="question in pagedWrongQuestions" :key="question.wrongQuestionId">
              <td>
                <div class="title-cell">
                  <strong>{{ question.questionTitle || '题目已删除' }}</strong>
                  <span>{{ resolveTypeLabel(question.questionType) }} / {{ resolveDifficultyLabel(question.difficulty) }}</span>
                  <span v-if="question.analysis" class="analysis-preview">{{ question.analysis }}</span>
                </div>
              </td>
              <td>{{ question.subjectName || '-' }}</td>
              <td>{{ question.tags || '-' }}</td>
              <td>{{ formatAnswerDisplay(question.correctAnswer) }}</td>
              <td>{{ question.wrongCount ?? 0 }}</td>
              <td>{{ formatDateTime(question.lastWrongTime) }}</td>
              <td>
                <router-link
                  v-if="question.subjectId"
                  class="practice-link"
                  :to="`/practice?subjectId=${question.subjectId}`"
                >
                  去练习
                </router-link>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <BasePagination
        v-if="!loading && wrongQuestions.length"
        v-model="currentPage"
        :total="wrongQuestions.length"
      />
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
const wrongQuestions = ref([])
const { currentPage, pagedItems: pagedWrongQuestions, resetPage } = useClientPagination(wrongQuestions)

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

const loadWrongQuestions = async () => {
  loading.value = true
  try {
    const response = await practiceApi.listWrongQuestions({
      subjectId: filters.subjectId ? Number(filters.subjectId) : undefined
    })
    wrongQuestions.value = response.data || []
    resetPage()
  } catch (error) {
    showToast(error.message || '加载错题记录失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = async () => {
  filters.subjectId = resolveRouteSubjectId()
  await loadWrongQuestions()
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

const resolveDifficultyLabel = (difficulty) => {
  switch (difficulty) {
    case 1:
      return '简单'
    case 2:
      return '中等'
    case 3:
      return '困难'
    default:
      return difficulty ? `难度 ${difficulty}` : '-'
  }
}

const formatDateTime = (value) => {
  if (!value) {
    return '暂无记录'
  }
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

const formatAnswerDisplay = (value) => {
  if (value == null || String(value).trim() === '') {
    return '暂无答案'
  }
  return String(value).trim()
}

watch(
  () => route.query.subjectId,
  async () => {
    filters.subjectId = resolveRouteSubjectId()
    await loadWrongQuestions()
  }
)

onMounted(async () => {
  filters.subjectId = resolveRouteSubjectId()
  await loadSubjects()
  await loadWrongQuestions()
})
</script>

<style scoped>
.wrong-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background: linear-gradient(135deg, #f6efe7 0%, #dbe9f7 100%);
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

.title-cell {
  display: grid;
  gap: 6px;
}

.title-cell strong {
  color: #17324d;
  line-height: 1.6;
}

.title-cell span {
  color: #5e6d7c;
  font-size: 13px;
  line-height: 1.6;
}

.analysis-preview {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.empty-state {
  margin: 18px 0 0;
  padding: 34px;
  text-align: center;
  color: #6c7a8d;
  background: #f3f7fb;
  border-radius: 18px;
}

button,
.ghost,
.practice-link {
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

.practice-link {
  display: inline-flex;
  align-items: center;
  background: #17324d;
  color: #fff;
  padding: 8px 12px;
  border-radius: 12px;
  font-size: 13px;
}

@media (max-width: 768px) {
  .wrong-page {
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
