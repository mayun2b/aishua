import request from '../../../core/http/request'

const AI_TIMEOUT_MS = 60000

export default {
  run(payload) {
    return request.post('/ai/dify-test', payload, { timeout: AI_TIMEOUT_MS })
  },

  listHistory(params = {}) {
    return request.get('/ai/learning-analysis', { params })
  },

  getById(reportId) {
    return request.get(`/ai/learning-analysis/${reportId}`)
  }
}
