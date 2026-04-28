import request from '../../../core/http/request'

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
  getQuestions(sessionId) {
    return request.get(`/practice/${sessionId}/questions`)
  },
  submitAll(sessionId, payload) {
    return request.post(`/practice/${sessionId}/submit-all`, payload)
  }
}
