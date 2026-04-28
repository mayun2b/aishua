import request from '../../../core/http/request'

export default {
  list(params) {
    return request.get('/admin/questions', { params })
  },
  detail(id) {
    return request.get(`/admin/questions/${id}`)
  },
  create(payload) {
    return request.post('/admin/questions', payload)
  },
  update(id, payload) {
    return request.put(`/admin/questions/${id}`, payload)
  },
  remove(id) {
    return request.delete(`/admin/questions/${id}`)
  }
}
