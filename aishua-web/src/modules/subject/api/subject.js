import request from '../../../core/http/request'

export default {
  listCatalog() {
    return request.get('/subjects')
  },
  listMySubjects() {
    return request.get('/user/subjects')
  },
  join(subjectId) {
    return request.post(`/user/subjects/${subjectId}/join`)
  }
}
