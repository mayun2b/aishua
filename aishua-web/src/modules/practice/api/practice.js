import request from '../../../core/http/request'

const AI_TIMEOUT_MS = 60000

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
  submitAll(sessionId, payload) {
    return request.post(`/practice/${sessionId}/submit-all`, payload)
  }
}
