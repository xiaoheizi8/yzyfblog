<template>
  <div class="login-wrap">
    <div class="login-bg"></div>
    <div class="login-bg-circle circle-1"></div>
    <div class="login-bg-circle circle-2"></div>
    <div class="login-bg-circle circle-3"></div>

    <div class="login-content">
      <div class="login-left">
        <h1 class="brand-title">风月博客 · 管理后台</h1>
        <p class="brand-subtitle">写下每一篇故事，掌控每一次流量</p>
      </div>

      <a-card class="login-card" :bordered="false">
        <div class="login-card-header">
          <h2>欢迎回来</h2>
          <p>请输入账号密码进入后台管理</p>
        </div>
        <a-form :model="form" @finish="onSubmit" layout="vertical">
          <a-form-item name="username" :rules="[{ required: true, message: '请输入用户名' }]">
            <a-input v-model:value="form.username" placeholder="用户名" size="large" />
          </a-form-item>
          <a-form-item name="password" :rules="[{ required: true, message: '请输入密码' }]">
            <a-input-password v-model:value="form.password" placeholder="密码" size="large" />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" html-type="submit" size="large" block :loading="loading">
              登录
            </a-button>
          </a-form-item>
        </a-form>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const form = reactive({ username: '', password: '' })
const loading = ref(false)

async function onSubmit() {
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    message.success('登录成功')
    router.push('/')
  } catch (e: any) {
    message.error(e?.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at top left, #2f54eb, #722ed1 40%, #001529 90%);
  color: #fff;
}

.login-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(120deg, rgba(255, 255, 255, 0.08), transparent 40%, rgba(255, 255, 255, 0.05));
  opacity: 0.7;
  pointer-events: none;
}

.login-bg-circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  opacity: 0.6;
  animation: float 18s ease-in-out infinite alternate;
}

.circle-1 {
  width: 320px;
  height: 320px;
  background: #40a9ff;
  top: -80px;
  right: -40px;
}

.circle-2 {
  width: 260px;
  height: 260px;
  background: #9254de;
  bottom: -60px;
  left: -40px;
  animation-delay: -6s;
}

.circle-3 {
  width: 200px;
  height: 200px;
  background: #ffd666;
  top: 40%;
  left: 55%;
  animation-delay: -3s;
}

@keyframes float {
  0% {
    transform: translate3d(0, 0, 0);
  }
  100% {
    transform: translate3d(-40px, -20px, 0);
  }
}

.login-content {
  position: relative;
  z-index: 1;
  width: 900px;
  max-width: 96%;
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(0, 1fr);
  gap: 32px;
  align-items: center;
}

.login-left {
  color: #fff;
}

.brand-title {
  margin: 0 0 12px;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: 1px;
}

.brand-subtitle {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.login-card {
  backdrop-filter: blur(16px);
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  box-shadow: 0 18px 45px rgba(0, 0, 0, 0.35);
}

.login-card-header {
  margin-bottom: 12px;
}

.login-card-header h2 {
  margin: 0 0 4px;
  font-size: 20px;
  font-weight: 600;
}

.login-card-header p {
  margin: 0;
  color: #8c8c8c;
  font-size: 13px;
}

@media (max-width: 768px) {
  .login-content {
    grid-template-columns: minmax(0, 1fr);
  }
  .login-left {
    text-align: center;
  }
}
</style>
