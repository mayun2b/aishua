import request from '../../../core/http/request'

export default {
  list(params) {
    return request.get('/admin/subjects', { params })
  },
  create(payload) {
    return request.post('/admin/subjects', payload)
  },
  update(id, payload) {
    return request.put(`/admin/subjects/${id}`, payload)
  },
  updateEnabled(id, enabled) {
    return request.put(`/admin/subjects/${id}/enabled`, null, {
      params: { enabled }
    })
  },
  remove(id) {
    return request.delete(`/admin/subjects/${id}`)
  }
}
