<template>
  <view class="page">
    <view class="card">
      <text class="title">账号注册</text>
      <view class="form-item">
        <text class="label">用户名</text>
        <input v-model="username" placeholder="请输入用户名" class="input" />
      </view>
      <view class="form-item">
        <text class="label">昵称</text>
        <input v-model="nickname" placeholder="请输入昵称" class="input" />
      </view>
      <view class="form-item">
        <text class="label">密码</text>
        <input v-model="password" placeholder="请输入密码" password class="input" />
      </view>
      <button class="btn" @click="onRegister">注册</button>
      <view class="switch" @click="goLogin">已有账号？去登录</view>
    </view>
  </view>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref } from 'vue'
import request from '@/utils/request'

const username = ref('')
const nickname = ref('')
const password = ref('')
const loading = ref(false)

function onRegister() {
  if (!username.value.trim() || !password.value.trim()) {
    uni.showToast({ title: '请输入用户名和密码', icon: 'none' })
    return
  }
  loading.value = true
  request({
    url: '/portal/auth/register',
    method: 'POST',
    data: { username: username.value, password: password.value, nickname: nickname.value },
    skipAuth: true,
    success(res) {
      if (res.data && res.data.code === 200) {
        uni.showToast({ title: '注册成功，请登录', icon: 'success' })
        setTimeout(() => {
          uni.redirectTo({ url: '/pages/auth/login' })
        }, 500)
      } else {
        uni.showToast({ title: res.data?.msg || '注册失败', icon: 'none' })
      }
    },
    fail() {
      uni.showToast({ title: '网络错误', icon: 'none' })
    },
    complete() {
      loading.value = false
    },
  })
}

function goLogin() {
  uni.navigateTo({ url: '/pages/auth/login' })
}
</script>

<style scoped>
.page {
  padding: 40rpx;
}
.card {
  margin-top: 80rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx 28rpx;
  box-shadow: 0 12rpx 24rpx rgba(15, 35, 68, 0.08);
}
.title {
  font-size: 36rpx;
  font-weight: 600;
  margin-bottom: 32rpx;
}
.form-item {
  margin-bottom: 24rpx;
}
.label {
  display: block;
  font-size: 26rpx;
  color: #555;
  margin-bottom: 8rpx;
}
.input {
  height: 72rpx;
  border-radius: 12rpx;
  background: #f5f5f5;
  padding: 0 20rpx;
}
.btn {
  margin-top: 12rpx;
  height: 76rpx;
  border-radius: 12rpx;
  background: linear-gradient(135deg, #13c2c2, #36cfc9);
  color: #fff;
  font-size: 30rpx;
}
.switch {
  margin-top: 24rpx;
  font-size: 24rpx;
  color: #1890ff;
  text-align: center;
}
</style>

