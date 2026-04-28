<template>
  <div class="subject-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">学科管理</p>
        <h1>管理员学科管理</h1>
        <p class="description">
          这里维护学科的名称、编码、排序与启停状态，字段规则与数据库 `subject` 表保持一致。
        </p>
      </div>

      <div class="hero-actions">
        <router-link class="ghost" to="/admin">返回管理台</router-link>
        <router-link class="ghost" to="/admin/directories">目录树管理</router-link>
        <router-link class="ghost" to="/admin/tags">考点标签管理</router-link>
        <router-link class="ghost" to="/admin/questions">题库管理</router-link>
        <button type="button" @click="openCreateForm">新增学科</button>
      </div>
    </section>

    <section class="filter-card">
      <div class="filter-grid">
        <label>
          <span>关键字</span>
          <input v-model.trim="filters.keyword" type="text" placeholder="按学科名称或编码搜索" />
        </label>

        <label>
          <span>状态</span>
          <select v-model="filters.enabled">
            <option value="">全部</option>
            <option value="1">启用</option>
            <option value="0">禁用</option>
          </select>
        </label>
      </div>

      <div class="filter-actions">
        <button type="button" class="ghost" @click="resetFilters">重置</button>
        <button type="button" @click="loadSubjects">查询</button>
      </div>
    </section>

    <section class="table-card">
      <div class="table-head">
        <h2>学科列表</h2>
        <span>{{ subjects.length }} 个学科</span>
      </div>

      <div v-if="loading" class="empty-state">正在加载学科数据...</div>
      <div v-else-if="!subjects.length" class="empty-state">暂无符合条件的学科</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>名称</th>
              <th>编码</th>
              <th>题量</th>
              <th>排序</th>
              <th>状态</th>
              <th>描述</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="subject in pagedSubjects" :key="subject.id">
              <td>
                <div class="subject-name">
                  <strong>{{ subject.name }}</strong>
                  <span>{{ subject.icon || '未设置图标' }}</span>
                </div>
              </td>
              <td>{{ subject.code }}</td>
              <td>{{ subject.questionCount ?? 0 }}</td>
              <td>{{ subject.sort }}</td>
              <td>
                <button
                  type="button"
                  class="status-chip"
                  :class="{ disabled: subject.isEnabled !== 1 }"
                  @click="toggleEnabled(subject)"
                >
                  {{ subject.isEnabled === 1 ? '启用' : '禁用' }}
                </button>
              </td>
              <td class="description-cell">{{ subject.description || '-' }}</td>
              <td>
                <div class="row-actions">
                  <button type="button" class="ghost small" @click="openEditForm(subject)">编辑</button>
                  <button type="button" class="danger small" @click="removeSubject(subject)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <BasePagination v-if="!loading && subjects.length" v-model="currentPage" :total="subjects.length" />
    </section>
  </div>

  <div v-if="modalVisible" class="modal-mask" @click.self="closeModal">
    <section class="modal-card">
      <div class="form-head">
        <h2>{{ editingId ? '编辑学科' : '新增学科' }}</h2>
        <div class="form-actions">
          <button v-if="editingId" type="button" class="ghost small" @click="openCreateForm">切换为新增</button>
          <button type="button" class="ghost small" @click="closeModal">关闭</button>
        </div>
      </div>

      <form class="subject-form" @submit.prevent="submitForm">
        <label>
          <span>学科名称</span>
          <input v-model.trim="form.name" type="text" maxlength="100" placeholder="例如：高中数学" />
        </label>

        <label>
          <span>学科编码</span>
          <input v-model.trim="form.code" type="text" maxlength="50" placeholder="例如：HS_MATH" />
        </label>

        <label>
          <span>图标</span>
          <input v-model.trim="form.icon" type="text" maxlength="255" placeholder="可填图标地址" />
        </label>

        <label>
          <span>排序</span>
          <input v-model.number="form.sort" type="number" min="0" />
        </label>

        <label>
          <span>状态</span>
          <select v-model.number="form.isEnabled">
            <option :value="1">启用</option>
            <option :value="0">禁用</option>
          </select>
        </label>

        <label>
          <span>描述</span>
          <textarea
            v-model.trim="form.description"
            rows="5"
            maxlength="1000"
            placeholder="补充学科说明"
          ></textarea>
        </label>

        <div class="form-actions">
          <button type="button" class="ghost" @click="resetForm">重置</button>
          <button type="submit" :disabled="submitting">
            {{ submitting ? '提交中...' : editingId ? '保存修改' : '创建学科' }}
          </button>
        </div>
      </form>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import BasePagination from '@/components/BasePagination.vue'
import useClientPagination from '@/composables/useClientPagination'
import { showToast } from 'vant'
import subjectApi from '../api/subject'

const loading = ref(false)
const submitting = ref(false)
const subjects = ref([])
const { currentPage, pagedItems: pagedSubjects, resetPage } = useClientPagination(subjects)
const editingId = ref(null)
const modalVisible = ref(false)

const filters = reactive({
  keyword: '',
  enabled: ''
})

const createEmptyForm = () => ({
  name: '',
  code: '',
  description: '',
  icon: '',
  sort: 0,
  isEnabled: 1
})

const form = reactive(createEmptyForm())

const fillForm = (payload) => {
  form.name = payload.name
  form.code = payload.code
  form.description = payload.description
  form.icon = payload.icon
  form.sort = payload.sort
  form.isEnabled = payload.isEnabled
}

const loadSubjects = async () => {
  loading.value = true
  try {
    const response = await subjectApi.list({
      keyword: filters.keyword || undefined,
      enabled: filters.enabled === '' ? undefined : Number(filters.enabled)
    })
    subjects.value = response.data || []
  } catch (error) {
    showToast(error.message || '加载学科失败')
  } finally {
    loading.value = false
  }
}

const openCreateForm = () => {
  editingId.value = null
  fillForm(createEmptyForm())
  modalVisible.value = true
}

const openEditForm = (subject) => {
  editingId.value = subject.id
  fillForm({
    name: subject.name || '',
    code: subject.code || '',
    description: subject.description || '',
    icon: subject.icon || '',
    sort: subject.sort ?? 0,
    isEnabled: subject.isEnabled ?? 1
  })
  modalVisible.value = true
}

const resetForm = () => {
  fillForm(createEmptyForm())
}

const closeModal = () => {
  modalVisible.value = false
}

const validateForm = () => {
  if (!form.name) {
    showToast('请输入学科名称')
    return false
  }
  if (!/^[A-Za-z0-9_-]{2,50}$/.test(form.code)) {
    showToast('学科编码只能包含字母、数字、下划线和短横线')
    return false
  }
  if (!Number.isInteger(form.sort) || form.sort < 0) {
    showToast('排序必须是大于等于 0 的整数')
    return false
  }
  if (form.isEnabled !== 0 && form.isEnabled !== 1) {
    showToast('启用状态只能是 0 或 1')
    return false
  }
  return true
}

const submitForm = async () => {
  if (!validateForm()) {
    return
  }

  submitting.value = true
  try {
    const payload = {
      name: form.name,
      code: form.code,
      description: form.description,
      icon: form.icon,
      sort: form.sort,
      isEnabled: form.isEnabled
    }

    if (editingId.value) {
      await subjectApi.update(editingId.value, payload)
      showToast('学科更新成功')
    } else {
      await subjectApi.create(payload)
      showToast('学科创建成功')
    }

    closeModal()
    await loadSubjects()
  } catch (error) {
    showToast(error.message || '保存学科失败')
  } finally {
    submitting.value = false
  }
}

const toggleEnabled = async (subject) => {
  try {
    const nextEnabled = subject.isEnabled === 1 ? 0 : 1
    await subjectApi.updateEnabled(subject.id, nextEnabled)
    showToast('状态更新成功')
    await loadSubjects()
  } catch (error) {
    showToast(error.message || '更新状态失败')
  }
}

const removeSubject = async (subject) => {
  if (!window.confirm(`确认删除学科“${subject.name}”吗？`)) {
    return
  }

  try {
    await subjectApi.remove(subject.id)
    showToast('学科删除成功')
    if (editingId.value === subject.id) {
      editingId.value = null
      fillForm(createEmptyForm())
      closeModal()
    }
    await loadSubjects()
  } catch (error) {
    showToast(error.message || '删除学科失败')
  }
}

const resetFilters = async () => {
  filters.keyword = ''
  filters.enabled = ''
  resetPage()
  await loadSubjects()
}

onMounted(() => {
  fillForm(createEmptyForm())
  loadSubjects()
})
</script>

<style scoped>
.subject-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background: linear-gradient(135deg, #f2ecff 0%, #dff0e7 100%);
}

.hero-card,
.filter-card,
.table-card,
.modal-card {
  background: rgba(255, 255, 255, 0.92);
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
  max-width: 720px;
  margin: 16px 0 0;
  color: #5e6d7c;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  align-items: flex-start;
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
.subject-form label {
  display: grid;
  gap: 8px;
  color: #17324d;
  font-weight: 600;
}

.filter-card input,
.filter-card select,
.subject-form input,
.subject-form select,
.subject-form textarea {
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

.subject-name {
  display: grid;
  gap: 6px;
}

.subject-name strong {
  color: #17324d;
}

.subject-name span,
.description-cell {
  color: #5e6d7c;
  line-height: 1.6;
}

.empty-state {
  padding: 36px 20px;
  text-align: center;
  color: #6c7a8d;
  background: #f4f8fb;
  border-radius: 18px;
}

.subject-form {
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
  justify-content: center;
  align-items: center;
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

.status-chip {
  padding: 8px 12px;
  border-radius: 999px;
  background: #d7f4e4;
  color: #0f7a43;
}

.status-chip.disabled {
  background: #f6e3e3;
  color: #b42318;
}

@media (max-width: 768px) {
  .subject-page {
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
