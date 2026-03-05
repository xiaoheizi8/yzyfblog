import request from '@/utils/request'

export interface ArticleLikeRankItem {
  articleId: number
  title: string
  likeCount: number
  rank: number
}

export interface ArticleRecord {
  id?: number
  title: string
  summary?: string
  content?: string
  viewCount?: number
  likeCount?: number
  commentCount?: number
  publishTime?: string
  status?: number
  authorName?: string
}

export interface PageResult<T> {
  total: number
  current: number
  size: number
  records: T[]
}

export const articleApi = {
  ranking(limit = 10) {
    return request.get<ArticleLikeRankItem[]>('/admin/article/ranking', { params: { limit } })
  },
  page(params: { current: number; size: number; title?: string; author?: string; status?: number }) {
    return request.get<PageResult<ArticleRecord>>('/admin/article/page', { params })
  },
  updateStatus(id: number, status: number) {
    return request.put<unknown>(`/admin/article/${id}/status`, undefined, { params: { status } })
  },
  getById(id: number) {
    return request.get<ArticleRecord>(`/admin/article/${id}`)
  },
  getTagIds(id: number) {
    return request.get<number[]>(`/admin/article/${id}/tags`)
  },
  create(data: any) {
    return request.post<unknown>('/admin/article', data)
  },
  update(id: number, data: any) {
    return request.put<unknown>(`/admin/article/${id}`, data)
  },
}

