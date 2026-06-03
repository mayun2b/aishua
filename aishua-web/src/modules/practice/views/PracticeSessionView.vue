<template>
  <div class="practice-session-page">
    <section class="hero-card">
      <div>
        <p class="eyebrow">Practice Session</p>
        <h1>独立作答页面</h1>
        <p class="description">
          当前页面仅用于答题与提交，配置参数已在上一页确定。
        </p>
      </div>

      <div class="hero-actions">
        <router-link class="ghost" :to="configBackLink">返回练习配置</router-link>
        <router-link class="ghost" to="/practice-records">练习记录</router-link>
      </div>
    </section>

    <section v-if="loadingInitial" class="state-card">正在加载练习会话...</section>

    <section v-else-if="loadError" class="state-card">
      <p>{{ loadError }}</p>
      <div class="state-actions">
        <button type="button" class="secondary-button" @click="goToConfig">返回练习配置</button>
        <button type="button" @click="reloadPage">重新加载</button>
      </div>
    </section>

    <template v-else>
      <section v-if="session" class="summary-card">
        <div class="summary-item">
          <span>学科</span>
          <strong>{{ session.subjectName || '-' }}</strong>
        </div>
        <div class="summary-item">
          <span>模式</span>
          <strong>{{ resolveModeLabel(session.practiceMode) }}</strong>
        </div>
        <div class="summary-item">
          <span>题目数量</span>
          <strong>{{ questions.length }}</strong>
        </div>
        <div class="summary-item">
          <span>作答进度</span>
          <strong>{{ progressText }}</strong>
        </div>
      </section>

      <section class="sheet-card">
        <div class="section-head">
          <h2>答题导航</h2>
          <span>{{ draftStatusText }}</span>
        </div>

        <div v-if="loadingSheet" class="empty-state">正在加载本次题目...</div>
        <div v-else-if="!questions.length" class="empty-state">当前练习没有可作答题目。</div>
        <div v-else class="sheet-body">
          <div class="question-nav">
            <button
              v-for="(question, index) in questions"
              :key="question.questionId"
              type="button"
              :class="[
                'nav-chip',
                index === currentIndex ? 'active' : '',
                isAnswered(question.questionId) ? 'answered' : '',
                getQuestionResult(question.questionId)?.isCorrect === 1 ? 'correct' : '',
                getQuestionResult(question.questionId)?.isCorrect === 0 ? 'wrong' : ''
              ]"
              @click="goToQuestion(index)"
            >
              {{ index + 1 }}
            </button>
          </div>

          <div v-if="currentQuestion" class="question-panel">
            <div class="question-head">
              <div class="question-meta">
                <span class="chip">{{ resolveTypeLabel(currentQuestion.type) }}</span>
                <span class="chip ghost-chip">{{ resolveDifficultyLabel(currentQuestion.difficulty) }}</span>
                <span class="chip ghost-chip">第 {{ currentIndex + 1 }} / {{ questions.length }} 题</span>
              </div>
              <span class="answer-state">
                {{ isAnswered(currentQuestion.questionId) ? '已作答' : '未作答' }}
              </span>
            </div>

            <h3>{{ currentQuestion.title }}</h3>
            <p v-if="currentQuestion.content" class="question-content">{{ currentQuestion.content }}</p>
            <QuestionImageList
              v-model="currentImageAnnotationDrafts"
              :image-urls="currentQuestion.imageUrls"
              :image-desc="currentQuestion.imageDesc"
              :annotation-object-names="currentImageAnnotationObjectNames"
              :readonly="isSubmitted"
              @dirty="markCurrentImageAnnotationDirty"
            />

            <div v-if="currentQuestion.type === 1 || currentQuestion.type === 2" class="options-list">
              <label v-for="option in parsedOptions" :key="option.optionKey" class="option-item">
                <input
                  v-if="currentQuestion.type === 2"
                  v-model="multiChoiceAnswer"
                  type="checkbox"
                  :value="option.optionValue"
                  :disabled="isSubmitted"
                />
                <input
                  v-else
                  v-model="singleChoiceAnswer"
                  type="radio"
                  :value="option.optionValue"
                  :disabled="isSubmitted"
                />
                <span class="option-marker">{{ option.optionLabel }}</span>
                <span class="option-text">{{ option.optionText }}</span>
              </label>
            </div>

            <div v-else-if="currentQuestion.type === 3" class="options-list">
              <label class="option-item">
                <input v-model="singleChoiceAnswer" type="radio" value="正确" :disabled="isSubmitted" />
                <span>正确</span>
              </label>
              <label class="option-item">
                <input v-model="singleChoiceAnswer" type="radio" value="错误" :disabled="isSubmitted" />
                <span>错误</span>
              </label>
            </div>

            <div v-else class="answer-block" :class="currentQuestion.type === 5 ? 'essay-answer-block' : ''">
              <div v-if="currentQuestion.type === 5" class="essay-toolbar">
                <div class="essay-mode-group">
                  <button
                    type="button"
                    :class="['essay-mode-button', currentEssayMode === 'text' ? 'active' : '']"
                    :disabled="isSubmitted"
                    @click="currentEssayMode = 'text'"
                  >
                    文本
                  </button>
                  <button
                    type="button"
                    :class="['essay-mode-button', currentEssayMode === 'canvas' ? 'active' : '']"
                    :disabled="isSubmitted"
                    @click="currentEssayMode = 'canvas'"
                  >
                    手写
                  </button>
                </div>
                <div class="essay-meta">
                  <span class="essay-tip">支持多行输入，Ctrl + Enter 可快速进入下一题。</span>
                  <span class="essay-count">已输入 {{ textAnswerLength }} 字</span>
                  <span v-if="currentEssayCanvasObjectName" class="essay-upload-status">手写草稿已保存</span>
                </div>
              </div>
              <div v-if="currentQuestion.type === 5 && currentEssayMode === 'canvas'" class="essay-canvas-wrap">
                <AnswerCanvas v-model="currentEssayCanvasDraft" :disabled="isSubmitted" :height="300" />
                <div class="essay-canvas-actions">
                  <button
                    type="button"
                    class="secondary-button small-button"
                    :disabled="isSubmitted || !currentEssayCanvasDraft"
                    @click="clearEssayCanvasDraft"
                  >
                    清空手写
                  </button>
                </div>
              </div>
              <textarea
                v-model="textAnswer"
                :class="currentQuestion.type === 5 && currentEssayMode === 'canvas' ? 'essay-conclusion-textarea' : ''"
                :rows="currentQuestion.type === 5 && currentEssayMode === 'canvas' ? 5 : currentQuestion.type === 5 ? 10 : 5"
                maxlength="2000"
                :disabled="isSubmitted"
                :placeholder="currentQuestion.type === 4
                  ? '请输入填空答案'
                  : currentQuestion.type === 5 && currentEssayMode === 'canvas'
                    ? '可选：补充文字结论，便于系统判题'
                    : '请输入你的作答内容'"
                @keydown.ctrl.enter.prevent="handleTextAnswerCtrlEnter"
              ></textarea>
              <div v-if="currentQuestion.type === 5" class="essay-actions">
                <button type="button" class="secondary-button small-button" :disabled="isSubmitted || !textAnswerLength" @click="clearTextAnswer">
                  清空文本
                </button>
                <button type="button" class="secondary-button small-button" :disabled="isSubmitted" @click="handleTextAnswerCtrlEnter">
                  下一题
                </button>
              </div>
            </div>

            <div class="question-actions">
              <button type="button" class="secondary-button" :disabled="currentIndex === 0" @click="goToPreviousQuestion">
                上一题
              </button>
              <button
                type="button"
                class="secondary-button"
                :disabled="currentIndex >= questions.length - 1"
                @click="goToNextQuestion"
              >
                下一题
              </button>
              <button type="button" :disabled="submitting || essayCanvasUploading || imageAnnotationUploading || !canSubmitAll" @click="submitAllAnswers">
                {{ submitting ? '正在提交...' : '统一提交' }}
              </button>
            </div>

            <div v-if="!isSubmitted" :class="['assistant-card', { collapsed: !assistantPanelVisible }]">
              <div class="assistant-head">
                <h2>AI 做题辅助助手</h2>
                <div class="assistant-actions">
                  <button type="button" class="secondary-button" @click="assistantPanelVisible = !assistantPanelVisible">
                    {{ assistantPanelVisible ? '收起' : '展开' }}
                  </button>
                  <button
                    v-if="assistantPanelVisible && assistantSession?.status === 1"
                    type="button"
                    class="secondary-button"
                    :disabled="assistantSending"
                    @click="closePracticeAssistantSession"
                  >
                    结束会话
                  </button>
                </div>
              </div>

              <template v-if="assistantPanelVisible">
                <div v-if="assistantLoading" class="assistant-empty">正在加载辅助会话...</div>
                <div v-else class="assistant-body">
                  <div v-if="!assistantMessages.length" class="assistant-empty">
                    当前还没有消息，开始提问吧。
                  </div>
                  <div ref="assistantMessagesContainer" v-else class="assistant-messages">
                    <article
                      v-for="message in assistantMessages"
                      :key="message.messageId"
                      :class="['assistant-message', message.roleName === 'user' ? 'user' : 'assistant']"
                    >
                      <p class="role">{{ message.roleName === 'user' ? '你' : 'AI' }}</p>
                      <p class="content">
                        {{ resolveAssistantMessageDisplayText(message) }}
                      </p>
                    </article>
                  </div>
                </div>

                <div class="assistant-input-row">
                  <textarea
                    v-model="assistantInput"
                    rows="2"
                    maxlength="1000"
                    :disabled="assistantSending || assistantLoading"
                    placeholder="例如：这一步为什么错了？"
                    @keydown.enter.exact.prevent="sendPracticeAssistantMessage"
                  ></textarea>
                  <button type="button" :disabled="!canSendAssistantMessage" @click="sendPracticeAssistantMessage">
                    {{ assistantSending ? '发送中...' : '发送' }}
                  </button>
                </div>
                <p class="assistant-tip">提示：AI 辅导仅供学习参考。</p>
              </template>
            </div>

            <div v-if="currentResult" class="analysis-card">
              <div class="section-head compact">
                <h2>当前题判定</h2>
                <span :class="['result-badge', Number(currentResult.isCorrect) === 1 ? 'success' : 'danger']">
                  {{ Number(currentResult.isCorrect) === 1 ? '回答正确' : '回答错误' }}
                </span>
              </div>

              <div class="result-grid">
                <div class="result-item">
                  <span>你的答案</span>
                  <strong>{{ formatAnswerDisplay(currentResult.userAnswer) }}</strong>
                </div>
                <div class="result-item">
                  <span>标准答案</span>
                  <strong>{{ formatAnswerDisplay(currentResult.correctAnswer || currentResult.standardAnswer) }}</strong>
                </div>
                <div class="result-item">
                  <span>本题耗时</span>
                  <strong>{{ currentResult.timeCost || 0 }} 秒</strong>
                </div>
              </div>

              <p v-if="currentResult.analysis" class="analysis-text">{{ currentResult.analysis }}</p>
            </div>
          </div>
        </div>
      </section>

      <section v-if="summaryResult" class="result-card">
        <div class="section-head">
          <h2>提交结果</h2>
          <span class="result-badge success">已完成</span>
        </div>

        <div class="result-grid">
          <div class="result-item">
              <span>总题数</span>
            <strong>{{ summaryResult.questionCount }}</strong>
          </div>
          <div class="result-item">
            <span>答对</span>
            <strong>{{ summaryResult.correctCount }}</strong>
          </div>
          <div class="result-item">
            <span>答错</span>
            <strong>{{ summaryResult.wrongCount }}</strong>
          </div>
          <div class="result-item">
              <span>正确率</span>
            <strong>{{ summaryResult.correctRate }}%</strong>
          </div>
        </div>

        <div class="config-actions">
          <button type="button" @click="goToConfig">重新开始</button>
          <router-link class="ghost" :to="`/practice-records/${sessionId}`">查看本次记录</router-link>
        </div>
      </section>

    </template>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import AnswerCanvas from '../../../components/AnswerCanvas.vue'
import QuestionImageList from '../../../components/QuestionImageList.vue'
import fileApi from '../../common/api/file'
import {
  buildEssayAnswerPayload,
  hasEssayCanvasMarker,
  parseEssayAnswerPayload,
  stripEssayCanvasMarker
} from '../../common/utils/essayCanvasAnswer'
import {
  buildImageAnnotationPayload,
  parseImageAnnotationPayload
} from '../../common/utils/imageAnnotationAnswer'
import { parseQuestionImageRefs } from '../../common/utils/questionImages'
import practiceApi from '../api/practice'

const route = useRoute()
const router = useRouter()

const AUTO_SAVE_INTERVAL_MS = 30000
const ASSISTANT_TYPEWRITER_INTERVAL_MS = 20
const ASSISTANT_TYPEWRITER_CHARS_PER_TICK = 2
let autoSaveTimer = null

const loadingInitial = ref(false)
const loadingSheet = ref(false)
const submitting = ref(false)
const savingDraft = ref(false)
const hasDraftChanges = ref(false)
const draftSavedAt = ref('')
const draftVersion = ref(0)
const loadError = ref('')

const session = ref(null)
const questionSheet = ref(null)
const submitResult = ref(null)
const judgedRecords = ref([])

const currentIndex = ref(0)
const currentQuestionEnteredAt = ref(0)
const answers = ref({})
const timeSpentMs = ref({})
const essayAnswerModes = ref({})
const essayCanvasDrafts = ref({})
const essayCanvasObjectNames = ref({})
const essayCanvasUploading = ref(false)
const essayCanvasLoading = ref({})
const imageAnnotationDrafts = ref({})
const imageAnnotationObjectNames = ref({})
const imageAnnotationUploading = ref(false)
const imageAnnotationLoading = ref({})
const draftDirtyQuestionIds = new Set()
const assistantLoading = ref(false)
const assistantSending = ref(false)
const assistantSession = ref(null)
const assistantMessages = ref([])
const assistantInput = ref('')
const assistantPanelVisible = ref(true)
const assistantMessagesContainer = ref(null)
let assistantLoadVersion = 0
let assistantLocalMessageSeed = 0
let assistantScrollFrame = 0
let assistantStreamAbortController = null
let activeAssistantTypewriter = null

const sessionId = computed(() => Number(route.params.sessionId || 0))
const questions = computed(() => questionSheet.value?.questions || [])
const currentQuestion = computed(() => questions.value[currentIndex.value] || null)
const questionMapById = computed(() => {
  const entries = questions.value
    .filter((item) => item?.questionId)
    .map((item) => [Number(item.questionId), item])
  return Object.fromEntries(entries)
})

const configBackLink = computed(() => {
  const subjectId = session.value?.subjectId || route.query.subjectId
  const mode = session.value?.practiceMode || route.query.mode
  const query = new URLSearchParams()
  if (subjectId) {
    query.set('subjectId', String(subjectId))
  }
  if (mode) {
    query.set('mode', String(mode))
  }
  const suffix = query.toString()
  return suffix ? `/practice?${suffix}` : '/practice'
})

const isSubmitted = computed(() => {
  return Boolean(submitResult.value) || Number(session.value?.status) === 2
})

const answeredCount = computed(() => questions.value.filter((question) => isAnswered(question.questionId)).length)
const unansweredCount = computed(() => Math.max(questions.value.length - answeredCount.value, 0))
const canSubmitAll = computed(() => questions.value.length > 0 && unansweredCount.value === 0 && !isSubmitted.value)
const progressText = computed(() => `${answeredCount.value} / ${questions.value.length}`)
const canSendAssistantMessage = computed(() => {
  if (isSubmitted.value) {
    return false
  }
  if (!currentQuestion.value?.questionId) {
    return false
  }
  if (assistantLoading.value || assistantSending.value) {
    return false
  }
  return assistantInput.value.trim().length > 0
})

const currentEssayMode = computed({
  get() {
    const questionId = Number(currentQuestion.value?.questionId || 0)
    if (!questionId || Number(currentQuestion.value?.type) !== 5) {
      return 'text'
    }
    return essayAnswerModes.value[questionId] === 'canvas' ? 'canvas' : 'text'
  },
  set(mode) {
    const questionId = Number(currentQuestion.value?.questionId || 0)
    if (!questionId || Number(currentQuestion.value?.type) !== 5 || isSubmitted.value) {
      return
    }
    const nextMode = mode === 'canvas' ? 'canvas' : 'text'
    if (essayAnswerModes.value[questionId] === nextMode) {
      return
    }
    essayAnswerModes.value = {
      ...essayAnswerModes.value,
      [questionId]: nextMode
    }
    markDraftChanged(questionId)
  }
})

const currentEssayCanvasDraft = computed({
  get() {
    const questionId = Number(currentQuestion.value?.questionId || 0)
    if (!questionId || Number(currentQuestion.value?.type) !== 5) {
      return ''
    }
    return essayCanvasDrafts.value[questionId] || ''
  },
  set(value) {
    const questionId = Number(currentQuestion.value?.questionId || 0)
    if (!questionId || Number(currentQuestion.value?.type) !== 5 || isSubmitted.value) {
      return
    }
    const nextDraft = value ? String(value) : ''
    const oldDraft = String(essayCanvasDrafts.value[questionId] || '')
    const oldObjectName = String(essayCanvasObjectNames.value[questionId] || '')
    if (nextDraft === oldDraft && !oldObjectName) {
      return
    }
    essayCanvasDrafts.value = {
      ...essayCanvasDrafts.value,
      [questionId]: nextDraft
    }
    essayCanvasObjectNames.value = {
      ...essayCanvasObjectNames.value,
      [questionId]: ''
    }
    markDraftChanged(questionId)
  }
})

const currentEssayCanvasObjectName = computed(() => {
  const questionId = Number(currentQuestion.value?.questionId || 0)
  if (!questionId || Number(currentQuestion.value?.type) !== 5) {
    return ''
  }
  return String(essayCanvasObjectNames.value[questionId] || '').trim()
})

const currentImageAnnotationDrafts = computed({
  get() {
    const questionId = Number(currentQuestion.value?.questionId || 0)
    if (!questionId) {
      return {}
    }
    return imageAnnotationDrafts.value[questionId] || {}
  },
  set(value) {
    const questionId = Number(currentQuestion.value?.questionId || 0)
    if (!questionId || isSubmitted.value) {
      return
    }
    const nextDrafts = value || {}
    const previousDrafts = imageAnnotationDrafts.value[questionId] || {}
    const previousObjectNames = imageAnnotationObjectNames.value[questionId] || {}
    const nextObjectNames = { ...previousObjectNames }
    for (const [imageKey, dataUrl] of Object.entries(nextDrafts)) {
      if (String(dataUrl || '') !== String(previousDrafts[imageKey] || '')) {
        nextObjectNames[imageKey] = ''
      }
    }
    imageAnnotationDrafts.value = {
      ...imageAnnotationDrafts.value,
      [questionId]: nextDrafts
    }
    imageAnnotationObjectNames.value = {
      ...imageAnnotationObjectNames.value,
      [questionId]: nextObjectNames
    }
    markDraftChanged(questionId)
  }
})

const currentImageAnnotationObjectNames = computed(() => {
  const questionId = Number(currentQuestion.value?.questionId || 0)
  if (!questionId) {
    return {}
  }
  return imageAnnotationObjectNames.value[questionId] || {}
})

const markCurrentImageAnnotationDirty = () => {
  const questionId = Number(currentQuestion.value?.questionId || 0)
  if (questionId) {
    markDraftChanged(questionId)
  }
}

const blobToDataUrl = (blob) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result || ''))
    reader.onerror = () => reject(new Error('手写草稿读取失败'))
    reader.readAsDataURL(blob)
  })
}

const loadEssayCanvasDraftFromObjectName = async (questionId, { silent = true } = {}) => {
  const normalizedQuestionId = Number(questionId || 0)
  if (!normalizedQuestionId) {
    return ''
  }
  const objectName = String(essayCanvasObjectNames.value[normalizedQuestionId] || '').trim()
  if (!objectName) {
    return ''
  }
  const draft = String(essayCanvasDrafts.value[normalizedQuestionId] || '').trim()
  if (draft) {
    return draft
  }
  if (essayCanvasLoading.value[normalizedQuestionId]) {
    return ''
  }

  essayCanvasLoading.value = {
    ...essayCanvasLoading.value,
    [normalizedQuestionId]: true
  }

  try {
    const blob = await fileApi.download(objectName)
    const dataUrl = await blobToDataUrl(blob)
    if (!dataUrl) {
      return ''
    }
    if (String(essayCanvasObjectNames.value[normalizedQuestionId] || '').trim() !== objectName) {
      return ''
    }
    essayCanvasDrafts.value = {
      ...essayCanvasDrafts.value,
      [normalizedQuestionId]: dataUrl
    }
    return dataUrl
  } catch (error) {
    if (!silent) {
      showToast(error.message || '手写草稿加载失败')
    }
    return ''
  } finally {
    essayCanvasLoading.value = {
      ...essayCanvasLoading.value,
      [normalizedQuestionId]: false
    }
  }
}

const ensureCurrentEssayCanvasDraftLoaded = async ({ silent = true } = {}) => {
  const questionId = Number(currentQuestion.value?.questionId || 0)
  if (!questionId || Number(currentQuestion.value?.type) !== 5) {
    return
  }
  if (currentEssayMode.value !== 'canvas') {
    return
  }
  await loadEssayCanvasDraftFromObjectName(questionId, { silent })
}

const loadImageAnnotationDraftsFromObjectNames = async (questionId, { silent = true } = {}) => {
  const normalizedQuestionId = Number(questionId || 0)
  if (!normalizedQuestionId) {
    return {}
  }
  const objectMap = imageAnnotationObjectNames.value[normalizedQuestionId] || {}
  const currentDrafts = imageAnnotationDrafts.value[normalizedQuestionId] || {}
  const pendingEntries = Object.entries(objectMap)
    .map(([imageKey, objectName]) => [imageKey, String(objectName || '').trim()])
    .filter(([imageKey, objectName]) => objectName && !String(currentDrafts[imageKey] || '').trim())

  if (!pendingEntries.length || imageAnnotationLoading.value[normalizedQuestionId]) {
    return currentDrafts
  }

  imageAnnotationLoading.value = {
    ...imageAnnotationLoading.value,
    [normalizedQuestionId]: true
  }

  const nextDrafts = { ...currentDrafts }
  try {
    for (const [imageKey, objectName] of pendingEntries) {
      const blob = await fileApi.download(objectName)
      const dataUrl = await blobToDataUrl(blob)
      if (dataUrl && String((imageAnnotationObjectNames.value[normalizedQuestionId] || {})[imageKey] || '').trim() === objectName) {
        nextDrafts[imageKey] = dataUrl
      }
    }
    imageAnnotationDrafts.value = {
      ...imageAnnotationDrafts.value,
      [normalizedQuestionId]: nextDrafts
    }
    return nextDrafts
  } catch (error) {
    if (!silent) {
      showToast(error.message || '图片辅助线加载失败')
    }
    return currentDrafts
  } finally {
    imageAnnotationLoading.value = {
      ...imageAnnotationLoading.value,
      [normalizedQuestionId]: false
    }
  }
}

const ensureCurrentImageAnnotationDraftsLoaded = async ({ silent = true } = {}) => {
  const questionId = Number(currentQuestion.value?.questionId || 0)
  if (!questionId) {
    return
  }
  await loadImageAnnotationDraftsFromObjectNames(questionId, { silent })
}

const draftStatusText = computed(() => {
  if (isSubmitted.value) {
    return '已提交，可查看结果'
  }
  if (Object.values(essayCanvasLoading.value).some(Boolean)) {
    return `未作答 ${unansweredCount.value} 题 · 手写草稿加载中...`
  }
  if (Object.values(imageAnnotationLoading.value).some(Boolean)) {
    return `未作答 ${unansweredCount.value} 题 · 图片辅助线加载中...`
  }
  if (essayCanvasUploading.value) {
    return `未作答 ${unansweredCount.value} 题 · 手写草稿上传中...`
  }
  if (imageAnnotationUploading.value) {
    return `未作答 ${unansweredCount.value} 题 · 图片辅助线上传中...`
  }
  if (savingDraft.value) {
    return `未作答 ${unansweredCount.value} 题 · 草稿保存中...`
  }
  if (draftSavedAt.value) {
    return `未作答 ${unansweredCount.value} 题 · 草稿已保存 ${draftSavedAt.value}`
  }
  return `未作答 ${unansweredCount.value} 题`
})

const currentResults = computed(() => {
  if (submitResult.value?.results?.length) {
    return submitResult.value.results
  }
  if (Number(session.value?.status) === 2) {
    return judgedRecords.value
  }
  return []
})

const resultMap = computed(() => {
  const entries = (currentResults.value || []).map((item) => [item.questionId, item])
  return Object.fromEntries(entries)
})

const currentResult = computed(() => {
  if (!currentQuestion.value) {
    return null
  }
  return resultMap.value[currentQuestion.value.questionId] || null
})

const summaryResult = computed(() => {
  if (submitResult.value) {
    return {
      questionCount: submitResult.value.questionCount || 0,
      correctCount: submitResult.value.correctCount || 0,
      wrongCount: submitResult.value.wrongCount || 0,
      correctRate: submitResult.value.correctRate || 0
    }
  }

  if (Number(session.value?.status) === 2) {
    return {
      questionCount: session.value?.questionCount || questions.value.length || 0,
      correctCount: session.value?.correctCount || 0,
      wrongCount: session.value?.wrongCount || 0,
      correctRate: session.value?.correctRate || 0
    }
  }

  return null
})

const parsedOptions = computed(() => {
  if (!currentQuestion.value?.options) {
    return []
  }

  try {
    const parsed = JSON.parse(currentQuestion.value.options)
    if (!Array.isArray(parsed)) {
      return []
    }

    return parsed.map((item, index) => {
      const label = resolveOptionLabel(item, index)
      const value = resolveOptionValue(item, label)
      const text = resolveOptionText(item, label, value)
      return {
        optionKey: `${label}-${value}-${text}-${index}`,
        optionValue: value,
        optionLabel: label,
        optionText: text
      }
    })
  } catch (error) {
    return []
  }
})

const normalizeOptionField = (value) => {
  if (value == null) {
    return ''
  }
  return String(value).trim()
}

const isSameOptionText = (left, right) => {
  return normalizeOptionField(left).toUpperCase() === normalizeOptionField(right).toUpperCase()
}

const resolveOptionLabel = (item, index) => {
  const explicitLabel = normalizeOptionField(item?.label)
  if (explicitLabel) {
    return explicitLabel
  }
  return String.fromCharCode(65 + index)
}

const normalizeChoiceOptionValue = (rawValue, fallbackLabel) => {
  const normalized = normalizeOptionField(rawValue)
  if (!normalized) {
    return ''
  }
  const normalizedFallbackLabel = normalizeOptionField(fallbackLabel).toUpperCase()
  if (!normalizedFallbackLabel) {
    return normalized
  }
  if (normalized.toUpperCase() === normalizedFallbackLabel) {
    return normalizedFallbackLabel
  }

  // Support raw option strings like "A. xxx" / "B) xxx" / "C - xxx".
  const prefixedLabelMatch = normalized.toUpperCase().match(/^([A-Z])(?:[.．、,:：)）-]\s*|\s+)/)
  if (prefixedLabelMatch?.[1] === normalizedFallbackLabel) {
    return normalizedFallbackLabel
  }
  return normalized
}

const resolveOptionValue = (item, fallbackLabel) => {
  const candidates = [
    item?.value,
    item?.key,
    item?.optionValue,
    typeof item === 'string' ? item : null
  ]
  for (const candidate of candidates) {
    const normalized = normalizeChoiceOptionValue(candidate, fallbackLabel)
    if (normalized) {
      return normalized
    }
  }
  return fallbackLabel
}

const resolveOptionText = (item, label, value) => {
  const textCandidates = [
    item?.text,
    item?.content,
    item?.title,
    item?.desc,
    item?.description
  ]
  for (const candidate of textCandidates) {
    const normalized = normalizeOptionField(candidate)
    if (normalized && !isSameOptionText(normalized, label)) {
      return normalized
    }
  }

  if (!isSameOptionText(value, label)) {
    return value
  }
  return label
}

const markDraftChanged = (questionId) => {
  if (isSubmitted.value) {
    return
  }
  hasDraftChanges.value = true
  if (questionId) {
    draftDirtyQuestionIds.add(Number(questionId))
  }
}

const singleChoiceAnswer = computed({
  get() {
    if (!currentQuestion.value) {
      return ''
    }
    const answer = answers.value[currentQuestion.value.questionId]
    return typeof answer === 'string' ? answer : ''
  },
  set(value) {
    if (!currentQuestion.value || isSubmitted.value) {
      return
    }
    answers.value = {
      ...answers.value,
      [currentQuestion.value.questionId]: String(value)
    }
    markDraftChanged(currentQuestion.value.questionId)
  }
})

const multiChoiceAnswer = computed({
  get() {
    if (!currentQuestion.value) {
      return []
    }
    const answer = answers.value[currentQuestion.value.questionId]
    return Array.isArray(answer) ? answer : []
  },
  set(value) {
    if (!currentQuestion.value || isSubmitted.value) {
      return
    }
    answers.value = {
      ...answers.value,
      [currentQuestion.value.questionId]: Array.isArray(value) ? [...value] : []
    }
    markDraftChanged(currentQuestion.value.questionId)
  }
})

const textAnswer = computed({
  get() {
    if (!currentQuestion.value) {
      return ''
    }
    const answer = answers.value[currentQuestion.value.questionId]
    return typeof answer === 'string' ? answer : ''
  },
  set(value) {
    if (!currentQuestion.value || isSubmitted.value) {
      return
    }
    answers.value = {
      ...answers.value,
      [currentQuestion.value.questionId]: value == null ? '' : String(value)
    }
    markDraftChanged(currentQuestion.value.questionId)
  }
})

const textAnswerLength = computed(() => {
  if (!currentQuestion.value) {
    return 0
  }
  const answer = answers.value[currentQuestion.value.questionId]
  return typeof answer === 'string' ? answer.length : 0
})

const clearTextAnswer = () => {
  if (!currentQuestion.value || isSubmitted.value) {
    return
  }
  textAnswer.value = ''
}

const clearEssayCanvasDraft = () => {
  if (!currentQuestion.value || Number(currentQuestion.value.type) !== 5 || isSubmitted.value) {
    return
  }
  const questionId = Number(currentQuestion.value.questionId || 0)
  if (!questionId) {
    return
  }
  essayCanvasDrafts.value = {
    ...essayCanvasDrafts.value,
    [questionId]: ''
  }
  essayCanvasObjectNames.value = {
    ...essayCanvasObjectNames.value,
    [questionId]: ''
  }
  markDraftChanged(questionId)
}

const handleTextAnswerCtrlEnter = () => {
  if (!currentQuestion.value || isSubmitted.value) {
    return
  }
  if (currentIndex.value >= questions.value.length - 1) {
    showToast('已是最后一题，可点击“统一提交”完成练习')
    return
  }
  goToNextQuestion()
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
      return `题型 ${type}`
  }
}

const resolveDifficultyLabel = (difficulty) => {
  switch (Number(difficulty || 1)) {
    case 1:
      return '简单'
    case 2:
      return '中等'
    case 3:
      return '困难'
    default:
      return difficulty ? `难度 ${difficulty}` : '简单'
  }
}

const resolveModeLabel = (mode) => {
  switch (mode) {
    case 2:
      return '随机练习'
    case 3:
      return '知识点练习'
    case 4:
      return '错题重练'
    default:
      return '顺序练习'
  }
}

const formatAnswerDisplay = (value) => {
  const hasCanvasAnswer = hasEssayCanvasMarker(value)
  const plainAnswer = stripEssayCanvasMarker(value)
  const normalized = String(plainAnswer ?? '').trim()
  if (!normalized) {
    return hasCanvasAnswer ? '已提交手写作答' : '未作答'
  }
  if (normalized.startsWith('[') && normalized.endsWith(']')) {
    try {
      const parsed = JSON.parse(normalized)
      if (Array.isArray(parsed)) {
        return parsed.join(', ')
      }
    } catch (error) {
      return normalized
    }
  }
  return normalized
}

const parseSavedAnswer = (question, rawAnswer) => {
  if (rawAnswer == null) {
    return question.type === 2 ? [] : ''
  }

  const source = Number(question.type) === 5
    ? parseEssayAnswerPayload(rawAnswer).text
    : rawAnswer
  const normalized = String(source).trim()
  if (!normalized) {
    return question.type === 2 ? [] : ''
  }

  if (question.type === 2) {
    if (normalized.startsWith('[') && normalized.endsWith(']')) {
      try {
        const parsed = JSON.parse(normalized)
        if (Array.isArray(parsed)) {
          return parsed.map((item) => String(item))
        }
      } catch (error) {
        return []
      }
    }

    if (normalized.includes(',')) {
      return normalized.split(',').map((item) => item.trim()).filter(Boolean)
    }

    return [normalized]
  }

  return normalized
}

const isAnswered = (questionId) => {
  const question = questionMapById.value[Number(questionId)]
  if (Number(question?.type) === 5) {
    const answer = answers.value[questionId]
    const textAnswered = typeof answer === 'string' ? answer.trim().length > 0 : false
    const canvasDraft = String(essayCanvasDrafts.value[questionId] || '').trim()
    const canvasObjectName = String(essayCanvasObjectNames.value[questionId] || '').trim()
    return textAnswered || canvasDraft.length > 0 || canvasObjectName.length > 0
  }

  const answer = answers.value[questionId]
  if (Array.isArray(answer)) {
    return answer.length > 0
  }
  return typeof answer === 'string' ? answer.trim().length > 0 : false
}

const getQuestionResult = (questionId) => {
  return resultMap.value[questionId] || null
}

const beginQuestionTimer = () => {
  if (!currentQuestion.value || isSubmitted.value) {
    currentQuestionEnteredAt.value = 0
    return
  }
  currentQuestionEnteredAt.value = Date.now()
}

const syncCurrentQuestionTime = () => {
  if (!currentQuestion.value || !currentQuestionEnteredAt.value || isSubmitted.value) {
    return
  }

  const elapsedMs = Math.max(Date.now() - currentQuestionEnteredAt.value, 0)
  if (elapsedMs > 0) {
    const questionId = currentQuestion.value.questionId
    timeSpentMs.value = {
      ...timeSpentMs.value,
      [questionId]: (timeSpentMs.value[questionId] || 0) + elapsedMs
    }
    if (isAnswered(questionId)) {
      markDraftChanged(questionId)
    }
  }

  currentQuestionEnteredAt.value = Date.now()
}

const applyDraftSnapshot = (draftSnapshot) => {
  if (!questions.value.length || !draftSnapshot) {
    return
  }

  const nextAnswers = { ...answers.value }
  const nextTimeSpentMs = { ...timeSpentMs.value }
  const nextEssayAnswerModes = { ...essayAnswerModes.value }
  const nextEssayCanvasObjectNames = { ...essayCanvasObjectNames.value }
  const nextImageAnnotationObjectNames = { ...imageAnnotationObjectNames.value }
  const draftAnswerMap = Object.fromEntries((draftSnapshot.answers || []).map((item) => [item.questionId, item]))

  questions.value.forEach((question) => {
    const draftItem = draftAnswerMap[question.questionId]
    if (!draftItem) {
      return
    }

    const parsedImageAnnotations = parseImageAnnotationPayload(draftItem.userAnswer)
    if (parsedImageAnnotations.annotations.length) {
      nextImageAnnotationObjectNames[question.questionId] = parsedImageAnnotations.annotations.reduce((result, item) => {
        result[item.imageObjectName] = item.annotationObjectName
        return result
      }, {})
    } else {
      delete nextImageAnnotationObjectNames[question.questionId]
    }

    if (Number(question.type) === 5) {
      const parsedEssayAnswer = parseEssayAnswerPayload(parsedImageAnnotations.text)
      nextAnswers[question.questionId] = parsedEssayAnswer.text
      if (parsedEssayAnswer.canvasObjectName) {
        nextEssayCanvasObjectNames[question.questionId] = parsedEssayAnswer.canvasObjectName
      } else {
        delete nextEssayCanvasObjectNames[question.questionId]
      }
      if (!nextEssayAnswerModes[question.questionId]) {
        nextEssayAnswerModes[question.questionId] = parsedEssayAnswer.canvasObjectName && !parsedEssayAnswer.text
          ? 'canvas'
          : 'text'
      }
    } else {
      nextAnswers[question.questionId] = parseSavedAnswer(question, parsedImageAnnotations.text)
    }
    if (draftItem.timeCost != null) {
      nextTimeSpentMs[question.questionId] = Math.max(Number(draftItem.timeCost), 0) * 1000
    }
  })

  answers.value = nextAnswers
  timeSpentMs.value = nextTimeSpentMs
  essayAnswerModes.value = nextEssayAnswerModes
  essayCanvasObjectNames.value = nextEssayCanvasObjectNames
  imageAnnotationObjectNames.value = nextImageAnnotationObjectNames
  draftVersion.value = Math.max(Number(draftSnapshot.version) || 0, 0)
  const savedAt = Number(draftSnapshot.savedAt)
  draftSavedAt.value = savedAt > 0
    ? new Date(savedAt).toLocaleTimeString('zh-CN', { hour12: false })
    : ''
}

const loadSessionDetail = async () => {
  const detailResponse = await practiceApi.getSessionDetail(sessionId.value)
  session.value = detailResponse.data || null

  const records = detailResponse.data?.records || []
  judgedRecords.value = records.filter((item) => item?.isCorrect === 0 || item?.isCorrect === 1)
  draftVersion.value = Math.max(Number(detailResponse.data?.draftVersion) || 0, 0)
}

const loadQuestionSheet = async () => {
  loadingSheet.value = true
  try {
    const questionResponse = await practiceApi.getQuestions(sessionId.value)
    questionSheet.value = questionResponse.data || null
    currentIndex.value = 0
    beginQuestionTimer()
  } finally {
    loadingSheet.value = false
  }
}

const loadDraftSnapshot = async () => {
  if (!sessionId.value || isSubmitted.value) {
    return
  }
  const draftResponse = await practiceApi.getDraft(sessionId.value)
  applyDraftSnapshot(draftResponse.data || null)
}

const loadPage = async () => {
  if (!sessionId.value) {
    loadError.value = '练习会话不存在'
    return
  }

  loadingInitial.value = true
  loadError.value = ''
  submitResult.value = null
  answers.value = {}
  timeSpentMs.value = {}
  essayAnswerModes.value = {}
  essayCanvasDrafts.value = {}
  essayCanvasObjectNames.value = {}
  essayCanvasLoading.value = {}
  imageAnnotationDrafts.value = {}
  imageAnnotationObjectNames.value = {}
  imageAnnotationLoading.value = {}
  draftSavedAt.value = ''
  draftVersion.value = 0
  draftDirtyQuestionIds.clear()
  try {
    await loadSessionDetail()
    await loadQuestionSheet()
    await loadDraftSnapshot()
    hasDraftChanges.value = false
    draftDirtyQuestionIds.clear()
  } catch (error) {
    loadError.value = error.message || '加载练习会话失败'
    showToast(loadError.value)
  } finally {
    loadingInitial.value = false
  }
}

const goToQuestion = (index) => {
  if (index < 0 || index >= questions.value.length || index === currentIndex.value) {
    return
  }
  syncCurrentQuestionTime()
  currentIndex.value = index
  beginQuestionTimer()
}

const goToPreviousQuestion = () => {
  goToQuestion(currentIndex.value - 1)
}

const goToNextQuestion = () => {
  goToQuestion(currentIndex.value + 1)
}

const buildQuestionImageAnnotationPayload = (question, text) => {
  const questionId = Number(question?.questionId || 0)
  const objectMap = imageAnnotationObjectNames.value[questionId] || {}
  const annotations = Object.entries(objectMap)
    .map(([imageObjectName, annotationObjectName]) => ({
      imageObjectName,
      annotationObjectName
    }))
    .filter((item) => String(item.imageObjectName || '').trim() && String(item.annotationObjectName || '').trim())
  return buildImageAnnotationPayload(text, annotations)
}

const buildUserAnswer = (question) => {
  const answer = answers.value[question.questionId]
  let answerText = ''
  if (Array.isArray(answer)) {
    answerText = JSON.stringify(answer)
  } else if (Number(question.type) === 5) {
    answerText = buildEssayAnswerPayload({
      text: typeof answer === 'string' ? answer.trim() : '',
      canvasObjectName: essayCanvasObjectNames.value[question.questionId] || ''
    })
  } else if (typeof answer === 'string') {
    answerText = answer.trim()
  }
  return buildQuestionImageAnnotationPayload(question, answerText)
}

const buildTimeCost = (questionId) => {
  const ms = timeSpentMs.value[questionId] || 0
  return Math.max(Math.round(ms / 1000), 0)
}

const dataUrlToFile = (dataUrl, fileName) => {
  const normalized = String(dataUrl || '')
  const [header, base64] = normalized.split(',')
  if (!header || !base64) {
    return null
  }
  const mimeMatch = header.match(/data:(.*?);base64/i)
  const mimeType = mimeMatch?.[1] || 'image/png'
  const binary = window.atob(base64)
  const bytes = new Uint8Array(binary.length)
  for (let index = 0; index < binary.length; index += 1) {
    bytes[index] = binary.charCodeAt(index)
  }
  return new File([bytes], fileName, { type: mimeType })
}

const uploadEssayCanvasForQuestion = async (questionId) => {
  const normalizedQuestionId = Number(questionId || 0)
  if (!normalizedQuestionId) {
    return ''
  }

  const existedObjectName = String(essayCanvasObjectNames.value[normalizedQuestionId] || '').trim()
  if (existedObjectName) {
    return existedObjectName
  }

  const dataUrl = String(essayCanvasDrafts.value[normalizedQuestionId] || '').trim()
  if (!dataUrl) {
    return ''
  }

  const file = dataUrlToFile(dataUrl, `practice-essay-canvas-${sessionId.value}-${normalizedQuestionId}.png`)
  if (!file) {
    return ''
  }

  const response = await fileApi.upload(file)
  const objectName = String(response?.objectName || response?.data?.objectName || '').trim()
  if (!objectName) {
    throw new Error('手写草稿上传失败，请稍后重试')
  }

  essayCanvasObjectNames.value = {
    ...essayCanvasObjectNames.value,
    [normalizedQuestionId]: objectName
  }
  markDraftChanged(normalizedQuestionId)
  return objectName
}

const ensureEssayCanvasUploaded = async ({ questionIds = [], silent = true, throwOnError = false } = {}) => {
  const candidateQuestionIds = questionIds.length
    ? questionIds.map((item) => Number(item)).filter((item) => item > 0)
    : questions.value
      .filter((question) => Number(question.type) === 5)
      .map((question) => Number(question.questionId))

  const pendingQuestionIds = candidateQuestionIds.filter((questionId) => {
    const draft = String(essayCanvasDrafts.value[questionId] || '').trim()
    const objectName = String(essayCanvasObjectNames.value[questionId] || '').trim()
    return draft.length > 0 && !objectName
  })

  if (!pendingQuestionIds.length) {
    return
  }

  essayCanvasUploading.value = true
  try {
    for (const questionId of pendingQuestionIds) {
      await uploadEssayCanvasForQuestion(questionId)
    }
  } catch (error) {
    if (!silent) {
      showToast(error.message || '手写草稿上传失败')
    }
    if (throwOnError) {
      throw error
    }
  } finally {
    essayCanvasUploading.value = false
  }
}

const buildImageAnnotationUploadFileName = (questionId, imageKey) => {
  const safeKey = String(imageKey || 'image').replace(/[^a-zA-Z0-9_.-]/g, '-').slice(-80)
  return `practice-image-annotation-${sessionId.value}-${questionId}-${Date.now()}-${safeKey}.png`
}

const uploadImageAnnotationsForQuestion = async (question) => {
  const questionId = Number(question?.questionId || 0)
  if (!questionId) {
    return {}
  }

  const drafts = imageAnnotationDrafts.value[questionId] || {}
  const uploaded = imageAnnotationObjectNames.value[questionId] || {}
  const nextUploaded = { ...uploaded }
  const refs = parseQuestionImageRefs(question.imageUrls)

  for (const item of refs) {
    const imageKey = item.objectName || item.key
    if (!imageKey) {
      continue
    }
    const dataUrl = String(drafts[imageKey] || '').trim()
    if (!dataUrl) {
      delete nextUploaded[imageKey]
      continue
    }
    if (String(nextUploaded[imageKey] || '').trim()) {
      continue
    }
    const file = dataUrlToFile(dataUrl, buildImageAnnotationUploadFileName(questionId, imageKey))
    if (!file) {
      continue
    }

    const response = await fileApi.upload(file)
    const objectName = String(response?.objectName || response?.data?.objectName || '').trim()
    if (!objectName) {
      throw new Error('图片辅助线保存失败，请稍后重试')
    }
    nextUploaded[imageKey] = objectName
  }

  imageAnnotationObjectNames.value = {
    ...imageAnnotationObjectNames.value,
    [questionId]: nextUploaded
  }
  return nextUploaded
}

const ensureImageAnnotationsUploaded = async ({ questionIds = [], silent = true, throwOnError = false } = {}) => {
  const candidateQuestionIds = questionIds.length
    ? questionIds.map((item) => Number(item)).filter((item) => item > 0)
    : questions.value.map((question) => Number(question.questionId))

  const pendingQuestionIds = candidateQuestionIds.filter((questionId) => {
    const question = questionMapById.value[Number(questionId)]
    if (!question?.imageUrls) {
      return false
    }
    const refs = parseQuestionImageRefs(question.imageUrls)
    const drafts = imageAnnotationDrafts.value[questionId] || {}
    const uploaded = imageAnnotationObjectNames.value[questionId] || {}
    return refs.some((item) => {
      const imageKey = item.objectName || item.key
      return String(drafts[imageKey] || '').trim() && !String(uploaded[imageKey] || '').trim()
    })
  })

  if (!pendingQuestionIds.length) {
    return
  }

  imageAnnotationUploading.value = true
  try {
    for (const questionId of pendingQuestionIds) {
      await uploadImageAnnotationsForQuestion(questionMapById.value[Number(questionId)])
    }
  } catch (error) {
    if (!silent) {
      showToast(error.message || '图片辅助线保存失败')
    }
    if (throwOnError) {
      throw error
    }
  } finally {
    imageAnnotationUploading.value = false
  }
}

const isNotFoundError = (error) => {
  const status = Number(error?.response?.status ?? error?.status ?? 0)
  if (status === 404) {
    return true
  }

  // The backend may return HTTP 200 with business code 404.
  const businessCode = Number(error?.response?.data?.code ?? error?.code ?? 0)
  if (businessCode === 404) {
    return true
  }

  const message = String(error?.message || '')
  return message.includes('暂无助手会话') || message.includes('当前题目暂无助手会话')
}

const isNetworkError = (error) => {
  const code = String(error?.code || '')
  if (code === 'ERR_NETWORK') {
    return true
  }
  const message = String(error?.message || '').toLowerCase()
  return message.includes('network error') || message.includes('failed to fetch')
}

const resolveRequestErrorMessage = (error, fallback) => {
  if (isNetworkError(error)) {
    return '网络连接异常，请检查后端服务是否可用'
  }
  if (error?.code === 'ECONNABORTED') {
    return 'AI 响应超时，请稍后重试'
  }
  return error?.message || fallback
}

const normalizeMathLikeText = (text) => {
  if (!text) {
    return ''
  }

  return String(text)
    .replace(/\$\$([\s\S]*?)\$\$/g, '$1')
    .replace(/\$([^$\n]+)\$/g, '$1')
    .replace(/\\vec\s*\{\s*([^{}]+)\s*\}/g, '向量$1')
    .replace(/\\overrightarrow\s*\{\s*([^{}]+)\s*\}/g, '向量$1')
    .replace(/\\frac\s*\{\s*([^{}]+)\s*\}\s*\{\s*([^{}]+)\s*\}/g, '$1/$2')
    .replace(/\\times/g, '×')
    .replace(/\\cdot/g, '·')
    .replace(/\\div/g, '÷')
    .replace(/\\leq?/g, '<=')
    .replace(/\\geq?/g, '>=')
    .replace(/\\neq/g, '!=')
    .replace(/\\pm/g, '±')
    .replace(/\\angle\s*/g, '角')
    .replace(/\\triangle\s*/g, '三角形')
    .replace(/[{}]/g, '')
    .replace(/`/g, '')
    .replace(/\\/g, '')
}

const resolveAssistantMessageDisplayText = (message) => {
  const rawContent = message?.content == null ? '' : String(message.content)
  if (rawContent) {
    if (message?.roleName === 'assistant') {
      return normalizeMathLikeText(rawContent)
    }
    return rawContent
  }
  if (message?.status === 2) {
    return message.errorMessage || 'AI 回复失败'
  }
  return '...'
}

const isAbortError = (error) => {
  if (!error) {
    return false
  }
  if (error.name === 'AbortError') {
    return true
  }
  const message = String(error.message || '').toLowerCase()
  return message.includes('aborted') || message.includes('aborterror')
}

const clearAssistantScrollFrame = () => {
  if (!assistantScrollFrame) {
    return
  }
  window.cancelAnimationFrame(assistantScrollFrame)
  assistantScrollFrame = 0
}

const scrollAssistantMessagesToBottom = () => {
  const container = assistantMessagesContainer.value
  if (!container) {
    return
  }
  container.scrollTop = container.scrollHeight
}

const queueAssistantMessagesScrollToBottom = () => {
  if (!assistantPanelVisible.value) {
    return
  }
  if (assistantScrollFrame) {
    return
  }
  assistantScrollFrame = window.requestAnimationFrame(() => {
    assistantScrollFrame = 0
    scrollAssistantMessagesToBottom()
  })
}

const abortAssistantStream = () => {
  if (!assistantStreamAbortController) {
    return
  }
  assistantStreamAbortController.abort()
  assistantStreamAbortController = null
}

const resetAssistantTypewriter = () => {
  if (!activeAssistantTypewriter) {
    return
  }
  activeAssistantTypewriter.cancel()
  activeAssistantTypewriter = null
}

const createAssistantTypewriter = (messageId) => {
  let renderedContent = ''
  let pendingContent = ''
  let timerId = null
  let finishPromise = null
  let finishResolve = null
  let destroyed = false

  const stopTimer = () => {
    if (!timerId) {
      return
    }
    clearInterval(timerId)
    timerId = null
  }

  const settleIfDone = () => {
    if (pendingContent.length || !finishResolve) {
      return
    }
    const resolve = finishResolve
    finishResolve = null
    finishPromise = null
    resolve(renderedContent)
  }

  const flushNext = () => {
    if (destroyed) {
      stopTimer()
      return
    }

    if (!pendingContent.length) {
      stopTimer()
      settleIfDone()
      return
    }

    const appendChunk = pendingContent.slice(0, ASSISTANT_TYPEWRITER_CHARS_PER_TICK)
    pendingContent = pendingContent.slice(appendChunk.length)
    renderedContent += appendChunk
    patchAssistantMessageById(messageId, { content: renderedContent })
    queueAssistantMessagesScrollToBottom()

    if (!pendingContent.length) {
      stopTimer()
      settleIfDone()
    }
  }

  const ensureTimer = () => {
    if (timerId) {
      return
    }
    timerId = window.setInterval(flushNext, ASSISTANT_TYPEWRITER_INTERVAL_MS)
  }

  return {
    pushDelta(delta) {
      if (destroyed || !delta) {
        return
      }
      pendingContent += delta
      ensureTimer()
      flushNext()
    },
    finish(finalContent = '') {
      if (destroyed) {
        return Promise.resolve(renderedContent)
      }

      if (finalContent) {
        if (finalContent.length > renderedContent.length) {
          pendingContent += finalContent.slice(renderedContent.length)
        } else if (finalContent !== renderedContent) {
          renderedContent = finalContent
          patchAssistantMessageById(messageId, { content: renderedContent })
          queueAssistantMessagesScrollToBottom()
        }
      }

      if (!pendingContent.length) {
        patchAssistantMessageById(messageId, { content: renderedContent })
        queueAssistantMessagesScrollToBottom()
        return Promise.resolve(renderedContent)
      }

      ensureTimer()
      if (!finishPromise) {
        finishPromise = new Promise((resolve) => {
          finishResolve = resolve
        })
      }
      return finishPromise
    },
    cancel() {
      destroyed = true
      pendingContent = ''
      stopTimer()
      if (finishResolve) {
        const resolve = finishResolve
        finishResolve = null
        finishPromise = null
        resolve(renderedContent)
      }
    }
  }
}

const createLocalAssistantMessageId = (prefix) => {
  assistantLocalMessageSeed += 1
  return `${prefix}-${Date.now()}-${assistantLocalMessageSeed}`
}

const resetAssistantState = () => {
  abortAssistantStream()
  resetAssistantTypewriter()
  clearAssistantScrollFrame()
  assistantLoading.value = false
  assistantSending.value = false
  assistantSession.value = null
  assistantMessages.value = []
  assistantInput.value = ''
}

const patchAssistantMessageById = (messageId, patch) => {
  assistantMessages.value = assistantMessages.value.map((message) => {
    if (String(message.messageId) !== String(messageId)) {
      return message
    }
    return {
      ...message,
      ...patch
    }
  })
}

const removeAssistantMessagesByIds = (messageIds) => {
  if (!Array.isArray(messageIds) || !messageIds.length) {
    return
  }
  const idSet = new Set(messageIds.map((id) => String(id)))
  assistantMessages.value = assistantMessages.value.filter(
    (message) => !idSet.has(String(message.messageId))
  )
}

const loadPracticeAssistantMessages = async (questionId, assistantSessionId, loadVersion) => {
  try {
    const response = await practiceApi.listPracticeQuestionAiMessages(
      sessionId.value,
      questionId,
      assistantSessionId
    )
    if (loadVersion !== assistantLoadVersion) {
      return
    }
    assistantMessages.value = response.data || []
    void nextTick(() => {
      queueAssistantMessagesScrollToBottom()
    })
  } catch (error) {
    if (loadVersion !== assistantLoadVersion) {
      return
    }
    if (isNotFoundError(error)) {
      assistantMessages.value = []
      return
    }
    if (isNetworkError(error)) {
      assistantSession.value = null
      assistantMessages.value = []
      return
    }
    showToast(resolveRequestErrorMessage(error, '加载辅助消息失败'))
  }
}

const loadPracticeAssistantSession = async () => {
  const questionId = currentQuestion.value?.questionId
  if (!sessionId.value || !questionId || isSubmitted.value) {
    resetAssistantState()
    return
  }

  assistantLoadVersion += 1
  const loadVersion = assistantLoadVersion
  assistantLoading.value = true
  assistantSession.value = null
  assistantMessages.value = []
  assistantInput.value = ''
  try {
    const response = await practiceApi.getLatestPracticeQuestionAiSession(sessionId.value, questionId)
    if (loadVersion !== assistantLoadVersion) {
      return
    }
    assistantSession.value = response.data || null
    if (assistantSession.value?.sessionId) {
      await loadPracticeAssistantMessages(questionId, assistantSession.value.sessionId, loadVersion)
    }
  } catch (error) {
    if (loadVersion !== assistantLoadVersion) {
      return
    }
    if (isNotFoundError(error)) {
      assistantSession.value = null
      assistantMessages.value = []
      return
    }
    if (isNetworkError(error)) {
      assistantSession.value = null
      assistantMessages.value = []
      return
    }
    showToast(resolveRequestErrorMessage(error, '加载辅助会话失败'))
  } finally {
    if (loadVersion === assistantLoadVersion) {
      assistantLoading.value = false
    }
  }
}

const ensurePracticeAssistantSession = async (questionId) => {
  if (
    assistantSession.value?.sessionId &&
    assistantSession.value?.status === 1 &&
    Number(assistantSession.value?.questionId) === Number(questionId)
  ) {
    return true
  }

  try {
    const previousSessionId = assistantSession.value?.sessionId
    const response = await practiceApi.createPracticeQuestionAiSession(
      sessionId.value,
      questionId,
      { triggerSource: 1 }
    )
    const nextSession = response.data || null
    assistantSession.value = nextSession
    if (!nextSession?.sessionId) {
      assistantMessages.value = []
      return false
    }

    if (!previousSessionId) {
      return true
    }
    if (String(previousSessionId) !== String(nextSession.sessionId)) {
      assistantMessages.value = []
    }
    return Boolean(assistantSession.value?.sessionId)
  } catch (error) {
    if (isNetworkError(error)) {
      assistantSession.value = null
      assistantMessages.value = []
      return false
    }
    showToast(resolveRequestErrorMessage(error, '创建辅助会话失败'))
    return false
  }
}

const sendPracticeAssistantMessage = async () => {
  if (!canSendAssistantMessage.value || assistantSending.value) {
    return
  }

  const question = currentQuestion.value
  const questionId = question?.questionId
  const content = assistantInput.value.trim()
  if (!sessionId.value || !questionId || !content) {
    return
  }

  assistantSending.value = true
  const streamController = new AbortController()
  assistantStreamAbortController = streamController
  const requestTime = new Date().toISOString()
  const baseSeq = assistantMessages.value.length
    ? Number(assistantMessages.value[assistantMessages.value.length - 1]?.seqNo || assistantMessages.value.length)
    : 0

  const userLocalMessage = {
    messageId: createLocalAssistantMessageId('local-practice-user'),
    sessionId: assistantSession.value?.sessionId || null,
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
    messageId: createLocalAssistantMessageId('local-practice-assistant'),
    sessionId: assistantSession.value?.sessionId || null,
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
    const sessionReady = await ensurePracticeAssistantSession(questionId)
    if (!sessionReady) {
      return
    }

    const assistantSessionId = assistantSession.value.sessionId
    userLocalMessage.sessionId = assistantSessionId
    assistantLocalMessage.sessionId = assistantSessionId
    assistantMessages.value = [...assistantMessages.value, userLocalMessage, assistantLocalMessage]
    assistantInput.value = ''
    queueAssistantMessagesScrollToBottom()

    let streamedContent = ''
    const typewriter = createAssistantTypewriter(assistantLocalMessage.messageId)
    activeAssistantTypewriter = typewriter
    const finalMessage = await practiceApi.sendPracticeQuestionAiMessageStream(
      sessionId.value,
      questionId,
      assistantSessionId,
      {
        content,
        draftAnswer: buildUserAnswer(question),
        timeCost: buildTimeCost(questionId)
      },
      {
        signal: streamController.signal,
        onChunk(delta) {
          if (!delta) {
            return
          }
          streamedContent += delta
          typewriter.pushDelta(delta)
        }
      }
    )

    const finalContent = finalMessage?.content || streamedContent || assistantLocalMessage.content || ''
    await typewriter.finish(finalContent)

    if (finalMessage) {
      const stableMessageId = finalMessage.messageId ?? assistantLocalMessage.messageId
      patchAssistantMessageById(assistantLocalMessage.messageId, {
        messageId: stableMessageId,
        sessionId: finalMessage.sessionId ?? assistantLocalMessage.sessionId,
        seqNo: finalMessage.seqNo ?? assistantLocalMessage.seqNo,
        role: finalMessage.role ?? assistantLocalMessage.role,
        roleName: finalMessage.roleName || assistantLocalMessage.roleName,
        content: finalContent,
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
    if (isAbortError(error) || streamController.signal.aborted) {
      return
    }
    removeAssistantMessagesByIds([userLocalMessage.messageId, assistantLocalMessage.messageId])
    if (Number(currentQuestion.value?.questionId) === Number(questionId)) {
      assistantInput.value = content
    }
    showToast(resolveRequestErrorMessage(error, '发送消息失败'))
  } finally {
    if (assistantStreamAbortController === streamController) {
      assistantStreamAbortController = null
    }
    resetAssistantTypewriter()
    assistantSending.value = false
  }
}

const closePracticeAssistantSession = async () => {
  if (!assistantSession.value) {
    return
  }
  const questionId = currentQuestion.value?.questionId
  if (!sessionId.value || !questionId || !assistantSession.value?.sessionId) {
    assistantSession.value = {
      ...assistantSession.value,
      status: 2
    }
    return
  }
  try {
    const response = await practiceApi.closePracticeQuestionAiSession(
      sessionId.value,
      questionId,
      assistantSession.value.sessionId
    )
    assistantSession.value = response.data || {
      ...assistantSession.value,
      status: 2
    }
  } catch (error) {
    if (isNetworkError(error)) {
      return
    }
    showToast(resolveRequestErrorMessage(error, '结束辅助会话失败'))
  }
}

const isDraftConflictError = (error) => Number(error?.response?.data?.code) === 409

const getDraftQuestionIdsToSave = ({ force = false } = {}) => {
  const validQuestionIds = new Set(questions.value.map((question) => Number(question.questionId)).filter((questionId) => questionId > 0))
  if (draftDirtyQuestionIds.size) {
    return [...draftDirtyQuestionIds].filter((questionId) => validQuestionIds.has(Number(questionId)))
  }
  if (!force) {
    return []
  }
  const currentQuestionId = Number(currentQuestion.value?.questionId || 0)
  return currentQuestionId > 0 && validQuestionIds.has(currentQuestionId) ? [currentQuestionId] : []
}

const buildDraftChanges = (questionIds) => {
  return questionIds.map((questionId) => {
    const question = questions.value.find((item) => Number(item.questionId) === Number(questionId))
    return {
      questionId: Number(questionId),
      userAnswer: question ? buildUserAnswer(question) : '',
      timeCost: buildTimeCost(Number(questionId))
    }
  })
}

const saveDraft = async ({ silent = true, force = false } = {}) => {
  if (!sessionId.value || !session.value || isSubmitted.value || !questions.value.length || savingDraft.value) {
    return
  }
  syncCurrentQuestionTime()
  if (!hasDraftChanges.value) {
    return
  }
  const questionIds = getDraftQuestionIdsToSave({ force })
  if (!questionIds.length) {
    return
  }

  savingDraft.value = true
  try {
    await ensureEssayCanvasUploaded({ questionIds, silent, throwOnError: true })
    await ensureImageAnnotationsUploaded({ questionIds, silent, throwOnError: true })

    const payload = {
      baseVersion: draftVersion.value,
      changes: buildDraftChanges(questionIds)
    }
    const response = await practiceApi.saveDraft(sessionId.value, payload)
    const draftData = response.data || {}
    hasDraftChanges.value = false
    draftDirtyQuestionIds.clear()
    draftVersion.value = Math.max(Number(draftData.version) || draftVersion.value + 1, 0)
    draftSavedAt.value = new Date(Number(draftData.savedAt) || Date.now()).toLocaleTimeString('zh-CN', { hour12: false })

    session.value = {
      ...session.value,
      answeredCount: Number(draftData.answeredCount) || answeredCount.value,
      totalTimeCost: Number(draftData.totalTimeCost)
        || questions.value.map((question) => buildTimeCost(question.questionId)).reduce((sum, seconds) => sum + seconds, 0)
    }
  } catch (error) {
    if (isDraftConflictError(error)) {
      try {
        await loadDraftSnapshot()
      } catch (refreshError) {
        if (!silent) {
          showToast(refreshError.message || '草稿刷新失败')
        }
      }
      hasDraftChanges.value = false
      draftDirtyQuestionIds.clear()
      if (!silent) {
        showToast('草稿版本冲突，已刷新最新草稿')
      }
      return
    }
    if (!silent) {
      showToast(resolveRequestErrorMessage(error, '保存草稿失败'))
    }
  } finally {
    savingDraft.value = false
  }
}

const startAutoSaveTimer = () => {
  if (autoSaveTimer) {
    clearInterval(autoSaveTimer)
    autoSaveTimer = null
  }
  autoSaveTimer = window.setInterval(() => {
    void saveDraft({ silent: true })
  }, AUTO_SAVE_INTERVAL_MS)
}

const stopAutoSaveTimer = () => {
  if (!autoSaveTimer) {
    return
  }
  clearInterval(autoSaveTimer)
  autoSaveTimer = null
}

const handleVisibilityChange = () => {
  if (document.visibilityState === 'hidden') {
    void saveDraft({ silent: true, force: true })
  }
}

const handlePageHide = () => {
  void saveDraft({ silent: true, force: true })
}

const submitAllAnswers = async () => {
  if (!canSubmitAll.value) {
    if (unansweredCount.value > 0) {
      showToast('还有未作答题目，完成后才能提交')
    }
    return
  }

  syncCurrentQuestionTime()
  stopAutoSaveTimer()
  submitting.value = true
  try {
    await ensureEssayCanvasUploaded({ silent: false, throwOnError: true })
    await ensureImageAnnotationsUploaded({ silent: false, throwOnError: true })

    const payload = {
      baseVersion: draftVersion.value,
      answers: questions.value.map((question) => ({
        questionId: question.questionId,
        userAnswer: buildUserAnswer(question),
        timeCost: buildTimeCost(question.questionId)
      }))
    }

    const response = await practiceApi.submitAll(sessionId.value, payload)
    submitResult.value = response.data
    judgedRecords.value = response.data?.results || judgedRecords.value
    session.value = {
      ...session.value,
      answeredCount: response.data?.answeredCount,
      correctCount: response.data?.correctCount,
      wrongCount: response.data?.wrongCount,
      correctRate: response.data?.correctRate,
      status: response.data?.finished ? 2 : 1
    }
    currentQuestionEnteredAt.value = 0
    hasDraftChanges.value = false
    draftDirtyQuestionIds.clear()
    draftVersion.value = Math.max(Number(draftVersion.value) + 1, 0)
    showToast('提交成功')
  } catch (error) {
    if (isDraftConflictError(error)) {
      try {
        await loadDraftSnapshot()
      } catch (refreshError) {
        showToast(refreshError.message || '草稿刷新失败')
      }
      hasDraftChanges.value = false
      draftDirtyQuestionIds.clear()
      startAutoSaveTimer()
      showToast('草稿版本冲突，请确认最新答案后再提交')
      return
    }
    if (!isSubmitted.value) {
      startAutoSaveTimer()
    }
    showToast(resolveRequestErrorMessage(error, '提交失败'))
  } finally {
    submitting.value = false
  }
}

const goToConfig = () => {
  router.push(configBackLink.value)
}

const reloadPage = () => {
  assistantLoadVersion += 1
  resetAssistantState()
  answers.value = {}
  timeSpentMs.value = {}
  essayAnswerModes.value = {}
  essayCanvasDrafts.value = {}
  essayCanvasObjectNames.value = {}
  essayCanvasLoading.value = {}
  imageAnnotationDrafts.value = {}
  imageAnnotationObjectNames.value = {}
  imageAnnotationLoading.value = {}
  currentIndex.value = 0
  currentQuestionEnteredAt.value = 0
  hasDraftChanges.value = false
  draftSavedAt.value = ''
  draftVersion.value = 0
  draftDirtyQuestionIds.clear()
  void loadPage()
}

watch(
  () => [currentQuestion.value?.questionId, isSubmitted.value],
  ([questionId, submitted]) => {
    if (submitted || !questionId) {
      assistantLoadVersion += 1
      resetAssistantState()
      return
    }
    void loadPracticeAssistantSession()
  },
  { immediate: true }
)

watch(
  () => [currentQuestion.value?.questionId, currentEssayMode.value, currentEssayCanvasObjectName.value, isSubmitted.value],
  ([questionId, mode, objectName, submitted]) => {
    if (submitted || !questionId || mode !== 'canvas' || !objectName) {
      return
    }
    void ensureCurrentEssayCanvasDraftLoaded({ silent: true })
  }
)

watch(
  () => [currentQuestion.value?.questionId, currentQuestion.value?.imageUrls, isSubmitted.value],
  ([questionId, imageUrls, submitted]) => {
    if (submitted || !questionId || !imageUrls) {
      return
    }
    void ensureCurrentImageAnnotationDraftsLoaded({ silent: true })
  }
)

watch(
  () => assistantPanelVisible.value,
  (visible) => {
    if (!visible) {
      return
    }
    void nextTick(() => {
      queueAssistantMessagesScrollToBottom()
    })
  }
)

onMounted(async () => {
  await loadPage()
  await ensureCurrentEssayCanvasDraftLoaded({ silent: true })
  await ensureCurrentImageAnnotationDraftsLoaded({ silent: true })
  if (!isSubmitted.value) {
    startAutoSaveTimer()
  }
  document.addEventListener('visibilitychange', handleVisibilityChange)
  window.addEventListener('pagehide', handlePageHide)
})

onBeforeUnmount(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  window.removeEventListener('pagehide', handlePageHide)
  stopAutoSaveTimer()
  syncCurrentQuestionTime()
  clearAssistantScrollFrame()
  assistantLoadVersion += 1
  resetAssistantState()
  void saveDraft({ silent: true, force: true })
})
</script>

<style scoped>
.practice-session-page {
  min-height: 100vh;
  padding: 32px 24px 48px;
  background:
    radial-gradient(circle at top left, rgba(244, 208, 111, 0.18), transparent 28%),
    radial-gradient(circle at top right, rgba(31, 78, 121, 0.14), transparent 32%),
    linear-gradient(135deg, #f4efe4 0%, #e3edf5 100%);
}

.hero-card,
.summary-card,
.sheet-card,
.result-card,
.state-card {
  max-width: 1180px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 28px;
  box-shadow: 0 24px 60px rgba(38, 57, 77, 0.12);
}

.hero-card,
.sheet-card,
.result-card,
.state-card {
  padding: 28px;
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}

.eyebrow {
  margin: 0;
  color: #7a8793;
  font-size: 12px;
  letter-spacing: 0.24em;
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
.config-actions,
.question-actions,
.state-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.summary-card,
.sheet-card,
.result-card,
.state-card {
  margin-top: 18px;
}

.summary-card {
  padding: 20px 24px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
}

.summary-item,
.result-item {
  display: grid;
  gap: 6px;
}

.summary-item span,
.result-item span {
  color: #6c7a8d;
  font-size: 13px;
}

.summary-item strong,
.result-item strong {
  color: #17324d;
  font-size: 18px;
}

.state-card {
  text-align: center;
  color: #5e6d7c;
}

.state-actions {
  justify-content: center;
  margin-top: 14px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.section-head.compact h2 {
  font-size: 20px;
}

.section-head h2 {
  margin: 0;
  color: #17324d;
  font-size: 24px;
}

.section-head span {
  color: #6c7a8d;
}

.empty-state {
  margin-top: 18px;
  padding: 34px;
  text-align: center;
  color: #6c7a8d;
  background: #f3f7fb;
  border-radius: 18px;
}

.sheet-body {
  margin-top: 18px;
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 20px;
}

.question-nav {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  align-content: start;
}

.nav-chip {
  min-width: 0;
  padding: 12px 0;
  border-radius: 16px;
  border: 1px solid #d7e1eb;
  background: #f7fafc;
  color: #17324d;
}

.nav-chip.answered {
  background: #ecf7f1;
  border-color: #bde3ce;
}

.nav-chip.active {
  background: #17324d;
  border-color: #17324d;
  color: #fff;
}

.nav-chip.correct {
  background: #dff3e7;
  border-color: #98d1af;
}

.nav-chip.wrong {
  background: #fce9e7;
  border-color: #efb9b3;
}

.question-panel {
  padding: 8px 0 0;
}

.question-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.question-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.chip,
.result-badge,
.answer-state {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
}

.chip {
  background: #ddf5e9;
  color: #0f7a43;
}

.ghost-chip {
  background: #e8edf6;
  color: #17324d;
}

.answer-state {
  background: #f3f7fb;
  color: #526173;
}

.question-panel h3 {
  margin: 18px 0 0;
  color: #17324d;
  line-height: 1.6;
  font-size: 24px;
}

.question-content {
  margin: 14px 0 0;
  color: #5e6d7c;
  line-height: 1.7;
  white-space: pre-wrap;
}

.options-list {
  margin-top: 20px;
  display: grid;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid #d9e3ee;
  background: #f8fbff;
  color: #17324d;
  line-height: 1.6;
}

.option-marker {
  flex: 0 0 auto;
  min-width: 32px;
  font-weight: 700;
  color: #17324d;
}

.option-text {
  flex: 1;
  min-width: 0;
  color: #17324d;
  word-break: break-word;
}

.answer-block {
  margin-top: 20px;
}

.answer-block textarea {
  width: 100%;
  padding: 13px 14px;
  border: 1px solid #cad7e3;
  border-radius: 14px;
  font-size: 14px;
  box-sizing: border-box;
  resize: vertical;
  background: #fff;
}

.essay-answer-block textarea {
  min-height: 220px;
  line-height: 1.7;
}

.essay-answer-block textarea.essay-conclusion-textarea {
  min-height: 110px;
}

.essay-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.essay-mode-group {
  display: inline-flex;
  gap: 8px;
}

button.essay-mode-button {
  border: 1px solid #cad7e3;
  border-radius: 999px;
  background: #fff;
  color: #17324d;
  padding: 6px 12px;
  font-size: 12px;
}

button.essay-mode-button.active {
  border-color: #17324d;
  background: #17324d;
  color: #fff;
}

.essay-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.essay-tip {
  color: #617386;
  font-size: 13px;
}

.essay-count {
  color: #17324d;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.essay-upload-status {
  color: #0f7a43;
  font-size: 12px;
}

.essay-canvas-wrap {
  margin-bottom: 10px;
}

.essay-canvas-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}

.essay-actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.small-button {
  padding: 8px 12px;
  font-size: 13px;
}

.question-actions {
  margin-top: 22px;
}

.assistant-card {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 70;
  width: min(460px, calc(100vw - 48px));
  margin-top: 0;
  padding: 16px;
  border-radius: 20px;
  border: 1px solid #d7e2ee;
  background: rgba(244, 248, 255, 0.96);
  box-shadow: 0 18px 36px rgba(31, 55, 82, 0.2);
  backdrop-filter: blur(4px);
}

.assistant-card.collapsed {
  width: auto;
  min-width: 240px;
}

.assistant-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.assistant-head h2 {
  margin: 0;
  color: #17324d;
  font-size: 18px;
}

.assistant-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.assistant-empty {
  margin-top: 12px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #fff;
  color: #637487;
}

.assistant-body {
  margin-top: 12px;
}

.assistant-messages {
  max-height: min(280px, 34vh);
  overflow: auto;
  display: grid;
  gap: 10px;
  padding-right: 4px;
}

.assistant-message {
  border-radius: 12px;
  padding: 10px 12px;
  line-height: 1.6;
}

.assistant-message.user {
  background: #e8f0ff;
}

.assistant-message.assistant {
  background: #fff;
  border: 1px solid #d7e1ed;
}

.assistant-message .role {
  margin: 0;
  font-size: 12px;
  font-weight: 700;
  color: #526173;
}

.assistant-message .content {
  margin: 6px 0 0;
  color: #17324d;
  white-space: pre-wrap;
  word-break: break-word;
}

.assistant-input-row {
  margin-top: 12px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  align-items: end;
}

.assistant-input-row textarea {
  width: 100%;
  min-height: 70px;
  padding: 10px 12px;
  border: 1px solid #cad7e3;
  border-radius: 12px;
  font-size: 14px;
  box-sizing: border-box;
  resize: vertical;
  background: #fff;
}

.assistant-tip {
  margin: 10px 0 0;
  color: #6c7a8d;
  font-size: 12px;
}

.analysis-card {
  margin-top: 24px;
  padding: 20px;
  border-radius: 20px;
  background: #f7fafc;
}

.result-grid {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.analysis-text {
  margin: 18px 0 0;
  padding: 16px 18px;
  border-radius: 16px;
  background: #edf3f8;
  color: #4d6074;
  line-height: 1.7;
  white-space: pre-wrap;
}

.result-badge.success {
  background: #ddf5e9;
  color: #0f7a43;
}

.result-badge.danger {
  background: #fce7e7;
  color: #b42318;
}

button,
.ghost {
  border: 0;
  border-radius: 14px;
  padding: 12px 18px;
  font-size: 14px;
  text-decoration: none;
  cursor: pointer;
  transition: transform 0.18s ease, opacity 0.18s ease, background-color 0.18s ease;
}

button:hover:not(:disabled),
.ghost:hover {
  transform: translateY(-1px);
}

button {
  background: #17324d;
  color: #fff;
}

.secondary-button,
.ghost {
  background: #e7eef6;
  color: #17324d;
}

button:disabled {
  opacity: 0.65;
  cursor: not-allowed;
  transform: none;
}

@media (max-width: 900px) {
  .sheet-body {
    grid-template-columns: 1fr;
  }

  .question-nav {
    grid-template-columns: repeat(5, minmax(0, 1fr));
  }

  .assistant-input-row {
    grid-template-columns: 1fr;
  }

  .assistant-card {
    right: 14px;
    bottom: 14px;
    width: min(420px, calc(100vw - 28px));
  }
}

@media (max-width: 768px) {
  .practice-session-page {
    padding: 18px 14px 30px;
  }

  .hero-card,
  .summary-card,
  .sheet-card,
  .result-card,
  .state-card {
    border-radius: 22px;
    padding: 20px;
  }

  .hero-card h1 {
    font-size: 28px;
  }

  .question-nav {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .assistant-card {
    left: 10px;
    right: 10px;
    bottom: 10px;
    width: auto;
    max-height: 72vh;
    border-radius: 16px;
  }

  .assistant-card.collapsed {
    min-width: 0;
  }

  .assistant-head h2 {
    font-size: 16px;
  }

  .assistant-messages {
    max-height: min(260px, 32vh);
  }
}
</style>
