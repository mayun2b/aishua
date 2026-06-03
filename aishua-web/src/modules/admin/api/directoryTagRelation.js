import request from '../../../core/http/request'

export default {
  list(params) {
    return request.get('/admin/directory-tag-relations', { params })
  },
  create(payload) {
    return request.post('/admin/directory-tag-relations', payload)
  },
  update(id, payload) {
    return request.put(`/admin/directory-tag-relations/${id}`, payload)
  },
  updateEnabled(id, enabled) {
    return request.put(`/admin/directory-tag-relations/${id}/enabled`, null, {
      params: { enabled }
    })
  },
  remove(id) {
    return request.delete(`/admin/directory-tag-relations/${id}`)
  }
}
