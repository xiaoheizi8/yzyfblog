import axios from 'axios'
import { message } from 'ant-design-vue'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = token
  return config
})

request.interceptors.response.use(
  (res) => {
    const data = res.data
    // 后端统一 Result<T>，未登录可返回 code=401
    if (data && typeof data === 'object' && (data.code === 401 || data.code === 40101)) {
      localStorage.removeItem('token')
      message.warning(data.message || '登录已失效，请重新登录')
      window.location.href = '/login'
      return Promise.reject(new Error(data.message || '未登录'))
    }
    return data
  },
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      message.warning('登录已失效，请重新登录')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default request
