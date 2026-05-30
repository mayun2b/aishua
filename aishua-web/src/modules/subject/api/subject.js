import request from '../../../core/http/request'

export default {
  listCatalog() {
    return request.get('/subjects')
  },
  listMySubjects() {
    return request.get('/user/subjects')
  },
  listDirectories(subjectId) {
    return request.get(`/user/subjects/${subjectId}/directories`)
  },
  listDirectoryTags(subjectId, directoryId) {
    return request.get(`/user/subjects/${subjectId}/directories/${directoryId}/tags`)
  },
  join(subjectId) {
    return request.post(`/user/subjects/${subjectId}/join`)
  }
}
