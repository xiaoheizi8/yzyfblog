<template>
  <a-layout class="admin-layout">
    <a-layout-sider v-model:collapsed="collapsed" collapsible class="admin-sider">
      <div class="logo">
        <div class="logo-mark">风</div>
        <div class="logo-text">
          <div class="logo-title">风月博客</div>
          <div class="logo-sub">管理后台</div>
        </div>
      </div>
      <a-menu v-model:selectedKeys="selectedKeys" theme="dark" mode="inline">
        <a-menu-item key="/dashboard">
          <router-link to="/dashboard">仪表盘</router-link>
        </a-menu-item>
        <a-menu-item key="/article">
          <router-link to="/article">文章管理</router-link>
        </a-menu-item>
        <a-menu-item key="/tag">
          <router-link to="/tag">标签管理</router-link>
        </a-menu-item>
        <a-menu-item key="/comment">
          <router-link to="/comment">评论管理</router-link>
        </a-menu-item>
        <a-menu-item key="/user">
          <router-link to="/user">用户管理</router-link>
        </a-menu-item>
        <a-menu-item key="/config">
          <router-link to="/config">博客配置</router-link>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout class="admin-main">
      <a-layout-header class="admin-header">
        <div class="header-left"></div>
        <div class="header-right">
          <a-dropdown>
            <a class="ant-dropdown-link user-info" @click.prevent>
              <span class="user-name">{{ userStore.user?.nickname || userStore.user?.username }}</span>
              <DownOutlined />
            </a>
            <template #overlay>
              <a-menu @click="handleLogout">
                <a-menu-item key="logout">退出登录</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      <a-layout-content class="admin-content">
        <div class="admin-content-inner">
          <router-view />
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DownOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(false)
const selectedKeys = computed(() => [route.path])

onMounted(() => {
  userStore.fetchUserInfo()
  userStore.fetchMenus()
})

async function handleLogout() {
  await authApi.logout()
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.admin-sider {
  background: linear-gradient(180deg, #001529 0%, #000c17 60%, #120338 100%);
}

.logo {
  display: flex;
  align-items: center;
  padding: 16px 12px;
  color: #fff;
}

.logo-mark {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #722ed1, #40a9ff);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  margin-right: 8px;
}

.logo-text {
  line-height: 1.2;
}

.logo-title {
  font-size: 16px;
  font-weight: 600;
}

.logo-sub {
  font-size: 12px;
  opacity: 0.8;
}

.admin-main {
  background: radial-gradient(circle at top left, #f0f5ff, #ffffff 40%, #f9f0ff 100%);
}

.admin-header {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.admin-content {
  margin: 16px;
}

.admin-content-inner {
  background: #ffffff;
  min-height: calc(100vh - 96px);
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.user-info {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.user-name {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
