import request from '@/utils/request'

export interface CoinRankItem {
  userId: number
  nickname: string
  avatar?: string
  coin: number
  rank: number
}

export const coinApi = {
  ranking(limit = 10) {
    return request.get<CoinRankItem[]>('/admin/coin/ranking', { params: { limit } })
  },
}

