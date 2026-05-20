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
        <button type="button" @click="loadRecords">查询</button>
      </div>
    </section>

    <section class="table-card">
      <div class="table-head">
        <h2>考试记录</h2>
        <span>{{ records.length }} 条记录</span>
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
            <tr v-for="record in pagedRecords" :key="record.id">
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
      <BasePagination v-if="!loadingRecords && records.length" v-model="recordPage" :total="records.length" />
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

    <div v-if="questionModalVisible" class="modal-mask" @click.self="closeQuestionModal">
      <section class="modal-card large-card">
        <div class="form-head">
          <h2>配置试卷题目：{{ selectedPaper?.paperName }}</h2>
          <button type="button" class="ghost small" @click="closeQuestionModal">关闭</button>
        </div>

        <p class="field-tip">左侧题库已按试卷学科自动过滤：{{ selectedPaper?.subjectName || '-' }}</p>

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
              <table>
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
                    <td>{{ question.title }}</td>
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

          <section class="config-panel">
            <div class="table-head">
              <h3>已选题目</h3>
              <span>{{ editablePaperQuestions.length }} 道</span>
            </div>
            <div v-if="!editablePaperQuestions.length" class="empty-state small-empty">当前试卷未配置题目</div>
            <div v-else class="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>题干</th>
                    <th>排序</th>
                    <th>分值</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in sortedEditableQuestions" :key="item.questionId">
                    <td>{{ item.questionId }}</td>
                    <td>{{ item.title }}</td>
                    <td>
                      <input
                        v-model.number="item.sort"
                        type="number"
                        min="1"
                        class="mini-number"
                      />
                    </td>
                    <td>
                      <input
                        v-model.number="item.score"
                        type="number"
                        min="1"
                        class="mini-number"
                      />
                    </td>
                    <td>
                      <button type="button" class="danger small" @click="removeQuestionFromPaper(item.questionId)">移除</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
        </div>

        <div class="form-actions">
          <button type="button" class="ghost" @click="loadCurrentPaperQuestions">重新加载当前配置</button>
          <button type="button" :disabled="submittingQuestions" @click="submitQuestionConfig">
            {{ submittingQuestions ? '保存中...' : '保存题目配置' }}
          </button>
        </div>
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
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { showToast } from 'vant'
import BasePagination from '@/components/BasePagination.vue'
import useClientPagination from '@/composables/useClientPagination'
import examApi from '../api/exam'
import subjectApi from '../api/subject'

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

const subjects = ref([])

const loadingPapers = ref(false)
const papers = ref([])
const { currentPage: paperPage, pagedItems: pagedPapers, resetPage: resetPaperPage } = useClientPagination(papers)

const loadingRecords = ref(false)
const records = ref([])
const { currentPage: recordPage, pagedItems: pagedRecords, resetPage: resetRecordPage } = useClientPagination(records)

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

const questionModalVisible = ref(false)
const submittingQuestions = ref(false)
const selectedPaper = ref(null)
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
const sortedEditableQuestions = computed(() => {
  return [...editablePaperQuestions.value].sort((left, right) => {
    const sortLeft = Number(left.sort || 0)
    const sortRight = Number(right.sort || 0)
    if (sortLeft !== sortRight) {
      return sortLeft - sortRight
    }
    return Number(left.questionId || 0) - Number(right.questionId || 0)
  })
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

const loadRecords = async () => {
  loadingRecords.value = true
  try {
    const response = await examApi.listRecords({
      subjectId: recordFilters.subjectId ? Number(recordFilters.subjectId) : undefined,
      userId: recordFilters.userId ? Number(recordFilters.userId) : undefined,
      keyword: recordFilters.keyword || undefined,
      startDate: recordFilters.startDate || undefined,
      endDate: recordFilters.endDate || undefined
    })
    records.value = response.data || []
    resetRecordPage()
  } catch (error) {
    showToast(error.message || '加载考试记录失败')
  } finally {
    loadingRecords.value = false
  }
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
  loadRecords()
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

const loadCurrentPaperQuestions = async () => {
  if (!selectedPaper.value) {
    return
  }
  const response = await examApi.getPaperQuestions(selectedPaper.value.id)
  editablePaperQuestions.value = (response.data || []).map((item) => ({
    questionId: item.questionId,
    title: item.title,
    type: item.type,
    difficulty: item.difficulty,
    sort: item.sort,
    score: item.score
  }))
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
  if (!selectedPaper.value) {
    return
  }
  const directoryResponse = await examApi.getPaperDirectories(selectedPaper.value.id)
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
  if (!selectedPaper.value || !directoryId) {
    tagOptions.value = []
    return
  }
  const response = await examApi.getPaperDirectoryTags(selectedPaper.value.id, directoryId)
  tagOptions.value = response.data || []
}

// 筛选动作统一收口，避免目录/标签/题型/难度各自重复写查询逻辑。
const refreshQuestionBankByFilters = async ({ reloadTags = false } = {}) => {
  if (!selectedPaper.value) {
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
  if (!selectedPaper.value) {
    return
  }
  if (resetPage) {
    questionBankPage.value = 1
  }
  loadingQuestionBank.value = true
  try {
    const tagIdsParam = questionBankFilters.tagId || undefined
    const response = await examApi.getAvailablePaperQuestions(selectedPaper.value.id, {
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

const nextSortValue = () => {
  if (!editablePaperQuestions.value.length) {
    return 1
  }
  return Math.max(...editablePaperQuestions.value.map((item) => Number(item.sort || 0)), 0) + 1
}

const isQuestionSelected = (questionId) => {
  return editablePaperQuestions.value.some((item) => item.questionId === questionId)
}

const addQuestionToPaper = (question) => {
  if (isQuestionSelected(question.id)) {
    showToast('题目已在试卷中')
    return
  }

  // 选入题目时允许教师即时设定分值，避免后续批量回改。
  const rawScore = window.prompt('请输入该题分值（正整数）', '5')
  if (rawScore === null) {
    return
  }
  const score = Number(rawScore)
  if (!Number.isInteger(score) || score <= 0) {
    showToast('分值必须是大于 0 的整数')
    return
  }

  editablePaperQuestions.value.push({
    questionId: question.id,
    title: question.title,
    type: question.type,
    difficulty: question.difficulty,
    sort: nextSortValue(),
    score
  })
}

const removeQuestionFromPaper = (questionId) => {
  editablePaperQuestions.value = editablePaperQuestions.value.filter((item) => item.questionId !== questionId)
}

const buildQuestionPayload = () => {
  if (!editablePaperQuestions.value.length) {
    throw new Error('请至少选择一道题')
  }

  const seen = new Set()
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
      if (!Number.isInteger(item.score) || item.score <= 0) {
        throw new Error('分值必须是正整数')
      }
      if (seen.has(item.questionId)) {
        throw new Error('题目存在重复，请检查')
      }
      seen.add(item.questionId)
      return item
    })
}

const openQuestionConfig = async (paper) => {
  selectedPaper.value = paper
  questionModalVisible.value = true
  questionBankPage.value = 1
  questionBankTotal.value = 0
  questionBankFilters.volumeId = ''
  questionBankFilters.directoryId = ''
  questionBankFilters.tagId = ''
  questionBankFilters.keyword = ''
  questionBankFilters.type = ''
  questionBankFilters.difficulty = ''
  try {
    await Promise.all([loadCurrentPaperQuestions(), loadKnowledgeMeta()])
    await loadQuestionBank({ resetPage: true, silent: true })
  } catch (error) {
    showToast(error.message || '加载配题数据失败')
  }
}

const closeQuestionModal = () => {
  questionModalVisible.value = false
  selectedPaper.value = null
  volumeOptions.value = []
  directoryOptions.value = []
  tagOptions.value = []
  questionBank.value = []
  questionBankPage.value = 1
  questionBankTotal.value = 0
  editablePaperQuestions.value = []
}

const submitQuestionConfig = async () => {
  if (!selectedPaper.value) {
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
    const response = await examApi.updatePaperQuestions(selectedPaper.value.id, {
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
    showToast('题目配置保存成功')
    await loadPapers()
  } catch (error) {
    showToast(error.message || '题目配置保存失败')
  } finally {
    submittingQuestions.value = false
  }
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

const resolveTypeLabel = (type) => {
  return QUESTION_TYPE_MAP[type] || `类型${type || '-'}`
}

const resolveDifficultyLabel = (difficulty) => {
  return DIFFICULTY_MAP[difficulty] || `难度${difficulty || '-'}`
}

watch(questionBankPage, async (nextPage, previousPage) => {
  if (!questionModalVisible.value || nextPage === previousPage) {
    return
  }
  await loadQuestionBank()
})

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
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 10px;
}

.config-panel {
  border: 1px solid #e8eef5;
  border-radius: 14px;
  padding: 12px;
  background: #fbfdff;
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
}
</style>
