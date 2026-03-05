import request from '@/utils/request'

export interface DashboardStats {
  article: number
  comment: number
  message: number
}

export interface PieItem {
  name: string
  value: number
}

export interface RecentArticleItem {
  id: number
  title: string
  viewCount: number
  likeCount: number
  status: number
  publishTime: string
}

export interface RecentUserItem {
  id: number
  username: string
  nickname: string
  status: number
  createTime: string
}

export interface RuntimeInfo {
  startTime: string
  uptime: string
  mysqlStatus: string
  mysqlVersion: string
  redisStatus: string
  redisVersion: string
}

export interface DashboardOverview {
  userStatus: PieItem[]
  articleStatus: PieItem[]
  articleTag: PieItem[]
  recentArticles: RecentArticleItem[]
  recentUsers: RecentUserItem[]
  runtime: RuntimeInfo
}

export const dashboardApi = {
  stats() {
    return request.get<DashboardStats>('/admin/dashboard/stats')
  },
  overview() {
    return request.get<DashboardOverview>('/admin/dashboard/overview')
  },
}

