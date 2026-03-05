<template>
  <view class="page">
    <view class="card">
      <text class="title">编辑资料</text>
      <view class="form-item">
        <text class="label">昵称</text>
        <input v-model="form.nickname" placeholder="请输入昵称" class="input" />
      </view>
      <view class="form-item">
        <text class="label">邮箱</text>
        <input v-model="form.email" placeholder="请输入邮箱" class="input" />
      </view>
      <view class="form-item">
        <text class="label">手机号</text>
        <input v-model="form.phone" placeholder="请输入手机号" class="input" />
      </view>
      <view class="actions">
        <button class="btn-cancel" @click="onCancel">取消</button>
        <button class="btn-save" @click="onSave">保存</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
// @ts-nocheck
import { reactive, onMounted } from 'vue'
import request from '@/utils/request'

const form = reactive({
  nickname: '',
  email: '',
  phone: '',
})

onMounted(() => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    uni.redirectTo({ url: '/pages/auth/login' })
    return
  }
  loadProfile()
})

function loadProfile() {
  request({
    url: '/portal/auth/profile',
    method: 'GET',
    success(res) {
      const data = res.data?.data || res.data
      if (data) {
        form.nickname = data.nickname || ''
        form.email = data.email || ''
        form.phone = data.phone || ''
      }
    },
  })
}

function onSave() {
  request({
    url: '/portal/auth/profile',
    method: 'PUT',
    data: {
      nickname: form.nickname,
      email: form.email,
      phone: form.phone,
    },
    success(res) {
      const data = res.data?.data || res.data
      if (data) {
        const cached = uni.getStorageSync('user') || {}
        const merged = { ...cached, ...data }
        uni.setStorageSync('user', merged)
        uni.showToast({ title: '已保存', icon: 'success' })
        setTimeout(() => {
          uni.navigateBack()
        }, 400)
      } else {
        uni.showToast({ title: res.data?.message || res.data?.msg || '保存失败', icon: 'none' })
      }
    },
    fail() {
      uni.showToast({ title: '网络错误', icon: 'none' })
    },
  })
}

function onCancel() {
  uni.navigateBack()
}
</script>

<style scoped>
.page {
  padding: 24rpx;
}
.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx 24rpx;
}
.title {
  font-size: 34rpx;
  font-weight: 600;
  margin-bottom: 24rpx;
}
.form-item {
  margin-bottom: 20rpx;
}
.label {
  display: block;
  font-size: 26rpx;
  color: #555;
  margin-bottom: 8rpx;
}
.input {
  height: 72rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  padding: 0 20rpx;
}
.actions {
  margin-top: 24rpx;
  display: flex;
  gap: 20rpx;
  justify-content: flex-end;
}
.btn-cancel {
  flex: 1;
  height: 72rpx;
  border-radius: 12rpx;
  background: #f5f5f5;
  color: #666;
  font-size: 26rpx;
}
.btn-save {
  flex: 1;
  height: 72rpx;
  border-radius: 12rpx;
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  color: #fff;
  font-size: 26rpx;
}
</style>

