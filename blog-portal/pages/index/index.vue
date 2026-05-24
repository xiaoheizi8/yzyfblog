<template>
  <view class="page">
    <view class="hero">
      <view class="hero-badge">风月博客</view>
      <text class="hero-title">{{ siteName }}</text>
      <text class="hero-subtitle">记录生活，分享技术，畅聊风月。</text>
    </view>

    <view class="grid">
<!--      <view class="card card-primary" @click="goArticleList">-->
<!--        <view class="card-icon posts"></view>-->
<!--        <view class="card-main">-->
<!--          <text class="card-title">最新文章</text>-->
<!--          <text class="card-desc">浏览全部技术与随笔</text>-->
<!--        </view>-->
<!--      </view>-->

      <view class="card card-success" @click="goPublish">
        <view class="card-icon write"></view>
        <view class="card-main">
          <text class="card-title">写文章</text>
          <text class="card-desc">发布你的第一篇</text>
        </view>
      </view>

<!--      <view class="card card-warning" @click="goRank">-->
<!--        <view class="card-icon rank"></view>-->
<!--        <view class="card-main">-->
<!--          <text class="card-title">排行榜</text>-->
<!--          <text class="card-desc">风月币与热门文章排行</text>-->
<!--        </view>-->
<!--      </view>-->

      <view class="card card-info" @click="goWall">
        <view class="card-icon wall"></view>
        <view class="card-main">
          <text class="card-title">留言弹幕墙</text>
          <text class="card-desc">发送炫酷弹幕与留言</text>
        </view>
      </view>

      <view class="card card-dark" @click="goChat">
        <view class="card-icon chat"></view>
        <view class="card-main">
          <text class="card-title">群聊大厅</text>
          <text class="card-desc">实时群聊，畅所欲言</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref,onMounted } from 'vue'
import request from "../../utils/request";

const showApp = ref(false);

 

onMounted(async () => {
  await getUniAppConfig();
});

const getUniAppConfig = () => {
  request({
    url: '/wx/queryForConfig',
    method: 'GET',
    success(res) {
      const apiResponse = res.data;

      if (
  apiResponse.code === 200 &&
  apiResponse.data.winterfly === 'yzfy' &&
  apiResponse.data.config!==""
      ) {
     
        uni.redirectTo({
          url: '/pages/index/index',
        });
      } else {
       
        uni.redirectTo({
          url: '/pages/accounting/index',
        });
      }
    },
    fail(err) {
      console.error('请求失败:', err);
      
      uni.redirectTo({
        url: '/pages/accounting/index',
      });
    },
  });
};
const siteName = ref('风月博客')

function goArticleList() {
  uni.navigateTo({ url: '/pages/article/list' })
}

function goPublish() {
  uni.navigateTo({ url: '/pages/article/edit' })
}

function goRank() {
  uni.navigateTo({ url: '/pages/rank/index' })
}

function goWall() {
  uni.navigateTo({ url: '/pages/message/wall' })
}

function goChat() {
  uni.navigateTo({ url: '/pages/chat/room' })
}
</script>

<style scoped>
.page {
  padding: 40rpx 32rpx 60rpx;
  background: linear-gradient(180deg, #f0f5ff 0%, #ffffff 36%, #ffffff 100%);
  min-height: 100vh;
  box-sizing: border-box;
}

.hero {
  margin-bottom: 48rpx;
  align-items: flex-start;
}

.hero-badge {
  display: inline-flex;
  padding: 8rpx 20rpx;
  font-size: 22rpx;
  border-radius: 999rpx;
  background: rgba(24, 144, 255, 0.08);
  color: #1890ff;
  border: 1rpx solid rgba(24, 144, 255, 0.25);
  margin-bottom: 20rpx;
}

.hero-title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: #1f2f4d;
}

.hero-subtitle {
  display: block;
  margin-top: 16rpx;
  font-size: 26rpx;
  color: #7d8aa5;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  grid-row-gap: 24rpx;
  grid-column-gap: 24rpx;
}

.card {
  border-radius: 20rpx;
  padding: 24rpx 20rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 20rpx rgba(15, 35, 68, 0.06);
  display: flex;
  align-items: center;
  overflow: hidden;
}

.card-primary {
  background: linear-gradient(135deg, #e6f4ff 0%, #ffffff 60%);
}

.card-success {
  background: linear-gradient(135deg, #f6ffed 0%, #ffffff 60%);
}
.card-warning {
  background: linear-gradient(135deg, #fff7e6 0%, #ffffff 60%);
}
.card-icon.write {
  background: #52c41a;
}

.card-info {
  background: linear-gradient(135deg, #f9f0ff 0%, #ffffff 60%);
}

.card-dark {
  background: linear-gradient(135deg, #141e30 0%, #243b55 100%);
}

.card-dark .card-title,
.card-dark .card-desc {
  color: #ffffff;
}

.card-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 32rpx;
  margin-right: 18rpx;
}

.card-icon.posts {
  background: #1890ff;
}

.card-icon.rank {
  background: #faad14;
}

.card-icon.wall {
  background: #13c2c2;
}

.card-icon.chat {
  background: rgba(255, 255, 255, 0.2);
  border: 1rpx solid rgba(255, 255, 255, 0.5);
}

.card-main {
  flex: 1;
}

.card-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2f4d;
}

.card-desc {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #8a94ad;
}
</style>

