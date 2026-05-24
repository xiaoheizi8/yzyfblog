// 统一封装 uni.request，规范请求地址与头部
// baseURL 指向后端地址
// 生产环境：http://1.14.69.51:8081/api
// 开发环境：http://127.0.0.1:8080/api
export const BASE_URL = 'http://1.14.69.51:8081/api'

export interface RequestOptions extends UniApp.RequestOptions {
  skipAuth?: boolean
}

export default function request(options: RequestOptions) {
  const opts: UniApp.RequestOptions = { ...options }

  // 组装完整 URL（非 http 开头的认为是相对路径）
  if (opts.url && !opts.url.startsWith('http')) {
    const path = opts.url.startsWith('/') ? opts.url : `/${opts.url}`
    opts.url = `${BASE_URL}${path}`
  }

  // 为写操作自动附加幂等 Key（简单随机串）
  const method = (opts.method || 'GET').toUpperCase()
  if (['POST', 'PUT', 'DELETE'].includes(method)) {
    const idemKey = `${Date.now().toString(36)}-${Math.random().toString(36).slice(2)}`
    opts.header = Object.assign({}, opts.header, { 'Idempotency-Key': idemKey })
  }

  // 统一加上 Authorization 头（除非显式 skipAuth）
  if (!options.skipAuth) {
    const token = uni.getStorageSync('token')
    if (token) {
      opts.header = Object.assign({}, opts.header, { Authorization: token })
    }
  }

  return uni.request(opts)
}

