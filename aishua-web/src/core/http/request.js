import axios from 'axios'
import { clearAuthStorage, loadAuthStorage } from '../../modules/auth/utils/authStorage'

const request = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use(
  (config) => {
    const { token } = loadAuthStorage()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      const error = new Error(res.message || '请求失败')
      error.response = response
      return Promise.reject(error)
    }
    return res
  },
  (error) => {
    if (error.response?.status === 401) {
      clearAuthStorage()
    }
    return Promise.reject(error)
  }
)

export default request
