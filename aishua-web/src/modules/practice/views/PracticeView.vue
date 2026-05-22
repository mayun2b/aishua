<template>
  <div class="practice-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">Practice Center</p>
        <h1>先配置，再进入独立作答页</h1>
        <p class="description">
          在这里选择学科、练习模式和题量。点击“开始练习”后会跳转到独立答题页面，避免配置页和作答页混在一起。
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
          <select v-model="selectedSubjectId" :disabled="starting">
            <option v-for="subject in subjects" :key="subject.subjectId" :value="String(subject.subjectId)">
              {{ subject.name }}（{{ subject.code }}）
            </option>
          </select>
        </label>

        <label>
          <span>模式</span>
          <select v-model.number="practiceMode" :disabled="starting">
            <option :value="1">顺序练习</option>
            <option :value="2">随机练习</option>
            <option :value="3">知识点练习</option>
            <option :value="4">错题重练</option>
          </select>
        </label>

        <label>
          <span>题量</span>
          <input v-model.number="questionCount" type="number" min="1" max="50" :disabled="starting" />
        </label>

        <div v-if="isKnowledgeMode" class="tag-picker">
          <div class="tag-picker-head">
            <span>知识点</span>
            <strong>已选 {{ selectedTagIds.length }} / {{ knowledgeTags.length }}</strong>
          </div>

          <div v-if="loadingTags" class="tag-state">正在加载知识点...</div>
          <div v-else-if="!knowledgeTags.length" class="tag-state">当前学科暂无可选知识点</div>
          <div v-else class="tag-picker-body">
            <div class="tag-filter-row">
              <input
                v-model.trim="tagKeyword"
                type="search"
                maxlength="60"
                placeholder="搜索知识点名称或备注"
                :disabled="starting"
              />
              <button type="button" class="secondary-button small-button" :disabled="!visibleKnowledgeTags.length || starting" @click="selectVisibleTags">
                选中当前
              </button>
              <button type="button" class="secondary-button small-button" :disabled="!selectedTagIds.length || starting" @click="clearSelectedTags">
                清空
              </button>
            </div>

            <div v-if="!visibleKnowledgeTags.length" class="tag-state compact">没有匹配的知识点</div>
            <div v-else class="tag-options">
              <label
                v-for="tag in visibleKnowledgeTags"
                :key="tag.id"
                :class="['tag-option', selectedTagIds.includes(tag.id) ? 'active' : '']"
              >
                <input v-model="selectedTagIds" type="checkbox" :value="tag.id" :disabled="starting" />
                <span>{{ tag.name }}</span>
                <small v-if="tag.tag">{{ tag.tag }}</small>
              </label>
            </div>
          </div>
        </div>
      </div>

      <div v-if="subjects.length" class="config-actions">
        <button type="button" :disabled="!canStartPractice" @click="startPractice">
          {{ starting ? '正在开始...' : '开始练习' }}
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

const loadingSubjects = ref(false)
const loadingTags = ref(false)
const starting = ref(false)

const subjects = ref([])
const knowledgeTags = ref([])
const selectedTagIds = ref([])
const tagKeyword = ref('')
const selectedSubjectId = ref('')
const practiceMode = ref(1)
const questionCount = ref(10)

const isKnowledgeMode = computed(() => Number(practiceMode.value) === 3)
const visibleKnowledgeTags = computed(() => {
  const keyword = tagKeyword.value.trim().toLowerCase()
  if (!keyword) {
    return knowledgeTags.value
  }
  return knowledgeTags.value.filter((tag) => {
    return String(tag.name || '').toLowerCase().includes(keyword)
      || String(tag.tag || '').toLowerCase().includes(keyword)
  })
})

const normalizedQuestionCount = computed(() => {
  const count = Number(questionCount.value)
  if (Number.isNaN(count)) {
    return 0
  }
  return Math.min(50, Math.max(1, Math.floor(count)))
})

const canStartPractice = computed(() => {
  return !starting.value
    && Boolean(selectedSubjectId.value)
    && normalizedQuestionCount.value >= 1
    && normalizedQuestionCount.value <= 50
    && (!isKnowledgeMode.value || (!loadingTags.value && selectedTagIds.value.length > 0))
})

const resolveRoutePracticeMode = () => {
  const routeMode = Number(route.query.mode)
  if ([1, 2, 3, 4].includes(routeMode)) {
    return routeMode
  }
  return 1
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

    practiceMode.value = resolveRoutePracticeMode()
  } catch (error) {
    showToast(error.message || '加载学科失败')
  } finally {
    loadingSubjects.value = false
  }
}

const loadKnowledgeTags = async () => {
  if (!selectedSubjectId.value || !isKnowledgeMode.value) {
    knowledgeTags.value = []
    selectedTagIds.value = []
    tagKeyword.value = ''
    return
  }

  loadingTags.value = true
  try {
    const response = await practiceApi.listTags({
      subjectId: Number(selectedSubjectId.value)
    })

    const tags = response.data || []
    const tagIdSet = new Set(tags.map((tag) => tag.id))

    knowledgeTags.value = tags
    selectedTagIds.value = selectedTagIds.value.filter((tagId) => tagIdSet.has(tagId))
  } catch (error) {
    knowledgeTags.value = []
    selectedTagIds.value = []
    showToast(error.message || '加载知识点失败')
  } finally {
    loadingTags.value = false
  }
}

const selectVisibleTags = () => {
  const mergedTagIds = new Set(selectedTagIds.value)
  visibleKnowledgeTags.value.forEach((tag) => mergedTagIds.add(tag.id))
  selectedTagIds.value = Array.from(mergedTagIds)
}

const clearSelectedTags = () => {
  selectedTagIds.value = []
}

const startPractice = async () => {
  if (!selectedSubjectId.value) {
    showToast('请先选择学科')
    return
  }

  if (isKnowledgeMode.value && !selectedTagIds.value.length) {
    showToast('请至少选择一个知识点')
    return
  }

  starting.value = true
  try {
    const payload = {
      subjectId: Number(selectedSubjectId.value),
      practiceMode: Number(practiceMode.value),
      questionCount: normalizedQuestionCount.value
    }

    if (isKnowledgeMode.value) {
      payload.tagIds = selectedTagIds.value.map(Number)
    }

    const response = await practiceApi.start(payload)
    const sessionId = Number(response?.data?.sessionId || 0)
    if (!sessionId) {
      throw new Error('创建练习会话失败')
    }

    await router.push({
      name: 'PracticeSession',
      params: { sessionId },
      query: {
        subjectId: selectedSubjectId.value,
        mode: String(practiceMode.value)
      }
    })
  } catch (error) {
    showToast(error.message || '开始练习失败')
  } finally {
    starting.value = false
  }
}

watch(
  [selectedSubjectId, practiceMode],
  async () => {
    if (isKnowledgeMode.value) {
      await loadKnowledgeTags()
      return
    }
    knowledgeTags.value = []
    selectedTagIds.value = []
    tagKeyword.value = ''
  }
)

watch(
  () => route.query.subjectId,
  (subjectId) => {
    if (subjectId && subjects.value.some((subject) => String(subject.subjectId) === String(subjectId))) {
      selectedSubjectId.value = String(subjectId)
    }
  }
)

watch(
  () => route.query.mode,
  (mode) => {
    if (mode != null) {
      practiceMode.value = resolveRoutePracticeMode()
    }
  }
)

onMounted(() => {
  loadSubjects()
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
.config-card {
  max-width: 1180px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 28px;
  box-shadow: 0 24px 60px rgba(38, 57, 77, 0.12);
}

.hero-card,
.config-card {
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
.config-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.config-card {
  margin-top: 18px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.section-head h2 {
  margin: 0;
  color: #17324d;
  font-size: 24px;
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
.config-grid input {
  width: 100%;
  padding: 13px 14px;
  border: 1px solid #cad7e3;
  border-radius: 14px;
  font-size: 14px;
  box-sizing: border-box;
  background: #fff;
}

.tag-picker {
  grid-column: 1 / -1;
  display: grid;
  gap: 12px;
}

.tag-picker-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  color: #17324d;
  font-weight: 600;
}

.tag-picker-head strong {
  color: #6c7a8d;
  font-size: 13px;
}

.tag-picker-body {
  display: grid;
  gap: 10px;
}

.tag-filter-row {
  display: grid;
  grid-template-columns: minmax(180px, 1fr) auto auto;
  gap: 10px;
  align-items: center;
}

.tag-filter-row input {
  width: 100%;
  padding: 11px 12px;
  border: 1px solid #cad7e3;
  border-radius: 14px;
  box-sizing: border-box;
  font-size: 14px;
}

.tag-options {
  max-height: 260px;
  overflow-y: auto;
  padding-right: 4px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 10px;
}

.tag-option {
  min-height: 58px;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 6px 10px;
  align-items: center;
  padding: 12px;
  border: 1px solid #d9e3ee;
  border-radius: 14px;
  background: #f8fbff;
  color: #17324d;
  cursor: pointer;
}

.tag-option.active {
  border-color: #78b998;
  background: #ecf7f1;
}

.tag-option input {
  width: auto;
  padding: 0;
  border: 0;
  border-radius: 0;
  accent-color: #0f7a43;
}

.tag-option small {
  grid-column: 2;
  min-width: 0;
  color: #6c7a8d;
  font-weight: 400;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tag-state {
  padding: 18px;
  border-radius: 14px;
  background: #f3f7fb;
  color: #6c7a8d;
}

.tag-state.compact {
  padding: 14px;
}

.small-button {
  padding: 8px 12px;
  font-size: 13px;
}

.config-actions {
  margin-top: 18px;
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

@media (max-width: 768px) {
  .practice-page {
    padding: 18px 14px 30px;
  }

  .tag-filter-row {
    grid-template-columns: 1fr;
  }

  .hero-card,
  .config-card {
    border-radius: 22px;
    padding: 20px;
  }

  .hero-card h1 {
    font-size: 28px;
  }
}
</style>
