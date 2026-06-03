<template>
  <div class="exam-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">试卷管理</p>
        <h1>教师端考试管理（MVP）</h1>
        <p class="description">
          支持试卷模板维护、试卷题目配置、考试记录查询与明细查看。先完成“组卷-发布-查成绩”主链路。
        </p>
      </div>

      <div class="hero-actions">
        <router-link class="ghost" to="/admin">返回管理台</router-link>
        <router-link class="ghost" to="/admin/questions">题库管理</router-link>
        <button type="button" @click="openCreatePaper">新增试卷</button>
      </div>
    </section>

    <section class="filter-card">
      <div class="table-head">
        <h2>试卷筛选</h2>
      </div>
      <div class="filter-grid">
        <label>
          <span>学科</span>
          <select v-model="paperFilters.subjectId">
            <option value="">全部学科</option>
            <option v-for="subject in subjects" :key="subject.id" :value="String(subject.id)">
              {{ subject.name }}（{{ subject.code }}）
            </option>
          </select>
        </label>
        <label>
          <span>状态</span>
          <select v-model="paperFilters.status">
            <option value="">全部状态</option>
            <option value="1">启用</option>
            <option value="0">禁用</option>
          </select>
        </label>
        <label>
          <span>关键字</span>
          <input v-model.trim="paperFilters.keyword" type="text" placeholder="按试卷名称搜索" />
        </label>
      </div>
      <div class="filter-actions">
        <button type="button" class="ghost" @click="resetPaperFilters">重置</button>
        <button type="button" @click="loadPapers">查询</button>
      </div>
    </section>

    <section class="table-card">
      <div class="table-head">
        <h2>试卷列表</h2>
        <span>{{ papers.length }} 张试卷</span>
      </div>

      <div v-if="loadingPapers" class="empty-state">正在加载试卷数据...</div>
      <div v-else-if="!papers.length" class="empty-state">暂无试卷</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>名称</th>
              <th>学科</th>
              <th>题数</th>
              <th>总分</th>
              <th>时长</th>
              <th>状态</th>
              <th>更新时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="paper in pagedPapers" :key="paper.id">
              <td>{{ paper.paperName }}</td>
              <td>{{ paper.subjectName || '-' }}</td>
              <td>{{ paper.totalQuestions || 0 }}</td>
              <td>{{ paper.totalScore || 0 }}</td>
              <td>{{ paper.duration || 0 }} 分钟</td>
              <td>
                <button
                  type="button"
                  class="status-chip"
                  :class="{ disabled: paper.status !== 1 }"
                  @click="togglePaperStatus(paper)"
                >
                  {{ paper.status === 1 ? '启用' : '禁用' }}
                </button>
              </td>
              <td>{{ formatDateTime(paper.updateTime) }}</td>
              <td>
                <div class="row-actions">
                  <button type="button" class="ghost small" @click="openEditPaper(paper)">编辑</button>
                  <button type="button" class="ghost small" @click="openQuestionConfig(paper)">配题</button>
                  <button type="button" class="danger small" @click="removePaper(paper)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <BasePagination v-if="!loadingPapers && papers.length" v-model="paperPage" :total="papers.length" />
    </section>

    <section class="filter-card">
      <div class="table-head">
        <h2>考试记录筛选</h2>
      </div>
      <div class="filter-grid">
        <label>
          <span>学科</span>
          <select v-model="recordFilters.subjectId">
            <option value="">全部学科</option>
            <option v-for="subject in subjects" :key="subject.id" :value="String(subject.id)">
              {{ subject.name }}（{{ subject.code }}）
            </option>
          </select>
        </label>
        <label>
          <span>用户ID</span>
          <input v-model.trim="recordFilters.userId" type="text" placeholder="精确用户ID" />
        </label>
        <label>
          <span>关键字</span>
          <input v-model.trim="recordFilters.keyword" type="text" placeholder="按考试名称搜索" />
        </label>
        <label>
          <span>开始日期</span>
          <input v-model="recordFilters.startDate" type="date" />
        </label>
        <label>
          <span>结束日期</span>
          <input v-model="recordFilters.endDate" type="date" />
        </label>
      </div>
      <div class="filter-actions">
        <button type="button" class="ghost" @click="resetRecordFilters">重置</button>
        <button type="button" @click="loadRecords({ resetPage: true })">查询</button>
      </div>
    </section>

    <section class="table-card">
      <div class="table-head">
        <h2>考试记录</h2>
        <span>{{ recordTotal }} 条记录</span>
      </div>

      <div v-if="loadingRecords" class="empty-state">正在加载考试记录...</div>
      <div v-else-if="!records.length" class="empty-state">暂无考试记录</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>考试名称</th>
              <th>学生</th>
              <th>学科</th>
              <th>分数</th>
              <th>正确题</th>
              <th>时长</th>
              <th>开始时间</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="record in records" :key="record.id">
              <td>{{ record.examName }}</td>
              <td>{{ record.userNickname || record.userPhone || record.userId }}</td>
              <td>{{ record.subjectName || '-' }}</td>
              <td>{{ record.score ?? 0 }}</td>
              <td>{{ record.correctQuestions || 0 }} / {{ record.totalQuestions || 0 }}</td>
              <td>{{ record.duration || 0 }} 分钟</td>
              <td>{{ formatDateTime(record.startTime) }}</td>
              <td>
                <span :class="['meta-chip', record.status === 2 ? 'done-chip' : 'running-chip']">
                  {{ record.status === 2 ? '已完成' : '进行中' }}
                </span>
              </td>
              <td>
                <button type="button" class="ghost small" @click="openRecordDetail(record)">查看明细</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <BasePagination
        v-if="!loadingRecords && recordTotal"
        :model-value="recordPage"
        :total="recordTotal"
        :page-size="recordPageSize"
        @update:modelValue="changeRecordPage"
      />
    </section>

    <div v-if="paperModalVisible" class="modal-mask" @click.self="closePaperModal">
      <section class="modal-card">
        <div class="form-head">
          <h2>{{ editingPaperId ? '编辑试卷' : '新增试卷' }}</h2>
          <button type="button" class="ghost small" @click="closePaperModal">关闭</button>
        </div>
        <form class="paper-form" @submit.prevent="submitPaper">
          <label>
            <span>试卷名称</span>
            <input v-model.trim="paperForm.paperName" type="text" maxlength="100" placeholder="例如：高一数学期中模拟卷" />
          </label>
          <label>
            <span>所属学科</span>
            <select v-model="paperForm.subjectId">
              <option value="">请选择学科</option>
              <option v-for="subject in subjects" :key="subject.id" :value="String(subject.id)">
                {{ subject.name }}（{{ subject.code }}）
              </option>
            </select>
          </label>
          <div class="row-grid">
            <label>
              <span>考试时长（分钟）</span>
              <input v-model.number="paperForm.duration" type="number" min="1" max="300" />
            </label>
            <label>
              <span>目标总分</span>
              <input v-model.number="paperForm.totalScore" type="number" min="1" max="1000" />
            </label>
            <label>
              <span>状态</span>
              <select v-model.number="paperForm.status">
                <option :value="1">启用</option>
                <option :value="0">禁用</option>
              </select>
            </label>
          </div>
          <div class="form-actions">
            <button type="button" class="ghost" @click="resetPaperForm">重置</button>
            <button type="submit" :disabled="submittingPaper">
              {{ submittingPaper ? '提交中...' : editingPaperId ? '保存修改' : '创建试卷' }}
            </button>
          </div>
        </form>
      </section>
    </div>

    <div v-if="recordModalVisible" class="modal-mask" @click.self="closeRecordModal">
      <section class="modal-card large-card">
        <div class="form-head">
          <h2>考试明细</h2>
          <button type="button" class="ghost small" @click="closeRecordModal">关闭</button>
        </div>

        <div v-if="recordDetail" class="record-summary">
          <span>{{ recordDetail.examName }}</span>
          <span>学生：{{ recordDetail.userNickname || recordDetail.userPhone || recordDetail.userId }}</span>
          <span>分数：{{ recordDetail.score ?? 0 }}</span>
          <span>正确：{{ recordDetail.correctQuestions || 0 }} / {{ recordDetail.totalQuestions || 0 }}</span>
          <span>时长：{{ recordDetail.duration || 0 }} 分钟</span>
        </div>

        <div v-if="loadingRecordDetail" class="empty-state">正在加载明细...</div>
        <div v-else-if="!recordQuestions.length" class="empty-state">暂无题目明细</div>
        <div v-else class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>题目ID</th>
                <th>题干</th>
                <th>用户答案</th>
                <th>标准答案</th>
                <th>判定</th>
                <th>耗时(秒)</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in recordQuestions" :key="item.id">
                <td>{{ item.questionId }}</td>
                <td>{{ item.title }}</td>
                <td>{{ item.userAnswer || '-' }}</td>
                <td>{{ item.standardAnswer || '-' }}</td>
                <td>
                  <span :class="['meta-chip', item.isCorrect === 1 ? 'done-chip' : 'danger-chip']">
                    {{ item.isCorrect === 1 ? '正确' : '错误' }}
                  </span>
                </td>
                <td>{{ item.answerTime || 0 }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import BasePagination from '@/components/BasePagination.vue'
import useClientPagination from '@/composables/useClientPagination'
import { normalizePageResult } from '../../common/utils/pageResult'
import examApi from '../api/exam'
import subjectApi from '../api/subject'

const router = useRouter()

const subjects = ref([])

const loadingPapers = ref(false)
const papers = ref([])
const { currentPage: paperPage, pagedItems: pagedPapers, resetPage: resetPaperPage } = useClientPagination(papers)

const loadingRecords = ref(false)
const records = ref([])
const recordTotal = ref(0)
const recordPage = ref(1)
const recordPageSize = 10

const paperFilters = reactive({
  subjectId: '',
  status: '',
  keyword: ''
})

const recordFilters = reactive({
  subjectId: '',
  userId: '',
  keyword: '',
  startDate: '',
  endDate: ''
})

const paperModalVisible = ref(false)
const submittingPaper = ref(false)
const editingPaperId = ref(null)
const paperForm = reactive({
  paperName: '',
  subjectId: '',
  duration: 120,
  totalScore: 100,
  status: 1
})

const recordModalVisible = ref(false)
const loadingRecordDetail = ref(false)
const recordDetail = ref(null)
const recordQuestions = ref([])

const loadSubjects = async () => {
  try {
    const response = await subjectApi.list({ enabled: 1 })
    subjects.value = response.data || []
  } catch (error) {
    showToast(error.message || '加载学科失败')
  }
}

const loadPapers = async () => {
  loadingPapers.value = true
  try {
    const response = await examApi.listPapers({
      subjectId: paperFilters.subjectId ? Number(paperFilters.subjectId) : undefined,
      status: paperFilters.status === '' ? undefined : Number(paperFilters.status),
      keyword: paperFilters.keyword || undefined
    })
    papers.value = response.data || []
    resetPaperPage()
  } catch (error) {
    showToast(error.message || '加载试卷失败')
  } finally {
    loadingPapers.value = false
  }
}

const loadRecords = async ({ resetPage = false } = {}) => {
  if (resetPage) {
    recordPage.value = 1
  }

  loadingRecords.value = true
  try {
    const response = await examApi.listRecords({
      subjectId: recordFilters.subjectId ? Number(recordFilters.subjectId) : undefined,
      userId: recordFilters.userId ? Number(recordFilters.userId) : undefined,
      keyword: recordFilters.keyword || undefined,
      startDate: recordFilters.startDate || undefined,
      endDate: recordFilters.endDate || undefined,
      pageNum: recordPage.value,
      pageSize: recordPageSize
    })
    const page = normalizePageResult(response.data, {
      pageNum: recordPage.value,
      pageSize: recordPageSize
    })
    records.value = page.records
    recordTotal.value = page.total
    recordPage.value = page.pageNum
  } catch (error) {
    showToast(error.message || '加载考试记录失败')
  } finally {
    loadingRecords.value = false
  }
}

const changeRecordPage = async (page) => {
  if (page === recordPage.value) {
    return
  }
  recordPage.value = page
  await loadRecords()
}

const resetPaperFilters = () => {
  paperFilters.subjectId = ''
  paperFilters.status = ''
  paperFilters.keyword = ''
  loadPapers()
}

const resetRecordFilters = () => {
  recordFilters.subjectId = ''
  recordFilters.userId = ''
  recordFilters.keyword = ''
  recordFilters.startDate = ''
  recordFilters.endDate = ''
  loadRecords({ resetPage: true })
}

const fillPaperForm = (payload) => {
  paperForm.paperName = payload.paperName || ''
  paperForm.subjectId = payload.subjectId ? String(payload.subjectId) : ''
  paperForm.duration = payload.duration ?? 120
  paperForm.totalScore = payload.totalScore ?? 100
  paperForm.status = payload.status ?? 1
}

const resetPaperForm = () => {
  fillPaperForm({
    paperName: '',
    subjectId: '',
    duration: 120,
    totalScore: 100,
    status: 1
  })
}

const openCreatePaper = () => {
  editingPaperId.value = null
  resetPaperForm()
  paperModalVisible.value = true
}

const openEditPaper = (paper) => {
  editingPaperId.value = paper.id
  fillPaperForm({
    paperName: paper.paperName,
    subjectId: paper.subjectId,
    duration: paper.duration,
    totalScore: paper.totalScore,
    status: paper.status
  })
  paperModalVisible.value = true
}

const closePaperModal = () => {
  paperModalVisible.value = false
}

const validatePaperForm = () => {
  if (!paperForm.paperName) {
    showToast('请输入试卷名称')
    return false
  }
  if (!paperForm.subjectId) {
    showToast('请选择学科')
    return false
  }
  if (!Number.isInteger(paperForm.duration) || paperForm.duration < 1 || paperForm.duration > 300) {
    showToast('考试时长需在 1 到 300 分钟之间')
    return false
  }
  if (!Number.isInteger(paperForm.totalScore) || paperForm.totalScore < 1 || paperForm.totalScore > 1000) {
    showToast('目标总分需在 1 到 1000 之间')
    return false
  }
  if (paperForm.status !== 0 && paperForm.status !== 1) {
    showToast('状态只能是 0 或 1')
    return false
  }
  return true
}

const submitPaper = async () => {
  if (!validatePaperForm()) {
    return
  }
  submittingPaper.value = true
  try {
    const payload = {
      paperName: paperForm.paperName,
      subjectId: Number(paperForm.subjectId),
      duration: paperForm.duration,
      totalScore: paperForm.totalScore,
      status: paperForm.status
    }
    if (editingPaperId.value) {
      await examApi.updatePaper(editingPaperId.value, payload)
      showToast('试卷更新成功')
    } else {
      await examApi.createPaper(payload)
      showToast('试卷创建成功')
    }
    closePaperModal()
    await loadPapers()
  } catch (error) {
    showToast(error.message || '提交失败')
  } finally {
    submittingPaper.value = false
  }
}

const togglePaperStatus = async (paper) => {
  try {
    await examApi.updatePaperEnabled(paper.id, paper.status === 1 ? 0 : 1)
    showToast('状态已更新')
    await loadPapers()
  } catch (error) {
    showToast(error.message || '状态更新失败')
  }
}

const removePaper = async (paper) => {
  if (!window.confirm(`确认删除试卷“${paper.paperName}”？`)) {
    return
  }
  try {
    await examApi.removePaper(paper.id)
    showToast('试卷已删除')
    await loadPapers()
  } catch (error) {
    showToast(error.message || '删除失败')
  }
}

const openQuestionConfig = async (paper) => {
  const paperId = Number(paper?.id)
  if (!Number.isInteger(paperId) || paperId <= 0) {
    showToast('试卷ID无效，无法进入配题页')
    return
  }
  await router.push({
    name: 'AdminExamQuestionConfig',
    params: { paperId: String(paperId) },
    query: {
      paperName: paper.paperName || '',
      subjectName: paper.subjectName || '',
      subjectId: paper.subjectId ? String(paper.subjectId) : ''
    }
  })
}

const openRecordDetail = async (record) => {
  recordModalVisible.value = true
  loadingRecordDetail.value = true
  recordDetail.value = null
  recordQuestions.value = []
  try {
    const [detailResponse, questionsResponse] = await Promise.all([
      examApi.getRecord(record.id),
      examApi.getRecordQuestions(record.id)
    ])
    recordDetail.value = detailResponse.data || null
    recordQuestions.value = questionsResponse.data || []
  } catch (error) {
    showToast(error.message || '加载考试明细失败')
  } finally {
    loadingRecordDetail.value = false
  }
}

const closeRecordModal = () => {
  recordModalVisible.value = false
  recordDetail.value = null
  recordQuestions.value = []
}

const formatDateTime = (value) => {
  if (!value) {
    return '-'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return date.toLocaleString('zh-CN')
}

onMounted(async () => {
  await loadSubjects()
  await Promise.all([loadPapers(), loadRecords()])
})
</script>

<style scoped>
.exam-page {
  min-height: 100vh;
  padding: 24px;
  background: linear-gradient(135deg, #f4f7ff 0%, #edf5f1 100%);
  display: grid;
  gap: 18px;
}

.hero-card,
.filter-card,
.table-card {
  background: #fff;
  border-radius: 20px;
  padding: 20px 22px;
  box-shadow: 0 10px 30px rgba(25, 47, 89, 0.08);
}

.eyebrow {
  margin: 0;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #5f6e83;
  font-size: 12px;
}

h1,
h2,
h3 {
  margin: 0;
  color: #17324d;
}

.description {
  margin: 10px 0 0;
  color: #5f6e83;
  line-height: 1.6;
}

.hero-actions {
  margin-top: 18px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.hero-actions button,
.hero-actions .ghost,
.filter-actions button,
.row-actions button,
.form-actions button {
  border: 0;
  border-radius: 12px;
  padding: 9px 14px;
  cursor: pointer;
  font-size: 14px;
  text-decoration: none;
}

button {
  background: #17324d;
  color: #fff;
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

.table-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.table-head span {
  color: #6e7d92;
  font-size: 14px;
}

.table-head-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

label {
  display: grid;
  gap: 6px;
}

label > span {
  font-size: 13px;
  color: #5f6e83;
}

input,
select,
textarea {
  border: 1px solid #d7e0ea;
  border-radius: 12px;
  padding: 9px 11px;
  font-size: 14px;
  outline: none;
  background: #fff;
}

textarea {
  resize: vertical;
}

.filter-actions,
.form-actions {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.table-wrap {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 820px;
}

th,
td {
  border-bottom: 1px solid #edf1f6;
  padding: 10px 8px;
  text-align: left;
  font-size: 14px;
  color: #2c3d53;
  vertical-align: top;
}

th {
  color: #5f6e83;
  font-weight: 600;
}

.row-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.status-chip,
.meta-chip {
  border: 0;
  border-radius: 999px;
  padding: 5px 11px;
  font-size: 12px;
  background: #e6f7ff;
  color: #1677ff;
}

.status-chip.disabled,
.running-chip {
  background: #fff7e6;
  color: #d48806;
}

.done-chip {
  background: #f6ffed;
  color: #389e0d;
}

.danger-chip {
  background: #fff1f0;
  color: #cf1322;
}

.empty-state {
  padding: 32px 8px;
  text-align: center;
  color: #75859a;
}

.small-empty {
  padding: 16px 8px;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(16, 32, 50, 0.44);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  z-index: 50;
}

.modal-card {
  width: min(760px, 100%);
  max-height: 88vh;
  overflow: auto;
  border-radius: 18px;
  background: #fff;
  padding: 18px;
}

.large-card {
  width: min(980px, 100%);
}

.form-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.row-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.config-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(0, 1fr);
  gap: 12px;
  margin-top: 10px;
}

.config-panel {
  border: 1px solid #e8eef5;
  border-radius: 14px;
  padding: 12px;
  background: #fbfdff;
}

.selected-panel {
  background: #f8fbff;
}

.mini-filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 8px;
  margin-bottom: 10px;
}

.mini-number {
  width: 88px;
  min-width: 88px;
}

.selected-list {
  margin-top: 6px;
  max-height: 460px;
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

.selected-item-tags .meta-chip {
  background: #edf3fc;
  color: #415a78;
  padding: 4px 10px;
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

.score-input {
  width: 72px;
  min-width: 72px;
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

.sub-table {
  margin-top: 12px;
}

.record-summary {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.record-summary span {
  background: #f5f8fc;
  color: #2c3d53;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 13px;
}

.field-tip {
  color: #5f6e83;
  font-size: 13px;
  margin: 4px 0 10px;
}

@media (max-width: 900px) {
  .exam-page {
    padding: 14px;
  }

  .row-grid {
    grid-template-columns: 1fr;
  }

  .config-grid {
    grid-template-columns: 1fr;
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
