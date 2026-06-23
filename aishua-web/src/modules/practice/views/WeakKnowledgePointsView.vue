<template>
  <div class="weak-page">
    <section class="page-head">
      <div>
        <p class="eyebrow">Weak Points</p>
        <h1>薄弱知识点定点练习</h1>
        <p class="description">
          根据历史练习掌握度排序，优先处理正确率低、错题多的知识点。
        </p>
      </div>

      <div class="head-actions">
        <router-link class="ghost" to="/dashboard">返回工作台</router-link>
        <router-link class="ghost" to="/practice-records">练习记录</router-link>
      </div>
    </section>

    <section class="filter-panel">
      <label>
        <span>学科</span>
        <select v-model="subjectFilterId" :disabled="loadingSubjects || loading || starting">
          <option value="">全部学科</option>
          <option v-for="subject in subjects" :key="subject.subjectId" :value="String(subject.subjectId)">
            {{ subject.name }}
          </option>
        </select>
      </label>

      <label>
        <span>每次题量</span>
        <input v-model.number="questionCount" type="number" min="1" max="50" :disabled="starting" />
      </label>

      <button type="button" class="secondary" :disabled="loadingSubjects || loading || starting" @click="loadWeakPoints">
        刷新
      </button>
    </section>

    <section class="content-panel">
      <div class="panel-head">
        <h2>薄弱知识点</h2>
        <span>{{ weakPoints.length }} 个</span>
      </div>

      <div v-if="loading" class="empty-state">正在加载薄弱知识点...</div>
      <div v-else-if="!weakPoints.length" class="empty-state">暂无薄弱知识点记录。</div>
      <div v-else class="weak-list">
        <button
          v-for="item in weakPoints"
          :key="`${item.subjectId}-${item.tagId}`"
          type="button"
          class="weak-row"
          :disabled="starting"
          @click="startWeakPointPractice(item)"
        >
          <div class="row-main">
            <strong>{{ item.tagName || '未知知识点' }}</strong>
            <span>{{ item.subjectName || '未知学科' }}</span>
          </div>

          <div class="stat-group">
            <div>
              <span>正确率</span>
              <strong>{{ formatRate(item.correctRate) }}</strong>
            </div>
            <div>
              <span>错题</span>
              <strong>{{ item.wrongCount || 0 }}</strong>
            </div>
            <div>
              <span>累计</span>
              <strong>{{ item.totalCount || 0 }}</strong>
            </div>
          </div>

          <span :class="['level-chip', masteryClass(item.masteryLevel)]">
            {{ masteryLabel(item.masteryLevel) }}
          </span>
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import practiceApi from '../api/practice'
import subjectApi from '../../subject/api/subject'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const loadingSubjects = ref(false)
const starting = ref(false)
const subjects = ref([])
const weakPoints = ref([])
const subjectFilterId = ref('')
const questionCount = ref(10)

const normalizedQuestionCount = computed(() => {
  const count = Number(questionCount.value)
  if (Number.isNaN(count)) {
    return 10
  }
  return Math.min(Math.max(Math.floor(count), 1), 50)
})

const loadSubjects = async () => {
  loadingSubjects.value = true
  try {
    const response = await subjectApi.listMySubjects()
    subjects.value = response.data || []
    const routeSubjectId = route.query.subjectId ? String(route.query.subjectId) : ''
    if (routeSubjectId && subjects.value.some((subject) => String(subject.subjectId) === routeSubjectId)) {
      subjectFilterId.value = routeSubjectId
    }
  } catch (error) {
    showToast(error.message || '加载学科失败')
  } finally {
    loadingSubjects.value = false
  }
}

const loadWeakPoints = async () => {
  loading.value = true
  try {
    const params = {
      limit: 100
    }
    if (subjectFilterId.value) {
      params.subjectId = Number(subjectFilterId.value)
    }
    const response = await practiceApi.listWeakPoints(params)
    weakPoints.value = response.data || []
  } catch (error) {
    weakPoints.value = []
    showToast(error.message || '加载薄弱知识点失败')
  } finally {
    loading.value = false
  }
}

const startWeakPointPractice = async (item) => {
  const subjectId = Number(item?.subjectId || 0)
  const tagId = Number(item?.tagId || 0)
  if (!subjectId || !tagId) {
    showToast('知识点数据不完整，无法开始练习')
    return
  }

  starting.value = true
  try {
    const response = await practiceApi.start({
      subjectId,
      practiceMode: 3,
      questionCount: normalizedQuestionCount.value,
      tagIds: [tagId]
    })
    const sessionId = Number(response?.data?.sessionId || 0)
    if (!sessionId) {
      throw new Error('创建练习会话失败')
    }
    await router.push({
      name: 'PracticeSession',
      params: { sessionId },
      query: {
        subjectId: String(subjectId),
        mode: '3'
      }
    })
  } catch (error) {
    showToast(error.message || '开始定点练习失败')
  } finally {
    starting.value = false
  }
}

const formatRate = (value) => {
  const rate = Number(value || 0)
  const rounded = Math.round(rate * 10) / 10
  return `${Number.isInteger(rounded) ? rounded.toFixed(0) : rounded.toFixed(1)}%`
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

watch(subjectFilterId, () => {
  loadWeakPoints()
})

onMounted(async () => {
  await loadSubjects()
  await loadWeakPoints()
})
</script>

<style scoped>
.weak-page {
  min-height: 100vh;
  padding: 28px 24px 48px;
  background: transparent;
}

.page-head,
.filter-panel,
.content-panel {
  max-width: 1180px;
  margin: 0 auto;
}

.page-head {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  flex-wrap: wrap;
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
  color: #111827;
  font-size: 30px;
  line-height: 1.25;
}

.description {
  margin: 10px 0 0;
  max-width: 720px;
  color: #667085;
  line-height: 1.7;
}

.head-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-panel,
.content-panel {
  margin-top: 18px;
  border: 1px solid #e4e7ec;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
}

.filter-panel {
  min-height: 76px;
  padding: 16px;
  display: grid;
  grid-template-columns: minmax(180px, 1fr) 160px auto;
  gap: 14px;
  align-items: end;
}

label {
  display: grid;
  gap: 7px;
}

label span {
  color: #667085;
  font-size: 13px;
}

select,
input {
  width: 100%;
  border: 1px solid #d0d5dd;
  border-radius: 8px;
  padding: 10px 12px;
  color: #111827;
  background: #fff;
  box-sizing: border-box;
  font-size: 14px;
}

.content-panel {
  padding: 20px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.panel-head h2 {
  margin: 0;
  color: #111827;
  font-size: 18px;
}

.panel-head span {
  color: #667085;
  font-size: 14px;
}

.weak-list {
  margin-top: 14px;
  display: grid;
  gap: 10px;
}

.weak-row {
  width: 100%;
  min-height: 82px;
  border: 1px solid #edf0f5;
  border-radius: 8px;
  padding: 14px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 0.7fr) auto;
  gap: 16px;
  align-items: center;
  background: #fcfcfd;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.weak-row:hover:not(:disabled) {
  border-color: #b8c7dc;
  background: #f8fbff;
}

.weak-row:disabled {
  cursor: wait;
  opacity: 0.7;
}

.row-main {
  min-width: 0;
  display: grid;
  gap: 6px;
}

.row-main strong {
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.row-main span,
.stat-group span {
  color: #667085;
  font-size: 13px;
}

.stat-group {
  display: grid;
  grid-template-columns: repeat(3, minmax(70px, 1fr));
  gap: 10px;
}

.stat-group div {
  display: grid;
  gap: 4px;
}

.stat-group strong {
  color: #111827;
  font-size: 16px;
  font-variant-numeric: tabular-nums;
}

.level-chip {
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

.empty-state {
  margin-top: 14px;
  padding: 28px;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  color: #667085;
  text-align: center;
}

button,
.ghost {
  min-height: 40px;
  border: 1px solid #d0d5dd;
  border-radius: 8px;
  padding: 0 14px;
  background: #fff;
  color: #344054;
  font-size: 14px;
  text-decoration: none;
}

.secondary {
  background: #2563eb;
  border-color: #2563eb;
  color: #fff;
  cursor: pointer;
}

.ghost {
  display: inline-flex;
  align-items: center;
}

@media (max-width: 900px) {
  .filter-panel,
  .weak-row {
    grid-template-columns: 1fr;
  }

  .stat-group {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 560px) {
  .weak-page {
    padding: 20px 14px 36px;
  }

  .page-head h1 {
    font-size: 24px;
  }

  .head-actions,
  .ghost {
    width: 100%;
  }

  .stat-group {
    grid-template-columns: 1fr;
  }
}
</style>
