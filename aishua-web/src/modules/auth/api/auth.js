import request from '../../../core/http/request'

export default {
  login(payload) {
    return request.post('/user/login', payload)
  },
  register(payload) {
    return request.post('/user/register', payload)
  },
  fetchProfile() {
    return request.get('/user/profile')
  }
}
