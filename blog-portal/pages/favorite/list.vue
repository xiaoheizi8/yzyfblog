<template>
  <view class="page">
    <view v-if="!token" class="empty-tip">
      <text>请先登录后查看收藏</text>
      <button class="btn-login" @click="goLogin">去登录</button>
    </view>
    <view v-else class="list">
      <view v-for="item in list" :key="item.id" class="item" @click="goDetail(item.id)">
        <text class="item-title">{{ item.title }}</text>
        <text class="item-summary">{{ item.summary || '暂无摘要' }}</text>
        <text class="item-meta">浏览 {{ item.viewCount || 0 }} · 点赞 {{ item.likeCount || 0 }}</text>
      </view>
      <view v-if="list.length === 0 && !loading" class="empty">暂无收藏</view>
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

function loadList() {
  if (!token.value) return
  loading.value = true
  request({
    url: '/portal/favorite/list',
    method: 'GET',
    success(res) {
      if (res.data && res.data.code !== undefined && res.data.code !== 200) {
        uni.showToast({ title: res.data.message || res.data.msg || '请先登录', icon: 'none' })
        list.value = []
        return
      }
      const data = res.data?.data || res.data
      list.value = Array.isArray(data) ? data : []
    },
    fail() {
      uni.showToast({ title: '加载失败', icon: 'none' })
    },
    complete() {
      loading.value = false
    },
  })
}

function goDetail(id) {
  uni.navigateTo({ url: `/pages/article/detail?id=${id}` })
}

function goLogin() {
  uni.navigateTo({ url: '/pages/auth/login' })
}

onMounted(() => {
  token.value = uni.getStorageSync('token') || ''
  if (token.value) {
    loadList()
  }
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
</style>
