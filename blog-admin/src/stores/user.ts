import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const user = ref<Record<string, unknown> | null>(null)
  const menus = ref<any[]>([])

  async function login(username: string, password: string) {
    const res = await authApi.login({ username, password })
    const data = res.data as { token: string; user: Record<string, unknown> }
    token.value = data.token
    user.value = data.user
    localStorage.setItem('token', data.token)
    return data
  }

  async function fetchUserInfo() {
    const res = await authApi.info()
    user.value = (res.data as Record<string, unknown>) || null
  }

  async function fetchMenus() {
    const res = await authApi.menus()
    menus.value = (res.data as any[]) || []
  }

  function logout() {
    token.value = ''
    user.value = null
    menus.value = []
    localStorage.removeItem('token')
  }

  return { token, user, menus, login, fetchUserInfo, fetchMenus, logout }
})
