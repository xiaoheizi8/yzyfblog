<template>
  <view class="page">
    <view class="card">
      <text class="title">发布文章</text>
      <view class="form-item">
        <text class="label">标题</text>
        <input v-model="form.title" placeholder="请输入文章标题" class="input" />
      </view>
      <view class="form-item">
        <text class="label">摘要</text>
        <textarea v-model="form.summary" placeholder="一句话简介" class="textarea" />
      </view>
      <view class="form-item">
        <text class="label">标签</text>
        <view class="tag-select" @click="toggleTagPanel">
          <text class="tag-select-placeholder" v-if="!selectedTagNames.length">请选择标签</text>
          <text class="tag-select-text" v-else>{{ selectedTagNames.join('、') }}</text>
          <text class="tag-select-arrow">▼</text>
        </view>
        <view v-if="showTagPanel" class="tag-dropdown">
          <scroll-view scroll-y class="tag-scroll">
            <view
              v-for="tag in tags"
              :key="tag.id"
              class="tag-option"
              :class="{ active: selectedTagIds.includes(tag.id) }"
              @click.stop="toggleTag(tag.id)"
            >
              <text class="tag-option-name">{{ tag.name }}</text>
              <text v-if="selectedTagIds.includes(tag.id)" class="tag-option-check">✓</text>
            </view>
          </scroll-view>
        </view>
        <view class="tag-input-wrap">
          <input
            v-model="newTagName"
            placeholder="输入新标签，回车添加"
            class="input"
            @confirm="addNewTag"
          />
          <view v-if="newTags.length" class="new-tags">
            <view v-for="t in newTags" :key="t" class="tag-item active">
              {{ t }}
            </view>
          </view>
        </view>
      </view>
      <view class="form-item">
        <text class="label">内容（Markdown）</text>
        <textarea v-model="form.content" placeholder="支持 Markdown，可后续接编辑器" class="textarea body" />
      </view>
      <view class="form-item">
        <text class="label">图片（最多 9 张）</text>
        <view class="images-wrap">
          <view v-for="(img, index) in images" :key="img" class="img-item">
            <image :src="img" mode="aspectFill" class="img-thumb" />
            <view class="img-remove" @click="removeImage(index)">×</view>
          </view>
          <view v-if="images.length < 9" class="img-add" @click="chooseImages">
            <text class="img-add-plus">＋</text>
          </view>
        </view>
      </view>
      <button class="btn" @click="submit">发布</button>
    </view>
  </view>
</template>

<script setup lang="ts">
// @ts-nocheck
import { reactive, ref, onMounted, computed } from 'vue'
import request, { BASE_URL } from '@/utils/request'

const form = reactive({
  title: '',
  summary: '',
  content: '',
})
const images = ref<string[]>([])
const tags = ref<any[]>([])
const selectedTagIds = ref<number[]>([])
const newTagName = ref('')
const newTags = ref<string[]>([])
const showTagPanel = ref(false)
const selectedTagNames = computed(() => {
  if (!tags.value?.length || !selectedTagIds.value?.length) return []
  const map = new Map(tags.value.map((t: any) => [t.id, t.name]))
  return selectedTagIds.value.map((id) => map.get(id)).filter(Boolean)
})

onMounted(() => {
  loadTags()
})

function loadTags() {
  request({
    url: '/portal/tag/list',
    method: 'GET',
    success(res) {
      const data = res.data?.data || res.data
      const list = data || []
      // 根据 ID 去重，避免标签重复显示
      const map = new Map()
      list.forEach((t: any) => {
        if (t && t.id != null && !map.has(t.id)) {
          map.set(t.id, t)
        }
      })
      tags.value = Array.from(map.values())
    },
  })
}

function chooseImages() {
  const remain = 9 - images.value.length
  if (remain <= 0) {
    uni.showToast({ title: '最多选择 9 张图片', icon: 'none' })
    return
  }
  uni.chooseImage({
    count: remain,
    success(res) {
      const tempFiles = res.tempFilePaths || []
      uploadImages(tempFiles)
    },
  })
}

function uploadImages(paths: string[]) {
  if (!paths || !paths.length) return
  const uploadUrl = `${BASE_URL.replace(/\/$/, '')}/portal/upload/image`
  paths.forEach((p) => {
    uni.uploadFile({
      url: uploadUrl,
      filePath: p,
      name: 'file',
      success(res) {
        try {
          // 兼容：微信等端可能返回已解析对象，或字符串需 JSON.parse
          const raw = res.data
          const data = typeof raw === 'string' ? JSON.parse(raw) : raw
          let url = data?.data ?? data?.url ?? data
          if (url != null && typeof url !== 'string') url = String(url)
          if (typeof url === 'string' && url.trim()) {
            // 兼容后端返回的相对路径（/uploads/xxx），前端补全为完整 URL
            if (!url.startsWith('http')) {
              url = `${BASE_URL.replace(/\/$/, '')}${url.startsWith('/') ? '' : '/'}${url}`
            }
            images.value.push(url)
          }
        } catch (e) {
          console.error('upload parse error', e)
          uni.showToast({ title: '解析上传结果失败', icon: 'none' })
        }
      },
      fail() {
        uni.showToast({ title: '上传失败', icon: 'none' })
      },
    })
  })
}

function removeImage(index: number) {
  images.value.splice(index, 1)
}

function toggleTag(id: number) {
  const idx = selectedTagIds.value.indexOf(id)
  if (idx >= 0) {
    selectedTagIds.value.splice(idx, 1)
  } else {
    selectedTagIds.value.push(id)
  }
}

function toggleTagPanel() {
  showTagPanel.value = !showTagPanel.value
}

function addNewTag() {
  const name = newTagName.value.trim()
  if (!name) return
  // 如果与已有标签同名，则直接勾选该标签，不再作为“新增标签”重复出现
  const exist = tags.value.find((t: any) => t && t.name === name)
  if (exist && exist.id != null) {
    if (!selectedTagIds.value.includes(exist.id)) {
      selectedTagIds.value.push(exist.id)
    }
  } else {
    // 在新增标签列表中去重
    if (!newTags.value.includes(name)) {
      newTags.value.push(name)
    }
  }
  newTagName.value = ''
}

function submit() {
  if (!form.title.trim() || !form.content.trim()) {
    uni.showToast({ title: '标题和内容不能为空', icon: 'none' })
    return
  }
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  // 将图片以 Markdown 形式附加到内容末尾，只使用有效的字符串 URL，避免 ![]([object Object])
  let content = form.content
  const validUrls = images.value
    .map((url) => (typeof url === 'string' ? url : (url && (url.url || url.path)) ? String(url.url || url.path) : ''))
    .filter((u) => u && u.trim() && !u.includes('[object Object]'))
  if (validUrls.length) {
    const md = validUrls.map((url) => `![](${url})`).join('\n')
    content = `${content}\n\n${md}`
  }
  request({
    url: '/portal/article',
    method: 'POST',
    data: {
      title: form.title,
      summary: form.summary,
      content,
      tagIds: selectedTagIds.value,
      newTags: newTags.value,
    },
    success(res) {
      if (res.data && res.data.code === 200) {
        uni.showToast({ title: '发布成功', icon: 'success' })
        setTimeout(() => {
          uni.switchTab({ url: '/pages/article/list' })
        }, 500)
      } else {
        uni.showToast({ title: res.data?.message || res.data?.msg || '发布失败', icon: 'none' })
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
  padding: 24rpx 20rpx;
  background: #f7f8fa;
}
.card {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 24rpx 20rpx;
  box-shadow: 0 8rpx 24rpx rgba(125, 137, 149, 0.12);
}
.title {
  font-size: 36rpx;
  font-weight: 600;
  margin-bottom: 24rpx;
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
.textarea {
  min-height: 120rpx;
  border-radius: 12rpx;
  background: #f5f5f5;
  padding: 16rpx 20rpx;
}
.textarea.body {
  min-height: 220rpx;
}
.btn {
  margin-top: 8rpx;
  height: 76rpx;
  border-radius: 12rpx;
  background: linear-gradient(135deg, #1890ff, #40a9ff);
  color: #fff;
  font-size: 30rpx;
}
.images-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 12rpx;
}
.img-item {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  overflow: hidden;
}
.img-thumb {
  width: 100%;
  height: 100%;
}
.img-remove {
  position: absolute;
  top: 4rpx;
  right: 4rpx;
  width: 36rpx;
  height: 36rpx;
  border-radius: 18rpx;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  text-align: center;
  line-height: 36rpx;
  font-size: 24rpx;
}
.img-add {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  border: 2rpx dashed #d9d9d9;
  align-items: center;
  justify-content: center;
  display: flex;
}
.img-add-plus {
  font-size: 48rpx;
  color: #999;
}
.tag-select {
  height: 72rpx;
  border-radius: 12rpx;
  background: #f5f5f5;
  padding: 0 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.tag-select-placeholder {
  color: #999;
  font-size: 26rpx;
}
.tag-select-text {
  font-size: 26rpx;
  color: #333;
  flex: 1;
  margin-right: 12rpx;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.tag-select-arrow {
  font-size: 22rpx;
  color: #999;
}
.tag-dropdown {
  margin-top: 8rpx;
  border-radius: 12rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.08);
  max-height: 320rpx;
  overflow: hidden;
}
.tag-scroll {
  max-height: 320rpx;
}
.tag-option {
  padding: 18rpx 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.tag-option:last-child {
  border-bottom-width: 0;
}
.tag-option-name {
  font-size: 26rpx;
  color: #333;
}
.tag-option-check {
  font-size: 26rpx;
  color: #1890ff;
}
.tag-option.active .tag-option-name {
  color: #1890ff;
}
.tag-input-wrap {
  margin-top: 12rpx;
}
.new-tags {
  margin-top: 8rpx;
  flex-direction: row;
  flex-wrap: wrap;
  display: flex;
  gap: 12rpx;
}
.tag-item {
  padding: 6rpx 16rpx;
  border-radius: 24rpx;
  background: #f5f5f5;
  font-size: 24rpx;
  color: #666;
}
.tag-item.active {
  background: #e6f7ff;
  color: #1890ff;
}
</style>

