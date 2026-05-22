<template>
  <div class="question-config-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">试卷组题</p>
        <h1>{{ pageTitle }}</h1>
        <p class="description">
          学科：{{ paperMeta.subjectName || '-' }} ｜ 已选 {{ editablePaperQuestions.length }} 道 ｜ 当前总分 {{ selectedTotalScore }}
        </p>
      </div>
      <div class="hero-actions">
        <button type="button" class="ghost" @click="goBack">返回试卷列表</button>
        <button type="button" class="ghost" @click="loadCurrentPaperQuestions">重新加载当前配置</button>
        <button type="button" :disabled="submittingQuestions" @click="submitQuestionConfig">
          {{ submittingQuestions ? '保存中...' : '保存题目配置' }}
        </button>
      </div>
    </section>

    <div class="config-grid">
      <section class="config-panel">
        <div class="table-head">
          <h3>题库候选题</h3>
          <span>{{ questionBankTotal }} 道</span>
        </div>
        <div class="mini-filters">
          <select v-model="questionBankFilters.volumeId" @change="onVolumeFilterChange">
            <option value="">选择册</option>
            <option v-for="volume in volumeOptions" :key="volume.id" :value="String(volume.id)">
              {{ volume.name }}
            </option>
          </select>
          <select v-model="questionBankFilters.directoryId" @change="onDirectoryFilterChange">
            <option value="">选择知识点（目录）</option>
            <option v-for="directory in filteredDirectoryOptions" :key="directory.id" :value="String(directory.id)">
              {{ directory.label }}
            </option>
          </select>
          <select v-model="questionBankFilters.tagId" @change="onTagFilterChange">
            <option value="">选择考点（标签）</option>
            <option v-for="tag in tagOptions" :key="tag.tagId" :value="String(tag.tagId)">
              {{ tag.tagName }}（{{ tag.questionCount }}）
            </option>
          </select>
          <input
            v-model.trim="questionBankFilters.keyword"
            type="text"
            placeholder="题目关键字"
            @keyup.enter="searchQuestionBank"
          />
          <select v-model="questionBankFilters.type" @change="onSimpleFilterChange">
            <option value="">题型</option>
            <option value="1">单选</option>
            <option value="2">多选</option>
            <option value="3">判断</option>
            <option value="4">填空</option>
            <option value="5">简答</option>
          </select>
          <select v-model="questionBankFilters.difficulty" @change="onSimpleFilterChange">
            <option value="">难度</option>
            <option value="1">简单</option>
            <option value="2">中等</option>
            <option value="3">困难</option>
          </select>
          <button type="button" class="ghost small" @click="searchQuestionBank">按知识点/考点选题</button>
          <button type="button" class="ghost small" @click="resetQuestionFilters">重置筛选</button>
        </div>

        <div v-if="loadingQuestionBank" class="empty-state small-empty">加载题库中...</div>
        <div v-else-if="!questionBank.length" class="empty-state small-empty">暂无可选题目</div>
        <div v-else class="table-wrap">
          <table class="candidate-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>题干</th>
                <th>题型/难度</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="question in questionBank" :key="question.id">
                <td>{{ question.id }}</td>
                <td>
                  <div class="title-cell" :title="question.title">{{ question.title }}</div>
                </td>
                <td>{{ resolveTypeLabel(question.type) }} / {{ resolveDifficultyLabel(question.difficulty) }}</td>
                <td>
                  <button
                    type="button"
                    class="ghost small"
                    :disabled="isQuestionSelected(question.id)"
                    @click="addQuestionToPaper(question)"
                  >
                    {{ isQuestionSelected(question.id) ? '已加入' : '加入' }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <BasePagination
          v-if="!loadingQuestionBank && questionBankTotal > 0"
          v-model="questionBankPage"
          :total="questionBankTotal"
          :page-size="questionBankPageSize"
        />
      </section>

      <section class="config-panel selected-panel">
        <div class="table-head">
          <h3>已选题目</h3>
          <div class="table-head-actions">
            <span>{{ editablePaperQuestions.length }} 道 / {{ selectedTotalScore }} 分</span>
            <button
              v-if="editablePaperQuestions.length > 1"
              type="button"
              class="ghost small"
              @click="normalizeQuestionOrder"
            >
              一键重排
            </button>
          </div>
        </div>
        <p class="field-tip">右侧顺序固定展示，使用上移/下移调整题目顺序。</p>
        <div v-if="!editablePaperQuestions.length" class="empty-state small-empty">当前试卷未配置题目</div>
        <div v-else class="selected-list">
          <article
            v-for="(item, index) in editablePaperQuestions"
            :key="item.questionId"
            class="selected-item"
          >
            <div class="selected-item-head">
              <div class="selected-item-tags">
                <span class="order-chip">#{{ item.sort }}</span>
                <span class="meta-chip">{{ resolveTypeLabel(item.type) }}</span>
                <span class="meta-chip">{{ resolveDifficultyLabel(item.difficulty) }}</span>
              </div>
              <label class="score-editor" :for="`score-${item.questionId}`">
                <span>分值</span>
                <input
                  :id="`score-${item.questionId}`"
                  v-model.number="item.score"
                  type="number"
                  min="1"
                  class="mini-number score-input"
                />
              </label>
            </div>
            <div class="selected-item-title" :title="item.title">
              {{ item.title }}
            </div>
            <div class="selected-item-foot">
              <span class="question-id">题目ID：{{ item.questionId }}</span>
              <div class="selected-actions">
                <button
                  type="button"
                  class="ghost small"
                  :disabled="index === 0"
                  @click="moveQuestion(item.questionId, -1)"
                >
                  上移
                </button>
                <button
                  type="button"
                  class="ghost small"
                  :disabled="index === editablePaperQuestions.length - 1"
                  @click="moveQuestion(item.questionId, 1)"
                >
                  下移
                </button>
                <button type="button" class="danger small" @click="removeQuestionFromPaper(item.questionId)">移除</button>
              </div>
            </div>
          </article>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import BasePagination from '@/components/BasePagination.vue'
import examApi from '../api/exam'

const QUESTION_TYPE_MAP = {
  1: '单选',
  2: '多选',
  3: '判断',
  4: '填空',
  5: '简答'
}

const DIFFICULTY_MAP = {
  1: '简单',
  2: '中等',
  3: '困难'
}

const route = useRoute()
const router = useRouter()

const paperId = computed(() => Number(route.params.paperId || 0))
const pageTitle = computed(() => {
  if (paperMeta.paperName) {
    return `配置试卷题目：${paperMeta.paperName}`
  }
  return `配置试卷题目：试卷 #${paperId.value || '-'}`
})

const paperMeta = reactive({
  paperName: '',
  subjectName: '-',
  subjectId: null
})

const submittingQuestions = ref(false)
const loadingQuestionBank = ref(false)
const volumeOptions = ref([])
const directoryOptions = ref([])
const tagOptions = ref([])
const questionBankFilters = reactive({
  volumeId: '',
  directoryId: '',
  tagId: '',
  keyword: '',
  type: '',
  difficulty: ''
})
const questionBank = ref([])
const questionBankPage = ref(1)
const questionBankPageSize = 10
const questionBankTotal = ref(0)
const editablePaperQuestions = ref([])
const selectedTotalScore = computed(() => {
  return editablePaperQuestions.value.reduce((sum, item) => {
    const score = Number(item.score)
    return sum + (Number.isInteger(score) && score > 0 ? score : 0)
  }, 0)
})
const ready = ref(false)

const hydratePaperMetaFromRoute = () => {
  paperMeta.paperName = String(route.query.paperName || '').trim()
  paperMeta.subjectName = String(route.query.subjectName || '-').trim() || '-'
  const subjectId = Number(route.query.subjectId || 0)
  paperMeta.subjectId = Number.isInteger(subjectId) && subjectId > 0 ? subjectId : null
}

const loadPaperMeta = async () => {
  try {
    const response = await examApi.listPapers({})
    const target = (response.data || []).find((item) => Number(item.id) === paperId.value)
    if (!target) {
      return
    }
    paperMeta.paperName = target.paperName || paperMeta.paperName
    paperMeta.subjectName = target.subjectName || paperMeta.subjectName
    paperMeta.subjectId = target.subjectId || paperMeta.subjectId
  } catch (error) {
    // ignore paper meta fallback failure
  }
}

const goBack = async () => {
  await router.push('/admin/exams')
}

const loadCurrentPaperQuestions = async () => {
  if (!paperId.value) {
    return
  }
  const response = await examApi.getPaperQuestions(paperId.value)
  editablePaperQuestions.value = (response.data || [])
    .map((item) => ({
      questionId: item.questionId,
      title: item.title,
      type: item.type,
      difficulty: item.difficulty,
      sort: item.sort,
      score: item.score
    }))
    .sort((left, right) => {
      const sortLeft = Number(left.sort || 0)
      const sortRight = Number(right.sort || 0)
      if (sortLeft !== sortRight) {
        return sortLeft - sortRight
      }
      return Number(left.questionId || 0) - Number(right.questionId || 0)
    })
  normalizeQuestionOrder()
}

const flattenDirectoryTree = (nodes, level = 0, volumeId = null, output = []) => {
  if (!Array.isArray(nodes)) {
    return output
  }
  for (const node of nodes) {
    const currentVolumeId = volumeId ?? node.id
    output.push({
      id: node.id,
      label: `${'　'.repeat(level)}${node.name}`,
      volumeId: currentVolumeId
    })
    flattenDirectoryTree(node.children || [], level + 1, currentVolumeId, output)
  }
  return output
}

const getDirectoryOptionsByVolume = (volumeId) => {
  if (!volumeId) {
    return directoryOptions.value
  }
  return directoryOptions.value.filter((item) => String(item.volumeId) === String(volumeId))
}

const filteredDirectoryOptions = computed(() => {
  return getDirectoryOptionsByVolume(questionBankFilters.volumeId)
})

const loadKnowledgeMeta = async () => {
  if (!paperId.value) {
    return
  }
  const directoryResponse = await examApi.getPaperDirectories(paperId.value)
  const directoryTree = directoryResponse.data || []
  volumeOptions.value = directoryTree.map((node) => ({
    id: node.id,
    name: node.name
  }))
  directoryOptions.value = flattenDirectoryTree(directoryTree)
  if (!directoryOptions.value.length || !volumeOptions.value.length) {
    questionBankFilters.volumeId = ''
    questionBankFilters.directoryId = ''
    questionBankFilters.tagId = ''
    tagOptions.value = []
    return
  }

  const currentVolumeId = questionBankFilters.volumeId
    && volumeOptions.value.some((item) => String(item.id) === questionBankFilters.volumeId)
    ? questionBankFilters.volumeId
    : String(volumeOptions.value[0].id)
  questionBankFilters.volumeId = currentVolumeId

  const scopedDirectoryOptions = getDirectoryOptionsByVolume(currentVolumeId)
  const currentDirectoryId = questionBankFilters.directoryId
    && scopedDirectoryOptions.some((item) => String(item.id) === questionBankFilters.directoryId)
    ? questionBankFilters.directoryId
    : (scopedDirectoryOptions.length ? String(scopedDirectoryOptions[0].id) : '')
  questionBankFilters.directoryId = currentDirectoryId
  questionBankFilters.tagId = ''
  if (!currentDirectoryId) {
    tagOptions.value = []
    return
  }
  await loadDirectoryTags(Number(currentDirectoryId))
}

const loadDirectoryTags = async (directoryId) => {
  if (!paperId.value || !directoryId) {
    tagOptions.value = []
    return
  }
  const response = await examApi.getPaperDirectoryTags(paperId.value, directoryId)
  tagOptions.value = response.data || []
}

const refreshQuestionBankByFilters = async ({ reloadTags = false } = {}) => {
  if (!paperId.value) {
    return
  }
  if (reloadTags) {
    const nextDirectoryId = questionBankFilters.directoryId ? Number(questionBankFilters.directoryId) : null
    questionBankFilters.tagId = ''
    await loadDirectoryTags(nextDirectoryId)
  }
  await loadQuestionBank({ resetPage: true, silent: true })
}

const onVolumeFilterChange = async () => {
  const scopedDirectoryOptions = getDirectoryOptionsByVolume(questionBankFilters.volumeId)
  questionBankFilters.directoryId = scopedDirectoryOptions.length ? String(scopedDirectoryOptions[0].id) : ''
  await refreshQuestionBankByFilters({ reloadTags: true })
}

const onDirectoryFilterChange = async () => {
  await refreshQuestionBankByFilters({ reloadTags: true })
}

const onTagFilterChange = async () => {
  await refreshQuestionBankByFilters()
}

const onSimpleFilterChange = async () => {
  await refreshQuestionBankByFilters()
}

const loadQuestionBank = async ({ resetPage = false, silent = false } = {}) => {
  if (!paperId.value) {
    return
  }
  if (resetPage) {
    questionBankPage.value = 1
  }
  loadingQuestionBank.value = true
  try {
    const tagIdsParam = questionBankFilters.tagId || undefined
    const response = await examApi.getAvailablePaperQuestions(paperId.value, {
      directoryId: questionBankFilters.directoryId
        ? Number(questionBankFilters.directoryId)
        : undefined,
      tagIds: tagIdsParam,
      type: questionBankFilters.type ? Number(questionBankFilters.type) : undefined,
      difficulty: questionBankFilters.difficulty ? Number(questionBankFilters.difficulty) : undefined,
      keyword: questionBankFilters.keyword || undefined,
      page: questionBankPage.value,
      pageSize: questionBankPageSize
    })
    const payload = response.data || {}
    questionBank.value = payload.records || []
    questionBankTotal.value = Number(payload.total || 0)
    questionBankPage.value = Number(payload.page || questionBankPage.value)
    if (!silent && !questionBank.value.length) {
      showToast('当前筛选条件下暂无题目')
    }
  } catch (error) {
    showToast(error.message || '加载题库失败')
  } finally {
    loadingQuestionBank.value = false
  }
}

const searchQuestionBank = async () => {
  await loadQuestionBank({ resetPage: true })
}

const resetQuestionFilters = async () => {
  questionBankFilters.volumeId = volumeOptions.value.length ? String(volumeOptions.value[0].id) : ''
  const scopedDirectoryOptions = getDirectoryOptionsByVolume(questionBankFilters.volumeId)
  questionBankFilters.directoryId = scopedDirectoryOptions.length ? String(scopedDirectoryOptions[0].id) : ''
  questionBankFilters.tagId = ''
  questionBankFilters.keyword = ''
  questionBankFilters.type = ''
  questionBankFilters.difficulty = ''
  await loadDirectoryTags(questionBankFilters.directoryId ? Number(questionBankFilters.directoryId) : null)
  await loadQuestionBank({ resetPage: true, silent: true })
}

const normalizeQuestionOrder = () => {
  editablePaperQuestions.value.forEach((item, index) => {
    item.sort = index + 1
  })
}

const isQuestionSelected = (questionId) => {
  return editablePaperQuestions.value.some((item) => item.questionId === questionId)
}

const addQuestionToPaper = (question) => {
  if (isQuestionSelected(question.id)) {
    showToast('题目已在试卷中')
    return
  }

  const score = 5

  editablePaperQuestions.value.push({
    questionId: question.id,
    title: question.title,
    type: question.type,
    difficulty: question.difficulty,
    sort: editablePaperQuestions.value.length + 1,
    score
  })
  normalizeQuestionOrder()
  showToast('题目已加入，可在右侧修改分值')
}

const removeQuestionFromPaper = (questionId) => {
  editablePaperQuestions.value = editablePaperQuestions.value.filter((item) => item.questionId !== questionId)
  normalizeQuestionOrder()
}

const moveQuestion = (questionId, direction) => {
  const currentIndex = editablePaperQuestions.value.findIndex((item) => item.questionId === questionId)
  if (currentIndex < 0) {
    return
  }
  const targetIndex = currentIndex + direction
  if (targetIndex < 0 || targetIndex >= editablePaperQuestions.value.length) {
    return
  }
  const [current] = editablePaperQuestions.value.splice(currentIndex, 1)
  editablePaperQuestions.value.splice(targetIndex, 0, current)
  normalizeQuestionOrder()
}

const buildQuestionPayload = () => {
  if (!editablePaperQuestions.value.length) {
    throw new Error('请至少选择一道题')
  }

  normalizeQuestionOrder()
  const seenQuestionId = new Set()
  const seenSort = new Set()
  return editablePaperQuestions.value.map((item) => ({
      questionId: item.questionId,
      sort: Number(item.sort),
      score: Number(item.score)
    })).map((item) => {
      if (!Number.isInteger(item.questionId) || item.questionId <= 0) {
        throw new Error('存在无效题目ID')
      }
      if (!Number.isInteger(item.sort) || item.sort <= 0) {
        throw new Error('排序必须是正整数')
      }
      if (!seenSort.add(item.sort)) {
        throw new Error('排序存在重复，请点击一键重排后再保存')
      }
      if (!Number.isInteger(item.score) || item.score <= 0) {
        throw new Error('分值必须是正整数')
      }
      if (seenQuestionId.has(item.questionId)) {
        throw new Error('题目存在重复，请检查')
      }
      seenQuestionId.add(item.questionId)
      return item
    })
}

const submitQuestionConfig = async () => {
  if (!paperId.value) {
    showToast('试卷ID无效')
    return
  }
  let normalized
  try {
    normalized = buildQuestionPayload()
  } catch (error) {
    showToast(error.message || '题目配置校验失败')
    return
  }

  submittingQuestions.value = true
  try {
    const response = await examApi.updatePaperQuestions(paperId.value, {
      questions: normalized
    })
    editablePaperQuestions.value = (response.data || []).map((item) => ({
      questionId: item.questionId,
      title: item.title,
      type: item.type,
      difficulty: item.difficulty,
      sort: item.sort,
      score: item.score
    }))
    normalizeQuestionOrder()
    showToast('题目配置保存成功')
  } catch (error) {
    showToast(error.message || '题目配置保存失败')
  } finally {
    submittingQuestions.value = false
  }
}

const resolveTypeLabel = (type) => {
  return QUESTION_TYPE_MAP[type] || `类型${type || '-'}`
}

const resolveDifficultyLabel = (difficulty) => {
  return DIFFICULTY_MAP[difficulty] || `难度${difficulty || '-'}`
}

watch(questionBankPage, async (nextPage, previousPage) => {
  if (!ready.value || nextPage === previousPage) {
    return
  }
  await loadQuestionBank()
})

onMounted(async () => {
  if (!Number.isInteger(paperId.value) || paperId.value <= 0) {
    showToast('试卷ID无效')
    await goBack()
    return
  }

  hydratePaperMetaFromRoute()
  try {
    await Promise.all([
      loadPaperMeta(),
      loadCurrentPaperQuestions(),
      loadKnowledgeMeta()
    ])
    await loadQuestionBank({ resetPage: true, silent: true })
    ready.value = true
  } catch (error) {
    showToast(error.message || '加载配题数据失败')
  }
})
</script>

<style scoped>
.question-config-page {
  min-height: 100vh;
  padding: 24px;
  background: linear-gradient(140deg, #f4f8ff 0%, #eff8f2 100%);
  display: grid;
  gap: 16px;
}

.hero-card {
  background: #fff;
  border-radius: 18px;
  padding: 18px 20px;
  box-shadow: 0 10px 30px rgba(22, 40, 70, 0.08);
}

.eyebrow {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.12em;
  color: #64809f;
}

h1,
h3 {
  margin: 0;
  color: #17324d;
}

.description {
  margin: 8px 0 0;
  color: #5f6e83;
}

.hero-actions {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

button {
  border: 0;
  border-radius: 12px;
  padding: 9px 14px;
  background: #17324d;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

.ghost {
  background: #e9eef5;
  color: #17324d;
}

.danger {
  background: #fff1f0;
  color: #c41d3a;
}

.small {
  padding: 6px 10px;
  font-size: 13px;
}

.config-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(0, 1fr);
  gap: 12px;
  align-items: flex-start;
}

.config-panel {
  border: 1px solid #e5edf8;
  border-radius: 16px;
  padding: 14px;
  background: #fff;
}

.selected-panel {
  position: sticky;
  top: 12px;
  background: #f8fbff;
}

.table-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.table-head h3 {
  font-size: 22px;
}

.table-head span {
  color: #63768d;
}

.table-head-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mini-filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 8px;
  margin-bottom: 10px;
}

input,
select {
  border: 1px solid #d7e0ea;
  border-radius: 12px;
  padding: 9px 11px;
  font-size: 14px;
  outline: none;
  background: #fff;
}

.table-wrap {
  overflow-x: auto;
}

.candidate-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 700px;
}

th,
td {
  border-bottom: 1px solid #edf1f6;
  padding: 10px 8px;
  text-align: left;
  font-size: 14px;
  color: #2c3d53;
}

th {
  color: #5f6e83;
  font-weight: 600;
}

.title-cell {
  word-break: break-word;
  line-height: 1.45;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.empty-state {
  padding: 30px 8px;
  text-align: center;
  color: #75859a;
}

.small-empty {
  padding: 16px 8px;
}

.field-tip {
  color: #5f6e83;
  font-size: 13px;
  margin: 4px 0 10px;
}

.meta-chip {
  border: 0;
  border-radius: 999px;
  padding: 5px 11px;
  font-size: 12px;
  background: #edf3fc;
  color: #415a78;
}

.selected-list {
  margin-top: 6px;
  max-height: 560px;
  overflow: auto;
  padding-right: 4px;
  display: grid;
  gap: 10px;
}

.selected-item {
  border: 1px solid #dfe8f3;
  border-radius: 12px;
  padding: 10px 12px;
  background: #fff;
}

.selected-item-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.selected-item-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  align-items: center;
}

.order-chip {
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  color: #fff;
  background: #29496b;
  font-weight: 600;
}

.score-editor {
  display: flex;
  align-items: center;
  gap: 6px;
}

.score-editor span {
  color: #5f6e83;
  font-size: 12px;
}

.mini-number {
  width: 76px;
  min-width: 76px;
  text-align: center;
}

.selected-item-title {
  margin-top: 8px;
  color: #1f3148;
  line-height: 1.5;
  word-break: break-word;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.selected-item-foot {
  margin-top: 9px;
  border-top: 1px dashed #e5edf6;
  padding-top: 9px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.question-id {
  color: #6e7d92;
  font-size: 13px;
}

.selected-actions {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

@media (max-width: 1080px) {
  .config-grid {
    grid-template-columns: 1fr;
  }

  .selected-panel {
    position: static;
  }
}

@media (max-width: 900px) {
  .question-config-page {
    padding: 14px;
  }

  .mini-filters {
    grid-template-columns: 1fr;
  }

  .selected-item-head {
    flex-direction: column;
    align-items: stretch;
  }

  .score-editor {
    justify-content: space-between;
  }
}
</style>
