import request from '@/utils/request'

export interface UserRecord {
  id?: number
  username: string
  password?: string
  nickname?: string
  avatar?: string
  email?: string
  phone?: string
  status?: number
  createTime?: string
  updateTime?: string
}

export interface PageResult<T> {
  total: number
  current: number
  size: number
  records: T[]
}

export const userApi = {
  page(params: { current: number; size: number; username?: string; status?: number }) {
    return request.get<PageResult<UserRecord>>('/admin/user/page', { params })
  },
  getById(id: number) {
    return request.get<UserRecord>(`/admin/user/${id}`)
  },
  save(data: UserRecord) {
    return request.post<unknown>('/admin/user', data)
  },
  update(data: UserRecord) {
    return request.put<unknown>('/admin/user', data)
  },
  delete(id: number) {
    return request.delete<unknown>(`/admin/user/${id}`)
  },
  disable(id: number) {
    return request.put<unknown>(`/admin/user/${id}/disable`)
  },
  enable(id: number) {
    return request.put<unknown>(`/admin/user/${id}/enable`)
  },
  getRoleIds(userId: number) {
    return request.get<number[]>(`/admin/user/${userId}/roles`)
  },
  assignRoles(userId: number, roleIds: number[]) {
    return request.put<unknown>(`/admin/user/${userId}/roles`, roleIds)
  },
  roleOptions() {
    return request.get<{ id: number; roleCode: string; roleName: string }[]>('/admin/user/roles/options')
  },
}
