import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { message } from 'ant-design-vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue') },
      { path: 'article', name: 'Article', component: () => import('@/views/ArticleList.vue') },
      { path: 'article/edit/:id?', name: 'ArticleEdit', component: () => import('@/views/ArticleEdit.vue') },
      { path: 'user', name: 'User', component: () => import('@/views/UserList.vue') },
      { path: 'tag', name: 'Tag', component: () => import('@/views/TagList.vue') },
          { path: 'comment', name: 'Comment', component: () => import('@/views/CommentList.vue') },
      { path: 'config', name: 'Config', component: () => import('@/views/Config.vue') },
      {path:'wxConfig' , name: 'WxConfig', component: () => import('@/views/wx/WxConfig.vue')}
    ],
  },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, _from, next) => {
  const store = useUserStore()
  if (to.meta.public) return next()
  if (!store.token) {
    message.warning('请先登录')
    return next('/login')
  }
  next()
})

export default router
