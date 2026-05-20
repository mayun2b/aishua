import request from '../../../core/http/request'

export default {
  listPapers(params) {
    return request.get('/admin/exams/papers', { params })
  },
  createPaper(payload) {
    return request.post('/admin/exams/papers', payload)
  },
  updatePaper(id, payload) {
    return request.put(`/admin/exams/papers/${id}`, payload)
  },
  updatePaperEnabled(id, enabled) {
    return request.put(`/admin/exams/papers/${id}/enabled`, null, {
      params: { enabled }
    })
  },
  removePaper(id) {
    return request.delete(`/admin/exams/papers/${id}`)
  },
  getPaperQuestions(id) {
    return request.get(`/admin/exams/papers/${id}/questions`)
  },
  getPaperDirectories(id) {
    return request.get(`/admin/exams/papers/${id}/directories`)
  },
  getPaperDirectoryTags(id, directoryId) {
    return request.get(`/admin/exams/papers/${id}/directories/${directoryId}/tags`)
  },
  getAvailablePaperQuestions(id, params) {
    return request.get(`/admin/exams/papers/${id}/questions/available`, { params })
  },
  updatePaperQuestions(id, payload) {
    return request.put(`/admin/exams/papers/${id}/questions`, payload)
  },
  listRecords(params) {
    return request.get('/admin/exams/records', { params })
  },
  getRecord(id) {
    return request.get(`/admin/exams/records/${id}`)
  },
  getRecordQuestions(id) {
    return request.get(`/admin/exams/records/${id}/questions`)
  }
}
