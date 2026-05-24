<template>
  <div class="wrong-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">Wrong Questions</p>
        <h1>错题记录</h1>
        <p class="description">
          这里集中展示你在练习中答错过的题目，方便按学科回顾、定位薄弱点并继续练习。
        </p>
      </div>

      <div class="hero-actions">
        <router-link class="ghost" to="/dashboard">返回工作台</router-link>
        <router-link class="ghost" to="/practice-records">查看练习记录</router-link>
      </div>
    </section>

    <section class="filter-card">
      <div class="filter-grid">
        <label>
          <span>学科</span>
          <select v-model="filters.subjectId">
            <option value="">全部学科</option>
            <option v-for="subject in subjects" :key="subject.subjectId" :value="String(subject.subjectId)">
              {{ subject.name }}
            </option>
          </select>
        </label>

        <label>
          <span>目录</span>
          <select v-model="filters.directoryId">
            <option value="">全部目录</option>
            <option v-for="directory in directoryOptions" :key="directory.directoryId" :value="String(directory.directoryId)">
              {{ directory.directoryName }}
            </option>
          </select>
        </label>

        <label>
          <span>掌握状态</span>
          <select v-model="filters.masterStatus">
            <option value="">全部状态</option>
            <option value="0">未掌握</option>
            <option value="1">已掌握</option>
          </select>
        </label>
      </div>

      <div class="filter-actions">
        <button type="button" class="ghost" @click="resetFilters">重置</button>
        <button type="button" @click="applyFilters">查询</button>
      </div>
    </section>

    <section class="table-card">
      <div class="table-head">
        <h2>错题变化趋势</h2>
        <div class="trend-actions">
          <button
            v-for="option in trendDayOptions"
            :key="option"
            type="button"
            :class="['ghost', selectedTrendDays === option ? 'active-trend-button' : '']"
            @click="changeTrendDays(option)"
          >
            近 {{ option }} 天
          </button>
        </div>
      </div>

      <div v-if="trendLoading" class="empty-state">正在加载趋势数据...</div>
      <div v-else-if="!wrongTrendItems.length" class="empty-state">暂无趋势数据。</div>
      <div v-else class="trend-chart">
        <div
          v-for="(item, index) in wrongTrendItems"
          :key="item.statDate || index"
          class="trend-column"
          :title="`${formatDate(item.statDate)}：错答 ${item.wrongAnswerCount || 0} 次，涉及 ${item.uniqueWrongQuestionCount || 0} 题`"
        >
          <div class="trend-track">
            <span class="trend-fill" :style="{ height: trendBarHeight(item) }"></span>
          </div>
          <span class="trend-label">{{ trendDayLabel(index, item.statDate) }}</span>
        </div>
      </div>
    </section>

    <section class="table-card">
      <div class="table-head">
        <h2>错题列表</h2>
        <span>{{ wrongQuestions.length }} 条</span>
      </div>

      <div v-if="loading" class="empty-state">正在加载错题记录...</div>
      <div v-else-if="!wrongQuestions.length" class="empty-state">暂无错题记录。</div>
      <div v-else class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>题目</th>
              <th>学科</th>
              <th>目录</th>
              <th>标签</th>
              <th>标准答案</th>
              <th>错误次数</th>
              <th>掌握状态</th>
              <th>最近出错</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="question in pagedWrongQuestions" :key="question.wrongQuestionId">
              <td>
                <div class="title-cell">
                  <strong>{{ question.questionTitle || '题目已删除' }}</strong>
                  <span>{{ resolveTypeLabel(question.questionType) }} / {{ resolveDifficultyLabel(question.difficulty) }}</span>
                  <span v-if="question.analysis" class="analysis-preview">{{ question.analysis }}</span>
                </div>
              </td>
              <td>{{ question.subjectName || '-' }}</td>
              <td>{{ question.directoryName || '-' }}</td>
              <td>{{ question.tags || '-' }}</td>
              <td>{{ formatAnswerDisplay(question.correctAnswer) }}</td>
              <td>{{ question.wrongCount ?? 0 }}</td>
              <td>
                <span :class="['status-chip', question.masterStatus === 1 ? 'mastered' : 'pending']">
                  {{ question.masterStatus === 1 ? '已掌握' : '未掌握' }}
                </span>
              </td>
              <td>{{ formatDateTime(question.lastWrongTime) }}</td>
              <td>
                <div class="action-stack">
                  <router-link
                    v-if="question.subjectId"
                    class="practice-link"
                    :to="`/practice?subjectId=${question.subjectId}`"
                  >
                    去练习
                  </router-link>
                  <router-link
                    v-if="question.subjectId"
                    class="retry-link"
                    :to="`/practice?subjectId=${question.subjectId}&mode=4`"
                  >
                    错题重练
                  </router-link>
                  <button
                    type="button"
                    class="status-toggle-button"
                    :disabled="masterStatusLoadingId === question.wrongQuestionId"
                    @click="toggleMasterStatus(question)"
                  >
                    {{ masterStatusLoadingId === question.wrongQuestionId ? '处理中...' : (question.masterStatus === 1 ? '取消掌握' : '标记掌握') }}
                  </button>
                  <button
                    type="button"
                    class="ai-link"
                    :disabled="aiAnalyzing && activeWrongQuestion?.wrongQuestionId === question.wrongQuestionId"
                    @click="openAiAssistant(question)"
                  >
                    {{ aiAnalyzing && activeWrongQuestion?.wrongQuestionId === question.wrongQuestionId ? '分析中...' : 'AI分析' }}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <BasePagination
        v-if="!loading && wrongQuestions.length"
        v-model="currentPage"
        :total="wrongQuestions.length"
      />
    </section>

    <div v-if="aiPanelVisible" class="ai-modal-overlay" @click.self="closeAiPanel">
      <section class="ai-modal" role="dialog" aria-modal="true" aria-label="AI 错题分析助手">
        <header class="ai-modal-head">
          <div>
            <h2>AI 错题分析助手</h2>
            <p>{{ activeWrongQuestion?.questionTitle || '-' }}</p>
          </div>
          <div class="panel-actions">
            <button type="button" class="ghost" @click="refreshAiAnalysis">重新分析</button>
            <button type="button" class="ghost" @click="closeAiPanel">关闭</button>
          </div>
        </header>

        <div class="ai-modal-body ai-modal-split">
          <div class="ai-analysis-pane">
            <div v-if="aiAnalyzing" class="empty-state">AI 正在分析该错题，请稍候...</div>
            <div v-else-if="!aiAnalysis" class="empty-state">暂无 AI 分析结果。</div>
            <div v-else class="analysis-card">
              <h3>分析摘要</h3>
              <p class="summary">{{ aiAnalysis.summary || '暂无摘要' }}</p>

              <div class="analysis-grid">
                <div>
                  <h4>错因判断</h4>
                  <ul>
                    <li v-for="item in aiAnalysis.errorReasons" :key="`reason-${item}`">{{ item }}</li>
                  </ul>
                </div>
                <div>
                  <h4>判断依据</h4>
                  <ul>
                    <li v-for="item in aiAnalysis.reasonEvidence" :key="`evidence-${item}`">{{ item }}</li>
                  </ul>
                </div>
                <div>
                  <h4>解题步骤</h4>
                  <ul>
                    <li v-for="item in aiAnalysis.solutionSteps" :key="`step-${item}`">{{ item }}</li>
                  </ul>
                </div>
                <div>
                  <h4>涉及考点</h4>
                  <ul>
                    <li v-for="item in aiAnalysis.knowledgePoints" :key="`kp-${item}`">{{ item }}</li>
                  </ul>
                </div>
                <div>
                  <h4>避免再错建议</h4>
                  <ul>
                    <li v-for="item in aiAnalysis.avoidanceTips" :key="`tip-${item}`">{{ item }}</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>

          <div class="ai-chat-pane">
            <div class="chat-card">
              <div class="chat-head">
                <h3>继续追问</h3>
              </div>

              <div v-if="chatLoading" class="empty-state">正在加载追问记录...</div>
              <div v-else class="chat-body">
                <div v-if="!aiMessages.length" class="empty-state">当前题目还没有消息，开始提问吧。</div>
                <div v-else class="message-list">
                  <div
                    v-for="message in aiMessages"
                    :key="message.messageId"
                    :class="['message-item', message.roleName === 'user' ? 'user' : 'assistant']"
                  >
                    <p class="role">{{ message.roleName === 'user' ? '你' : 'AI' }}</p>
                    <p class="content">{{ formatAiMessageContent(message.content || message.errorMessage) }}</p>
                    <p class="time">{{ formatDateTime(message.createTime) }}</p>
                  </div>
                </div>
              </div>

              <div class="input-row">
                <input
                  v-model="chatInput"
                  type="text"
                  placeholder="例如：第二步为什么可以这样变形？"
                  :disabled="!canInputMessage"
                  @keyup.enter="sendAiMessage"
                >
                <button type="button" :disabled="!canSendMessage || sendingMessage" @click="sendAiMessage">
                  {{ sendingMessage ? '发送中...' : '发送' }}
                </button>
              </div>
              <p class="disclaimer">提示：AI 分析用于学习参考，请结合教材与老师讲解综合判断。</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import BasePagination from '@/components/BasePagination.vue'
import useClientPagination from '@/composables/useClientPagination'
import subjectApi from '../../subject/api/subject'
import practiceApi from '../api/practice'

const route = useRoute()

const loading = ref(false)
const subjects = ref([])
const wrongQuestions = ref([])
const { currentPage, pagedItems: pagedWrongQuestions, resetPage } = useClientPagination(wrongQuestions)
const masterStatusLoadingId = ref(null)

const trendDayOptions = [7, 30, 90]
const selectedTrendDays = ref(30)
const trendLoading = ref(false)
const wrongTrendItems = ref([])

const aiPanelVisible = ref(false)
const aiAnalyzing = ref(false)
const chatLoading = ref(false)
const sendingMessage = ref(false)
const activeWrongQuestion = ref(null)
const aiAnalysis = ref(null)
const aiSession = ref(null)
const aiMessages = ref([])
const chatInput = ref('')

const filters = reactive({
  subjectId: '',
  directoryId: '',
  masterStatus: ''
})

const directoryOptions = computed(() => {
  const directoryMap = new Map()
  for (const item of wrongQuestions.value) {
    if (!item?.directoryId) {
      continue
    }
    if (!directoryMap.has(item.directoryId)) {
      directoryMap.set(item.directoryId, {
        directoryId: item.directoryId,
        directoryName: item.directoryName || `目录 ${item.directoryId}`
      })
    }
  }
  return Array.from(directoryMap.values()).sort((left, right) => String(left.directoryName).localeCompare(String(right.directoryName), 'zh-CN'))
})

const trendMax = computed(() => Math.max(...wrongTrendItems.value.map((item) => Number(item.wrongAnswerCount || 0)), 1))

const canInputMessage = computed(() => {
  return Boolean(activeWrongQuestion.value?.wrongQuestionId) && !chatLoading.value
})

const canSendMessage = computed(() => {
  return canInputMessage.value && chatInput.value.trim().length > 0
})

const toggleBodyScroll = (locked) => {
  if (typeof document === 'undefined') {
    return
  }
  document.body.style.overflow = locked ? 'hidden' : ''
}

const resolveErrorMessage = (error, fallback) => {
  const message = error?.message || ''
  if (error?.code === 'ECONNABORTED' || message.toLowerCase().includes('timeout')) {
    return 'AI 响应超时，请稍后重试'
  }
  return message || fallback
}

const resolveRouteSubjectId = () => {
  return route.query.subjectId ? String(route.query.subjectId) : ''
}

const loadSubjects = async () => {
  try {
    const response = await subjectApi.listMySubjects()
    subjects.value = response.data || []
  } catch (error) {
    showToast(error.message || '加载学科失败')
  }
}

const loadWrongQuestions = async () => {
  loading.value = true
  try {
    const response = await practiceApi.listWrongQuestions({
      subjectId: filters.subjectId ? Number(filters.subjectId) : undefined,
      directoryId: filters.directoryId ? Number(filters.directoryId) : undefined,
      masterStatus: filters.masterStatus === '' ? undefined : Number(filters.masterStatus)
    })
    wrongQuestions.value = response.data || []
    resetPage()
  } catch (error) {
    showToast(error.message || '加载错题记录失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = async () => {
  filters.subjectId = resolveRouteSubjectId()
  filters.directoryId = ''
  filters.masterStatus = ''
  selectedTrendDays.value = 30
  await loadWrongQuestions()
  await loadWrongTrends()
}

const loadWrongTrends = async () => {
  trendLoading.value = true
  try {
    const response = await practiceApi.getWrongQuestionTrends({
      days: selectedTrendDays.value,
      subjectId: filters.subjectId ? Number(filters.subjectId) : undefined,
      directoryId: filters.directoryId ? Number(filters.directoryId) : undefined
    })
    wrongTrendItems.value = response.data || []
  } catch (error) {
    showToast(error.message || '加载错题趋势失败')
    wrongTrendItems.value = []
  } finally {
    trendLoading.value = false
  }
}

const applyFilters = async () => {
  await loadWrongQuestions()
  await loadWrongTrends()
}

const changeTrendDays = async (days) => {
  if (selectedTrendDays.value === days) {
    return
  }
  selectedTrendDays.value = days
  await loadWrongTrends()
}

const toggleMasterStatus = async (question) => {
  if (!question?.wrongQuestionId) {
    return
  }

  const nextStatus = Number(question.masterStatus) === 1 ? 0 : 1
  masterStatusLoadingId.value = question.wrongQuestionId
  try {
    const response = await practiceApi.updateWrongQuestionMasterStatus(question.wrongQuestionId, nextStatus)
    const updated = response.data || {}
    wrongQuestions.value = wrongQuestions.value.map((item) => {
      if (item.wrongQuestionId !== question.wrongQuestionId) {
        return item
      }
      return {
        ...item,
        masterStatus: updated.masterStatus ?? nextStatus
      }
    })
    await loadWrongTrends()
    if (filters.masterStatus !== '') {
      await loadWrongQuestions()
    }
    showToast(nextStatus === 1 ? '已标记为掌握' : '已取消掌握')
  } catch (error) {
    showToast(error.message || '更新掌握状态失败')
  } finally {
    masterStatusLoadingId.value = null
  }
}

const openAiAssistant = async (question) => {
  activeWrongQuestion.value = question
  aiPanelVisible.value = true
  aiSession.value = null
  aiMessages.value = []
  chatInput.value = ''
  await analyzeCurrentWrongQuestion({ preferLatest: true })
  await tryLoadLatestAiSession()
}

const tryLoadLatestAnalysis = async () => {
  if (!activeWrongQuestion.value?.wrongQuestionId) {
    return false
  }
  try {
    const response = await practiceApi.getLatestWrongQuestionAnalysis(activeWrongQuestion.value.wrongQuestionId)
    aiAnalysis.value = normalizeAnalysis(response.data)
    return true
  } catch (error) {
    // 404 表示当前错题还没有分析记录，此时由调用方决定是否发起新分析。
    if (error.response?.data?.code !== 404) {
      showToast(resolveErrorMessage(error, '加载历史分析失败'))
    }
    return false
  }
}

const analyzeCurrentWrongQuestion = async ({ preferLatest = false } = {}) => {
  if (!activeWrongQuestion.value?.wrongQuestionId) {
    return
  }

  if (preferLatest) {
    const loaded = await tryLoadLatestAnalysis()
    if (loaded) {
      return
    }
  }

  aiAnalyzing.value = true
  try {
    const response = await practiceApi.analyzeWrongQuestion(activeWrongQuestion.value.wrongQuestionId, {})
    aiAnalysis.value = normalizeAnalysis(response.data)
  } catch (error) {
    showToast(resolveErrorMessage(error, 'AI 分析失败'))
    aiAnalysis.value = null
  } finally {
    aiAnalyzing.value = false
  }
}

const refreshAiAnalysis = async () => {
  await analyzeCurrentWrongQuestion({ preferLatest: false })
}

const tryLoadLatestAiSession = async () => {
  if (!activeWrongQuestion.value?.wrongQuestionId) {
    return false
  }
  chatLoading.value = true
  try {
    const response = await practiceApi.getLatestWrongQuestionAiSession(activeWrongQuestion.value.wrongQuestionId)
    aiSession.value = response.data || null
    if (!aiSession.value?.sessionId) {
      return false
    }
    await loadAiMessages()
    return true
  } catch (error) {
    if (error.response?.data?.code !== 404) {
      showToast(resolveErrorMessage(error, '加载历史追问记录失败'))
    }
    aiSession.value = null
    aiMessages.value = []
    return false
  } finally {
    chatLoading.value = false
  }
}

const ensureAiSessionCreated = async () => {
  if (!activeWrongQuestion.value?.wrongQuestionId) {
    return false
  }
  if (aiSession.value?.sessionId && Number(aiSession.value?.status) === 1) {
    return true
  }
  try {
    const response = await practiceApi.createWrongQuestionAiSession(activeWrongQuestion.value.wrongQuestionId, {
      analysisId: aiAnalysis.value?.analysisId
    })
    aiSession.value = response.data || null
    return Boolean(aiSession.value?.sessionId)
  } catch (error) {
    showToast(resolveErrorMessage(error, '初始化追问记录失败'))
    return false
  }
}

const loadAiMessages = async () => {
  if (!activeWrongQuestion.value?.wrongQuestionId || !aiSession.value?.sessionId) {
    aiMessages.value = []
    return
  }
  try {
    const response = await practiceApi.listWrongQuestionAiMessages(
      activeWrongQuestion.value.wrongQuestionId,
      aiSession.value.sessionId
    )
    aiMessages.value = response.data || []
  } catch (error) {
    showToast(resolveErrorMessage(error, '加载追问消息失败'))
  }
}

const updateAiMessageById = (messageId, patch) => {
  aiMessages.value = aiMessages.value.map((message) => {
    if (String(message.messageId) !== String(messageId)) {
      return message
    }
    return {
      ...message,
      ...patch
    }
  })
}

const sendAiMessage = async () => {
  if (!canSendMessage.value || sendingMessage.value) {
    return
  }
  const questionId = activeWrongQuestion.value?.wrongQuestionId
  const content = chatInput.value.trim()
  if (!questionId || !content) {
    return
  }

  sendingMessage.value = true
  const requestTime = new Date().toISOString()
  const baseSeq = aiMessages.value.length ? Number(aiMessages.value[aiMessages.value.length - 1]?.seqNo || aiMessages.value.length) : 0
  const userLocalMessage = {
    messageId: `local-user-${Date.now()}`,
    sessionId: aiSession.value?.sessionId || null,
    seqNo: baseSeq + 1,
    role: 2,
    roleName: 'user',
    content,
    status: 1,
    errorMessage: '',
    promptTokens: 0,
    completionTokens: 0,
    totalTokens: 0,
    latencyMs: 0,
    createTime: requestTime
  }
  const assistantLocalMessage = {
    messageId: `local-assistant-${Date.now()}`,
    sessionId: aiSession.value?.sessionId || null,
    seqNo: baseSeq + 2,
    role: 3,
    roleName: 'assistant',
    content: '',
    status: 1,
    errorMessage: '',
    promptTokens: 0,
    completionTokens: 0,
    totalTokens: 0,
    latencyMs: 0,
    createTime: requestTime
  }

  try {
    const sessionReady = await ensureAiSessionCreated()
    if (!sessionReady) {
      return
    }

    userLocalMessage.sessionId = aiSession.value.sessionId
    assistantLocalMessage.sessionId = aiSession.value.sessionId
    aiMessages.value = [...aiMessages.value, userLocalMessage, assistantLocalMessage]
    chatInput.value = ''

    let streamedContent = ''
    const finalMessage = await practiceApi.sendWrongQuestionAiMessageStream(
      questionId,
      aiSession.value.sessionId,
      { content },
      {
        onChunk(delta) {
          streamedContent += delta || ''
          updateAiMessageById(assistantLocalMessage.messageId, { content: streamedContent })
        }
      }
    )

    if (finalMessage) {
      const stableMessageId = finalMessage.messageId ?? assistantLocalMessage.messageId
      updateAiMessageById(assistantLocalMessage.messageId, {
        messageId: stableMessageId,
        sessionId: finalMessage.sessionId ?? assistantLocalMessage.sessionId,
        seqNo: finalMessage.seqNo ?? assistantLocalMessage.seqNo,
        role: finalMessage.role ?? assistantLocalMessage.role,
        roleName: finalMessage.roleName || assistantLocalMessage.roleName,
        content: finalMessage.content || streamedContent || assistantLocalMessage.content,
        status: finalMessage.status ?? assistantLocalMessage.status,
        errorMessage: finalMessage.errorMessage || '',
        promptTokens: finalMessage.promptTokens ?? assistantLocalMessage.promptTokens,
        completionTokens: finalMessage.completionTokens ?? assistantLocalMessage.completionTokens,
        totalTokens: finalMessage.totalTokens ?? assistantLocalMessage.totalTokens,
        latencyMs: finalMessage.latencyMs ?? assistantLocalMessage.latencyMs,
        createTime: finalMessage.createTime || assistantLocalMessage.createTime
      })
      assistantLocalMessage.messageId = stableMessageId
    }
  } catch (error) {
    aiMessages.value = aiMessages.value.filter((message) => message.messageId !== userLocalMessage.messageId && message.messageId !== assistantLocalMessage.messageId)
    chatInput.value = content
    showToast(resolveErrorMessage(error, '发送失败'))
  } finally {
    sendingMessage.value = false
  }
}

const closeAiPanel = () => {
  aiPanelVisible.value = false
  activeWrongQuestion.value = null
  aiAnalysis.value = null
  aiSession.value = null
  aiMessages.value = []
  chatInput.value = ''
}

const handleEscClose = (event) => {
  if (event.key === 'Escape' && aiPanelVisible.value) {
    closeAiPanel()
  }
}

const normalizeAnalysis = (analysis) => {
  const toArray = (value) => (Array.isArray(value) ? value : [])
  return {
    ...analysis,
    errorReasons: toArray(analysis?.errorReasons),
    reasonEvidence: toArray(analysis?.reasonEvidence),
    solutionSteps: toArray(analysis?.solutionSteps),
    knowledgePoints: toArray(analysis?.knowledgePoints),
    avoidanceTips: toArray(analysis?.avoidanceTips)
  }
}

const resolveTypeLabel = (type) => {
  switch (type) {
    case 1:
      return '单选题'
    case 2:
      return '多选题'
    case 3:
      return '判断题'
    case 4:
      return '填空题'
    case 5:
      return '简答题'
    default:
      return type ? `题型 ${type}` : '-'
  }
}

const resolveDifficultyLabel = (difficulty) => {
  switch (difficulty) {
    case 1:
      return '简单'
    case 2:
      return '中等'
    case 3:
      return '困难'
    default:
      return difficulty ? `难度 ${difficulty}` : '-'
  }
}

const formatDateTime = (value) => {
  if (!value) {
    return '暂无记录'
  }
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

const formatDate = (value) => {
  if (!value) {
    return '-'
  }
  return String(value).slice(0, 10)
}

const trendBarHeight = (item) => {
  const count = Number(item?.wrongAnswerCount || 0)
  if (count <= 0) {
    return '0%'
  }
  return `${Math.max(8, Math.round((count / trendMax.value) * 100))}%`
}

const trendDayLabel = (index, value) => {
  const total = wrongTrendItems.value.length
  const step = Math.max(1, Math.ceil(total / 5))
  if (total <= 10 || index === 0 || index === total - 1 || index % step === 0) {
    return formatDate(value).slice(5)
  }
  return ''
}

const formatAnswerDisplay = (value) => {
  if (value == null || String(value).trim() === '') {
    return '暂无答案'
  }
  return String(value).trim()
}

const normalizeLatexFractions = (rawText) => {
  let text = rawText
  let guard = 0
  const fractionExistsPattern = /\\frac\s*\{([^{}]+)\}\s*\{([^{}]+)\}/
  while (fractionExistsPattern.test(text) && guard < 8) {
    text = text.replace(/\\frac\s*\{([^{}]+)\}\s*\{([^{}]+)\}/g, '($1)/($2)')
    guard += 1
  }
  return text
}

const formatAiMessageContent = (value) => {
  if (value == null) {
    return ''
  }
  let text = String(value).replace(/\r\n/g, '\n')

  // markdown markers
  text = text.replace(/^#{1,6}\s*/gm, '')
  text = text.replace(/\*\*(.*?)\*\*/g, '$1')
  text = text.replace(/`([^`]+)`/g, '$1')

  // latex wrappers
  text = text.replace(/\\\[([\s\S]*?)\\\]/g, '$1')
  text = text.replace(/\\\(([\s\S]*?)\\\)/g, '$1')
  text = text.replace(/\$\$([\s\S]*?)\$\$/g, '$1')
  text = text.replace(/\$([^$\n]+)\$/g, '$1')

  // latex commands to readable symbols
  text = normalizeLatexFractions(text)
  text = text.replace(/\\sqrt\s*\{([^{}]+)\}/g, '√($1)')
  text = text.replace(/\\left/g, '')
  text = text.replace(/\\right/g, '')
  text = text.replace(/\\cdot/g, '·')
  text = text.replace(/\\times/g, '×')
  text = text.replace(/\\div/g, '÷')
  text = text.replace(/\\leq/g, '≤')
  text = text.replace(/\\geq/g, '≥')
  text = text.replace(/\\neq/g, '≠')
  text = text.replace(/\\approx/g, '≈')
  text = text.replace(/\\pm/g, '±')
  text = text.replace(/\\Delta/g, 'Δ')
  text = text.replace(/\\alpha/g, 'α')
  text = text.replace(/\\beta/g, 'β')
  text = text.replace(/\\theta/g, 'θ')
  text = text.replace(/\\pi/g, 'π')

  // superscript/subscript braces
  text = text.replace(/\^\{([^{}]+)\}/g, '^$1')
  text = text.replace(/_\{([^{}]+)\}/g, '_$1')
  text = text.replace(/\{([^{}]+)\}/g, '$1')

  // fallback cleanup for leftover escapes
  text = text.replace(/\\,/g, ' ')
  text = text.replace(/\\;/g, ' ')
  text = text.replace(/\\\\/g, '\n')
  text = text.replace(/\\+/g, '')

  return text.trim()
}

watch(
  () => route.query.subjectId,
  async () => {
    filters.subjectId = resolveRouteSubjectId()
    filters.directoryId = ''
    await applyFilters()
  }
)

watch(
  () => filters.subjectId,
  () => {
    filters.directoryId = ''
  }
)

watch(
  () => aiPanelVisible.value,
  (visible) => {
    toggleBodyScroll(visible)
  }
)

onMounted(() => {
  window.addEventListener('keydown', handleEscClose)
})

onMounted(async () => {
  filters.subjectId = resolveRouteSubjectId()
  await loadSubjects()
  await applyFilters()
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleEscClose)
  toggleBodyScroll(false)
})
</script>

<style scoped>
.wrong-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background: linear-gradient(135deg, #f6efe7 0%, #dbe9f7 100%);
}

.hero-card,
.filter-card,
.table-card {
  max-width: 1180px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.94);
  border-radius: 28px;
  box-shadow: 0 24px 60px rgba(38, 57, 77, 0.12);
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
  margin: 14px 0 0;
  max-width: 760px;
  color: #5e6d7c;
  line-height: 1.7;
}

.hero-actions,
.filter-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.trend-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-card,
.table-card {
  margin-top: 18px;
  padding: 24px;
}

.filter-card {
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

.filter-grid label {
  display: grid;
  gap: 8px;
  color: #17324d;
  font-weight: 600;
}

.filter-grid select {
  width: 100%;
  padding: 13px 14px;
  border: 1px solid #cad7e3;
  border-radius: 14px;
  font-size: 14px;
  box-sizing: border-box;
  background: #fff;
}

.table-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.table-head h2 {
  margin: 0;
  color: #17324d;
  font-size: 24px;
}

.table-head span {
  color: #6c7a8d;
}

.table-wrap {
  margin-top: 18px;
  overflow: auto;
}

.trend-chart {
  height: 220px;
  margin-top: 16px;
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: minmax(12px, 1fr);
  gap: 6px;
  align-items: end;
  overflow-x: auto;
  padding-bottom: 4px;
}

.trend-column {
  min-width: 0;
  height: 100%;
  display: grid;
  grid-template-rows: 1fr 20px;
  gap: 6px;
}

.trend-track {
  position: relative;
  height: 100%;
  border-radius: 8px;
  background: #e8edf4;
  overflow: hidden;
}

.trend-fill {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 8px 8px 0 0;
  background: #b42318;
}

.trend-label {
  min-height: 20px;
  color: #6c7a8d;
  font-size: 11px;
  text-align: center;
  white-space: nowrap;
}

.ai-modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1200;
  padding: 20px;
  background: rgba(18, 30, 45, 0.45);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-modal {
  width: min(1280px, 100%);
  max-height: calc(100vh - 40px);
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 24px;
  box-shadow: 0 30px 80px rgba(22, 38, 58, 0.28);
  overflow: hidden;
}

.ai-modal-head {
  padding: 22px 24px 18px;
  border-bottom: 1px solid #e7edf4;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
}

.ai-modal-head h2 {
  margin: 0;
  color: #17324d;
}

.ai-modal-head p {
  margin: 8px 0 0;
  color: #5e6d7c;
}

.ai-modal-body {
  flex: 1;
  min-height: 0;
  padding: 20px 24px 24px;
  overflow: hidden;
}

.ai-modal-split {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(360px, 1fr);
  gap: 16px;
  align-items: start;
  height: 100%;
}

.ai-analysis-pane,
.ai-chat-pane {
  min-height: 0;
  height: 100%;
  overflow-y: auto;
  padding-right: 4px;
}

.ai-modal-body > .empty-state,
.ai-modal-body > .analysis-card {
  margin-top: 0;
}

.ai-analysis-pane > .empty-state,
.ai-analysis-pane > .analysis-card,
.ai-chat-pane > .empty-state,
.ai-chat-pane > .chat-card {
  margin-top: 0;
}

table {
  width: 100%;
  min-width: 980px;
  border-collapse: collapse;
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
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.title-cell {
  display: grid;
  gap: 6px;
}

.title-cell strong {
  color: #17324d;
  line-height: 1.6;
}

.title-cell span {
  color: #5e6d7c;
  font-size: 13px;
  line-height: 1.6;
}

.analysis-preview {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.action-stack {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.empty-state {
  margin: 18px 0 0;
  padding: 34px;
  text-align: center;
  color: #6c7a8d;
  background: #f3f7fb;
  border-radius: 18px;
}

button,
.ghost,
.practice-link,
.ai-link,
.retry-link,
.status-toggle-button {
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

.practice-link,
.ai-link,
.retry-link,
.status-toggle-button {
  display: inline-flex;
  align-items: center;
  background: #17324d;
  color: #fff;
  padding: 8px 12px;
  border-radius: 12px;
  font-size: 13px;
}

.ai-link {
  background: #0f7a43;
}

.retry-link {
  background: #7a3e00;
}

.status-toggle-button {
  background: #445f7d;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 66px;
  border-radius: 999px;
  padding: 5px 10px;
  font-size: 12px;
}

.status-chip.mastered {
  background: #ddf5e9;
  color: #0f7a43;
}

.status-chip.pending {
  background: #fce7e7;
  color: #b42318;
}

.active-trend-button {
  background: #17324d;
  color: #fff;
}

.panel-head,
.chat-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
}

.panel-head h2,
.chat-head h3 {
  margin: 0;
  color: #17324d;
}

.panel-head p {
  margin: 8px 0 0;
  color: #5e6d7c;
}

.panel-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.analysis-card,
.chat-card {
  margin-top: 16px;
  padding: 18px;
  border-radius: 18px;
  background: #f7fbff;
}

.chat-card {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.analysis-card h3,
.analysis-card h4 {
  margin: 0;
  color: #17324d;
}

.summary {
  margin: 10px 0 0;
  color: #334b64;
  line-height: 1.8;
}

.analysis-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 16px;
}

.analysis-grid ul {
  margin: 8px 0 0;
  padding-left: 18px;
  color: #4f6175;
  line-height: 1.7;
}

.chat-body {
  margin-top: 14px;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.message-list {
  display: grid;
  gap: 10px;
}

.message-item {
  padding: 12px;
  border-radius: 12px;
  background: #ecf2f8;
}

.message-item.user {
  background: #e7f8ef;
}

.message-item .role {
  margin: 0;
  color: #17324d;
  font-size: 12px;
  font-weight: 600;
}

.message-item .content {
  margin: 8px 0 0;
  color: #334b64;
  white-space: pre-wrap;
  line-height: 1.7;
}

.message-item .time {
  margin: 6px 0 0;
  color: #7b8a9b;
  font-size: 12px;
}

.input-row {
  margin-top: 16px;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
}

.input-row input {
  min-width: 0;
  border: 1px solid #c6d5e4;
  border-radius: 12px;
  padding: 10px 12px;
  font-size: 14px;
}

.disclaimer {
  margin: 12px 0 0;
  color: #7b8a9b;
  font-size: 12px;
}

@media (max-width: 768px) {
  .wrong-page {
    padding: 18px 14px 30px;
  }

  .hero-card,
  .filter-card,
  .table-card {
    border-radius: 22px;
    padding: 20px;
  }

  .hero-card h1 {
    font-size: 28px;
  }

  .ai-modal-overlay {
    padding: 10px;
  }

  .ai-modal {
    max-height: calc(100vh - 20px);
    border-radius: 18px;
  }

  .ai-modal-head,
  .ai-modal-body {
    padding: 16px;
  }

  .ai-modal-body {
    overflow-y: auto;
  }

  .ai-modal-split {
    grid-template-columns: 1fr;
    gap: 12px;
    height: auto;
  }

  .ai-analysis-pane,
  .ai-chat-pane {
    height: auto;
    overflow: visible;
    padding-right: 0;
  }

  .chat-card,
  .chat-body {
    min-height: 0;
    overflow: visible;
  }

  .input-row {
    grid-template-columns: 1fr;
  }
}
</style>
