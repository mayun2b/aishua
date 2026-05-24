import request from '../../../core/http/request'
import { clearAuthStorage, loadAuthStorage } from '../../auth/utils/authStorage'

const AI_TIMEOUT_MS = 60000
const AI_STREAM_TIMEOUT_MS = 120000

const resolveApiBaseUrl = () => {
  const raw = process.env.VUE_APP_API_BASE_URL || '/api'
  return raw.endsWith('/') ? raw.slice(0, -1) : raw
}

const buildApiUrl = (path) => {
  const normalized = path.startsWith('/') ? path : `/${path}`
  return `${resolveApiBaseUrl()}${normalized}`
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

        const { eventName, rawData } = parseSseEvent(eventBlock)
        const parsedPayload = parseJsonSafe(rawData) || {}

        if (eventName === 'chunk') {
          const delta = parsedPayload?.delta || ''
          if (delta) {
            onChunk?.(delta)
          }
        } else if (eventName === 'done') {
          donePayload = parsedPayload
          onDone?.(donePayload)
        } else if (eventName === 'error') {
          throw new Error(parsedPayload?.message || 'AI 服务异常，请稍后重试')
        }

        splitIndex = buffer.indexOf('\n\n')
      }
    }

    if (!donePayload && buffer.trim()) {
      const { eventName, rawData } = parseSseEvent(buffer)
      const parsedPayload = parseJsonSafe(rawData) || {}
      if (eventName === 'done') {
        donePayload = parsedPayload
        onDone?.(donePayload)
      } else if (eventName === 'error') {
        throw new Error(parsedPayload?.message || 'AI 服务异常，请稍后重试')
      }
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

export default {
  start(payload) {
    return request.post('/practice/start', payload)
  },
  listSessions(params) {
    return request.get('/practice/sessions', { params })
  },
  getSessionDetail(sessionId) {
    return request.get(`/practice/sessions/${sessionId}`)
  },
  listRecords(params) {
    return request.get('/practice/records', { params })
  },
  listWrongQuestions(params) {
    return request.get('/practice/wrong-questions', { params })
  },
  updateWrongQuestionMasterStatus(wrongQuestionId, masterStatus) {
    return request.put(`/practice/wrong-questions/${wrongQuestionId}/master-status`, null, {
      params: { masterStatus }
    })
  },
  getWrongQuestionTrends(params) {
    return request.get('/practice/wrong-questions/trend', { params })
  },
  analyzeWrongQuestion(wrongQuestionId, payload) {
    return request.post(
      `/practice/wrong-questions/${wrongQuestionId}/ai-analysis`,
      payload || {},
      { timeout: AI_TIMEOUT_MS }
    )
  },
  getLatestWrongQuestionAnalysis(wrongQuestionId) {
    return request.get(`/practice/wrong-questions/${wrongQuestionId}/ai-analysis/latest`)
  },
  getLatestWrongQuestionAiSession(wrongQuestionId) {
    return request.get(`/practice/wrong-questions/${wrongQuestionId}/ai-chat/sessions/latest`)
  },
  createWrongQuestionAiSession(wrongQuestionId, payload) {
    return request.post(`/practice/wrong-questions/${wrongQuestionId}/ai-chat/sessions`, payload || {})
  },
  listWrongQuestionAiMessages(wrongQuestionId, sessionId) {
    return request.get(`/practice/wrong-questions/${wrongQuestionId}/ai-chat/sessions/${sessionId}/messages`)
  },
  sendWrongQuestionAiMessage(wrongQuestionId, sessionId, payload) {
    return request.post(
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
    return request.put(`/practice/wrong-questions/${wrongQuestionId}/ai-chat/sessions/${sessionId}/close`)
  },
  getStats(params) {
    return request.get('/practice/stats', { params })
  },
  listTags(params) {
    return request.get('/practice/tags', { params })
  },
  getQuestions(sessionId) {
    return request.get(`/practice/${sessionId}/questions`)
  },
  saveDraft(sessionId, payload) {
    return request.put(`/practice/${sessionId}/draft`, payload)
  },
  submitAll(sessionId, payload) {
    return request.post(`/practice/${sessionId}/submit-all`, payload)
  },
  getLatestPracticeQuestionAiSession(sessionId, questionId) {
    return request.get(`/practice/${sessionId}/questions/${questionId}/assistant/sessions/latest`)
  },
  createPracticeQuestionAiSession(sessionId, questionId, payload) {
    return request.post(
      `/practice/${sessionId}/questions/${questionId}/assistant/sessions`,
      payload || {}
    )
  },
  listPracticeQuestionAiMessages(sessionId, questionId, assistantSessionId) {
    return request.get(
      `/practice/${sessionId}/questions/${questionId}/assistant/sessions/${assistantSessionId}/messages`
    )
  },
  sendPracticeQuestionAiMessage(sessionId, questionId, assistantSessionId, payload) {
    return request.post(
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
    return request.put(
      `/practice/${sessionId}/questions/${questionId}/assistant/sessions/${assistantSessionId}/close`
    )
  }
}
