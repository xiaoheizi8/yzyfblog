<template>
  <view class="page">
    <view class="login-wrapper">
      <view class="brand">
        <text class="brand-title">风月博客</text>
        <text class="brand-sub">欢迎登录</text>
      </view>
      <view class="card">
        <view class="card-body">
          <view class="field">
            <text class="field-label">用户名</text>
            <input v-model="username" placeholder="请输入用户名" class="field-input" />
          </view>
          <view class="field">
            <text class="field-label">密码</text>
            <input v-model="password" placeholder="请输入密码" password class="field-input" />
          </view>
          <button class="btn-primary" :loading="loading" @click="onLogin">登录</button>
        </view>
        <view class="card-footer" @click="goRegister">
          <text class="footer-text">没有账号？</text>
          <text class="footer-link">去注册</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref } from 'vue'
import request from '@/utils/request'

const username = ref('')
const password = ref('')
const loading = ref(false)

function onLogin() {
  if (!username.value.trim() || !password.value.trim()) {
    uni.showToast({ title: '请输入用户名和密码', icon: 'none' })
    return
  }
  loading.value = true
  request({
    url: '/portal/auth/login',
    method: 'POST',
    data: { username: username.value, password: password.value },
    skipAuth: true,
    success(res) {
      const data = res.data?.data || res.data
      if (data && data.token) {
        uni.setStorageSync('token', data.token)
        uni.setStorageSync('user', data.user)
        uni.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => {
          uni.switchTab({ url: '/pages/profile/index' })
        }, 500)
      } else {
        uni.showToast({ title: res.data?.msg || '登录失败', icon: 'none' })
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

function goRegister() {
  uni.navigateTo({ url: '/pages/auth/register' })
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 64rpx 40rpx;
  background: #f7f8fa;
  box-sizing: border-box;
}
.login-wrapper {
  margin-top: 40rpx;
}
.brand {
  margin-bottom: 40rpx;
}
.brand-title {
  font-size: 40rpx;
  font-weight: 600;
  color: #323233;
}
.brand-sub {
  margin-top: 8rpx;
  font-size: 26rpx;
  color: #969799;
}
.card {
  background: #ffffff;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(125, 137, 149, 0.16);
  overflow: hidden;
}
.card-body {
  padding: 32rpx 28rpx 16rpx;
}
.field {
  margin-bottom: 24rpx;
}
.field-label {
  display: block;
  font-size: 26rpx;
  color: #323233;
  margin-bottom: 8rpx;
}
.field-input {
  height: 72rpx;
  padding: 0 20rpx;
  border-radius: 12rpx;
  background: #f7f8fa;
  font-size: 28rpx;
}
.btn-primary {
  margin-top: 8rpx;
  height: 80rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #1989fa, #2b85e4);
  color: #ffffff;
  font-size: 30rpx;
}
.card-footer {
  padding: 20rpx 28rpx 24rpx;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  border-top: 1rpx solid #f5f5f5;
}
.footer-text {
  font-size: 24rpx;
  color: #969799;
}
.footer-link {
  margin-left: 4rpx;
  font-size: 24rpx;
  color: #1989fa;
}
</style>

