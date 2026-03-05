<template>
  <div>
    <h2>仪表盘</h2>
    <!-- 顶部统计卡片 + 运行信息 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="8">
        <a-card><a-statistic title="文章数" :value="stats.article" /></a-card>
      </a-col>
      <a-col :span="8">
        <a-card><a-statistic title="评论数" :value="stats.comment" /></a-card>
      </a-col>
      <a-col :span="8">
        <a-card><a-statistic title="留言数" :value="stats.message" /></a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="12">
        <a-card title="网站运行信息">
          <a-descriptions size="small" :column="2" v-if="overview?.runtime">
            <a-descriptions-item label="启动时间">{{ overview?.runtime.startTime }}</a-descriptions-item>
            <a-descriptions-item label="已运行">{{ overview?.runtime.uptime }}</a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="中间件状态">
          <a-descriptions size="small" :column="2" v-if="overview?.runtime">
            <a-descriptions-item label="MySQL 状态">
              <a-badge :status="overview?.runtime.mysqlStatus === 'UP' ? 'success' : 'error'" />
              <span style="margin-left: 4px">{{ overview?.runtime.mysqlStatus }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="MySQL 版本">{{ overview?.runtime.mysqlVersion }}</a-descriptions-item>
            <a-descriptions-item label="Redis 状态">
              <a-badge :status="overview?.runtime.redisStatus === 'UP' ? 'success' : 'error'" />
              <span style="margin-left: 4px">{{ overview?.runtime.redisStatus }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="Redis 版本">{{ overview?.runtime.redisVersion }}</a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>
    </a-row>

    <!-- 中间：访问趋势 + 点赞排行榜 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="12">
        <a-card title="访问趋势（示意）">
          <div ref="chartRef" style="height: 260px"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="点赞排行榜">
          <a-spin :spinning="rankingLoading">
            <a-table
              :columns="rankingColumns"
              :data-source="rankingList"
              :pagination="false"
              size="small"
              row-key="articleId"
              :scroll="{ y: 220 }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'rank'">
                  <a-tag :color="record.rank <= 3 ? ['gold', 'silver', 'bronze'][record.rank - 1] : 'default'">
                    {{ record.rank }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'likeCount'">
                  <span style="color: #ff4d4f">❤ {{ record.likeCount }}</span>
                </template>
              </template>
            </a-table>
          </a-spin>
        </a-card>
      </a-col>
    </a-row>

    <!-- 用户 / 文章 / 标签 维度饼图 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="8">
        <a-card title="用户状态分布">
          <div ref="userPieRef" style="height: 260px"></div>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card title="文章状态分布">
          <div ref="articlePieRef" style="height: 260px"></div>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card title="按标签统计文章数">
          <div ref="tagPieRef" style="height: 280px"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 底部：风月币排行榜 + 最近数据滚动表格 -->
    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="8">
        <a-card title="风月币排行榜">
          <a-spin :spinning="coinLoading">
            <a-table
              :columns="coinColumns"
              :data-source="coinList"
              :pagination="false"
              size="small"
              row-key="userId"
              :scroll="{ y: 220 }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'rank'">
                  <a-tag :color="record.rank <= 3 ? ['gold', 'silver', 'bronze'][record.rank - 1] : 'default'">
                    {{ record.rank }}
                  </a-tag>
                </template>
                <template v-else-if="column.key === 'coin'">
                  <span style="color: #fa8c16">{{ record.coin }} 风月币</span>
                </template>
              </template>
            </a-table>
          </a-spin>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card title="最近文章（滚动表格）">
          <a-table
            :columns="recentArticleColumns"
            :data-source="overview?.recentArticles || []"
            :pagination="false"
            size="small"
            row-key="id"
            :scroll="{ y: 220 }"
          />
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card title="最近用户（滚动表格）">
          <a-table
            :columns="recentUserColumns"
            :data-source="overview?.recentUsers || []"
            :pagination="false"
            size="small"
            row-key="id"
            :scroll="{ y: 220 }"
          />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { articleApi, type ArticleLikeRankItem } from '@/api/article'
import {
  dashboardApi,
  type DashboardStats,
  type DashboardOverview,
  type PieItem,
  type RecentArticleItem,
  type RecentUserItem,
} from '@/api/dashboard'
import { coinApi, type CoinRankItem } from '@/api/coin'

const chartRef = ref<HTMLElement>()
const userPieRef = ref<HTMLElement>()
const articlePieRef = ref<HTMLElement>()
const tagPieRef = ref<HTMLElement>()

const stats = ref<DashboardStats>({ article: 0, comment: 0, message: 0 })
const overview = ref<DashboardOverview | null>(null)

const rankingColumns = [
  { title: '排名', key: 'rank', width: 70 },
  { title: '文章', dataIndex: 'title', ellipsis: true },
  { title: '点赞数', key: 'likeCount', width: 100 },
]
const rankingList = ref<ArticleLikeRankItem[]>([])
const rankingLoading = ref(false)

const coinColumns = [
  { title: '排名', key: 'rank', width: 70 },
  { title: '用户', dataIndex: 'nickname', ellipsis: true },
  { title: '风月币', key: 'coin', width: 120 },
]
const coinList = ref<CoinRankItem[]>([])
const coinLoading = ref(false)

const recentArticleColumns = [
  { title: '标题', dataIndex: 'title', ellipsis: true },
  { title: '浏览', dataIndex: 'viewCount', width: 80 },
  { title: '点赞', dataIndex: 'likeCount', width: 80 },
]

const recentUserColumns = [
  { title: '用户名', dataIndex: 'username', ellipsis: true },
  { title: '昵称', dataIndex: 'nickname', ellipsis: true },
  {
    title: '状态',
    dataIndex: 'status',
    width: 80,
    customRender: ({ text }: { text: number }) => (text === 1 ? '正常' : '禁用'),
  },
]

async function loadRanking() {
  rankingLoading.value = true
  try {
    const res: any = await articleApi.ranking(10)
    rankingList.value = res?.data ?? res ?? []
  } finally {
    rankingLoading.value = false
  }
}

async function loadCoinRanking() {
  coinLoading.value = true
  try {
    const res: any = await coinApi.ranking(10)
    coinList.value = res?.data ?? res ?? []
  } finally {
    coinLoading.value = false
  }
}

async function loadStats() {
  const res: any = await dashboardApi.stats()
  stats.value = (res?.data ?? res) || { article: 0, comment: 0, message: 0 }
}

async function loadOverview() {
  const res: any = await dashboardApi.overview()
  overview.value = (res?.data ?? res) || null
  await nextTick()
  renderPies()
}

function renderPies() {
  if (!overview.value) return
  const data = overview.value

  const userChart = userPieRef.value && echarts.init(userPieRef.value)
  const articleChart = articlePieRef.value && echarts.init(articlePieRef.value)
  const tagChart = tagPieRef.value && echarts.init(tagPieRef.value)

  if (userChart) {
    userChart.setOption(createPieOption('用户状态', data.userStatus || []))
  }
  if (articleChart) {
    articleChart.setOption(createPieOption('文章状态', data.articleStatus || []))
  }
  if (tagChart) {
    // 标签饼图：只取前 N 个标签，其余合并为“其他标签”，避免扇区过多
    const raw = (data.articleTag || []) as PieItem[]
    const sorted = [...raw].sort((a, b) => (b.value || 0) - (a.value || 0))
    const MAX = 8
    const main = sorted.slice(0, MAX)
    const rest = sorted.slice(MAX)
    const restTotal = rest.reduce((sum, item) => sum + (item.value || 0), 0)
    const finalList = restTotal > 0 ? [...main, { name: '其他标签', value: restTotal }] : main
    tagChart.setOption(
      createPieOption('标签文章数', finalList, {
        showLegend: true,
        legendRight: 0,
      }),
    )
  }
}

function createPieOption(
  title: string,
  list: PieItem[],
  extra?: { showLegend?: boolean; legendRight?: number | string },
) {
  return {
    tooltip: { trigger: 'item' },
    legend: extra?.showLegend
      ? {
          orient: 'vertical',
          right: extra.legendRight ?? 0,
          top: 'middle',
          type: 'scroll',
        }
      : { bottom: 0, left: 'center' },
    series: [
      {
        name: title,
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        label: { show: false, position: 'center' },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold',
          },
        },
        labelLine: { show: false },
        data: list,
      },
    ],
  }
}

onMounted(() => {
  loadStats()
  loadOverview()
  loadRanking()
  loadCoinRanking()
  nextTick(() => {
    if (chartRef.value) {
      const chart = echarts.init(chartRef.value)
      chart.setOption({
        xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
        yAxis: { type: 'value' },
        series: [{ data: [120, 200, 150, 80, 70, 110, 130], type: 'line' }],
      })
    }
  })
})
</script>
