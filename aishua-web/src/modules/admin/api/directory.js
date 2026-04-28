import request from '../../../core/http/request'

export default {
  listTree(subjectId) {
    return request.get('/admin/directories/tree', {
      params: { subjectId }
    })
  },
  create(payload) {
    return request.post('/admin/directories', payload)
  },
  update(id, payload) {
    return request.put(`/admin/directories/${id}`, payload)
  },
  remove(id) {
    return request.delete(`/admin/directories/${id}`)
  }
}
