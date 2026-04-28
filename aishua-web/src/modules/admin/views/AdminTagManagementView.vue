<template>
  <div class="tag-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">考点标签管理</p>
        <h1>管理员考点标签管理</h1>
        <p class="description">
          考点必须绑定学科，题目后续通过考点进行知识点聚合分析。这里可维护考点名称、所属学科与备注信息。
        </p>
      </div>

      <div class="hero-actions">
        <router-link class="ghost" to="/admin">返回管理台</router-link>
        <router-link class="ghost" to="/admin/directories">教材目录管理</router-link>
        <router-link class="ghost" to="/admin/questions">题库管理</router-link>
      </div>
    </section>

    <section class="filter-card">
      <div class="filter-grid">
        <label>
          <span>学科筛选</span>
          <select v-model="filters.subjectId">
            <option value="">全部学科</option>
            <option v-for="subject in subjects" :key="subject.id" :value="String(subject.id)">
              {{ subject.name }}（{{ subject.code }}）
            </option>
          </select>
        </label>

        <label>
          <span>关键字</span>
          <input v-model.trim="filters.keyword" type="text" placeholder="按考点名称或备注搜索" />
        </label>
      </div>

      <div class="filter-actions">
        <button type="button" @click="openCreateForm">新增考点</button>
        <button type="button" class="ghost" @click="resetFilters">重置</button>
        <button type="button" @click="loadTags">查询</button>
      </div>
    </section>

    <section class="table-card">
      <div class="table-head">
        <h2>考点列表</h2>
        <span>{{ tags.length }} 个考点</span>
      </div>

      <div v-if="loading" class="empty-state">正在加载考点数据...</div>
      <div v-else-if="!tags.length" class="empty-state">暂无符合条件的考点</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>考点名称</th>
              <th>所属学科</th>
              <th>备注</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="tag in pagedTags" :key="tag.id">
              <td>{{ tag.name }}</td>
              <td>{{ tag.subjectName || resolveSubjectName(tag.subjectId) }}</td>
              <td>{{ tag.tag || '-' }}</td>
              <td>{{ formatDateTime(tag.updateTime) }}</td>
              <td>
                <div class="row-actions">
                  <button type="button" class="ghost small" @click="openEditForm(tag)">编辑</button>
                  <button type="button" class="danger small" @click="removeTag(tag)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <BasePagination v-if="!loading && tags.length" v-model="currentPage" :total="tags.length" />
    </section>

    <div v-if="modalVisible" class="modal-mask" @click.self="closeModal">
      <section class="modal-card">
        <div class="form-head">
          <h2>{{ editingId ? '编辑考点' : '新增考点' }}</h2>
          <div class="form-actions">
            <button v-if="editingId" type="button" class="ghost small" @click="openCreateForm">切换为新增</button>
            <button type="button" class="ghost small" @click="closeModal">关闭</button>
          </div>
        </div>

        <form class="tag-form" @submit.prevent="submitForm">
          <label>
            <span>所属学科</span>
            <select v-model="form.subjectId">
              <option value="">请选择学科</option>
              <option v-for="subject in subjects" :key="subject.id" :value="String(subject.id)">
                {{ subject.name }}（{{ subject.code }}）
              </option>
            </select>
          </label>

          <label>
            <span>考点名称</span>
            <input v-model.trim="form.name" type="text" maxlength="100" placeholder="例如：函数单调性" />
          </label>

          <label>
            <span>备注</span>
            <textarea
              v-model.trim="form.tag"
              rows="4"
              maxlength="255"
              placeholder="可填：重点难点、常错点、题型方向等"
            ></textarea>
          </label>

          <div class="form-actions">
            <button type="button" class="ghost" @click="resetForm">重置</button>
            <button type="submit" :disabled="submitting">
              {{ submitting ? '提交中...' : editingId ? '保存修改' : '创建考点' }}
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
import subjectApi from '../api/subject'
import tagApi from '../api/tag'

const loading = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const modalVisible = ref(false)
const subjects = ref([])
const tags = ref([])
const { currentPage, pagedItems: pagedTags, resetPage } = useClientPagination(tags)

const filters = reactive({
  subjectId: '',
  keyword: ''
})

const createEmptyForm = (subjectId = filters.subjectId || '') => ({
  subjectId,
  name: '',
  tag: ''
})

const initialForm = ref(createEmptyForm())
const form = reactive(createEmptyForm())

const fillForm = (payload) => {
  form.subjectId = payload.subjectId
  form.name = payload.name
  form.tag = payload.tag
}

const useFormPayload = (payload) => {
  initialForm.value = { ...payload }
  fillForm(payload)
}

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
    tag: row.tag || ''
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
    showToast('请输入考点名称')
    return false
  }
  return true
}

const loadSubjects = async () => {
  try {
    const response = await subjectApi.list()
    subjects.value = response.data || []
  } catch (error) {
    showToast(error.message || '加载学科失败')
  }
}

const loadTags = async () => {
  loading.value = true
  try {
    const response = await tagApi.list({
      subjectId: filters.subjectId ? Number(filters.subjectId) : undefined,
      keyword: filters.keyword || undefined
    })
    tags.value = response.data || []
  } catch (error) {
    showToast(error.message || '加载考点失败')
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
    tag: form.tag
  }

  submitting.value = true
  try {
    if (editingId.value) {
      await tagApi.update(editingId.value, payload)
      showToast('考点更新成功')
    } else {
      await tagApi.create(payload)
      showToast('考点创建成功')
    }
    await loadTags()
    resetCreateForm()
    closeModal()
  } catch (error) {
    showToast(error.message || '保存考点失败')
  } finally {
    submitting.value = false
  }
}

const removeTag = async (row) => {
  if (!window.confirm(`确认删除考点“${row.name}”吗？`)) {
    return
  }
  try {
    await tagApi.remove(row.id)
    showToast('考点删除成功')
    await loadTags()
    if (editingId.value === row.id) {
      resetCreateForm()
      closeModal()
    }
  } catch (error) {
    showToast(error.message || '删除考点失败')
  }
}

const resetFilters = async () => {
  filters.subjectId = ''
  filters.keyword = ''
  resetPage()
  await loadTags()
}

const resolveSubjectName = (subjectId) => {
  const subject = subjects.value.find((item) => item.id === subjectId)
  return subject ? subject.name : `学科ID: ${subjectId}`
}

const formatDateTime = (value) => {
  if (!value) {
    return '-'
  }
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

onMounted(async () => {
  await loadSubjects()
  await loadTags()
  resetCreateForm()
})
</script>

<style scoped>
.tag-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background: linear-gradient(135deg, #f3ecdf 0%, #deebf8 100%);
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
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
  flex: 1;
}

.filter-card label,
.tag-form label {
  display: grid;
  gap: 8px;
  color: #17324d;
  font-weight: 600;
}

.filter-card input,
.filter-card select,
.tag-form input,
.tag-form select,
.tag-form textarea {
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
  min-width: 760px;
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

.empty-state {
  padding: 36px 20px;
  text-align: center;
  color: #6c7a8d;
  background: #f4f8fb;
  border-radius: 18px;
}

.tag-form {
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
  .tag-page {
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
}
</style>
