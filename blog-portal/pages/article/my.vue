<template>
  <view class="page">
    <view v-if="!token" class="empty-tip">
      <text>请先登录后查看我的文章</text>
      <button class="btn-login" @click="goLogin">去登录</button>
    </view>
    <view v-else class="list">
      <view v-for="item in list" :key="item.id" class="item" @click="goDetail(item.id)">
        <text class="item-title">{{ item.title }}</text>
        <text class="item-summary">{{ item.summary || '暂无摘要' }}</text>
        <text class="item-meta">
          浏览 {{ item.viewCount || 0 }} · 点赞 {{ item.likeCount || 0 }} · {{ item.status === 1 ? '已发布' : '草稿' }} · {{ item.publishTime || item.createTime }}
        </text>
      </view>
      <view v-if="list.length === 0 && !loading" class="empty">暂无文章，去发布一篇吧～</view>
      <view class="publish-wrap">
        <button class="btn-publish" @click="goPublish">发布文章</button>
      </view>
      <view v-if="list.length > 0" class="more" @click="loadMore" v-show="hasMore && !loading">
        {{ loading ? '加载中...' : '加载更多' }}
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const token = ref(uni.getStorageSync('token') || '')
const list = ref<any[]>([])
const loading = ref(false)
const current = ref(1)
const size = 10
const total = ref(0)
const hasMore = ref(true)

function loadList(isMore = false) {
  if (!token.value) return
  if (loading.value) return
  if (isMore && !hasMore.value) return
  if (isMore) current.value++
  else current.value = 1
  loading.value = true
  request({
    url: '/portal/article/my',
    method: 'GET',
    data: { current: current.value, size },
    success(res) {
      if (res.data && res.data.code !== undefined && res.data.code !== 200) {
        uni.showToast({ title: res.data.message || res.data.msg || '请先登录', icon: 'none' })
        list.value = []
        return
      }
      const data = res.data?.data || res.data
      const records = (data && data.records) ? data.records : []
      const tot = (data && data.total != null) ? data.total : 0
      total.value = tot
      if (isMore) {
        list.value = list.value.concat(records)
      } else {
        list.value = records
      }
      hasMore.value = list.value.length < tot
    },
    fail() {
      uni.showToast({ title: '加载失败', icon: 'none' })
    },
    complete() {
      loading.value = false
    },
  })
}

function loadMore() {
  loadList(true)
}

function goDetail(id) {
  uni.navigateTo({ url: `/pages/article/detail?id=${id}` })
}

function goLogin() {
  uni.navigateTo({ url: '/pages/auth/login' })
}

function goPublish() {
  uni.navigateTo({ url: '/pages/article/edit' })
}

onMounted(() => {
  setTimeout(() => {
    token.value = uni.getStorageSync('token') || ''
    if (token.value) loadList()
  }, 0)
})
</script>

<style scoped>
.page { padding: 24rpx; min-height: 100vh; }
.empty-tip { text-align: center; padding: 80rpx 24rpx; color: #999; }
.btn-login { margin-top: 24rpx; background: #1890ff; color: #fff; }
.list { padding-bottom: 32rpx; }
.item { background: #fff; padding: 24rpx; margin-bottom: 24rpx; border-radius: 12rpx; }
.item-title { font-size: 32rpx; font-weight: bold; display: block; }
.item-summary { color: #666; font-size: 28rpx; display: block; margin-top: 12rpx; }
.item-meta { font-size: 24rpx; color: #999; margin-top: 12rpx; display: block; }
.empty { text-align: center; padding: 80rpx 24rpx; color: #999; }
.publish-wrap { padding: 24rpx 0; }
.btn-publish { background: linear-gradient(135deg, #1890ff, #40a9ff); color: #fff; }
.more { text-align: center; padding: 24rpx; color: #1890ff; font-size: 28rpx; }
</style>
