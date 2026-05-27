import axios from 'axios'
import { clearAuthStorage, loadAuthStorage } from '../../modules/auth/utils/authStorage'

const request = axios.create({
  // 开发/部署统一通过环境变量切换，默认走前端代理 /api。
  baseURL: process.env.VUE_APP_API_BASE_URL || '/api',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use(
  (config) => {
    const { token } = loadAuthStorage()
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => {
    const payload = response.data

    // 兼容少量不走 Result<T> 包装的接口（例如纯文本或三方直透响应）。
    if (!payload || typeof payload !== 'object' || !('code' in payload)) {
      return payload
    }

    if (payload.code !== 200) {
      if (payload.code === 401) {
        clearAuthStorage()
      }

      const error = new Error(payload.message || '请求失败')
      error.response = response
      return Promise.reject(error)
    }
    return payload
  },
  (error) => {
    if (error.response?.status === 401) {
      clearAuthStorage()
    }
    return Promise.reject(error)
  }
)

export default request
