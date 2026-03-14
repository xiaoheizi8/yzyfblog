<template>
  <view class="page">
    <view class="list">
      <view v-for="m in messages" :key="m.id" class="msg" :class="{ self: m.isSelf, owner: m.isOwner }">
        <text class="msg-user">
          {{ m.nickname || '游客' }}
          <text v-if="m.isOwner" class="tag-owner">群主</text>
        </text>
        <text class="msg-time" v-if="m.time">{{ m.time }}</text>
        <view class="bubble">
          <text class="msg-content">{{ m.content }}</text>
        </view>
      </view>
    </view>
    <view class="input-wrap">
      <input v-model="input" placeholder="输入消息（支持表情、图片等）" class="input" @confirm="send" />
      <button class="btn-send" @click="send">发送</button>
    </view>
  </view>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, onMounted, onUnmounted } from 'vue'

const messages = ref<any[]>([])
const input = ref('')
let socketTask: UniApp.SocketTask | null = null
// 记录本地已发送的消息 ID，用于避免自己消息重复展示
const sentIds = new Set<string>()

const WS_URL = 'ws://localhost:8080/api/ws/chat'

function connect() {
  if (socketTask) return
  socketTask = uni.connectSocket({
    url: WS_URL,
  })
  socketTask.onOpen(() => {
    // 连接成功后，可发送一条系统消息或加入通知
  })
  socketTask.onMessage((res) => {
    try {
      const data = JSON.parse(res.data as string)
      const me = uni.getStorageSync('user') || {}
      const myName = me.nickname || me.username
      const fromName = data.nickname || data.username
      const isSelf = myName && fromName && myName === fromName
      // 如果是自己刚刚本地推送过的消息（通过 clientId 标识），则忽略这次回显
      if (data.clientId && sentIds.has(data.clientId)) {
        return
      }
      if (data.clientId) {
        sentIds.add(data.clientId)
      }
      messages.value.push({
        id: data.clientId || Date.now(),
        nickname: fromName || '游客',
        content: data.content || '',
        time: data.time || '',
        isOwner: !!data.isOwner,
        isSelf,
      })
      scrollToBottom()
    } catch (e) {
      // 非 JSON，按纯文本处理
      messages.value.push({
        id: Date.now(),
        nickname: '系统',
        content: String(res.data),
        time: '',
        isOwner: false,
        isSelf: false,
      })
      scrollToBottom()
    }
  })
  socketTask.onClose(() => {
    socketTask = null
  })
  socketTask.onError(() => {
    socketTask = null
  })
}

function scrollToBottom() {
  // H5/小程序默认会撑满高度，简单做法是保持 messages 列表自然滚动即可
}

function send() {
  const text = input.value.trim()
  if (!text) return
  const user = uni.getStorageSync('user') || {}
  const nickname = user.nickname || user.username || '我'
  const now = new Date()
  const hh = String(now.getHours()).padStart(2, '0')
  const mm = String(now.getMinutes()).padStart(2, '0')
  const time = `${hh}:${mm}`
  const isOwner = nickname === '管理员' || nickname === 'admin'
  const clientId = `${Date.now().toString(36)}-${Math.random().toString(36).slice(2)}`
  const payload = {
    type: 'TEXT',
    content: text,
    nickname,
    clientId,
    time,
    isOwner,
  }
  // 本地先展示一条，保证用户立即能看到自己发送的内容
  messages.value.push({
    id: clientId,
    nickname,
    content: text,
    time,
    isOwner,
    isSelf: true,
  })
  sentIds.add(clientId)
  scrollToBottom()
  input.value = ''
  if (!socketTask) {
    connect()
  }
  if (socketTask) {
    socketTask.send({
      data: JSON.stringify(payload),
      fail() {
        uni.showToast({ title: '发送失败', icon: 'none' })
      },
    })
  }
  
  console.log( messages.value)
}

onMounted(() => {
  connect()
})

onUnmounted(() => {
  if (socketTask) {
    socketTask.close({})
    socketTask = null
  }
})
</script>

<style scoped>
.page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #141e30, #243b55);
}
.list {
  flex: 1;
  overflow-y: auto;
  padding: 24rpx;
}
.msg {
  margin-bottom: 24rpx;
}
.msg.self {
  align-items: flex-end;
}
.msg-user {
  font-size: 24rpx;
  color: #cfd8dc;
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.tag-owner {
  font-size: 20rpx;
  color: #ffd666;
  border: 1rpx solid #ffd666;
  border-radius: 20rpx;
  padding: 0 10rpx;
}
.msg-time {
  font-size: 20rpx;
  color: #90a4ae;
}
.bubble {
  margin-top: 8rpx;
  max-width: 80%;
  padding: 16rpx 20rpx;
  border-radius: 16rpx;
  background: rgba(255, 255, 255, 0.1);
}
.msg.self .bubble {
  margin-left: auto;
  background: #1890ff;
}
.msg-content {
  font-size: 30rpx;
  color: #ffffff;
}
.input-wrap {
  padding: 24rpx;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  gap: 16rpx;
}
.input {
  flex: 1;
  height: 72rpx;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 36rpx;
  padding: 0 24rpx;
}
.btn-send {
  width: 140rpx;
  height: 72rpx;
  border-radius: 36rpx;
  background: linear-gradient(135deg, #ff9a9e, #fad0c4);
  color: #fff;
  font-size: 26rpx;
}
</style>
