import request from '@/utils/request'

export interface CommentRecord {
  id: number
  articleId: number
  userId?: number
  parentId?: number
  replyToId?: number
  content: string
  likeCount?: number
  status?: number
  createTime?: string
}

export interface PageResult<T> {
  total: number
  current: number
  size: number
  records: T[]
}

export const commentApi = {
  page(params: { current: number; size: number; articleId?: number }) {
    return request.get<PageResult<CommentRecord>>('/admin/comment/page', { params })
  },
  delete(id: number) {
    return request.delete<unknown>(`/admin/comment/${id}`)
  },
}

