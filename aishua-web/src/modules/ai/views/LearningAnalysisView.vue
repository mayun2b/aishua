<template>
  <div class="analysis-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">Learning Analysis</p>
        <h1>学情分析</h1>
        <p class="description">
          选择学科后，AI 会基于你的问题给出学习情况分析、薄弱点提示和可执行建议。
        </p>
      </div>

      <div class="hero-actions">
        <router-link class="ghost" to="/dashboard">返回工作台</router-link>
        <router-link class="ghost" to="/my-subjects">我的学科</router-link>
      </div>
    </section>

    <section class="form-card">
      <div class="form-grid">
        <label>
          <span>学科</span>
          <select v-model="form.subjectId">
            <option value="">请选择学科</option>
            <option v-for="subject in subjects" :key="subject.subjectId" :value="String(subject.subjectId)">
              {{ subject.name }}{{ subject.code ? `（${subject.code}）` : '' }}
            </option>
          </select>
        </label>

        <label>
          <span>年级（可选）</span>
          <input v-model="form.grade" type="text" placeholder="例如：高一">
        </label>

        <label>
          <span>教材版本（可选）</span>
          <input v-model="form.textbookVersion" type="text" placeholder="例如：人教版">
        </label>
      </div>

      <label class="query-field">
        <span>分析问题</span>
        <textarea
          v-model="form.query"
          rows="6"
          placeholder="例如：请结合我在该学科的最近练习情况，给出掌握程度评估、薄弱点和下周学习计划。"
        />
      </label>

      <div class="form-actions">
        <button type="button" class="ghost" @click="fillTemplateQuery">生成推荐问题</button>
        <button type="button" class="ghost" @click="resetForm">重置</button>
        <button type="button" :disabled="loading" @click="runAnalysis">
          {{ loading ? '分析中...' : '开始分析' }}
        </button>
      </div>
    </section>

    <section class="history-card">
      <div class="history-head">
        <div>
          <h2>历史分析记录</h2>
          <p>{{ historyFilterText }}</p>
        </div>
        <button type="button" class="ghost" :disabled="historyLoading" @click="loadHistory">
          {{ historyLoading ? '刷新中...' : '刷新' }}
        </button>
      </div>

      <div v-if="historyLoading" class="empty-state">正在加载历史分析记录...</div>
      <div v-else-if="!historyRecords.length" class="empty-state">暂无历史分析记录。</div>
      <div v-else class="history-list">
        <button
          v-for="record in historyRecords"
          :key="record.id"
          type="button"
          class="history-item"
          :class="{ active: selectedHistoryId === record.id }"
          @click="openHistoryRecord(record)"
        >
          <div class="history-title">
            <strong>{{ record.subjectName || '未指定学科' }}</strong>
            <span :class="['status-pill', resolveStatusClass(record.status)]">
              {{ resolveStatusText(record.status) }}
            </span>
          </div>
          <p class="history-query">{{ compactText(record.queryText, 96) }}</p>
          <p class="history-summary">{{ compactText(record.summary || record.errorMessage || '暂无摘要', 120) }}</p>
          <div class="history-meta">
            <span>{{ formatDateTime(record.createTime) }}</span>
            <span>{{ record.grade || '未填写年级' }}</span>
            <span>{{ record.textbookVersion || '未填写版本' }}</span>
          </div>
        </button>
      </div>

      <BasePagination
        v-if="!historyLoading && historyRecords.length"
        v-model="historyPage"
        :total="historyTotal"
        :page-size="historyPageSize"
      />
    </section>

    <!-- 优化后的分析结果区域 -->
    <section class="result-card" v-if="resultPayload">
      <div class="result-head">
        <h2>分析结果</h2>
        <span>{{ detailLoading ? '加载中...' : (analysisAt || '尚未分析') }}</span>
      </div>

      <div v-if="loading || detailLoading" class="empty-state">正在分析中，请稍候...</div>
      <div v-else class="enhanced-result-content">
        <div v-if="dataQualityNotice" class="quality-notice">
          <strong>数据提示</strong>
          <span>{{ dataQualityNotice }}</span>
        </div>

        <!-- 总体评估卡片 -->
        <div class="section-card overall-card" v-if="hasOverallData">
          <div class="section-title">
            <span class="section-icon">总</span>
            <h3>总体评估</h3>
          </div>
          <div class="overall-content">
            <!-- 掌握程度环 -->
            <div class="mastery-ring-wrapper" v-if="overallAssessment.score != null">
              <div class="ring-container">
                <svg class="ring-svg" viewBox="0 0 100 100">
                  <defs>
                    <linearGradient id="ringGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                      <stop offset="0%" stop-color="#3b82f6" />
                      <stop offset="100%" stop-color="#10b981" />
                    </linearGradient>
                  </defs>
                  <circle cx="50" cy="50" r="40" class="ring-bg"></circle>
                  <circle
                    cx="50" cy="50" r="40"
                    class="ring-progress"
                    :stroke-dasharray="`${overallAssessment.score * 2.51} 251`"
                    :stroke-dashoffset="62.75"
                  ></circle>
                </svg>
                <div class="ring-text">
                  <div class="score">{{ overallAssessment.score }}</div>
                  <div class="label">分</div>
                </div>
              </div>
            </div>

            <!-- 掌握程度文字 -->
            <div class="mastery-info">
              <div class="mastery-level-badge" v-if="overallAssessment.masteryLevel">
                {{ overallAssessment.masteryLevel }}
              </div>
              <div class="mastery-trend" v-if="overallAssessment.trend">
                <span>{{ overallAssessment.trend }}</span>
              </div>

              <!-- 优势和劣势标签 -->
              <div class="strengths-weaknesses">
                <div class="tag-group" v-if="overallAssessment.strongSubjects && Array.isArray(overallAssessment.strongSubjects) && overallAssessment.strongSubjects.length">
                  <span class="tag-label">优势</span>
                  <div class="tags">
                    <span v-for="(s, i) in overallAssessment.strongSubjects" :key="i" class="tag tag-success">{{ s }}</span>
                  </div>
                </div>
                <div class="tag-group" v-if="overallAssessment.weakSubjects && Array.isArray(overallAssessment.weakSubjects) && overallAssessment.weakSubjects.length">
                  <span class="tag-label">待提升</span>
                  <div class="tags">
                    <span v-for="(w, i) in overallAssessment.weakSubjects" :key="i" class="tag tag-warning">{{ w }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- AI 总结 -->
        <div class="section-card summary-card" v-if="summaryText">
          <div class="section-title">
            <span class="section-icon">AI</span>
            <h3>AI 总结</h3>
          </div>
          <p class="summary-text">{{ summaryText }}</p>
        </div>

        <!-- AI 完整分析 -->
        <div class="section-card fulltext-card" v-if="fullText && fullText !== summaryText">
          <div class="section-title">
            <span class="section-icon">析</span>
            <h3>详细分析</h3>
          </div>
          <p class="fulltext-content">{{ fullText }}</p>
        </div>

        <!-- 知识点分析 - 有详情时用卡片，无详情时用标签 -->
        <div class="section-card knowledge-card" v-if="Array.isArray(knowledgePoints) && knowledgePoints.length">
          <div class="section-title">
            <span class="section-icon">知</span>
            <h3>知识点分析</h3>
          </div>

          <!-- 无详情时：标签式展示 -->
          <div v-if="!hasRichContent" class="kp-tag-list">
            <span v-for="(kp, index) in knowledgePoints" :key="index" class="kp-tag" :class="`kp-priority-${kp.priority || 1}`">
              {{ kp.name }}
            </span>
          </div>

          <!-- 有详情时：卡片式展示 -->
          <div v-else class="knowledge-list">
            <div v-for="(kp, index) in knowledgePoints" :key="index" class="knowledge-item">
              <div class="kp-header">
                <div class="kp-name">
                  <span class="priority-dot" :class="`priority-${kp.priority || 1}`"></span>
                  {{ kp.name }}
                </div>
                <div class="kp-module" v-if="kp.moduleName">{{ kp.moduleName }}</div>
              </div>

              <div class="kp-mastery" v-if="kp.masteryLevel || kp.correctRate != null">
                <div class="mastery-label" v-if="kp.masteryLevel">{{ kp.masteryLevel }}</div>
                <div class="mastery-bar" v-if="kp.correctRate != null">
                  <div class="bar-bg"></div>
                  <div class="bar-fill" :style="{ width: `${kp.correctRatePercent}%` }"></div>
                  <div class="bar-text">{{ kp.correctRatePercent }}%</div>
                </div>
              </div>

              <div class="kp-stats" v-if="kp.sampleCount != null">
                <span class="stat">练习: {{ kp.sampleCount }} 题</span>
                <span class="stat" v-if="kp.wrongCount != null">错误: {{ kp.wrongCount }} 题</span>
              </div>

              <p class="kp-reason" v-if="kp.reasonText">{{ kp.reasonText }}</p>

              <div class="kp-suggestions" v-if="kp.suggestions && Array.isArray(kp.suggestions) && kp.suggestions.length">
                <div class="sugg-label">建议</div>
                <ul class="sugg-list">
                  <li v-for="(s, i) in kp.suggestions" :key="i">{{ s }}</li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <!-- 学习建议 -->
        <div class="section-card suggestions-card" v-if="suggestionSections.length">
          <div class="section-title">
            <span class="section-icon">行</span>
            <h3>学习建议</h3>
          </div>
          <div class="suggestions-container">
            <div v-for="section in suggestionSections" :key="section.key" class="sugg-section">
              <div class="sugg-section-title">
                <span class="sugg-index">{{ section.badge }}</span>
                <span>{{ section.title }}</span>
              </div>
              <ul class="sugg-items">
                <li v-for="(item, i) in section.items" :key="i">{{ item }}</li>
              </ul>
            </div>
          </div>
        </div>

        <!-- 学习计划 -->
        <div class="section-card plan-card" v-if="Array.isArray(studyPlan) && studyPlan.length">
          <div class="section-title">
            <span class="section-icon">练</span>
            <h3>学习计划</h3>
          </div>
          <div class="plan-list">
            <div v-for="(step, index) in studyPlan" :key="index" class="plan-step">
              <div class="step-number">{{ step.order || index + 1 }}</div>
              <div class="step-content">
                <div class="step-topic">{{ step.topic }}</div>
                <div class="step-meta" v-if="step.estimatedHours">预计 {{ step.estimatedHours }} 小时</div>
                <div class="step-resources" v-if="step.resources && Array.isArray(step.resources) && step.resources.length">
                  {{ step.resources.join(' | ') }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 其他结构化信息 -->
        <div class="section-card detail-card" v-if="Array.isArray(readableOutputSections) && readableOutputSections.length">
          <div class="section-title">
            <span class="section-icon">详</span>
            <h3>重点明细</h3>
          </div>
          <div class="detail-section-list">
            <div v-for="section in readableOutputSections" :key="section.key" class="detail-section">
              <h4>{{ section.label }}</h4>
              <ul v-if="section.items.length" class="detail-list">
                <li v-for="(item, index) in section.items" :key="index">{{ item }}</li>
              </ul>
              <p v-else class="detail-text">{{ section.text }}</p>
            </div>
          </div>
        </div>

        <!-- JSON 查看器（折叠） -->
        <details class="raw-card">
          <summary>查看原始响应（调试用）</summary>
          <pre>{{ prettyResult }}</pre>
        </details>
      </div>
    </section>

    <!-- 未分析状态 -->
    <section class="result-card" v-else>
      <div class="result-head">
        <h2>分析结果</h2>
      </div>
      <div class="empty-state">请先选择学科并提交分析问题</div>
    </section>

    <!-- 视频搜索区域 -->
    <section v-if="resultPayload" class="video-search-card">
      <div class="video-search-head">
        <h2>找教学视频</h2>
        <span class="tip">为薄弱知识点找到相关的教学视频</span>
      </div>

      <div class="video-search-content">
        <div class="quick-search">
          <button
            v-for="(keyword, index) in quickSearchKeywords"
            :key="index"
            type="button"
            class="ghost quick-search-btn"
            @click="quickSearchVideo(keyword)"
          >
            {{ keyword }}
          </button>
          <button type="button" class="ghost quick-search-btn" @click="searchWithCurrentInfo">
            自定义搜索
          </button>
        </div>

        <div v-if="videoSearchLoading" class="video-search-loading">
          <p>正在搜索教学视频...</p>
        </div>

        <div v-else-if="videoSearchResult && videoSearchResult.length" class="video-results">
          <h3>推荐视频链接</h3>
          <div class="video-list">
            <a
              v-for="(video, index) in videoSearchResult"
              :key="index"
              :href="video.url"
              target="_blank"
              rel="noopener noreferrer"
              class="video-item"
            >
              <div class="video-icon" :class="video.source">
                {{ video.source === 'bilibili' ? 'B站' : video.source }}
              </div>
              <div class="video-info">
                <div class="video-title">{{ video.title }}</div>
                <div class="video-keyword">关键词：{{ video.keyword }}</div>
              </div>
              <div class="video-arrow">→</div>
            </a>
          </div>
        </div>

        <div v-else-if="videoSearchResult && !videoSearchResult.length" class="video-search-empty">
          <p>未找到相关视频，试试其他关键词</p>
        </div>

        <div v-else-if="videoSearchError" class="video-search-error">
          <p>{{ videoSearchError }}</p>
        </div>
      </div>
    </section>

    <!-- 视频搜索弹窗 -->
    <div v-if="showVideoSearchModal" class="video-search-modal-mask" @click="closeVideoSearchModal">
      <div class="video-search-modal" @click.stop>
        <div class="modal-header">
          <h3>搜索教学视频</h3>
          <button type="button" class="close-btn" @click="closeVideoSearchModal">×</button>
        </div>
        <div class="modal-body">
          <label>
            <span>知识点</span>
            <input v-model="videoSearchForm.knowledgePoint" type="text" placeholder="请输入要搜索的知识点" @keyup.enter="doVideoSearch">
          </label>
        </div>
        <div class="modal-footer">
          <button type="button" class="ghost" @click="closeVideoSearchModal">取消</button>
          <button type="button" :disabled="!videoSearchForm.knowledgePoint.trim()" @click="doVideoSearch">
            搜索
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import BasePagination from '@/components/BasePagination.vue'
import learningAnalysisApi from '../api/learningAnalysis'
import subjectApi from '../../subject/api/subject'
import videoSearchApi from '../api/videoSearch'
import { normalizePageResult } from '../../common/utils/pageResult'

const route = useRoute()

const loading = ref(false)
const detailLoading = ref(false)
const historyLoading = ref(false)
const subjects = ref([])
const resultPayload = ref(null)
const analysisAt = ref('')
const historyRecords = ref([])
const historyTotal = ref(0)
const historyPage = ref(1)
const historyPageSize = 8
const selectedHistoryId = ref(null)
const historyReady = ref(false)

const form = reactive({
  subjectId: '',
  grade: '',
  textbookVersion: '',
  query: ''
})

const SUMMARY_KEYS = [
  'summary',
  'summaryText',
  'analysisSummary',
  'conclusion',
  'answer',
  'analysis',
  'text',
  'fullText',
  'full_text',
  'result',
  'message',
  'content'
]

const FIELD_LABELS = {
  summary: '摘要',
  summaryText: '摘要',
  analysisSummary: '摘要',
  conclusion: '结论',
  answer: '回答',
  analysis: '分析',
  result: '结果',
  text: '正文',
  fullText: '完整分析',
  full_text: '完整分析',
  errorReasons: '薄弱原因',
  error_reasons: '薄弱原因',
  weakReasons: '薄弱原因',
  weak_reasons: '薄弱原因',
  reasonEvidence: '判断依据',
  reason_evidence: '判断依据',
  solutionSteps: '学习步骤',
  solution_steps: '学习步骤',
  avoidanceTips: '避免建议',
  avoidance_tips: '避免建议',
  knowledgePoints: '知识点',
  knowledge_points: '知识点',
  weakKnowledgePoints: '薄弱知识点',
  weak_knowledge_points: '薄弱知识点',
  suggestions: '学习建议',
  studySuggestions: '学习建议',
  study_suggestions: '学习建议',
  immediate: '立即行动',
  shortTerm: '短期计划',
  short_term: '短期计划',
  longTerm: '长期规划',
  long_term: '长期规划',
  nextFocus: '下一步重点',
  next_focus: '下一步重点',
  practiceOrder: '练习顺序',
  practice_order: '练习顺序',
  resourceRecommendations: '资源推荐',
  resource_recommendations: '资源推荐',
  dataQuality: '数据情况',
  data_quality: '数据情况',
  sufficient: '是否充分',
  missingMetrics: '缺失指标',
  missing_metrics: '缺失指标',
  subject: '学科',
  grade: '年级',
  textbookVersion: '教材版本',
  textbook_version: '教材版本',
  masteryLevel: '掌握程度',
  mastery_level: '掌握程度',
  correctRate: '正确率',
  correct_rate: '正确率',
  wrongCount: '错题数',
  wrong_count: '错题数',
  sampleCount: '样本数',
  sample_count: '样本数',
  module: '模块',
  moduleName: '模块',
  module_name: '模块',
  reason: '原因',
  reasonText: '原因',
  reason_text: '原因',
  content: '内容',
  title: '标题',
  description: '说明'
}

const RESERVED_OUTPUT_KEYS = new Set([
  'summary',
  'summaryText',
  'analysisSummary',
  'analysis',
  'answer',
  'result',
  'text',
  'fullText',
  'full_text',
  'message',
  'content',
  'overallAssessment',
  'overall_assessment',
  'knowledgePoints',
  'knowledge_points',
  'weakKnowledgePoints',
  'weak_knowledge_points',
  'suggestions',
  'studySuggestions',
  'study_suggestions',
  'immediate',
  'immediateActions',
  'immediate_actions',
  'actions',
  'actionItems',
  'action_items',
  'shortTerm',
  'short_term',
  'weeklyPlan',
  'weekly_plan',
  'nextWeekPlan',
  'next_week_plan',
  'longTerm',
  'long_term',
  'longRange',
  'long_range',
  'errorReasons',
  'error_reasons',
  'weakReasons',
  'weak_reasons',
  'problems',
  'solutionSteps',
  'solution_steps',
  'practiceOrder',
  'practice_order',
  'steps',
  'avoidanceTips',
  'avoidance_tips',
  'tips',
  'avoidance',
  'nextFocus',
  'next_focus',
  'focus',
  'focusPoints',
  'focus_points',
  'studyPlan',
  'study_plan',
  'dataQuality',
  'data_quality'
])

const HIDDEN_OUTPUT_KEYS = new Set([
  'inputs',
  'query',
  'subject_id',
  'learning_context_json',
  'conversation_context_json',
  'local_question_candidates_json',
  'resource_policy_json',
  'workflow_run_id',
  'workflowRunId',
  'task_id',
  'taskId',
  'id'
])

const SUGGESTION_SECTION_CONFIG = [
  {
    key: 'immediate',
    title: '立即行动',
    badge: '1',
    keys: ['immediate', 'immediateActions', 'immediate_actions', 'actions', 'actionItems', 'action_items']
  },
  {
    key: 'shortTerm',
    title: '短期计划',
    badge: '2',
    keys: ['shortTerm', 'short_term', 'weeklyPlan', 'weekly_plan', 'nextWeekPlan', 'next_week_plan']
  },
  {
    key: 'longTerm',
    title: '长期规划',
    badge: '3',
    keys: ['longTerm', 'long_term', 'longRange', 'long_range']
  },
  {
    key: 'weakReasons',
    title: '薄弱原因',
    badge: '因',
    keys: ['errorReasons', 'error_reasons', 'weakReasons', 'weak_reasons', 'problems']
  },
  {
    key: 'practiceOrder',
    title: '练习顺序',
    badge: '序',
    keys: ['solutionSteps', 'solution_steps', 'practiceOrder', 'practice_order', 'steps']
  },
  {
    key: 'avoidanceTips',
    title: '避免建议',
    badge: '防',
    keys: ['avoidanceTips', 'avoidance_tips', 'tips', 'avoidance']
  },
  {
    key: 'nextFocus',
    title: '下一步重点',
    badge: '焦',
    keys: ['nextFocus', 'next_focus', 'focus', 'focusPoints', 'focus_points']
  }
]

const selectedSubject = computed(() => {
  return subjects.value.find((item) => String(item.subjectId) === String(form.subjectId)) || null
})

const currentReport = computed(() => {
  const payload = resultPayload.value || {}
  if (payload.analysisReport) {
    return payload.analysisReport
  }
  if (payload.reportCode || payload.result || payload.rawResponse) {
    return payload
  }
  return null
})

const outputObject = computed(() => {
  const payload = resultPayload.value || {}
  const report = currentReport.value

  // 1. 优先查找 structured_output（Dify 工作流的结构化输出）
  const reportResult = unwrapStructuredPayload(report?.result)
  if (reportResult && typeof reportResult === 'object' && !Array.isArray(reportResult)) {
    return reportResult
  }

  // 2. 从 rawResponse 中提取
  const rawResp = report?.rawResponse
  if (rawResp && typeof rawResp === 'object') {
    const dataOutputs = unwrapStructuredPayload(rawResp.data?.outputs)
    if (dataOutputs && typeof dataOutputs === 'object' && !Array.isArray(dataOutputs)) return dataOutputs
    const rawOutputs = unwrapStructuredPayload(rawResp.outputs)
    if (rawOutputs && typeof rawOutputs === 'object' && !Array.isArray(rawOutputs)) return rawOutputs
  }

  // 3. 从 payload 直接提取
  const payloadDataOutputs = unwrapStructuredPayload(payload.data?.outputs)
  if (payloadDataOutputs && typeof payloadDataOutputs === 'object' && !Array.isArray(payloadDataOutputs)) return payloadDataOutputs
  const payloadOutputs = unwrapStructuredPayload(payload.outputs)
  if (payloadOutputs && typeof payloadOutputs === 'object' && !Array.isArray(payloadOutputs)) return payloadOutputs

  return {}
})

const summaryText = computed(() => {
  const outputs = outputObject.value || {}
  const report = currentReport.value || {}
  const candidates = [
    outputs.summary,
    outputs.summaryText,
    outputs.analysis,
    outputs.answer,
    outputs.result,
    report.summary,
    resultPayload.value?.answer,
    resultPayload.value?.message
  ]
  for (const candidate of candidates) {
    const normalized = extractPrimaryText(candidate)
    if (normalized) {
      return normalized
    }
  }
  return ''
})

// AI 完整分析文本（比 summary 更详细）
const fullText = computed(() => {
  const report = currentReport.value || {}
  const outputs = outputObject.value || {}
  const candidates = [
    report.fullText,
    outputs.fullText,
    outputs.text,
    outputs.full_text
  ]
  for (const candidate of candidates) {
    const normalized = extractPrimaryText(candidate)
    if (normalized) {
      return normalized
    }
  }
  return ''
})

// 是否有详细内容（fullText 或知识点有详情）
const hasRichContent = computed(() => {
  return !!fullText.value || knowledgePoints.value.some(kp =>
    kp.masteryLevel || kp.correctRate != null || kp.reasonText || (kp.suggestions && kp.suggestions.length)
  )
})

const readableOutputSections = computed(() => {
  const outputs = outputObject.value
  if (!outputs || typeof outputs !== 'object' || Array.isArray(outputs)) {
    return []
  }
  return Object.entries(outputs)
    .filter(([key, value]) => !RESERVED_OUTPUT_KEYS.has(key) && !HIDDEN_OUTPUT_KEYS.has(key) && !isEmptyDisplayValue(value))
    .map(([key, value]) => toReadableSection(key, value))
    .filter(Boolean)
})

// 增强的数据解析
const overallAssessment = computed(() => {
  const outputs = outputObject.value || {}
  const oa = (outputs.overallAssessment && typeof outputs.overallAssessment === 'object')
    ? outputs.overallAssessment
    : (outputs.overall_assessment && typeof outputs.overall_assessment === 'object')
      ? outputs.overall_assessment
      : {}
  return {
    masteryLevel: oa.masteryLevel || oa.mastery_level || '',
    score: normalizeScore(oa.score),
    strongSubjects: Array.isArray(oa.strongSubjects) ? oa.strongSubjects : (Array.isArray(oa.strong_subjects) ? oa.strong_subjects : []),
    weakSubjects: Array.isArray(oa.weakSubjects) ? oa.weakSubjects : (Array.isArray(oa.weak_subjects) ? oa.weak_subjects : []),
    trend: oa.trend || ''
  }
})

const knowledgePoints = computed(() => {
  const outputs = outputObject.value || {}
  const report = currentReport.value || {}

  const reportPoints = Array.isArray(report.knowledgePoints)
    ? report.knowledgePoints.map(normalizeKnowledgePoint).filter(kp => kp && kp.name)
    : []
  let points = []
  if (Array.isArray(outputs.knowledgePoints)) {
    points = outputs.knowledgePoints
  } else if (Array.isArray(outputs.knowledge_points)) {
    points = outputs.knowledge_points
  }

  // 3. 再次尝试 weakKnowledgePoints
  if (!points.length) {
    if (Array.isArray(outputs.weakKnowledgePoints)) {
      points = outputs.weakKnowledgePoints
    } else if (Array.isArray(outputs.weak_knowledge_points)) {
      points = outputs.weak_knowledge_points
    }
  }

  const outputPoints = Array.isArray(points)
    ? points.map(normalizeKnowledgePoint).filter(kp => kp && kp.name)
    : []
  const outputHasRichDetail = outputPoints.some(kp => kp.moduleName || kp.reasonText || kp.masteryLevel || kp.correctRate != null || kp.suggestions.length)
  if (outputHasRichDetail) {
    return outputPoints
  }
  if (reportPoints.length) {
    return reportPoints
  }
  return outputPoints
})

const suggestionSections = computed(() => {
  const outputs = outputObject.value || {}
  const parsedSuggestions = parseMaybeJson(outputs.suggestions || outputs.studySuggestions || outputs.study_suggestions)
  const suggestionObject = (parsedSuggestions && typeof parsedSuggestions === 'object' && !Array.isArray(parsedSuggestions))
    ? parsedSuggestions
    : {}
  const looseSuggestions = (Array.isArray(parsedSuggestions) || typeof parsedSuggestions === 'string')
    ? normalizeListItems(parsedSuggestions)
    : []

  return SUGGESTION_SECTION_CONFIG.map((section) => {
    const items = uniqueStrings([
      ...(section.key === 'immediate' ? looseSuggestions : []),
      ...normalizeListItems(firstKnownValue(suggestionObject, section.keys)),
      ...normalizeListItems(firstKnownValue(outputs, section.keys))
    ])
    return {
      ...section,
      items
    }
  }).filter((section) => section.items.length)
})

const studyPlan = computed(() => {
  const outputs = outputObject.value || {}
  let plan = []
  if (Array.isArray(outputs.studyPlan)) {
    plan = outputs.studyPlan
  } else if (Array.isArray(outputs.study_plan)) {
    plan = outputs.study_plan
  }
  if (!Array.isArray(plan)) return []
  return plan.map((item, index) => {
    if (typeof item === 'string') {
      return {
        order: index + 1,
        topic: item.trim(),
        estimatedHours: null,
        resources: []
      }
    }
    if (!item || typeof item !== 'object') {
      return null
    }
    return {
      order: item.order != null ? Number(item.order) : index + 1,
      topic: item.topic || '',
      estimatedHours: item.estimatedHours != null ? Number(item.estimatedHours) : null,
      resources: Array.isArray(item.resources) ? item.resources : []
    }
  }).filter(Boolean)
})

const hasOverallData = computed(() => {
  const oa = overallAssessment.value
  if (!oa) return false
  return (oa.score != null) || oa.masteryLevel ||
         (Array.isArray(oa.strongSubjects) && oa.strongSubjects.length) ||
         (Array.isArray(oa.weakSubjects) && oa.weakSubjects.length)
})

const dataQualityNotice = computed(() => {
  const report = currentReport.value || {}
  const outputs = outputObject.value || {}
  const dataQuality = parseMaybeJson(outputs.dataQuality || outputs.data_quality) || {}
  const missingMetrics = uniqueStrings([
    ...normalizeListItems(report.missingMetrics),
    ...normalizeListItems(dataQuality.missingMetrics || dataQuality.missing_metrics)
  ])
  const sufficient = report.dataQualitySufficient
  const insufficient = (sufficient != null && Number(sufficient) === 0) || isFalseLike(dataQuality.sufficient)
  if (!insufficient) {
    return ''
  }
  if (missingMetrics.length) {
    return `本次可用学习数据不完整，缺少 ${missingMetrics.join('、')}，分析结论建议结合实际练习情况判断。`
  }
  return '本次可用学习数据较少，分析结论建议结合实际练习情况判断。'
})

const prettyResult = computed(() => {
  if (!resultPayload.value) {
    return ''
  }
  return JSON.stringify(resultPayload.value, null, 2)
})

const historyFilterText = computed(() => {
  if (selectedSubject.value) {
    return `当前展示 ${selectedSubject.value.name} 的历史分析。`
  }
  return '当前展示全部学科的历史分析。'
})

const parseGradeFromSubjectName = (subjectName) => {
  if (!subjectName) {
    return ''
  }
  const gradePrefixes = ['高一', '高二', '高三', '初一', '初二', '初三', '小一', '小二', '小三', '小四', '小五', '小六']
  const matchedPrefix = gradePrefixes.find((prefix) => String(subjectName).startsWith(prefix))
  return matchedPrefix || ''
}

const buildTemplateQuery = (subjectName) => {
  return `请围绕我在${subjectName || '该学科'}的学习情况，输出一份学情分析，包含：1）当前掌握程度；2）薄弱知识点；3）下一阶段三条可执行学习建议；4）建议练习顺序。`
}

const normalizeNullableText = (value) => {
  if (value == null) {
    return undefined
  }
  const text = String(value).trim()
  return text ? text : undefined
}

const normalizeNullableSubjectId = (value) => {
  if (value == null || value === '') {
    return undefined
  }
  const parsed = Number(value)
  return Number.isInteger(parsed) && parsed > 0 ? parsed : undefined
}

const parseMaybeJson = (value) => {
  if (typeof value !== 'string') {
    return value
  }
  const text = value.trim()
  if (!text || (!text.startsWith('{') && !text.startsWith('['))) {
    return text
  }
  try {
    return JSON.parse(text)
  } catch (error) {
    return text
  }
}

const unwrapStructuredPayload = (value, depth = 0) => {
  const parsed = parseMaybeJson(value)
  if (depth > 3 || !parsed || typeof parsed !== 'object' || Array.isArray(parsed)) {
    return parsed
  }
  const structured = parseMaybeJson(parsed.structured_output || parsed.structuredOutput)
  if (structured && typeof structured === 'object' && !Array.isArray(structured)) {
    return unwrapStructuredPayload(structured, depth + 1)
  }
  const nestedTextKeys = ['text', 'result', 'answer', 'analysis', 'output', 'content']
  for (const key of nestedTextKeys) {
    const nested = parseMaybeJson(parsed[key])
    if (nested && typeof nested === 'object' && !Array.isArray(nested)) {
      return unwrapStructuredPayload(nested, depth + 1)
    }
  }
  return parsed
}

const cleanupText = (value) => {
  return String(value || '')
    .replace(/\r\n/g, '\n')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}

const firstKnownValue = (source, keys) => {
  if (!source || typeof source !== 'object') {
    return undefined
  }
  return keys.map((key) => source[key]).find((value) => !isEmptyDisplayValue(value))
}

const isEmptyDisplayValue = (value) => {
  if (value == null) {
    return true
  }
  const parsed = parseMaybeJson(value)
  if (parsed == null) {
    return true
  }
  if (typeof parsed === 'string') {
    return !parsed.trim()
  }
  if (Array.isArray(parsed)) {
    return parsed.length === 0
  }
  if (typeof parsed === 'object') {
    return Object.keys(parsed).length === 0
  }
  return false
}

const labelizeField = (key) => {
  if (FIELD_LABELS[key]) {
    return FIELD_LABELS[key]
  }
  return String(key)
    .replace(/_/g, ' ')
    .replace(/([a-z])([A-Z])/g, '$1 $2')
    .trim()
}

const booleanToText = (value) => {
  if (value === true) {
    return '是'
  }
  if (value === false) {
    return '否'
  }
  return ''
}

const isFalseLike = (value) => {
  if (value == null || value === '') {
    return false
  }
  return value === false || String(value).trim().toLowerCase() === 'false' || Number(value) === 0
}

const compactObjectSummary = (object) => {
  if (!object || typeof object !== 'object' || Array.isArray(object)) {
    return ''
  }
  const title = object.module || object.moduleName || object.module_name || object.name || object.title || ''
  const content = object.content || object.suggestion || object.text || object.description || object.reason || object.reasonText || object.reason_text || ''
  if (title && content) {
    return `${title}：${normalizeOutputValue(content)}`
  }
  if (content) {
    return normalizeOutputValue(content)
  }
  return ''
}

const objectToDisplayLines = (object) => {
  if (!object || typeof object !== 'object' || Array.isArray(object)) {
    return []
  }
  return Object.entries(object)
    .filter(([, value]) => !isEmptyDisplayValue(value))
    .map(([key, value]) => {
      const normalized = normalizeOutputValue(value)
      return normalized ? `${labelizeField(key)}：${normalized}` : ''
    })
    .filter(Boolean)
}

const normalizeOutputValue = (value) => {
  const parsed = parseMaybeJson(value)
  if (parsed == null) {
    return ''
  }
  if (typeof parsed === 'string') {
    return cleanupText(parsed)
  }
  if (typeof parsed === 'number') {
    return Number.isFinite(parsed) ? String(parsed) : ''
  }
  if (typeof parsed === 'boolean') {
    return booleanToText(parsed)
  }
  if (Array.isArray(parsed)) {
    return parsed.map((item) => normalizeOutputValue(item)).filter(Boolean).join('；')
  }
  if (typeof parsed === 'object') {
    const compact = compactObjectSummary(parsed)
    if (compact) {
      return compact
    }
    return objectToDisplayLines(parsed).join('；')
  }
  return String(parsed)
}

const extractPrimaryText = (value, depth = 0) => {
  if (depth > 3) {
    return ''
  }
  const parsed = parseMaybeJson(value)
  if (parsed == null) {
    return ''
  }
  if (typeof parsed === 'string') {
    return cleanupText(parsed)
  }
  if (typeof parsed === 'number' || typeof parsed === 'boolean') {
    return normalizeOutputValue(parsed)
  }
  if (Array.isArray(parsed)) {
    return parsed.map((item) => extractPrimaryText(item, depth + 1)).filter(Boolean).join('；')
  }
  if (typeof parsed === 'object') {
    for (const key of SUMMARY_KEYS) {
      if (!isEmptyDisplayValue(parsed[key])) {
        const text = extractPrimaryText(parsed[key], depth + 1)
        if (text) {
          return text
        }
      }
    }
  }
  return ''
}

const splitReadableText = (text) => {
  return cleanupText(text)
    .split(/\n+|[；;]/)
    .map((item) => item.replace(/^[-*•\d.、\s]+/, '').trim())
    .filter(Boolean)
}

const normalizeListItems = (value) => {
  const parsed = parseMaybeJson(value)
  if (parsed == null) {
    return []
  }
  if (Array.isArray(parsed)) {
    return parsed.map((item) => normalizeOutputValue(item)).filter(Boolean)
  }
  if (typeof parsed === 'object') {
    return objectToDisplayLines(parsed)
  }
  return splitReadableText(normalizeOutputValue(parsed))
}

const uniqueStrings = (items) => {
  const seen = new Set()
  const result = []
  items.forEach((item) => {
    const text = cleanupText(item)
    if (!text || seen.has(text)) {
      return
    }
    seen.add(text)
    result.push(text)
  })
  return result
}

const toReadableSection = (key, value) => {
  const parsed = parseMaybeJson(value)
  const items = normalizeListItems(parsed)
  const text = normalizeOutputValue(parsed)
  if (!items.length && !text) {
    return null
  }
  return {
    key,
    label: labelizeField(key),
    items: items.length > 1 ? items : [],
    text: items.length === 1 ? items[0] : text
  }
}

const normalizeScore = (value) => {
  if (value == null || value === '') {
    return null
  }
  const parsed = Number(value)
  if (!Number.isFinite(parsed)) {
    return null
  }
  const percent = parsed > 0 && parsed <= 1 ? parsed * 100 : parsed
  return Math.max(0, Math.min(100, Math.round(percent)))
}

const normalizeRatePercent = (value) => {
  if (value == null || value === '') {
    return null
  }
  const parsed = Number(value)
  if (!Number.isFinite(parsed)) {
    return null
  }
  const percent = parsed > 0 && parsed <= 1 ? parsed * 100 : parsed
  return Math.max(0, Math.min(100, Math.round(percent)))
}

const normalizeNumber = (value) => {
  if (value == null || value === '') {
    return null
  }
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : null
}

const normalizeKnowledgePoint = (kp, index) => {
  if (typeof kp === 'string') {
    return { name: kp, priority: Math.min(index + 1, 3), correctRate: null, correctRatePercent: null, suggestions: [] }
  }
  if (!kp || typeof kp !== 'object') return null
  const correctRate = normalizeNumber(kp.correctRate != null ? kp.correctRate : (kp.correct_rate != null ? kp.correct_rate : kp.accuracy))
  return {
    name: kp.name || kp.knowledgePoint || kp.knowledge_point || '',
    moduleName: kp.moduleName || kp.module || kp.module_name || '',
    reasonText: kp.reasonText || kp.reason || kp.reason_text || '',
    masteryLevel: kp.masteryLevel || kp.mastery_level || '',
    correctRate,
    correctRatePercent: normalizeRatePercent(correctRate),
    sampleCount: normalizeNumber(kp.sampleCount != null ? kp.sampleCount : (kp.sample_count != null ? kp.sample_count : (kp.totalCount != null ? kp.totalCount : kp.total_count))),
    wrongCount: normalizeNumber(kp.wrongCount != null ? kp.wrongCount : kp.wrong_count),
    priority: normalizeNumber(kp.priority) || Math.min(index + 1, 3),
    suggestions: normalizeListItems(kp.suggestions)
  }
}

const resolveRouteSubjectId = () => {
  return route.query.subjectId ? String(route.query.subjectId) : ''
}

const formatDateTime = (value) => {
  if (!value) {
    return '-'
  }
  let date
  if (Array.isArray(value)) {
    date = new Date(value[0], (value[1] || 1) - 1, value[2] || 1, value[3] || 0, value[4] || 0, value[5] || 0)
  } else {
    date = new Date(value)
  }
  if (Number.isNaN(date.getTime())) {
    return String(value)
  }
  return date.toLocaleString('zh-CN', { hour12: false })
}

const compactText = (value, maxLength = 80) => {
  const text = normalizeOutputValue(value).replace(/\s+/g, ' ').trim()
  if (!text) {
    return '-'
  }
  return text.length > maxLength ? `${text.slice(0, maxLength)}...` : text
}

const resolveStatusText = (status) => {
  if (Number(status) === 2) {
    return '失败'
  }
  if (Number(status) === 1) {
    return '成功'
  }
  return '未知'
}

const resolveStatusClass = (status) => {
  if (Number(status) === 2) {
    return 'failed'
  }
  if (Number(status) === 1) {
    return 'success'
  }
  return 'unknown'
}

const loadSubjects = async () => {
  try {
    const response = await subjectApi.listMySubjects()
    subjects.value = response.data || []
  } catch (error) {
    showToast(error.message || '加载学科失败')
  }
}

const loadHistory = async () => {
  historyLoading.value = true
  try {
    const response = await learningAnalysisApi.listHistory({
      subjectId: normalizeNullableSubjectId(form.subjectId),
      pageNum: historyPage.value,
      pageSize: historyPageSize
    })
    const page = normalizePageResult(response.data, {
      pageNum: historyPage.value,
      pageSize: historyPageSize
    })
    historyRecords.value = page.records
    historyTotal.value = page.total
    historyPage.value = page.pageNum
  } catch (error) {
    showToast(error.message || '加载历史分析记录失败')
  } finally {
    historyLoading.value = false
  }
}

const reloadHistoryFromFirstPage = () => {
  if (historyPage.value === 1) {
    loadHistory()
    return
  }
  historyPage.value = 1
}

const fillTemplateQuery = () => {
  if (!selectedSubject.value) {
    showToast('请先选择学科')
    return
  }
  form.query = buildTemplateQuery(selectedSubject.value.name)
}

const resetForm = () => {
  form.grade = parseGradeFromSubjectName(selectedSubject.value?.name)
  form.textbookVersion = ''
  form.query = selectedSubject.value ? buildTemplateQuery(selectedSubject.value.name) : ''
}

const runAnalysis = async () => {
  if (!selectedSubject.value) {
    showToast('请先选择学科')
    return
  }
  if (!form.query.trim()) {
    showToast('请输入分析问题')
    return
  }

  loading.value = true
  try {
    const response = await learningAnalysisApi.run({
      query: form.query.trim(),
      subject: selectedSubject.value.name,
      subjectId: normalizeNullableSubjectId(form.subjectId),
      grade: normalizeNullableText(form.grade),
      textbookVersion: normalizeNullableText(form.textbookVersion)
    })
    resultPayload.value = response.data || null
    const report = resultPayload.value?.analysisReport
    selectedHistoryId.value = report?.id || null
    analysisAt.value = report?.createTime ? formatDateTime(report.createTime) : new Date().toLocaleString('zh-CN', { hour12: false })
    reloadHistoryFromFirstPage()
    showToast('学情分析完成')
  } catch (error) {
    showToast(error.message || '学情分析失败')
  } finally {
    loading.value = false
  }
}

const openHistoryRecord = async (record) => {
  if (!record?.id) {
    return
  }
  detailLoading.value = true
  selectedHistoryId.value = record.id
  try {
    const response = await learningAnalysisApi.getById(record.id)
    resultPayload.value = response.data || record
    analysisAt.value = formatDateTime(resultPayload.value?.createTime || record.createTime)
  } catch (error) {
    showToast(error.message || '加载历史分析详情失败')
  } finally {
    detailLoading.value = false
  }
}

// ====== 视频搜索相关 ======
const videoSearchLoading = ref(false)
const videoSearchResult = ref([])
const videoSearchError = ref('')
const showVideoSearchModal = ref(false)
const videoSearchForm = reactive({
  knowledgePoint: ''
})

// 从分析结果中提取薄弱知识点作为快捷搜索关键词
const quickSearchKeywords = computed(() => {
  const report = currentReport.value
  if (!report) return []

  // 尝试从 knowledgePoints 中提取
  const kps = knowledgePoints.value
  if (Array.isArray(kps) && kps.length) {
    return kps.map(kp => kp.name).filter(Boolean).slice(0, 5)
  }

  // 尝试从可读明细中包含"薄弱"关键词的项提取
  const items = readableOutputSections.value
  if (Array.isArray(items)) {
    const weakItems = items.filter(
      item => item && item.label && /薄弱|弱项|不足|待提升/.test(item.label)
    )
    if (weakItems.length) {
      return weakItems.map(item => {
        const val = item.items?.length ? item.items.join(' ') : (item.text || '')
        return val.replace(/[；;，,、\s]+/g, ' ').trim().split(/\s+/)
      }).flat().filter(Boolean).slice(0, 5)
    }
  }

  // 尝试从总结文本中提取（简单分词）
  if (summaryText.value) {
    const matches = summaryText.value.match(/[\u4e00-\u9fa5]{2,8}(?:知识点|考点|概念|方法|原理|定理|公式)/g)
    if (matches && Array.isArray(matches) && matches.length) {
      return [...new Set(matches)].slice(0, 5)
    }
  }

  return []
})

const quickSearchVideo = async (keyword) => {
  videoSearchForm.knowledgePoint = keyword
  await doVideoSearch()
}

const searchWithCurrentInfo = () => {
  showVideoSearchModal.value = true
  // 如果有快捷关键词，预填第一个
  if (quickSearchKeywords.value.length && !videoSearchForm.knowledgePoint) {
    videoSearchForm.knowledgePoint = quickSearchKeywords.value[0]
  }
}

const closeVideoSearchModal = () => {
  showVideoSearchModal.value = false
}

const doVideoSearch = async () => {
  if (!videoSearchForm.knowledgePoint.trim()) {
    showToast('请输入知识点')
    return
  }

  videoSearchLoading.value = true
  videoSearchResult.value = []
  videoSearchError.value = ''
  showVideoSearchModal.value = false

  try {
    const response = await videoSearchApi.search({
      knowledgePoint: videoSearchForm.knowledgePoint.trim(),
      subject: selectedSubject.value?.name || undefined,
      subjectId: normalizeNullableSubjectId(form.subjectId),
      grade: normalizeNullableText(form.grade)
    })
    // request.js 拦截器已解包，response 就是后端 Result<T> 的 data，即 List<VideoSearchResultVO>
    videoSearchResult.value = response.data || []
    if (videoSearchResult.value.length) {
      showToast(`找到 ${videoSearchResult.value.length} 个视频链接`)
    } else {
      showToast('未找到相关视频')
    }
  } catch (error) {
    videoSearchError.value = error.message || '搜索视频失败'
    showToast(error.message || '搜索视频失败')
  } finally {
    videoSearchLoading.value = false
  }
}

watch(
  () => selectedSubject.value,
  (subject) => {
    if (!subject) {
      return
    }
    if (!form.grade) {
      form.grade = parseGradeFromSubjectName(subject.name)
    }
    if (!form.query.trim()) {
      form.query = buildTemplateQuery(subject.name)
    }
  }
)

watch(
  () => form.subjectId,
  () => {
    if (historyReady.value) {
      reloadHistoryFromFirstPage()
    }
  }
)

watch(historyPage, () => {
  if (historyReady.value) {
    loadHistory()
  }
})

watch(
  () => route.query.subjectId,
  () => {
    const subjectId = resolveRouteSubjectId()
    if (subjectId) {
      form.subjectId = subjectId
    }
  }
)

onMounted(async () => {
  await loadSubjects()
  const subjectId = resolveRouteSubjectId()
  if (subjectId) {
    form.subjectId = subjectId
  }
  await loadHistory()
  historyReady.value = true
})
</script>

<style scoped>
.analysis-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background: linear-gradient(135deg, #f3efe8 0%, #dbe7f8 100%);
}

.hero-card,
.form-card,
.history-card,
.result-card,
.video-search-card {
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
.form-actions {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  flex-wrap: wrap;
}

.form-card,
.history-card,
.result-card,
.video-search-card {
  margin-top: 18px;
  padding: 24px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.form-grid label,
.query-field {
  display: grid;
  gap: 8px;
}

.form-grid span,
.query-field span {
  color: #17324d;
  font-size: 14px;
  font-weight: 600;
}

.form-grid select,
.form-grid input,
.query-field textarea {
  width: 100%;
  border: 1px solid #cad7e3;
  border-radius: 14px;
  padding: 12px 14px;
  box-sizing: border-box;
  font-size: 14px;
  color: #17324d;
  background: #fff;
}

.query-field {
  margin-top: 16px;
}

.query-field textarea {
  resize: vertical;
  min-height: 140px;
  line-height: 1.6;
}

.form-actions {
  margin-top: 16px;
}

.result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.history-head {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: flex-start;
  flex-wrap: wrap;
}

.history-head h2,
.result-head h2,
.video-search-head h2 {
  margin: 0;
  color: #17324d;
  font-size: 24px;
}

.history-head p {
  margin: 8px 0 0;
  color: #6c7a8d;
  font-size: 14px;
}

.history-list {
  margin-top: 16px;
  display: grid;
  gap: 12px;
}

.history-item {
  width: 100%;
  display: grid;
  gap: 9px;
  text-align: left;
  border: 1px solid #d8e2ec;
  border-radius: 16px;
  padding: 14px;
  background: #fff;
  color: #17324d;
}

.history-item:hover,
.history-item.active {
  border-color: #5b8ec2;
  background: #f5f9fd;
}

.history-title,
.history-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.history-title strong {
  font-size: 16px;
}

.history-query,
.history-summary {
  margin: 0;
  line-height: 1.6;
}

.history-query {
  color: #344d66;
  font-weight: 600;
}

.history-summary {
  color: #5e6d7c;
}

.history-meta {
  color: #7a8793;
  font-size: 12px;
}

.status-pill {
  border-radius: 999px;
  padding: 3px 8px;
  font-size: 12px;
}

.status-pill.success {
  background: #e7f7ed;
  color: #1e7b45;
}

.status-pill.failed {
  background: #fdecec;
  color: #b3261e;
}

.status-pill.unknown {
  background: #edf1f5;
  color: #627184;
}

.result-head span,
.video-search-head .tip {
  color: #6c7a8d;
  font-size: 13px;
}

.empty-state {
  margin-top: 16px;
  padding: 34px;
  text-align: center;
  color: #6c7a8d;
  background: #f3f7fb;
  border-radius: 18px;
}

/* 增强的结果区域样式 */
.enhanced-result-content {
  display: grid;
  gap: 18px;
}

.section-card {
  padding: 22px;
  border-radius: 20px;
  background: #f7fbff;
  border: 1px solid #eaf2fb;
}

.quality-notice {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid #f4d48d;
  border-radius: 14px;
  background: #fff8e7;
  color: #6c4a12;
  line-height: 1.6;
}

.quality-notice strong {
  flex-shrink: 0;
  color: #8a5a00;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
}

.section-title .section-icon {
  width: 34px;
  height: 34px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: #17324d;
  color: #fff;
  font-size: 13px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}

.section-title h3 {
  margin: 0;
  color: #17324d;
  font-size: 20px;
  font-weight: 700;
}

/* 总体评估卡片 */
.overall-card {
  background: linear-gradient(135deg, #f0f9ff 0%, #fef3c7 100%);
  border-color: #fde68a;
}

.overall-content {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
  align-items: center;
}

.mastery-ring-wrapper {
  flex-shrink: 0;
}

.ring-container {
  position: relative;
  width: 140px;
  height: 140px;
}

.ring-svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.ring-bg {
  fill: none;
  stroke: #e5edf6;
  stroke-width: 10;
}

.ring-progress {
  fill: none;
  stroke: url(#ringGradient);
  stroke-width: 10;
  stroke-linecap: round;
  transition: stroke-dasharray 0.8s ease;
}

/* 内联 SVG 渐变 */
.ring-svg {
  --gradient-start: #3b82f6;
  --gradient-end: #10b981;
}

.ring-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.ring-text .score {
  font-size: 36px;
  font-weight: 800;
  color: #17324d;
  line-height: 1;
}

.ring-text .label {
  font-size: 14px;
  color: #6c7a8d;
  margin-top: 4px;
}

.mastery-info {
  flex: 1;
  min-width: 280px;
}

.mastery-level-badge {
  display: inline-block;
  padding: 6px 16px;
  background: #17324d;
  color: #fff;
  border-radius: 999px;
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 12px;
}

.mastery-trend {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #059669;
  font-weight: 600;
  margin-bottom: 20px;
}

.strengths-weaknesses {
  display: grid;
  gap: 16px;
}

.tag-group {
  display: grid;
  gap: 8px;
}

.tag-label {
  font-size: 13px;
  font-weight: 600;
  color: #6c7a8d;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 500;
}

.tag-success {
  background: #d1fae5;
  color: #059669;
}

.tag-warning {
  background: #fee2e2;
  color: #dc2626;
}

/* 总结卡片 */
.summary-text {
  margin: 0;
  color: #334b64;
  line-height: 1.9;
  font-size: 15px;
  white-space: pre-wrap;
}

/* 完整分析文本 */
.fulltext-content {
  margin: 0;
  color: #334b64;
  line-height: 2;
  font-size: 15px;
  white-space: pre-wrap;
}

/* 知识点标签式展示（无详情时） */
.kp-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.kp-tag {
  display: inline-block;
  padding: 8px 16px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 500;
  color: #17324d;
  background: #edf4fb;
  border: 1px solid #d8e2ec;
}

.kp-tag.kp-priority-1 {
  background: #fee2e2;
  color: #dc2626;
  border-color: #fecaca;
}

.kp-tag.kp-priority-2 {
  background: #fef3c7;
  color: #d97706;
  border-color: #fde68a;
}

.kp-tag.kp-priority-3 {
  background: #dbeafe;
  color: #2563eb;
  border-color: #bfdbfe;
}

/* 知识点卡片 */
.knowledge-list {
  display: grid;
  gap: 16px;
}

.knowledge-item {
  background: #fff;
  border-radius: 16px;
  padding: 18px;
  border: 1px solid #eaf2fb;
}

.kp-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.kp-name {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 17px;
  font-weight: 700;
  color: #17324d;
}

.priority-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.priority-dot.priority-1 {
  background: #ef4444;
}

.priority-dot.priority-2 {
  background: #f59e0b;
}

.priority-dot.priority-3 {
  background: #3b82f6;
}

.kp-module {
  font-size: 13px;
  color: #6c7a8d;
  background: #e5edf6;
  padding: 4px 10px;
  border-radius: 999px;
}

.kp-mastery {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.mastery-label {
  font-size: 13px;
  font-weight: 600;
  color: #6c7a8d;
  min-width: 60px;
}

.mastery-bar {
  flex: 1;
  height: 20px;
  background: #e5edf6;
  border-radius: 10px;
  position: relative;
  overflow: hidden;
}

.mastery-bar .bar-bg {
  position: absolute;
  inset: 0;
}

.mastery-bar .bar-fill {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, #10b981 0%, #34d399 100%);
  border-radius: 10px;
  transition: width 0.6s ease;
}

.mastery-bar .bar-text {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 11px;
  font-weight: 700;
  color: #17324d;
}

.kp-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 10px;
}

.kp-stats .stat {
  font-size: 13px;
  color: #6c7a8d;
}

.kp-reason {
  margin: 0 0 12px;
  font-size: 14px;
  color: #42596f;
  line-height: 1.7;
}

.kp-suggestions {
  padding-top: 12px;
  border-top: 1px dashed #eaf2fb;
}

.sugg-label {
  font-size: 13px;
  font-weight: 600;
  color: #3b82f6;
  margin-bottom: 8px;
}

.sugg-list {
  margin: 0;
  padding-left: 20px;
  display: grid;
  gap: 6px;
}

.sugg-list li {
  font-size: 14px;
  color: #42596f;
  line-height: 1.6;
}

/* 建议卡片 */
.suggestions-container {
  display: grid;
  gap: 20px;
}

.sugg-section {
  background: #fff;
  border-radius: 14px;
  padding: 16px;
}

.sugg-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #17324d;
  margin-bottom: 12px;
}

.sugg-index {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: 8px;
  background: #e5edf6;
  color: #17324d;
  font-size: 13px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}

.sugg-items {
  margin: 0;
  padding-left: 22px;
  display: grid;
  gap: 8px;
}

.sugg-items li {
  font-size: 14px;
  color: #42596f;
  line-height: 1.7;
}

/* 学习计划卡片 */
.plan-list {
  display: grid;
  gap: 14px;
}

.plan-step {
  display: flex;
  gap: 16px;
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  border: 1px solid #eaf2fb;
}

.step-number {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 18px;
}

.step-content {
  flex: 1;
}

.step-topic {
  font-size: 16px;
  font-weight: 700;
  color: #17324d;
  margin-bottom: 6px;
}

.step-meta,
.step-resources {
  font-size: 13px;
  color: #6c7a8d;
  margin-top: 4px;
}

/* 重点明细 */
.detail-section-list {
  display: grid;
  gap: 12px;
}

.detail-section {
  padding: 15px 16px;
  border: 1px solid #e5edf6;
  border-radius: 14px;
  background: #fff;
}

.detail-section h4 {
  margin: 0 0 10px;
  color: #17324d;
  font-size: 15px;
  font-weight: 700;
}

.detail-list {
  margin: 0;
  padding-left: 20px;
  display: grid;
  gap: 7px;
}

.detail-list li,
.detail-text {
  color: #42596f;
  line-height: 1.6;
  font-size: 14px;
  text-wrap: pretty;
}

.detail-text {
  margin: 0;
  white-space: pre-wrap;
}

.raw-card {
  padding: 14px 16px;
  border: 1px dashed #cad7e3;
  border-radius: 14px;
  background: #f7fbff;
}

.raw-card summary {
  cursor: pointer;
  color: #17324d;
  font-weight: 600;
  padding: 4px 0;
}

.raw-card pre {
  margin: 12px 0 0;
  padding: 14px;
  border-radius: 12px;
  background: #101923;
  color: #d7e3f0;
  overflow: auto;
  font-size: 12px;
  line-height: 1.5;
}

button,
.ghost {
  border: 0;
  border-radius: 14px;
  padding: 11px 16px;
  font-size: 14px;
  cursor: pointer;
  text-decoration: none;
}

button {
  background: #17324d;
  color: #fff;
}

button:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.ghost {
  background: #e5edf6;
  color: #17324d;
}

/* 视频搜索区域样式保持不变 */
.video-search-head {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.video-search-content {
  margin-top: 16px;
}

.quick-search {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.quick-search-btn {
  font-size: 13px !important;
  padding: 8px 14px !important;
  border-radius: 20px !important;
  transition-property: transform, background-color;
  transition-duration: 0.2s;
  transition-timing-function: ease;
}

.quick-search-btn:hover {
  background: #c8daf0;
  transform: translateY(-1px);
}

.video-search-loading {
  padding: 34px;
  text-align: center;
  color: #6c7a8d;
  background: #f3f7fb;
  border-radius: 18px;
}

.video-search-empty {
  padding: 24px;
  text-align: center;
  color: #6c7a8d;
  background: #f3f7fb;
  border-radius: 18px;
}

.video-search-empty p {
  margin: 0;
}

.video-results h3 {
  margin: 0 0 12px;
  color: #17324d;
  font-size: 18px;
}

.video-list {
  display: grid;
  gap: 10px;
}

.video-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px;
  border-radius: 16px;
  background: #f7fbff;
  border: 1px solid #d8e2ec;
  text-decoration: none;
  color: #17324d;
  transition-property: transform, border-color, background-color;
  transition-duration: 0.2s;
  transition-timing-function: ease;
}

.video-item:hover {
  border-color: #5b8ec2;
  background: #edf4fb;
  transform: translateX(4px);
}

.video-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
}

.video-icon.bilibili {
  background: linear-gradient(135deg, #fb7299, #e45a7c);
}

.video-info {
  flex: 1;
  min-width: 0;
}

.video-title {
  font-size: 15px;
  font-weight: 600;
  color: #17324d;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-keyword {
  font-size: 12px;
  color: #7a8793;
}

.video-arrow {
  flex-shrink: 0;
  font-size: 18px;
  color: #5b8ec2;
  transition: transform 0.2s ease;
}

.video-item:hover .video-arrow {
  transform: translateX(4px);
}

.video-search-error {
  padding: 18px;
  text-align: center;
  color: #b3261e;
  background: #fdecec;
  border-radius: 14px;
}

.video-search-error p {
  margin: 0;
}

/* 视频搜索弹窗 */
.video-search-modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.video-search-modal {
  width: 90%;
  max-width: 460px;
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.modal-header h3 {
  margin: 0;
  color: #17324d;
  font-size: 20px;
}

.close-btn {
  border: 0;
  background: #edf1f5;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #627184;
  padding: 0;
}

.modal-body label {
  display: grid;
  gap: 8px;
}

.modal-body span {
  color: #17324d;
  font-size: 14px;
  font-weight: 600;
}

.modal-body input {
  width: 100%;
  border: 1px solid #cad7e3;
  border-radius: 14px;
  padding: 12px 14px;
  box-sizing: border-box;
  font-size: 14px;
  color: #17324d;
  background: #fff;
}

.modal-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 18px;
}

/* 添加内联 SVG 渐变支持 */
.section-card svg {
  display: block;
}

@media (max-width: 768px) {
  .analysis-page {
    padding: 18px 14px 30px;
  }

  .hero-card,
  .form-card,
  .history-card,
  .result-card,
  .video-search-card {
    border-radius: 22px;
    padding: 20px;
  }

  .hero-card h1 {
    font-size: 28px;
  }

  .overall-content {
    flex-direction: column;
    text-align: center;
  }

  .mastery-info {
    text-align: left;
  }

  .video-item {
    padding: 10px;
    gap: 10px;
  }

  .plan-step {
    flex-direction: column;
    text-align: center;
  }

  .step-content {
    text-align: left;
  }
}
</style>
