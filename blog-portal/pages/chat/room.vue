<template>
  <view class="page">
    <!-- 在线用户列表侧边栏 -->
    <view class="online-users" v-if="showOnlineUsers">
      <view class="online-header">
        <text class="online-title">在线用户 ({{ onlineUsers.length }})</text>
        <button class="btn-close" @click="showOnlineUsers = false">×</button>
      </view>
      <scroll-view scroll-y class="online-list">
        <view v-for="user in onlineUsers" :key="user.userId" class="online-item">
          <image v-if="user.avatar" :src="user.avatar" class="online-avatar" mode="aspectFill" />
          <view v-else class="online-avatar default-avatar">{{ (user.nickname || 'U').charAt(0) }}</view>
          <text class="online-name">{{ user.nickname || '用户' }}</text>
        </view>
      </scroll-view>
    </view>

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
      <button class="btn-online" @click="showOnlineUsers = !showOnlineUsers">👥</button>
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
const onlineUsers = ref<any[]>([])
const showOnlineUsers = ref(false)
let socketTask: UniApp.SocketTask | null = null
// 记录本地已发送的消息 ID，用于避免自己消息重复展示
const sentIds = new Set<string>()

const WS_URL = 'ws://http://1.14.69.51/:8080/api/ws/chat'

function connect() {
  if (socketTask) return
  socketTask = uni.connectSocket({
    url: WS_URL,
  })
  socketTask.onOpen(() => {
    console.log('WebSocket 连接成功')
    // 连接成功后，注册用户信息
    const user = uni.getStorageSync('user') || {}
    if (user.id) {
      const registerMsg = {
        type: 'REGISTER',
        userId: user.id,
        nickname: user.nickname || user.username,
        avatar: user.avatar || '',
      }
      socketTask!.send({
        data: JSON.stringify(registerMsg),
      })
    }
  })
  socketTask.onMessage((res) => {
    try {
      const data = JSON.parse(res.data as string)
      
      // 处理在线用户列表更新
      if (data.type === 'ONLINE_USERS') {
        onlineUsers.value = data.users || []
        console.log('在线用户列表更新:', onlineUsers.value.length, '人')
        return
      }
      
      // 处理风月币通知
      if (data.type === 'COIN_NOTIFICATION') {
        uni.showModal({
          title: data.title || '通知',
          content: data.content || '',
          showCancel: false,
          confirmText: '知道了',
        })
        console.log('收到风月币通知:', data)
        return
      }
      
      // 处理聊天消息
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
    console.log('WebSocket 连接关闭')
    socketTask = null
    // 3秒后尝试重连
    setTimeout(() => connect(), 3000)
  })
  socketTask.onError((err) => {
    console.error('WebSocket 错误:', err)
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
  // 延迟连接，避免首屏渲染被 WebSocket 阻塞导致模拟器“长时间无响应”
  // 注意：App.vue 已经建立了全局通知 WebSocket，这里只处理聊天消息
  setTimeout(() => connect(), 100)
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
  position: relative;
}

/* 在线用户列表侧边栏 */
.online-users {
  position: absolute;
  top: 0;
  right: 0;
  width: 60%;
  max-width: 500rpx;
  height: 100vh;
  background: rgba(20, 30, 48, 0.95);
  z-index: 100;
  display: flex;
  flex-direction: column;
  box-shadow: -4rpx 0 20rpx rgba(0, 0, 0, 0.5);
}

.online-header {
  padding: 30rpx;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.1);
}

.online-title {
  font-size: 32rpx;
  color: #ffffff;
  font-weight: bold;
}

.btn-close {
  width: 60rpx;
  height: 60rpx;
  background: transparent;
  color: #ffffff;
  font-size: 48rpx;
  line-height: 60rpx;
  padding: 0;
  margin: 0;
}

.online-list {
  flex: 1;
  overflow-y: auto;
}

.online-item {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.05);
}

.online-avatar {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: bold;
}

.online-name {
  font-size: 28rpx;
  color: #ffffff;
  flex: 1;
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
  align-items: center;
}

.btn-online {
  width: 72rpx;
  height: 72rpx;
  border-radius: 36rpx;
  background: rgba(255, 255, 255, 0.2);
  font-size: 32rpx;
  padding: 0;
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
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
