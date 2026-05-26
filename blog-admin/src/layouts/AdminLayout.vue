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
        <a-menu-item key="/wxConfig">
          <router-link to="/wxConfig">小程序管理</router-link>
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
  background: #f5f2ea;
}

.admin-sider {
  /* 水墨渐变侧边栏，偏灰黑色 */
  background: radial-gradient(circle at 0 0, #4a4a4a 0%, #1f1f1f 45%, #000000 100%);
}

.logo {
  display: flex;
  align-items: center;
  padding: 16px 12px;
  color: #fdfaf4;
}

.logo-mark {
  width: 32px;
  height: 32px;
  border-radius: 999px;
  background: radial-gradient(circle at 30% 30%, #fdfaf4 0%, #d1bfa3 40%, #3f3f3f 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  margin-right: 8px;
  color: #1f1f1f;
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
  opacity: 0.75;
}

.admin-main {
  /* 背景类似宣纸的米色渐变 */
  background: radial-gradient(circle at top left, #f5f2ea 0%, #f9f5ee 40%, #f1e6d8 100%);
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
  background: #fcfaf5;
  min-height: calc(100vh - 96px);
  padding: 24px;
  border-radius: 12px;
  box-shadow:
    0 4px 16px rgba(0, 0, 0, 0.06),
    0 0 0 1px rgba(0, 0, 0, 0.02);
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
