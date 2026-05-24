<template>
  <view class="page">
    <view class="tip">留言弹幕墙 - 数据来自 /api/portal/message/list</view>
    <view class="danmaku-wrap">
      <view v-for="(msg, i) in messages" :key="msg.id" class="danmaku" :style="getStyle(i)">
        {{ msg.content }}
      </view>
    </view>
    <view class="input-wrap">
      <input v-model="input" placeholder="输入留言内容" class="input" />
      <button @click="submit">发送</button>
    </view>
  </view>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const messages = ref<any[]>([])
const input = ref('')
const loading = ref(false)
const submitting = ref(false)

function getStyle(i: number) {
  const row = i % 6
  const top = 8 + row * 14
  const duration = 10 + (i % 5) * 2
  const colors = ['#ff9a9e', '#fbc2eb', '#a1c4fd', '#84fab0', '#ffe082', '#b39ddb']
  const color = colors[i % colors.length]
  const fontSize = 26 + (i % 3) * 2
  const delay = (i % 5) * 0.8
  return {
    top: top + '%',
    animationDuration: duration + 's',
    color,
    fontSize: fontSize + 'rpx',
    animationDelay: delay + 's',
  }
}

function loadMessages() {
  loading.value = true
  request({
    url: '/portal/message/list',
    method: 'GET',
    data: { limit: 100 },
    success(res) {
      const data = res.data?.data || res.data
      messages.value = data || []
    },
    complete() {
      loading.value = false
    },
  })
}

onMounted(() => {
  setTimeout(() => loadMessages(), 0)
})

function submit() {
  if (!input.value.trim() || submitting.value) return
  const content = input.value
  submitting.value = true
  request({
    url: '/portal/message/submit',
    method: 'POST',
    data: { content },
    success(res) {
      const data = res.data?.data || res.data
      if (data) {
        messages.value.unshift(data)
      } else {
        messages.value.unshift({ id: Date.now(), content })
      }
      input.value = ''
    },
    complete() {
      submitting.value = false
    },
  })
}
</script>

<style scoped>
.page {
  height: 100vh;
  background: radial-gradient(circle at top, #3949ab, #0d1b2a);
  position: relative;
  overflow: hidden;
}
.tip {
  color: #fff;
  padding: 20rpx;
  text-align: center;
  font-size: 26rpx;
}
.danmaku-wrap {
  position: absolute;
  left: 0;
  right: 0;
  top: 40rpx;
  bottom: 120rpx;
}
.danmaku {
  position: absolute;
  left: 100%;
  white-space: nowrap;
  padding: 8rpx 24rpx;
  background: rgba(0, 0, 0, 0.25);
  border-radius: 24rpx;
  animation-name: move;
  animation-timing-function: linear;
  animation-iteration-count: infinite;
}
@keyframes move {
  to {
    transform: translateX(-220%);
  }
}
.input-wrap {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  gap: 16rpx;
}
.input {
  flex: 1;
  height: 72rpx;
  background: #fff;
  border-radius: 36rpx;
  padding: 0 24rpx;
}
</style>
