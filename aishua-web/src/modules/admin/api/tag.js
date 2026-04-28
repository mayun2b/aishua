import request from '../../../core/http/request'

export default {
  list(params) {
    return request.get('/admin/tags', { params })
  },
  create(payload) {
    return request.post('/admin/tags', payload)
  },
  update(id, payload) {
    return request.put(`/admin/tags/${id}`, payload)
  },
  remove(id) {
    return request.delete(`/admin/tags/${id}`)
  }
}
