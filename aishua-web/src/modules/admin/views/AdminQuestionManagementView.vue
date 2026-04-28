<template>
  <div class="question-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">题库管理</p>
        <h1>管理员题库管理</h1>
        <p class="description">
          题目必须绑定学科，可选绑定目录，并支持关联多个考点。这里可以完成题目的新增、编辑、筛选和删除。
        </p>
      </div>

      <div class="hero-actions">
        <router-link class="ghost" to="/admin">返回管理台</router-link>
        <router-link class="ghost" to="/admin/directories">目录树管理</router-link>
        <router-link class="ghost" to="/admin/tags">考点标签管理</router-link>
      </div>
    </section>

    <section class="filter-card">
      <div class="filter-grid">
        <label>
          <span>学科</span>
          <select v-model="filters.subjectId" @change="handleFilterSubjectChange">
            <option value="">全部学科</option>
            <option v-for="subject in subjects" :key="subject.id" :value="String(subject.id)">
              {{ subject.name }}（{{ subject.code }}）
            </option>
          </select>
        </label>

        <label>
          <span>目录</span>
          <select v-model="filters.directoryId">
            <option value="">全部目录</option>
            <option
              v-for="directory in filterDirectoryOptions"
              :key="directory.id"
              :value="String(directory.id)"
            >
              {{ directory.label }}
            </option>
          </select>
        </label>

        <label>
          <span>题型</span>
          <select v-model="filters.type">
            <option value="">全部题型</option>
            <option v-for="item in QUESTION_TYPES" :key="item.value" :value="String(item.value)">
              {{ item.label }}
            </option>
          </select>
        </label>

        <label>
          <span>难度</span>
          <select v-model="filters.difficulty">
            <option value="">全部难度</option>
            <option v-for="item in DIFFICULTY_OPTIONS" :key="item.value" :value="String(item.value)">
              {{ item.label }}
            </option>
          </select>
        </label>

        <label>
          <span>关键字</span>
          <input v-model.trim="filters.keyword" type="text" placeholder="按题干、内容、答案搜索" />
        </label>
      </div>

      <div class="filter-actions">
        <button type="button" @click="openCreateForm">新增题目</button>
        <button type="button" class="ghost" @click="resetFilters">重置</button>
        <button type="button" @click="loadQuestions">查询</button>
      </div>
    </section>

    <section class="table-card">
      <div class="table-head">
        <h2>题目列表</h2>
        <span>{{ questions.length }} 道题</span>
      </div>

      <div v-if="loading" class="empty-state">正在加载题目数据...</div>
      <div v-else-if="!questions.length" class="empty-state">暂无符合条件的题目</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>题干</th>
              <th>学科</th>
              <th>目录</th>
              <th>题型/难度</th>
              <th>考点</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="question in pagedQuestions" :key="question.id">
              <td>
                <div class="title-cell">
                  <strong>{{ question.title }}</strong>
                  <span>答案：{{ question.answer }}</span>
                </div>
              </td>
              <td>{{ question.subjectName || '-' }}</td>
              <td>{{ question.directoryName || '未绑定目录' }}</td>
              <td>
                <span class="meta-chip">{{ resolveTypeLabel(question.type) }}</span>
                <span class="meta-chip ghost-chip">{{ resolveDifficultyLabel(question.difficulty) }}</span>
              </td>
              <td>{{ resolveTagText(question) }}</td>
              <td>{{ formatDateTime(question.updateTime) }}</td>
              <td>
                <div class="row-actions">
                  <button type="button" class="ghost small" @click="openEditForm(question)">编辑</button>
                  <button type="button" class="danger small" @click="removeQuestion(question)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <BasePagination v-if="!loading && questions.length" v-model="currentPage" :total="questions.length" />
    </section>

    <div v-if="modalVisible" class="modal-mask" @click.self="closeModal">
      <section class="modal-card">
        <div class="form-head">
          <h2>{{ editingId ? '编辑题目' : '新增题目' }}</h2>
          <div class="form-actions">
            <button v-if="editingId" type="button" class="ghost small" @click="openCreateForm">切换为新增</button>
            <button type="button" class="ghost small" @click="closeModal">关闭</button>
          </div>
        </div>

        <form class="question-form" @submit.prevent="submitForm">
          <label>
            <span>所属学科</span>
            <select v-model="form.subjectId" @change="handleFormSubjectChange">
              <option value="">请选择学科</option>
              <option v-for="subject in subjects" :key="subject.id" :value="String(subject.id)">
                {{ subject.name }}（{{ subject.code }}）
              </option>
            </select>
          </label>

          <label>
            <span>所属目录</span>
            <select v-model="form.directoryId">
              <option value="">不绑定目录</option>
              <option
                v-for="directory in formDirectoryOptions"
                :key="directory.id"
                :value="String(directory.id)"
              >
                {{ directory.label }}
              </option>
            </select>
          </label>

          <div class="row-grid">
            <label>
              <span>题型</span>
              <select v-model.number="form.type">
                <option v-for="item in QUESTION_TYPES" :key="item.value" :value="item.value">
                  {{ item.label }}
                </option>
              </select>
            </label>

            <label>
              <span>难度</span>
              <select v-model.number="form.difficulty">
                <option v-for="item in DIFFICULTY_OPTIONS" :key="item.value" :value="item.value">
                  {{ item.label }}
                </option>
              </select>
            </label>
          </div>

          <label>
            <span>题干</span>
            <textarea
              v-model.trim="form.title"
              rows="3"
              maxlength="500"
              placeholder="请输入题干内容"
            ></textarea>
          </label>

          <label>
            <span>题目内容</span>
            <textarea
              v-model.trim="form.content"
              rows="4"
              maxlength="20000"
              placeholder="可填更完整的题目描述或上下文"
            ></textarea>
          </label>

          <label>
            <span>选项 JSON（单选/多选必填）</span>
            <textarea
              v-model.trim="form.options"
              rows="4"
              maxlength="10000"
              placeholder='例如：[{"label":"A","value":"选项一"},{"label":"B","value":"选项二"}]'
            ></textarea>
          </label>

          <label>
            <span>标准答案</span>
            <textarea
              v-model.trim="form.answer"
              rows="2"
              maxlength="10000"
              placeholder="请输入标准答案"
            ></textarea>
          </label>

          <label>
            <span>题目解析</span>
            <textarea
              v-model.trim="form.analysis"
              rows="4"
              maxlength="20000"
              placeholder="可填解析说明"
            ></textarea>
          </label>

          <label>
            <span>图片URL（多张可用逗号分隔）</span>
            <input
              v-model.trim="form.imageUrls"
              type="text"
              maxlength="2000"
              placeholder="例如：https://.../1.png,https://.../2.png"
            />
          </label>

          <label>
            <span>图片描述</span>
            <textarea
              v-model.trim="form.imageDesc"
              rows="3"
              maxlength="5000"
              placeholder="可填图片文字说明"
            ></textarea>
          </label>

          <label>
            <span>考点标签（可多选）</span>
            <div class="tag-selector" v-if="formTagOptions.length">
              <label v-for="tag in formTagOptions" :key="tag.id" class="tag-item">
                <input
                  v-model="form.tagIds"
                  type="checkbox"
                  :value="tag.id"
                />
                <span>{{ tag.name }}</span>
              </label>
            </div>
            <p v-else class="field-tip">当前学科下暂无考点标签，可先去“考点标签管理”创建。</p>
          </label>

          <div class="form-actions">
            <button type="button" class="ghost" @click="resetForm">重置</button>
            <button type="submit" :disabled="submitting">
              {{ submitting ? '提交中...' : editingId ? '保存修改' : '创建题目' }}
            </button>
          </div>
        </form>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import BasePagination from '@/components/BasePagination.vue'
import useClientPagination from '@/composables/useClientPagination'
import { showToast } from 'vant'
import directoryApi from '../api/directory'
import questionApi from '../api/question'
import subjectApi from '../api/subject'
import tagApi from '../api/tag'

const QUESTION_TYPES = [
  { value: 1, label: '单选题' },
  { value: 2, label: '多选题' },
  { value: 3, label: '判断题' },
  { value: 4, label: '填空题' },
  { value: 5, label: '简答题' }
]

const DIFFICULTY_OPTIONS = [
  { value: 1, label: '简单' },
  { value: 2, label: '中等' },
  { value: 3, label: '困难' }
]

const loading = ref(false)
const submitting = ref(false)
const modalVisible = ref(false)
const editingId = ref(null)

const subjects = ref([])
const questions = ref([])
const { currentPage, pagedItems: pagedQuestions, resetPage } = useClientPagination(questions)
const filterDirectoryOptions = ref([])
const formDirectoryOptions = ref([])
const formTagOptions = ref([])

const filters = reactive({
  subjectId: '',
  directoryId: '',
  type: '',
  difficulty: '',
  keyword: ''
})

const createEmptyForm = (subjectId = filters.subjectId || '') => ({
  subjectId,
  directoryId: '',
  type: 1,
  difficulty: 1,
  title: '',
  content: '',
  options: '',
  answer: '',
  analysis: '',
  imageUrls: '',
  imageDesc: '',
  tagIds: []
})

const initialForm = ref(createEmptyForm())
const form = reactive(createEmptyForm())

const fillForm = (payload) => {
  form.subjectId = payload.subjectId
  form.directoryId = payload.directoryId
  form.type = payload.type
  form.difficulty = payload.difficulty
  form.title = payload.title
  form.content = payload.content
  form.options = payload.options
  form.answer = payload.answer
  form.analysis = payload.analysis
  form.imageUrls = payload.imageUrls
  form.imageDesc = payload.imageDesc
  form.tagIds = [...payload.tagIds]
}

const useFormPayload = (payload) => {
  initialForm.value = {
    ...payload,
    tagIds: [...payload.tagIds]
  }
  fillForm(initialForm.value)
}

const flattenDirectoryTree = (nodes, level = 0) => {
  const result = []
  for (const node of nodes || []) {
    result.push({
      id: node.id,
      label: `${'　'.repeat(level)}${node.name}`
    })
    if (node.children?.length) {
      result.push(...flattenDirectoryTree(node.children, level + 1))
    }
  }
  return result
}

const loadSubjects = async () => {
  try {
    const response = await subjectApi.list()
    subjects.value = response.data || []
  } catch (error) {
    showToast(error.message || '加载学科失败')
  }
}

const loadFilterDirectories = async (subjectId) => {
  if (!subjectId) {
    filterDirectoryOptions.value = []
    return
  }
  try {
    const response = await directoryApi.listTree(Number(subjectId))
    filterDirectoryOptions.value = flattenDirectoryTree(response.data || [])
  } catch (error) {
    showToast(error.message || '加载目录失败')
    filterDirectoryOptions.value = []
  }
}

const loadFormBindings = async (subjectId) => {
  if (!subjectId) {
    formDirectoryOptions.value = []
    formTagOptions.value = []
    return
  }

  try {
    const [directoryResponse, tagResponse] = await Promise.all([
      directoryApi.listTree(Number(subjectId)),
      tagApi.list({ subjectId: Number(subjectId) })
    ])
    formDirectoryOptions.value = flattenDirectoryTree(directoryResponse.data || [])
    formTagOptions.value = tagResponse.data || []
  } catch (error) {
    showToast(error.message || '加载目录或考点失败')
    formDirectoryOptions.value = []
    formTagOptions.value = []
  }
}

const loadQuestions = async () => {
  loading.value = true
  try {
    const response = await questionApi.list({
      subjectId: filters.subjectId ? Number(filters.subjectId) : undefined,
      directoryId: filters.directoryId ? Number(filters.directoryId) : undefined,
      type: filters.type ? Number(filters.type) : undefined,
      difficulty: filters.difficulty ? Number(filters.difficulty) : undefined,
      keyword: filters.keyword || undefined
    })
    questions.value = response.data || []
  } catch (error) {
    showToast(error.message || '加载题目失败')
  } finally {
    loading.value = false
  }
}

const openCreateForm = async () => {
  editingId.value = null
  if (!subjects.value.length) {
    await loadSubjects()
  }
  const defaultSubjectId = filters.subjectId || (subjects.value[0] ? String(subjects.value[0].id) : '')
  useFormPayload(createEmptyForm(defaultSubjectId))
  await loadFormBindings(form.subjectId)
  modalVisible.value = true
}

const openEditForm = async (question) => {
  useFormPayload({
    subjectId: question.subjectId ? String(question.subjectId) : '',
    directoryId: question.directoryId ? String(question.directoryId) : '',
    type: question.type ?? 1,
    difficulty: question.difficulty ?? 1,
    title: question.title || '',
    content: question.content || '',
    options: question.options || '',
    answer: question.answer || '',
    analysis: question.analysis || '',
    imageUrls: question.imageUrls || '',
    imageDesc: question.imageDesc || '',
    tagIds: (question.tagIds || []).map((id) => Number(id))
  })
  editingId.value = question.id
  await loadFormBindings(form.subjectId)
  modalVisible.value = true
}

const closeModal = () => {
  modalVisible.value = false
}

const resetForm = () => {
  fillForm(initialForm.value)
}

const parseOptionsText = () => {
  const optionsText = form.options?.trim()
  if (!optionsText) {
    return null
  }
  try {
    const parsed = JSON.parse(optionsText)
    if (!Array.isArray(parsed)) {
      showToast('选项JSON必须是数组格式')
      return false
    }
    return optionsText
  } catch (error) {
    showToast('选项JSON格式错误')
    return false
  }
}

const validateForm = () => {
  if (!form.subjectId) {
    showToast('请选择所属学科')
    return false
  }
  if (!form.title) {
    showToast('请输入题干')
    return false
  }
  if (!form.answer) {
    showToast('请输入标准答案')
    return false
  }
  if (![1, 2, 3, 4, 5].includes(form.type)) {
    showToast('题型取值不合法')
    return false
  }
  if (![1, 2, 3].includes(form.difficulty)) {
    showToast('难度取值不合法')
    return false
  }

  const parsedOptions = parseOptionsText()
  if (parsedOptions === false) {
    return false
  }
  if ((form.type === 1 || form.type === 2) && !parsedOptions) {
    showToast('单选/多选题必须填写选项JSON')
    return false
  }
  if (parsedOptions) {
    const parsedArray = JSON.parse(parsedOptions)
    if ((form.type === 1 || form.type === 2) && parsedArray.length < 2) {
      showToast('单选/多选题至少需要 2 个选项')
      return false
    }
  }

  return true
}

const submitForm = async () => {
  if (!validateForm()) {
    return
  }

  const optionsText = form.options?.trim() ? form.options.trim() : null
  const tagIds = [...new Set((form.tagIds || []).map((id) => Number(id)).filter((id) => id > 0))]

  const payload = {
    subjectId: Number(form.subjectId),
    directoryId: form.directoryId ? Number(form.directoryId) : null,
    type: Number(form.type),
    difficulty: Number(form.difficulty),
    title: form.title,
    content: form.content || null,
    options: optionsText,
    answer: form.answer,
    analysis: form.analysis || null,
    imageUrls: form.imageUrls || null,
    imageDesc: form.imageDesc || null,
    tagIds
  }

  submitting.value = true
  try {
    if (editingId.value) {
      await questionApi.update(editingId.value, payload)
      showToast('题目更新成功')
    } else {
      await questionApi.create(payload)
      showToast('题目创建成功')
    }
    await loadQuestions()
    closeModal()
  } catch (error) {
    showToast(error.message || '保存题目失败')
  } finally {
    submitting.value = false
  }
}

const removeQuestion = async (question) => {
  if (!window.confirm(`确认删除题目“${question.title}”吗？`)) {
    return
  }
  try {
    await questionApi.remove(question.id)
    showToast('题目删除成功')
    await loadQuestions()
  } catch (error) {
    showToast(error.message || '删除题目失败')
  }
}

const handleFilterSubjectChange = async () => {
  filters.directoryId = ''
  await loadFilterDirectories(filters.subjectId)
}

const handleFormSubjectChange = async () => {
  form.directoryId = ''
  form.tagIds = []
  await loadFormBindings(form.subjectId)
}

const resetFilters = async () => {
  filters.subjectId = ''
  filters.directoryId = ''
  filters.type = ''
  filters.difficulty = ''
  filters.keyword = ''
  filterDirectoryOptions.value = []
  resetPage()
  await loadQuestions()
}

const resolveTypeLabel = (value) => {
  const item = QUESTION_TYPES.find((type) => type.value === value)
  return item ? item.label : `题型${value}`
}

const resolveDifficultyLabel = (value) => {
  const item = DIFFICULTY_OPTIONS.find((difficulty) => difficulty.value === value)
  return item ? item.label : `难度${value}`
}

const resolveTagText = (question) => {
  const tags = question.tags || []
  if (!tags.length) {
    return '-'
  }
  return tags.map((item) => item.name).join('、')
}

const formatDateTime = (value) => {
  if (!value) {
    return '-'
  }
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

onMounted(async () => {
  await loadSubjects()
  await loadQuestions()
})
</script>

<style scoped>
.question-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background: linear-gradient(135deg, #efe9de 0%, #dcebf7 100%);
}

.hero-card,
.filter-card,
.table-card,
.modal-card {
  background: rgba(255, 255, 255, 0.93);
  border-radius: 28px;
  box-shadow: 0 24px 60px rgba(38, 57, 77, 0.12);
}

.hero-card {
  max-width: 1280px;
  margin: 0 auto;
  padding: 32px;
  display: flex;
  justify-content: space-between;
  gap: 24px;
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
  max-width: 760px;
  margin: 16px 0 0;
  color: #5e6d7c;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-card {
  max-width: 1280px;
  margin: 20px auto 0;
  padding: 24px 28px;
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

.filter-card label,
.question-form label {
  display: grid;
  gap: 8px;
  color: #17324d;
  font-weight: 600;
}

.filter-card input,
.filter-card select,
.question-form input,
.question-form select,
.question-form textarea {
  width: 100%;
  padding: 13px 14px;
  border: 1px solid #cad7e3;
  border-radius: 14px;
  font-size: 14px;
  box-sizing: border-box;
  resize: vertical;
}

.filter-actions,
.form-actions,
.row-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.table-card {
  max-width: 1280px;
  margin: 20px auto 0;
  padding: 24px;
}

.table-head,
.form-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 18px;
}

.table-head h2,
.form-head h2 {
  margin: 0;
  color: #17324d;
  font-size: 24px;
}

.table-head span {
  color: #6c7a8d;
}

.table-wrap {
  overflow: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 980px;
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
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.title-cell {
  display: grid;
  gap: 6px;
}

.title-cell strong {
  color: #17324d;
  line-height: 1.5;
}

.title-cell span {
  color: #5e6d7c;
  font-size: 13px;
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 6px 10px;
  background: #ddf5e9;
  color: #0f7a43;
  font-size: 12px;
  margin-right: 6px;
}

.meta-chip.ghost-chip {
  background: #e8edf6;
  color: #17324d;
}

.empty-state {
  padding: 36px 20px;
  text-align: center;
  color: #6c7a8d;
  background: #f4f8fb;
  border-radius: 18px;
}

.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  padding: 28px 16px;
  background: rgba(19, 30, 45, 0.48);
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-card {
  width: min(860px, 100%);
  max-height: calc(100vh - 40px);
  overflow: auto;
  padding: 24px;
}

.question-form {
  display: grid;
  gap: 16px;
}

.row-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.tag-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 12px;
  border-radius: 14px;
  border: 1px solid #d8e1eb;
  background: #f8fbff;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #17324d;
}

.field-tip {
  margin: 0;
  color: #6c7a8d;
  font-size: 13px;
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

.danger {
  background: #fef0f0;
  color: #b42318;
}

.small {
  padding: 8px 12px;
  border-radius: 12px;
  font-size: 13px;
}

@media (max-width: 768px) {
  .question-page {
    padding: 20px 16px 36px;
  }

  .hero-card,
  .filter-card,
  .table-card,
  .modal-card {
    border-radius: 22px;
    padding: 20px;
  }

  .hero-card h1 {
    font-size: 28px;
  }

  .row-grid {
    grid-template-columns: 1fr;
  }
}
</style>
