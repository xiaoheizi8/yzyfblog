<template>
  <view class="page">
    <view class="header">
      <text class="title">风月币排行榜</text>
      <text class="desc">展示拥有风月币最多的用户</text>
    </view>

    <view v-if="list.length" class="rank-list">
      <view v-for="item in list" :key="item.userId" class="rank-item">
        <view class="rank-left">
          <view class="rank-badge" :class="'rank-' + item.rank">
            <text class="rank-text">{{ item.rank }}</text>
          </view>
          <view class="avatar-wrap">
            <image
              v-if="item.avatar"
              :src="item.avatar"
              class="avatar-img"
              mode="aspectFill"
            />
            <view v-else class="avatar-text">
              {{ (item.nickname || '风').slice(0, 1) }}
            </view>
          </view>
          <view class="info">
            <text class="name">{{ item.nickname }}</text>
            <text class="sub">用户ID：{{ item.userId }}</text>
          </view>
        </view>
        <view class="coin">
          <text class="coin-value">{{ item.coin }}</text>
          <text class="coin-unit">风月币</text>
        </view>
      </view>
    </view>

    <view v-else class="empty">
      <text class="empty-text">暂时还没有排行数据</text>
    </view>
  </view>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const list = ref<any[]>([])

function loadRanking() {
  request({
    url: '/portal/coin/ranking',
    method: 'GET',
    data: { limit: 20 },
    success(res) {
      const data = res.data?.data || res.data
      list.value = data || []
    },
  })
}

onMounted(() => {
  loadRanking()
})
</script>

<style scoped>
.page {
  padding: 32rpx;
}
.header {
  margin-bottom: 32rpx;
}
.title {
  font-size: 40rpx;
  font-weight: bold;
  display: block;
}
.desc {
  margin-top: 12rpx;
  color: #888;
  font-size: 26rpx;
}
.rank-list {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}
.rank-item {
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.rank-item:last-child {
  border-bottom-width: 0;
}
.rank-left {
  display: flex;
  align-items: center;
}
.avatar-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 32rpx;
  background: #f5f5f5;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  overflow: hidden;
}
.avatar-img {
  width: 64rpx;
  height: 64rpx;
  border-radius: 32rpx;
}
.avatar-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #666;
}
.rank-badge {
  width: 48rpx;
  height: 48rpx;
  border-radius: 24rpx;
  background: #f0f0f0;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
}
.rank-text {
  font-size: 26rpx;
  font-weight: 600;
}
.rank-1 {
  background: linear-gradient(135deg, #faad14, #fff7e6);
}
.rank-2 {
  background: linear-gradient(135deg, #d9d9d9, #f5f5f5);
}
.rank-3 {
  background: linear-gradient(135deg, #ffa940, #fff2e8);
}
.info .name {
  font-size: 30rpx;
  font-weight: 500;
}
.info .sub {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
}
.coin {
  align-items: flex-end;
}
.coin-value {
  font-size: 30rpx;
  font-weight: 600;
  color: #fa8c16;
}
.coin-unit {
  font-size: 22rpx;
  color: #999;
  margin-left: 6rpx;
}
.empty {
  padding: 80rpx 24rpx;
  background: #fff;
  border-radius: 16rpx;
  text-align: center;
}
.empty-text {
  color: #999;
}
</style>

