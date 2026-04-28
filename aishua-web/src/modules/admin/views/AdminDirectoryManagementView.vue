<template>
  <div class="directory-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">教材目录管理</p>
        <h1>管理员目录树管理</h1>
        <p class="description">
          每个目录都必须绑定学科，并通过父子关系组成目录树。目录是题目挂载的基础结构，请先按学科维护清晰的章节层级。
        </p>
      </div>

      <div class="hero-actions">
        <router-link class="ghost" to="/admin">返回管理台</router-link>
        <router-link class="ghost" to="/admin/tags">考点标签管理</router-link>
        <router-link class="ghost" to="/admin/questions">题库管理</router-link>
      </div>
    </section>

    <section class="filter-card">
      <label class="filter-item">
        <span>学科</span>
        <select v-model="selectedSubjectId" @change="handleSubjectChange">
          <option value="">请选择学科</option>
          <option v-for="subject in subjects" :key="subject.id" :value="String(subject.id)">
            {{ subject.name }}（{{ subject.code }}）
          </option>
        </select>
      </label>

      <div class="filter-actions">
        <button type="button" @click="openCreateForm">新增目录</button>
        <button type="button" class="ghost" @click="loadTree">刷新目录树</button>
      </div>
    </section>

    <section class="table-card">
      <div class="table-head">
        <h2>目录树</h2>
        <span>{{ flatRows.length }} 个目录节点</span>
      </div>

      <div v-if="loading" class="empty-state">正在加载目录数据...</div>
      <div v-else-if="!selectedSubjectId" class="empty-state">请先选择学科</div>
      <div v-else-if="!flatRows.length" class="empty-state">该学科下暂无目录，请先新增</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>目录名称</th>
              <th>父级ID</th>
              <th>排序</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in pagedRows" :key="row.id">
              <td>
                <div class="tree-name" :style="{ paddingLeft: `${row.level * 24 + 12}px` }">
                  <span class="tree-marker">{{ row.level === 0 ? '根' : '子' }}</span>
                  <strong>{{ row.name }}</strong>
                </div>
              </td>
              <td>{{ row.parentId || 0 }}</td>
              <td>{{ row.sort }}</td>
              <td>{{ formatDateTime(row.updateTime) }}</td>
              <td>
                <div class="row-actions">
                  <button type="button" class="ghost small" @click="openEditForm(row)">编辑</button>
                  <button type="button" class="danger small" @click="removeDirectory(row)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <BasePagination
        v-if="!loading && selectedSubjectId && flatRows.length"
        v-model="currentPage"
        :total="flatRows.length"
      />
    </section>

    <div v-if="modalVisible" class="modal-mask" @click.self="closeModal">
      <section class="modal-card">
        <div class="form-head">
          <h2>{{ editingId ? '编辑目录' : '新增目录' }}</h2>
          <div class="form-actions">
            <button v-if="editingId" type="button" class="ghost small" @click="openCreateForm">切换为新增</button>
            <button type="button" class="ghost small" @click="closeModal">关闭</button>
          </div>
        </div>

        <form class="directory-form" @submit.prevent="submitForm">
          <label>
            <span>所属学科</span>
            <select v-model="form.subjectId" :disabled="Boolean(editingId)">
              <option value="">请选择学科</option>
              <option v-for="subject in subjects" :key="subject.id" :value="String(subject.id)">
                {{ subject.name }}（{{ subject.code }}）
              </option>
            </select>
          </label>

          <label>
            <span>目录名称</span>
            <input v-model.trim="form.name" type="text" maxlength="100" placeholder="例如：必修一 / 函数概念" />
          </label>

          <label>
            <span>父目录</span>
            <select v-model="form.parentId">
              <option value="0">顶级目录</option>
              <option
                v-for="option in parentOptions"
                :key="option.id"
                :value="String(option.id)"
              >
                {{ option.label }}
              </option>
            </select>
          </label>

          <label>
            <span>排序</span>
            <input v-model.number="form.sort" type="number" min="0" />
          </label>

          <div class="form-actions">
            <button type="button" class="ghost" @click="resetForm">重置</button>
            <button type="submit" :disabled="submitting">
              {{ submitting ? '提交中...' : editingId ? '保存修改' : '创建目录' }}
            </button>
          </div>
        </form>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import BasePagination from '@/components/BasePagination.vue'
import useClientPagination from '@/composables/useClientPagination'
import { showToast } from 'vant'
import directoryApi from '../api/directory'
import subjectApi from '../api/subject'

const loading = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const modalVisible = ref(false)
const selectedSubjectId = ref('')
const subjects = ref([])
const tree = ref([])

const createEmptyForm = (subjectId = selectedSubjectId.value || '') => ({
  subjectId,
  name: '',
  parentId: '0',
  sort: 0
})

const initialForm = ref(createEmptyForm())
const form = reactive(createEmptyForm())

const fillForm = (payload) => {
  form.subjectId = payload.subjectId
  form.name = payload.name
  form.parentId = payload.parentId
  form.sort = payload.sort
}

const useFormPayload = (payload) => {
  initialForm.value = { ...payload }
  fillForm(payload)
}

const flatRows = computed(() => {
  const rows = []
  const visit = (nodes, level) => {
    for (const node of nodes) {
      rows.push({ ...node, level })
      if (node.children?.length) {
        visit(node.children, level + 1)
      }
    }
  }
  visit(tree.value || [], 0)
  return rows
})
const { currentPage, pagedItems: pagedRows, resetPage } = useClientPagination(flatRows)

const descendantsMap = computed(() => {
  const map = new Map()
  for (const row of flatRows.value) {
    const parentKey = row.parentId || 0
    if (!map.has(parentKey)) {
      map.set(parentKey, [])
    }
    map.get(parentKey).push(row.id)
  }
  return map
})

const parentOptions = computed(() => {
  const blocked = new Set()
  if (editingId.value) {
    blocked.add(editingId.value)
    const queue = [editingId.value]
    while (queue.length) {
      const current = queue.shift()
      const children = descendantsMap.value.get(current) || []
      for (const childId of children) {
        if (!blocked.has(childId)) {
          blocked.add(childId)
          queue.push(childId)
        }
      }
    }
  }

  return flatRows.value
    .filter((row) => !blocked.has(row.id))
    .map((row) => ({
      id: row.id,
      label: `${'　'.repeat(row.level)}${row.name}`
    }))
})

const openCreateForm = () => {
  editingId.value = null
  useFormPayload(createEmptyForm())
  modalVisible.value = true
}

const openEditForm = (row) => {
  editingId.value = row.id
  useFormPayload({
    subjectId: String(row.subjectId),
    name: row.name || '',
    parentId: String(row.parentId || 0),
    sort: row.sort ?? 0
  })
  modalVisible.value = true
}

const resetForm = () => {
  fillForm(initialForm.value)
}

const resetCreateForm = () => {
  editingId.value = null
  useFormPayload(createEmptyForm())
}

const closeModal = () => {
  modalVisible.value = false
}

const validateForm = () => {
  if (!form.subjectId) {
    showToast('请选择所属学科')
    return false
  }
  if (!form.name) {
    showToast('请输入目录名称')
    return false
  }
  if (!Number.isInteger(form.sort) || form.sort < 0) {
    showToast('排序必须是大于等于 0 的整数')
    return false
  }
  return true
}

const loadSubjects = async () => {
  try {
    const response = await subjectApi.list()
    subjects.value = response.data || []
    if (!selectedSubjectId.value && subjects.value.length) {
      selectedSubjectId.value = String(subjects.value[0].id)
    }
  } catch (error) {
    showToast(error.message || '加载学科失败')
  }
}

const loadTree = async () => {
  if (!selectedSubjectId.value) {
    tree.value = []
    return
  }

  loading.value = true
  try {
    const response = await directoryApi.listTree(Number(selectedSubjectId.value))
    tree.value = response.data || []

    // 如果当前编辑节点在新列表中已不存在，自动切回新增模式
    if (editingId.value && !flatRows.value.some((row) => row.id === editingId.value)) {
      openCreateForm()
    }
  } catch (error) {
    showToast(error.message || '加载目录树失败')
  } finally {
    loading.value = false
  }
}

const submitForm = async () => {
  if (!validateForm()) {
    return
  }

  const payload = {
    subjectId: Number(form.subjectId),
    name: form.name,
    parentId: Number(form.parentId || 0),
    sort: form.sort
  }

  submitting.value = true
  try {
    if (editingId.value) {
      await directoryApi.update(editingId.value, payload)
      showToast('目录更新成功')
    } else {
      await directoryApi.create(payload)
      showToast('目录创建成功')
    }
    await loadTree()
    resetCreateForm()
    closeModal()
  } catch (error) {
    showToast(error.message || '保存目录失败')
  } finally {
    submitting.value = false
  }
}

const removeDirectory = async (row) => {
  if (!window.confirm(`确认删除目录“${row.name}”吗？`)) {
    return
  }
  try {
    await directoryApi.remove(row.id)
    showToast('目录删除成功')
    await loadTree()
    if (editingId.value === row.id) {
      resetCreateForm()
      closeModal()
    }
  } catch (error) {
    showToast(error.message || '删除目录失败')
  }
}

const handleSubjectChange = async () => {
  resetPage()
  await loadTree()
  resetCreateForm()
  closeModal()
}

const formatDateTime = (value) => {
  if (!value) {
    return '-'
  }
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

onMounted(async () => {
  await loadSubjects()
  await loadTree()
  resetCreateForm()
})
</script>

<style scoped>
.directory-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background: linear-gradient(135deg, #efe7f9 0%, #e1f0e8 100%);
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
  padding: 22px 28px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-item,
.directory-form label {
  display: grid;
  gap: 8px;
  color: #17324d;
  font-weight: 600;
}

.filter-item {
  min-width: 320px;
}

.filter-item select,
.directory-form input,
.directory-form select {
  width: 100%;
  padding: 13px 14px;
  border: 1px solid #cad7e3;
  border-radius: 14px;
  font-size: 14px;
  box-sizing: border-box;
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
  align-items: center;
  gap: 12px;
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
  min-width: 720px;
  border-collapse: collapse;
}

th,
td {
  padding: 14px 12px;
  border-bottom: 1px solid #e5edf5;
  text-align: left;
}

th {
  color: #6c7a8d;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.tree-name {
  display: flex;
  gap: 8px;
  align-items: center;
}

.tree-marker {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 34px;
  border-radius: 999px;
  font-size: 12px;
  background: #e6eef8;
  color: #17324d;
  padding: 3px 8px;
}

.tree-name strong {
  color: #17324d;
}

.empty-state {
  padding: 34px 18px;
  text-align: center;
  background: #f4f8fb;
  border-radius: 18px;
  color: #6c7a8d;
}

.directory-form {
  display: grid;
  gap: 16px;
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
  width: min(680px, 100%);
  max-height: calc(100vh - 40px);
  overflow: auto;
  padding: 24px;
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
  .directory-page {
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

  .filter-item {
    min-width: 100%;
  }
}
</style>
