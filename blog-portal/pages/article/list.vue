<template>
  <view class="page">
    <view class="list">
      <view v-for="item in list" :key="item.id" class="item" @click="goDetail(item.id)">
        <text class="item-title">{{ item.title }}</text>
        <text class="item-summary">{{ item.summary }}</text>
        <text class="item-meta">浏览 {{ item.viewCount }} · {{ item.publishTime }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const list = ref<any[]>([])
const loading = ref(false)

function loadArticles() {
  loading.value = true
  request({
    url: '/portal/article/list',
    method: 'GET',
    data: { current: 1, size: 10 },
    success(res) {
      const data = res.data?.data || res.data
      list.value = (data && data.records) || []
    },
    complete() {
      loading.value = false
    },
  })
}

onMounted(() => {
  loadArticles()
})

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/article/detail?id=${id}` })
}
</script>

<style scoped>
.page { padding: 24rpx; }
.item { background: #fff; padding: 24rpx; margin-bottom: 24rpx; border-radius: 12rpx; }
.item-title { font-size: 32rpx; font-weight: bold; display: block; }
.item-summary { color: #666; font-size: 28rpx; display: block; margin-top: 12rpx; }
.item-meta { font-size: 24rpx; color: #999; margin-top: 12rpx; display: block; }
</style>
