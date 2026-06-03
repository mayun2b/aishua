<template>
  <div class="relation-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">目录-考点关系</p>
        <h1>章节与知识点绑定</h1>
        <p class="description">
          维护教材目录和考点标签之间的映射，练习筛选、考试配题和章节知识点展示都会复用这里的数据。
        </p>
      </div>

      <div class="hero-actions">
        <router-link class="ghost" to="/admin/directories">目录管理</router-link>
        <router-link class="ghost" to="/admin/tags">考点标签</router-link>
        <button type="button" @click="openCreateForm">新增关系</button>
      </div>
    </section>

    <section class="filter-card">
      <div class="filter-grid">
        <label>
          <span>学科</span>
          <select v-model="filters.subjectId">
            <option value="">全部学科</option>
            <option v-for="subject in subjects" :key="subject.id" :value="String(subject.id)">
              {{ subject.name }}（{{ subject.code }}）
            </option>
          </select>
        </label>

        <label>
          <span>目录</span>
          <select v-model="filters.directoryId" :disabled="!filters.subjectId">
            <option value="">全部目录</option>
            <option v-for="directory in filterDirectoryOptions" :key="directory.id" :value="String(directory.id)">
              {{ directory.label }}
            </option>
          </select>
        </label>

        <label>
          <span>状态</span>
          <select v-model="filters.isEnabled">
            <option value="">全部状态</option>
            <option value="1">启用</option>
            <option value="0">停用</option>
          </select>
        </label>

        <label>
          <span>关键字</span>
          <input v-model.trim="filters.keyword" type="text" placeholder="目录、考点或备注" />
        </label>
      </div>

      <div class="filter-actions">
        <button type="button" class="ghost" @click="resetFilters">重置</button>
        <button type="button" @click="loadRelations({ resetPage: true })">查询</button>
      </div>
    </section>

    <section v-if="formVisible" class="form-card">
      <div class="form-head">
        <h2>{{ editingId ? '编辑关系' : '新增关系' }}</h2>
        <button type="button" class="ghost small" @click="closeForm">收起</button>
      </div>

      <form class="relation-form" @submit.prevent="submitForm">
        <label>
          <span>学科</span>
          <select v-model="form.subjectId" :disabled="submitting">
            <option value="">请选择学科</option>
            <option v-for="subject in subjects" :key="subject.id" :value="String(subject.id)">
              {{ subject.name }}（{{ subject.code }}）
            </option>
          </select>
        </label>

        <label>
          <span>目录</span>
          <select v-model="form.directoryId" :disabled="!form.subjectId || submitting">
            <option value="">请选择目录</option>
            <option v-for="directory in formDirectoryOptions" :key="directory.id" :value="String(directory.id)">
              {{ directory.label }}
            </option>
          </select>
        </label>

        <label>
          <span>考点</span>
          <select v-model="form.tagId" :disabled="!form.subjectId || submitting">
            <option value="">请选择考点</option>
            <option v-for="tag in formTagOptions" :key="tag.id" :value="String(tag.id)">
              {{ tag.name }}
            </option>
          </select>
        </label>

        <label>
          <span>关系类型</span>
          <select v-model.number="form.relationType" :disabled="submitting">
            <option :value="1">直接关联</option>
            <option :value="2">前置关联</option>
            <option :value="3">拓展关联</option>
          </select>
        </label>

        <label>
          <span>重要程度</span>
          <input v-model.number="form.importanceLevel" type="number" min="1" max="5" :disabled="submitting" />
        </label>

        <label>
          <span>考频</span>
          <input v-model.number="form.examFrequency" type="number" min="1" max="5" :disabled="submitting" />
        </label>

        <label>
          <span>排序</span>
          <input v-model.number="form.sort" type="number" min="0" :disabled="submitting" />
        </label>

        <label>
          <span>状态</span>
          <select v-model.number="form.isEnabled" :disabled="submitting">
            <option :value="1">启用</option>
            <option :value="0">停用</option>
          </select>
        </label>

        <label class="full-line">
          <span>备注</span>
          <textarea v-model.trim="form.remark" maxlength="500" placeholder="可记录章节范围、命题说明或维护原因" :disabled="submitting"></textarea>
        </label>

        <div class="form-actions">
          <button type="button" class="ghost" @click="resetForm">重置</button>
          <button type="submit" :disabled="!canSubmitForm">
            {{ submitting ? '保存中...' : editingId ? '保存修改' : '创建关系' }}
          </button>
        </div>
      </form>
    </section>

    <section class="table-card">
      <div class="table-head">
        <h2>关系列表</h2>
        <span>共 {{ relationTotal }} 条</span>
      </div>

      <div v-if="loading" class="empty-state">正在加载关系数据...</div>
      <div v-else-if="!relations.length" class="empty-state">暂无目录-考点关系。</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>目录</th>
              <th>考点</th>
              <th>学科</th>
              <th>权重</th>
              <th>状态</th>
              <th>备注</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="relation in relations" :key="relation.id">
              <td>
                <div class="cell-stack">
                  <strong>{{ relation.directoryName || `目录 ${relation.directoryId}` }}</strong>
                  <span>ID {{ relation.directoryId }}</span>
                </div>
              </td>
              <td>
                <div class="cell-stack">
                  <strong>{{ relation.tagName || `考点 ${relation.tagId}` }}</strong>
                  <span>{{ relation.tagRemark || '-' }}</span>
                </div>
              </td>
              <td>{{ relation.subjectName || '-' }}</td>
              <td>
                <div class="cell-stack compact">
                  <span>{{ resolveRelationTypeLabel(relation.relationType) }}</span>
                  <span>重要 {{ relation.importanceLevel || 1 }} / 考频 {{ relation.examFrequency || 1 }}</span>
                  <span>排序 {{ relation.sort || 0 }}</span>
                </div>
              </td>
              <td>
                <button
                  type="button"
                  :class="['status-chip', Number(relation.isEnabled) === 1 ? 'enabled' : 'disabled']"
                  @click="toggleEnabled(relation)"
                >
                  {{ Number(relation.isEnabled) === 1 ? '启用' : '停用' }}
                </button>
              </td>
              <td class="remark-cell">{{ relation.remark || '-' }}</td>
              <td>{{ formatDateTime(relation.updateTime) }}</td>
              <td>
                <div class="row-actions">
                  <button type="button" class="ghost small" @click="openEditForm(relation)">编辑</button>
                  <button type="button" class="danger small" @click="removeRelation(relation)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <BasePagination
        v-if="!loading && relationTotal"
        :model-value="relationPage"
        :total="relationTotal"
        :page-size="relationPageSize"
        @update:modelValue="changeRelationPage"
      />
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { showToast } from 'vant'
import BasePagination from '@/components/BasePagination.vue'
import { normalizePageResult } from '@/modules/common/utils/pageResult'
import directoryApi from '../api/directory'
import directoryTagRelationApi from '../api/directoryTagRelation'
import subjectApi from '../api/subject'
import tagApi from '../api/tag'

const loading = ref(false)
const submitting = ref(false)
const formVisible = ref(false)
const editingId = ref(null)

const subjects = ref([])
const relations = ref([])
const relationTotal = ref(0)
const relationPage = ref(1)
const relationPageSize = 10

const filterDirectoryOptions = ref([])
const formDirectoryOptions = ref([])
const formTagOptions = ref([])

const filters = reactive({
  subjectId: '',
  directoryId: '',
  isEnabled: '',
  keyword: ''
})

const createEmptyForm = (subjectId = filters.subjectId || '') => ({
  subjectId,
  directoryId: '',
  tagId: '',
  relationType: 1,
  importanceLevel: 1,
  examFrequency: 1,
  sort: 0,
  isEnabled: 1,
  sourceType: 1,
  remark: ''
})

const initialForm = ref(createEmptyForm())
const form = reactive(createEmptyForm())

const canSubmitForm = computed(() => {
  return !submitting.value
    && Boolean(form.subjectId)
    && Boolean(form.directoryId)
    && Boolean(form.tagId)
    && Number(form.importanceLevel) >= 1
    && Number(form.importanceLevel) <= 5
    && Number(form.examFrequency) >= 1
    && Number(form.examFrequency) <= 5
})

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
    filterDirectoryOptions.value = []
    showToast(error.message || '加载目录失败')
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
    formDirectoryOptions.value = []
    formTagOptions.value = []
    showToast(error.message || '加载目录或考点失败')
  }
}

const loadRelations = async ({ resetPage = false } = {}) => {
  if (resetPage) {
    relationPage.value = 1
  }

  loading.value = true
  try {
    const response = await directoryTagRelationApi.list({
      subjectId: filters.subjectId ? Number(filters.subjectId) : undefined,
      directoryId: filters.directoryId ? Number(filters.directoryId) : undefined,
      isEnabled: filters.isEnabled === '' ? undefined : Number(filters.isEnabled),
      keyword: filters.keyword || undefined,
      pageNum: relationPage.value,
      pageSize: relationPageSize
    })
    const page = normalizePageResult(response.data, {
      pageNum: relationPage.value,
      pageSize: relationPageSize
    })
    relations.value = page.records
    relationTotal.value = page.total
    relationPage.value = page.pageNum
  } catch (error) {
    showToast(error.message || '加载目录-考点关系失败')
  } finally {
    loading.value = false
  }
}

const changeRelationPage = async (page) => {
  if (page === relationPage.value) {
    return
  }
  relationPage.value = page
  await loadRelations()
}

const applyFormPayload = (payload) => {
  form.subjectId = payload.subjectId ? String(payload.subjectId) : ''
  form.directoryId = payload.directoryId ? String(payload.directoryId) : ''
  form.tagId = payload.tagId ? String(payload.tagId) : ''
  form.relationType = Number(payload.relationType ?? 1)
  form.importanceLevel = Number(payload.importanceLevel ?? 1)
  form.examFrequency = Number(payload.examFrequency ?? 1)
  form.sort = Number(payload.sort ?? 0)
  form.isEnabled = Number(payload.isEnabled ?? 1)
  form.sourceType = Number(payload.sourceType ?? 1)
  form.remark = payload.remark || ''
}

const openCreateForm = async () => {
  editingId.value = null
  initialForm.value = createEmptyForm(filters.subjectId || (subjects.value[0] ? String(subjects.value[0].id) : ''))
  applyFormPayload(initialForm.value)
  await loadFormBindings(form.subjectId)
  formVisible.value = true
}

const openEditForm = async (relation) => {
  editingId.value = relation.id
  initialForm.value = {
    subjectId: relation.subjectId,
    directoryId: relation.directoryId,
    tagId: relation.tagId,
    relationType: relation.relationType,
    importanceLevel: relation.importanceLevel,
    examFrequency: relation.examFrequency,
    sort: relation.sort,
    isEnabled: relation.isEnabled,
    sourceType: relation.sourceType,
    remark: relation.remark
  }
  applyFormPayload(initialForm.value)
  await loadFormBindings(form.subjectId)
  formVisible.value = true
}

const closeForm = () => {
  formVisible.value = false
}

const resetForm = async () => {
  applyFormPayload(initialForm.value)
  await loadFormBindings(form.subjectId)
}

const buildPayload = () => ({
  subjectId: Number(form.subjectId),
  directoryId: Number(form.directoryId),
  tagId: Number(form.tagId),
  relationType: Number(form.relationType),
  importanceLevel: Number(form.importanceLevel),
  examFrequency: Number(form.examFrequency),
  sort: Math.max(0, Number(form.sort) || 0),
  isEnabled: Number(form.isEnabled),
  sourceType: Number(form.sourceType || 1),
  remark: form.remark || null
})

const submitForm = async () => {
  if (!canSubmitForm.value) {
    showToast('请完整填写关系信息')
    return
  }

  submitting.value = true
  try {
    const payload = buildPayload()
    if (editingId.value) {
      await directoryTagRelationApi.update(editingId.value, payload)
      showToast('关系已更新')
    } else {
      await directoryTagRelationApi.create(payload)
      showToast('关系已创建')
    }
    formVisible.value = false
    await loadRelations({ resetPage: !editingId.value })
  } catch (error) {
    showToast(error.message || '保存关系失败')
  } finally {
    submitting.value = false
  }
}

const toggleEnabled = async (relation) => {
  const nextEnabled = Number(relation.isEnabled) === 1 ? 0 : 1
  try {
    await directoryTagRelationApi.updateEnabled(relation.id, nextEnabled)
    await loadRelations()
  } catch (error) {
    showToast(error.message || '更新状态失败')
  }
}

const removeRelation = async (relation) => {
  const confirmed = window.confirm(`确定删除「${relation.directoryName || relation.directoryId} - ${relation.tagName || relation.tagId}」关系吗？`)
  if (!confirmed) {
    return
  }

  try {
    await directoryTagRelationApi.remove(relation.id)
    showToast('关系已删除')
    await loadRelations()
  } catch (error) {
    showToast(error.message || '删除关系失败')
  }
}

const resetFilters = async () => {
  filters.subjectId = ''
  filters.directoryId = ''
  filters.isEnabled = ''
  filters.keyword = ''
  filterDirectoryOptions.value = []
  await loadRelations({ resetPage: true })
}

const resolveRelationTypeLabel = (value) => {
  switch (Number(value)) {
    case 2:
      return '前置关联'
    case 3:
      return '拓展关联'
    default:
      return '直接关联'
  }
}

const formatDateTime = (value) => {
  if (!value) {
    return '-'
  }
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

watch(
  () => filters.subjectId,
  async (subjectId) => {
    filters.directoryId = ''
    await loadFilterDirectories(subjectId)
  }
)

watch(
  () => form.subjectId,
  async (subjectId, oldSubjectId) => {
    if (!formVisible.value || subjectId === oldSubjectId) {
      return
    }
    form.directoryId = ''
    form.tagId = ''
    await loadFormBindings(subjectId)
  }
)

onMounted(async () => {
  await loadSubjects()
  await loadRelations()
})
</script>

<style scoped>
.relation-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background: #f4f7fb;
}

.hero-card,
.filter-card,
.form-card,
.table-card {
  max-width: 1180px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 20px;
  box-shadow: 0 20px 48px rgba(28, 54, 84, 0.1);
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
  color: #64758b;
  font-size: 12px;
  letter-spacing: 0.16em;
}

.hero-card h1 {
  margin: 12px 0 0;
  color: #17324d;
  font-size: 32px;
}

.description {
  margin: 12px 0 0;
  max-width: 760px;
  color: #607184;
  line-height: 1.7;
}

.hero-actions,
.filter-actions,
.form-actions,
.row-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.filter-card,
.form-card,
.table-card {
  margin-top: 18px;
  padding: 24px;
}

.filter-card {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  flex-wrap: wrap;
}

.filter-grid,
.relation-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 16px;
  flex: 1;
}

label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: #17324d;
  font-weight: 700;
}

input,
select,
textarea {
  border: 1px solid #d6e1ec;
  border-radius: 12px;
  padding: 10px 12px;
  color: #17324d;
  background: #fff;
}

textarea {
  min-height: 92px;
  resize: vertical;
}

.full-line {
  grid-column: 1 / -1;
}

.form-head,
.table-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.form-head h2,
.table-head h2 {
  margin: 0;
  color: #17324d;
}

.table-head span {
  color: #64758b;
}

.table-wrap {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 980px;
}

th,
td {
  padding: 14px 12px;
  border-bottom: 1px solid #e4ebf3;
  text-align: left;
  vertical-align: top;
}

th {
  color: #64758b;
  font-size: 13px;
  background: #f8fbff;
}

.cell-stack {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.cell-stack span {
  color: #6d7f91;
  font-size: 13px;
}

.cell-stack.compact {
  gap: 3px;
}

.remark-cell {
  max-width: 220px;
  color: #4f6175;
  line-height: 1.6;
}

.empty-state {
  padding: 28px;
  text-align: center;
  color: #718198;
  border: 1px dashed #cfdbe8;
  border-radius: 16px;
  background: #f8fbff;
}

button,
.ghost {
  border: 0;
  border-radius: 999px;
  padding: 10px 16px;
  background: #17324d;
  color: #fff;
  text-decoration: none;
  cursor: pointer;
}

.ghost {
  border: 1px solid #d6e1ec;
  background: #fff;
  color: #17324d;
}

.small {
  padding: 7px 12px;
  font-size: 13px;
}

.danger {
  background: #fff1f2;
  color: #be123c;
}

button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.status-chip {
  padding: 7px 12px;
  border-radius: 999px;
  font-size: 13px;
}

.status-chip.enabled {
  background: #dcfce7;
  color: #166534;
}

.status-chip.disabled {
  background: #eef2f7;
  color: #64748b;
}

@media (max-width: 720px) {
  .relation-page {
    padding: 20px 12px 36px;
  }

  .hero-card {
    padding: 22px;
  }

  .hero-card h1 {
    font-size: 26px;
  }
}
</style>
