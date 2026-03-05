import request from '@/utils/request'

export interface TagRecord {
  id: number
  name: string
  slug?: string
}

export const tagApi = {
  list() {
    return request.get<TagRecord[]>('/admin/tag/list')
  },
  create(name: string) {
    return request.post<TagRecord>('/admin/tag', { name })
  },
  delete(id: number) {
    return request.delete<unknown>(`/admin/tag/${id}`)
  },
}

