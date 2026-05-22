import request from '../../../core/http/request'

export default {
  listPapers(params) {
    return request.get('/exam/papers', { params })
  },
  start(payload) {
    return request.post('/exam/start', payload)
  },
  submit(recordId, payload) {
    return request.post(`/exam/${recordId}/submit`, payload)
  },
  listMyRecords() {
    return request.get('/exam/records/me')
  },
  getMyRecord(recordId) {
    return request.get(`/exam/records/${recordId}`)
  },
  listMyRecordQuestions(recordId) {
    return request.get(`/exam/records/${recordId}/questions`)
  }
}
