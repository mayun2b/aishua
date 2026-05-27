import request from '../../../core/http/request'
import { clearAuthStorage, loadAuthStorage } from '../../auth/utils/authStorage'

const AI_TIMEOUT_MS = 60000
const AI_STREAM_TIMEOUT_MS = 120000
const AI_STREAM_ERROR_MESSAGE = 'AI 服务异常，请稍后重试'

const resolveApiBaseUrl = () => {
  const raw = process.env.VUE_APP_API_BASE_URL || '/api'
  return raw.endsWith('/') ? raw.slice(0, -1) : raw
}

const API_BASE_URL = resolveApiBaseUrl()

const buildApiUrl = (path) => {
  const normalized = path.startsWith('/') ? path : `/${path}`
  return `${API_BASE_URL}${normalized}`
}

const parseJsonSafe = (value) => {
  if (!value || typeof value !== 'string') {
    return null
  }
  try {
    return JSON.parse(value)
  } catch (error) {
    return null
  }
}

const parseSseEvent = (eventBlock) => {
  const lines = eventBlock.replace(/\r/g, '').split('\n')
  let eventName = 'message'
  const dataLines = []

  for (const line of lines) {
    if (!line) {
      continue
    }
    if (line.startsWith('event:')) {
      eventName = line.slice(6).trim() || 'message'
      continue
    }
    if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trim())
    }
  }

  return {
    eventName,
    rawData: dataLines.join('\n')
  }
}

const createTimeoutError = () => {
  const error = new Error('AI 响应超时，请稍后再试')
  error.code = 'ECONNABORTED'
  return error
}

// 统一处理 SSE 事件块，保持流式接口行为一致。
const handleSseEventBlock = (eventBlock, { onChunk, onDone }) => {
  const { eventName, rawData } = parseSseEvent(eventBlock)
  const parsedPayload = parseJsonSafe(rawData) || {}

  if (eventName === 'chunk') {
    const delta = parsedPayload.delta || ''
    if (delta) {
      onChunk?.(delta)
    }
    return null
  }

  if (eventName === 'done') {
    onDone?.(parsedPayload)
    return parsedPayload
  }

  if (eventName === 'error') {
    throw new Error(parsedPayload.message || AI_STREAM_ERROR_MESSAGE)
  }

  return null
}

const streamPost = async (path, payload, options = {}) => {
  const { onChunk, onDone, signal, timeoutMs = AI_STREAM_TIMEOUT_MS } = options
  const { token } = loadAuthStorage()

  const controller = new AbortController()
  let timeoutTriggered = false
  const timeoutId = setTimeout(() => {
    timeoutTriggered = true
    controller.abort()
  }, timeoutMs)

  const forwardAbort = () => controller.abort()
  if (signal) {
    if (signal.aborted) {
      controller.abort()
    } else {
      signal.addEventListener('abort', forwardAbort, { once: true })
    }
  }

  try {
    const response = await fetch(buildApiUrl(path), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      body: JSON.stringify(payload || {}),
      signal: controller.signal
    })

    if (!response.ok) {
      if (response.status === 401) {
        clearAuthStorage()
      }
      const raw = await response.text()
      const parsed = parseJsonSafe(raw)
      const message = parsed?.message || raw || `请求失败(${response.status})`
      const error = new Error(message)
      error.status = response.status
      throw error
    }

    if (!response.body) {
      throw new Error('当前浏览器不支持流式响应')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    let donePayload = null

    for (;;) {
      const { done, value } = await reader.read()
      if (done) {
        break
      }

      buffer += decoder.decode(value, { stream: true }).replace(/\r/g, '')
      let splitIndex = buffer.indexOf('\n\n')
      while (splitIndex !== -1) {
        const eventBlock = buffer.slice(0, splitIndex)
        buffer = buffer.slice(splitIndex + 2)

        const handledDonePayload = handleSseEventBlock(eventBlock, { onChunk, onDone })
        if (handledDonePayload) {
          donePayload = handledDonePayload
        }

        splitIndex = buffer.indexOf('\n\n')
      }
    }

    // 兜底处理最后一个未分隔完成的事件块。
    if (!donePayload && buffer.trim()) {
      donePayload = handleSseEventBlock(buffer, { onChunk, onDone })
    }

    if (!donePayload) {
      throw new Error('AI 响应中断，请稍后重试')
    }

    return donePayload
  } catch (error) {
    if (timeoutTriggered || (controller.signal.aborted && !signal?.aborted)) {
      throw createTimeoutError()
    }
    throw error
  } finally {
    clearTimeout(timeoutId)
    if (signal) {
      signal.removeEventListener('abort', forwardAbort)
    }
  }
}

const apiGet = (url, params) => request.get(url, { params })
const apiPost = (url, data, config) => request.post(url, data, config)
const apiPut = (url, data, config) => request.put(url, data, config)

export default {
  start(payload) {
    return apiPost('/practice/start', payload)
  },
  listSessions(params) {
    return apiGet('/practice/sessions', params)
  },
  getSessionDetail(sessionId) {
    return apiGet(`/practice/sessions/${sessionId}`)
  },
  listRecords(params) {
    return apiGet('/practice/records', params)
  },
  listWrongQuestions(params) {
    return apiGet('/practice/wrong-questions', params)
  },
  updateWrongQuestionMasterStatus(wrongQuestionId, masterStatus) {
    return apiPut(`/practice/wrong-questions/${wrongQuestionId}/master-status`, null, {
      params: { masterStatus }
    })
  },
  getWrongQuestionTrends(params) {
    return apiGet('/practice/wrong-questions/trend', params)
  },
  analyzeWrongQuestion(wrongQuestionId, payload) {
    return apiPost(
      `/practice/wrong-questions/${wrongQuestionId}/ai-analysis`,
      payload || {},
      { timeout: AI_TIMEOUT_MS }
    )
  },
  getLatestWrongQuestionAnalysis(wrongQuestionId) {
    return apiGet(`/practice/wrong-questions/${wrongQuestionId}/ai-analysis/latest`)
  },
  getLatestWrongQuestionAiSession(wrongQuestionId) {
    return apiGet(`/practice/wrong-questions/${wrongQuestionId}/ai-chat/sessions/latest`)
  },
  createWrongQuestionAiSession(wrongQuestionId, payload) {
    return apiPost(`/practice/wrong-questions/${wrongQuestionId}/ai-chat/sessions`, payload || {})
  },
  listWrongQuestionAiMessages(wrongQuestionId, sessionId) {
    return apiGet(`/practice/wrong-questions/${wrongQuestionId}/ai-chat/sessions/${sessionId}/messages`)
  },
  sendWrongQuestionAiMessage(wrongQuestionId, sessionId, payload) {
    return apiPost(
      `/practice/wrong-questions/${wrongQuestionId}/ai-chat/sessions/${sessionId}/messages`,
      payload,
      { timeout: AI_TIMEOUT_MS }
    )
  },
  sendWrongQuestionAiMessageStream(wrongQuestionId, sessionId, payload, options = {}) {
    return streamPost(
      `/practice/wrong-questions/${wrongQuestionId}/ai-chat/sessions/${sessionId}/messages/stream`,
      payload,
      options
    )
  },
  closeWrongQuestionAiSession(wrongQuestionId, sessionId) {
    return apiPut(`/practice/wrong-questions/${wrongQuestionId}/ai-chat/sessions/${sessionId}/close`)
  },
  getStats(params) {
    return apiGet('/practice/stats', params)
  },
  listTags(params) {
    return apiGet('/practice/tags', params)
  },
  getQuestions(sessionId) {
    return apiGet(`/practice/${sessionId}/questions`)
  },
  getDraft(sessionId) {
    return apiGet(`/practice/${sessionId}/draft`)
  },
  saveDraft(sessionId, payload) {
    return apiPut(`/practice/${sessionId}/draft`, payload)
  },
  submitAll(sessionId, payload) {
    return apiPost(`/practice/${sessionId}/submit-all`, payload)
  },
  getLatestPracticeQuestionAiSession(sessionId, questionId) {
    return apiGet(`/practice/${sessionId}/questions/${questionId}/assistant/sessions/latest`)
  },
  createPracticeQuestionAiSession(sessionId, questionId, payload) {
    return apiPost(
      `/practice/${sessionId}/questions/${questionId}/assistant/sessions`,
      payload || {}
    )
  },
  listPracticeQuestionAiMessages(sessionId, questionId, assistantSessionId) {
    return apiGet(
      `/practice/${sessionId}/questions/${questionId}/assistant/sessions/${assistantSessionId}/messages`
    )
  },
  sendPracticeQuestionAiMessage(sessionId, questionId, assistantSessionId, payload) {
    return apiPost(
      `/practice/${sessionId}/questions/${questionId}/assistant/sessions/${assistantSessionId}/messages`,
      payload,
      { timeout: AI_TIMEOUT_MS }
    )
  },
  sendPracticeQuestionAiMessageStream(sessionId, questionId, assistantSessionId, payload, options = {}) {
    return streamPost(
      `/practice/${sessionId}/questions/${questionId}/assistant/sessions/${assistantSessionId}/messages/stream`,
      payload,
      options
    )
  },
  closePracticeQuestionAiSession(sessionId, questionId, assistantSessionId) {
    return apiPut(
      `/practice/${sessionId}/questions/${questionId}/assistant/sessions/${assistantSessionId}/close`
    )
  }
}
