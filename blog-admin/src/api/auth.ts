import request from '@/utils/request'

export const authApi = {
  login(data: { username: string; password: string }) {
    return request.post<unknown>('/admin/auth/login', data)
  },
  logout() {
    return request.post<unknown>('/admin/auth/logout')
  },
  info() {
    return request.get<unknown>('/admin/auth/info')
  },
  menus() {
    return request.get<unknown>('/admin/auth/menus')
  },
}
