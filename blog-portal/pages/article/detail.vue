<template>
  <view class="page">
    <view v-if="article" class="article">
      <text class="title">{{ article.title }}</text>
      <text class="meta">发布时间 {{ article.publishTime }} · 浏览 {{ article.viewCount }} · 点赞 {{ likeCount }}</text>
      <view class="action-bar">
        <button class="action-btn like" :class="{ active: liked }" @click="toggleLike">
          {{ liked ? '已赞' : '点赞' }} ({{ likeCount }})
        </button>
        <button class="action-btn favorite" :class="{ active: favorited }" @click="toggleFavorite">
          {{ favorited ? '已收藏' : '收藏' }}
        </button>
        <button class="action-btn tip" @click="showTip = true">打赏风月币</button>
      </view>
      <rich-text class="content" :nodes="contentHtml"></rich-text>
    </view>

    <view v-if="showTip" class="tip-mask" @click="showTip = false">
      <view class="tip-popup" @click.stop>
        <text class="tip-title">打赏文章</text>
        <input v-model.number="tipAmount" type="digit" placeholder="输入风月币数量" class="tip-input" />
        <view class="tip-btns">
          <button class="tip-cancel" @click="showTip = false">取消</button>
          <button class="tip-ok" @click="doTip">确认打赏</button>
        </view>
      </view>
    </view>

    <view class="comments">
      <text class="c-title">评论</text>
      <view v-for="c in comments" :key="c.id" class="c-item">
        <text class="c-user">用户 {{ c.userId || '游客' }}</text>
        <text class="c-content">{{ c.content }}</text>
        <text class="c-time">{{ c.createTime }}</text>
      </view>
      <view v-if="!comments.length" class="c-empty">还没有评论，快来抢沙发吧～</view>
    </view>

    <view class="c-input-wrap">
      <input v-model="commentInput" placeholder="说点什么吧..." class="c-input" @confirm="submitComment" />
      <button class="c-btn" @click="submitComment">发送</button>
    </view>
  </view>
</template>

<script setup lang="ts">
// @ts-nocheck
import { ref, onMounted, computed } from 'vue'
import request, { BASE_URL } from '@/utils/request'

const article = ref<any>(null)
const loading = ref(false)
const comments = ref<any[]>([])
const commentInput = ref('')
const likeCount = ref(0)
const liked = ref(false)
const favorited = ref(false)
const showTip = ref(false)
const tipAmount = ref('')
let articleId: string | number | null = null

const contentHtml = computed(() => {
  if (!article.value?.content) return ''
  let md = String(article.value.content)
  // 超长内容只处理前 100000 字符，避免复杂正则阻塞主线程导致「长时间没有响应」
  const maxLen = 100000
  if (md.length > maxLen) md = md.slice(0, maxLen)
  const base = (BASE_URL || '').replace(/\/$/, '')
  md = md.replace(/!\[\]\(\[object Object\]\)/g, '')
  md = md.replace(/!\[\]\(\d+\)/g, '')
  const fullUrlReg = /!\[[^\]]*]\((https?:\/\/[^\s)]+)\)/g
  const relUrlReg = /!\[[^\]]*]\((\/[^\s)]+)\)/g
  let html = md.replace(
    fullUrlReg,
    '<img src="$1" style="max-width:100%;display:block;margin:12rpx 0;border-radius:8rpx;" />',
  )
  html = html.replace(
    relUrlReg,
    `<img src="${base}$1" style="max-width:100%;display:block;margin:12rpx 0;border-radius:8rpx;" />`,
  )
  html = html.replace(/\n/g, '<br/>')
  return html
})

function loadDetail(id: number | string) {
  loading.value = true
  request({
    url: `/portal/article/${id}`,
    method: 'GET',
    success(res) {
      const data = res.data?.data || res.data
      article.value = data || null
      if (data && (data.likeCount != null || data.like_count != null)) {
        likeCount.value = data.likeCount ?? data.like_count ?? 0
      }
    },
    complete() {
      loading.value = false
    },
  })
  loadLikeStatus(id)
  loadLikeCount(id)
  loadFavoriteStatus(id)
}

function loadLikeStatus(id) {
  const user = uni.getStorageSync('user')
  const userId = user && user.id ? user.id : null
  request({
    url: `/portal/article/${id}/liked`,
    method: 'GET',
    data: userId != null ? { userId } : {},
    success(res) {
      const data = res.data?.data ?? res.data
      liked.value = !!data
    },
  })
}

function loadLikeCount(id) {
  request({
    url: `/portal/article/${id}/like-count`,
    method: 'GET',
    success(res) {
      const data = res.data?.data ?? res.data
      if (data != null) likeCount.value = data
    },
  })
}

function loadFavoriteStatus(id) {
  request({
    url: '/portal/favorite/check',
    method: 'GET',
    data: { articleId: id },
    success(res) {
      const data = res.data?.data ?? res.data
      favorited.value = !!data
    },
  })
}

function toggleFavorite() {
  if (!articleId) return
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  const url = favorited.value ? '/portal/favorite/remove' : '/portal/favorite/add'
  request({
    url,
    method: 'POST',
    data: { articleId: Number(articleId) },
    success(res) {
      if (res.data && res.data.code === 200) {
        favorited.value = !favorited.value
        uni.showToast({ title: favorited.value ? '已收藏' : '已取消收藏', icon: 'success' })
      } else {
        uni.showToast({ title: res.data?.msg || '操作失败', icon: 'none' })
      }
    },
    fail() {
      uni.showToast({ title: '网络错误', icon: 'none' })
    },
  })
}

function toggleLike() {
  if (!articleId) return
  const user = uni.getStorageSync('user')
  const body = user && user.id ? { userId: user.id } : {}
  const url = liked.value
    ? `/portal/article/${articleId}/unlike`
    : `/portal/article/${articleId}/like`
  const options = {
    url,
    method: 'POST',
    data: body,
    success(res) {
      const data = res.data?.data ?? res.data
      if (data && data.likeCount != null) {
        likeCount.value = data.likeCount
        liked.value = !liked.value
      }
    },
  }
  request(options)
}

function doTip() {
  if (!articleId) return
  const num = parseInt(tipAmount.value, 10)
  if (!num || num < 1) {
    uni.showToast({ title: '请输入有效金额', icon: 'none' })
    return
  }
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  request({
    url: `/portal/coin/article/${articleId}/tip`,
    method: 'POST',
    header: { Authorization: token },
    data: { amount: num },
    success(res) {
      if (res.data && res.data.code === 200) {
        uni.showToast({ title: '打赏成功', icon: 'success' })
        showTip.value = false
        tipAmount.value = ''
      } else {
        uni.showToast({ title: res.data?.msg || '打赏失败', icon: 'none' })
      }
    },
    fail() {
      uni.showToast({ title: '网络错误', icon: 'none' })
    },
  })
}

function loadComments() {
  if (!articleId) return
  request({
    url: '/portal/comment/list',
    method: 'GET',
    data: { articleId },
    success(res) {
      const data = res.data?.data || res.data
      comments.value = data || []
    },
  })
}

function submitComment() {
  if (!articleId) return
  if (!commentInput.value.trim()) {
    uni.showToast({ title: '请输入评论内容', icon: 'none' })
    return
  }
  const data: any = { articleId, content: commentInput.value }
  const options: any = {
    url: '/portal/comment/submit',
    method: 'POST',
    data,
    success(res) {
      const obj = res.data?.data || res.data
      if (obj) {
        comments.value.push(obj)
        commentInput.value = ''
      } else {
        uni.showToast({ title: res.data?.msg || '评论失败', icon: 'none' })
      }
    },
    fail() {
      uni.showToast({ title: '网络错误', icon: 'none' })
    },
  }
  request(options)
}

onMounted(() => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1] as any
  const id = page.options?.id
  if (id) {
    articleId = id
    setTimeout(() => {
      loadDetail(id)
      loadComments()
    }, 0)
  }
})
</script>

<style scoped>
.page { padding: 24rpx; padding-bottom: 120rpx; }
.article { background: #fff; padding: 24rpx; border-radius: 12rpx; margin-bottom: 24rpx; }
.title { font-size: 40rpx; font-weight: bold; display: block; }
.meta { color: #999; font-size: 26rpx; display: block; margin-top: 16rpx; }
.action-bar { margin-top: 20rpx; display: flex; gap: 20rpx; flex-wrap: wrap; }
.action-btn { flex: 1; min-width: 160rpx; height: 64rpx; line-height: 64rpx; font-size: 26rpx; border-radius: 12rpx; }
.action-btn.like { background: #f5f5f5; color: #666; }
.action-btn.like.active { background: #fff1f0; color: #ff4d4f; }
.action-btn.favorite { background: #f0f5ff; color: #1890ff; }
.action-btn.favorite.active { background: #e6f7ff; color: #1890ff; }
.action-btn.tip { background: #fff7e6; color: #fa8c16; }
.tip-mask { position: fixed; left: 0; right: 0; top: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 100; display: flex; align-items: center; justify-content: center; }
.tip-popup { width: 560rpx; background: #fff; border-radius: 20rpx; padding: 32rpx 28rpx; }
.tip-title { font-size: 32rpx; font-weight: 600; margin-bottom: 24rpx; display: block; }
.tip-input { width: 100%; height: 80rpx; background: #f5f5f5; border-radius: 12rpx; padding: 0 20rpx; margin-bottom: 24rpx; box-sizing: border-box; }
.tip-btns { display: flex; gap: 20rpx; }
.tip-cancel { flex: 1; height: 72rpx; line-height: 72rpx; background: #f5f5f5; color: #666; border-radius: 12rpx; font-size: 28rpx; }
.tip-ok { flex: 1; height: 72rpx; line-height: 72rpx; background: #fa8c16; color: #fff; border-radius: 12rpx; font-size: 28rpx; }
.content { margin-top: 24rpx; line-height: 1.8; }
.comments { background: #fff; border-radius: 12rpx; padding: 24rpx 20rpx; }
.c-title { font-size: 32rpx; font-weight: 600; margin-bottom: 16rpx; display: block; }
.c-item { margin-bottom: 18rpx; }
.c-user { font-size: 24rpx; color: #999; display: block; }
.c-content { font-size: 28rpx; display: block; margin-top: 6rpx; }
.c-time { font-size: 22rpx; color: #bbb; margin-top: 4rpx; display: block; }
.c-empty { text-align: center; font-size: 24rpx; color: #aaa; padding: 40rpx 0; }
.c-input-wrap {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 16rpx 24rpx;
  background: #fff;
  display: flex;
  gap: 12rpx;
  box-shadow: 0 -4rpx 12rpx rgba(0, 0, 0, 0.04);
}
.c-input {
  flex: 1;
  height: 72rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  padding: 0 20rpx;
}
.c-btn {
  width: 140rpx;
  height: 72rpx;
  border-radius: 12rpx;
  background: #1890ff;
  color: #fff;
  font-size: 26rpx;
}
</style>
