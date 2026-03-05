<template>
  <view class="page">
    <view class="profile-card">
      <view class="avatar-wrap" @click="onChangeAvatar">
        <image v-if="user?.avatar" :src="user.avatar" class="avatar-img" mode="aspectFill" />
        <view v-else class="avatar-text">
          {{ (user?.nickname || user?.username || '风').slice(0, 1) }}
        </view>
      </view>
      <view class="info">
        <text class="name">{{ user?.nickname || user?.username || '未登录' }}</text>
        <text class="subtitle">
          {{ user ? '点击头像可修改资料' : '请先登录账号' }}
        </text>
      </view>
    </view>

    <view v-if="wallet" class="wallet-card">
      <view class="wallet-row">
        <text class="wallet-label">风月币余额</text>
        <text class="wallet-value">{{ wallet.balance }}</text>
      </view>
      <view class="wallet-row small">
        <text>累计收入：{{ wallet.totalIncome }} · 累计支出：{{ wallet.totalExpense }}</text>
      </view>
      <button class="sign-btn" @click="signIn">每日签到领币</button>
    </view>

    <view class="menu">
      <view class="menu-item">
        <text class="menu-text" @click="goMyArticle">我的文章</text>
      </view>
      <view class="menu-item">
        <text class="menu-text" @click="goFavorite">我的收藏</text>
      </view>
      <view class="menu-item">
        <text class="menu-text" @click="goEditProfile">编辑资料</text>
      </view>
      <view class="menu-item">
        <text class="menu-text" @click="goAuth">{{ user ? '退出登录' : '登录 / 注册' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, reactive, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import request, { BASE_URL } from '@/utils/request'

const user = ref<any | null>(null)
const wallet = ref<any | null>(null)
const editing = ref(false)
const profile = reactive({ nickname: '', email: '', phone: '', avatar: '' })

function syncUserFromStorage() {
  const cached = uni.getStorageSync('user')
  if (cached) {
    if (cached.avatar && typeof cached.avatar === 'string' && !cached.avatar.startsWith('http')) {
      cached.avatar = `${BASE_URL}${cached.avatar}`
    }
    user.value = cached
    profile.nickname = cached.nickname || ''
    profile.email = cached.email || ''
    profile.phone = cached.phone || ''
    profile.avatar = cached.avatar || ''
    loadWallet()
  } else {
    user.value = null
    wallet.value = null
  }
}

onMounted(() => {
  syncUserFromStorage()
})

onShow(() => {
  // 每次进入“我的”页面时同步一次，避免登录后需要手动刷新
  syncUserFromStorage()
})

function loadWallet() {
  const token = uni.getStorageSync('token')
  if (!token) return
  request({
    url: '/portal/coin/wallet',
    method: 'GET',
    success(res) {
      const data = res.data?.data || res.data
      wallet.value = data || null
    },
  })
}

function signIn() {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  request({
    url: '/portal/coin/sign-in',
    method: 'POST',
    success(res) {
      if (res.data && res.data.code === 200) {
        uni.showToast({ title: '签到成功', icon: 'success' })
        loadWallet()
      } else {
        uni.showToast({ title: res.data?.msg || '签到失败', icon: 'none' })
      }
    },
    fail() {
      uni.showToast({ title: '网络错误', icon: 'none' })
    },
  })
}

function goAuth() {
  if (user.value) {
    uni.removeStorageSync('token')
    uni.removeStorageSync('user')
    user.value = null
    wallet.value = null
    uni.showToast({ title: '已退出登录', icon: 'none' })
  } else {
    uni.navigateTo({ url: '/pages/auth/login' })
  }
}

function goMyArticle() {
  if (!user.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  uni.navigateTo({ url: '/pages/article/my' })
}

function goFavorite() {
  if (!user.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  uni.navigateTo({ url: '/pages/favorite/list' })
}

function goEditProfile() {
  if (!user.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  uni.navigateTo({ url: '/pages/profile/edit' })
}

function onChangeAvatar() {
  if (!user.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  uni.chooseImage({
    count: 1,
    success(res) {
      const path = res.tempFilePaths?.[0]
      if (!path) return
      uni.uploadFile({
        url: 'http://localhost:8080/api/portal/upload/avatar',
        filePath: path,
        name: 'file',
        success(r) {
          try {
            const data = JSON.parse(r.data)
            let url = data?.data || data
            if (url) {
              // 后端返回 /uploads/... 时补全为 http://localhost:8080/api/uploads/...
              if (typeof url === 'string' && !url.startsWith('http')) {
                url = `${BASE_URL}${url}`
              }
              profile.avatar = url
              saveProfile()
            }
          } catch (e) {
            uni.showToast({ title: '头像上传失败', icon: 'none' })
          }
        },
        fail() {
          uni.showToast({ title: '头像上传失败', icon: 'none' })
        },
      })
    },
  })
}

function saveProfile() {
  if (!user.value) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  request({
    url: '/portal/auth/profile',
    method: 'PUT',
    data: {
      nickname: profile.nickname,
      email: profile.email,
      phone: profile.phone,
      avatar: profile.avatar,
    },
    success(res) {
      const data = res.data?.data || res.data
      if (data) {
        user.value = { ...(user.value || {}), ...data }
        uni.setStorageSync('user', user.value)
        uni.showToast({ title: '已保存', icon: 'success' })
        editing.value = false
      } else {
        uni.showToast({ title: res.data?.message || res.data?.msg || '保存失败', icon: 'none' })
      }
    },
    fail() {
      uni.showToast({ title: '网络错误', icon: 'none' })
    },
  })
}
</script>

<style scoped>
.page {
  padding: 32rpx 24rpx;
  background: #f7f8fa;
}
.profile-card {
  display: flex;
  align-items: center;
  padding: 32rpx 24rpx;
  background: #ffffff;
  border-radius: 16rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(125, 137, 149, 0.12);
}
.avatar-wrap {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  background: linear-gradient(135deg, #722ed1, #40a9ff);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
}
.avatar-img {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
}
.avatar-text {
  color: #fff;
  font-size: 48rpx;
  font-weight: bold;
}
.info .name {
  font-size: 32rpx;
  font-weight: bold;
}
.info .subtitle {
  margin-top: 8rpx;
  color: #888;
  font-size: 26rpx;
}
.menu {
  background: #ffffff;
  border-radius: 16rpx;
  box-shadow: 0 6rpx 18rpx rgba(125, 137, 149, 0.08);
}
.menu-item {
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid #f0f0f0;
}
.menu-item:last-child {
  border-bottom-width: 0;
}
.menu-text {
  font-size: 28rpx;
}
.wallet-card {
  margin-top: 24rpx;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx 28rpx;
  box-shadow: 0 6rpx 18rpx rgba(125, 137, 149, 0.08);
}
.wallet-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.wallet-label {
  font-size: 28rpx;
  color: #555;
}
.wallet-value {
  font-size: 34rpx;
  font-weight: 600;
  color: #fa8c16;
}
.wallet-row.small {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #999;
}
.sign-btn {
  margin-top: 20rpx;
  height: 72rpx;
  border-radius: 12rpx;
  background: linear-gradient(135deg, #faad14, #fadb14);
  color: #fff;
  font-size: 28rpx;
}
.edit-card {
  margin-top: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx 28rpx;
}
.edit-actions {
  margin-top: 12rpx;
  display: flex;
  gap: 16rpx;
  justify-content: flex-end;
}
.btn-cancel {
  height: 64rpx;
  border-radius: 12rpx;
  background: #f5f5f5;
  color: #666;
  font-size: 26rpx;
}
.btn-save {
  height: 64rpx;
  border-radius: 12rpx;
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  color: #fff;
  font-size: 26rpx;
}
</style>

