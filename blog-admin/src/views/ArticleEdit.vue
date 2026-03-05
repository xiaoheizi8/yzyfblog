<template>
  <div>
    <h2>{{ isEdit ? '编辑文章' : '新建文章' }}</h2>
    <a-card style="margin-top: 16px">
      <a-form :model="form" layout="vertical">
        <a-form-item label="标题" required>
          <a-input v-model:value="form.title" placeholder="请输入标题" />
        </a-form-item>
        <a-form-item label="摘要">
          <a-input-text-area v-model:value="form.summary" rows="3" placeholder="文章摘要" />
        </a-form-item>
        <a-form-item label="内容（Markdown）" required>
          <a-input-text-area
            v-model:value="form.content"
            rows="10"
            placeholder="支持 Markdown，可后续接入编辑器"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" style="width: 160px">
            <a-select-option :value="1">已发布</a-select-option>
            <a-select-option :value="0">草稿</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="标签">
          <a-select
            v-model:value="selectedTagIds"
            mode="multiple"
            style="width: 100%"
            placeholder="选择已有标签"
            :options="tagOptions"
          />
          <div style="margin-top: 8px; display: flex; gap: 8px">
            <a-input
              v-model:value="newTagName"
              placeholder="输入新标签名称"
              style="width: 200px"
              @pressEnter="addNewTag"
            />
            <a-button @click="addNewTag">添加标签</a-button>
          </div>
          <div v-if="newTags.length" style="margin-top: 8px">
            <span style="margin-right: 8px">新标签：</span>
            <a-tag v-for="t in newTags" :key="t" color="blue">{{ t }}</a-tag>
          </div>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="submit" :loading="saving">保存</a-button>
            <a-button @click="goBack">返回</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { articleApi } from '@/api/article'
import { tagApi, type TagRecord } from '@/api/tag'

const route = useRoute()
const router = useRouter()

const form = reactive({
  title: '',
  summary: '',
  content: '',
  status: 1,
})

const isEdit = ref(false)
const saving = ref(false)
const tagOptions = ref<{ label: string; value: number }[]>([])
const selectedTagIds = ref<number[]>([])
const newTagName = ref('')
const newTags = ref<string[]>([])

onMounted(async () => {
  await loadTags()
  const id = route.params.id ? Number(route.params.id) : undefined
  if (id) {
    isEdit.value = true
    await loadDetail(id)
  }
})

async function loadTags() {
  const res: any = await tagApi.list()
  const list: TagRecord[] = res?.data ?? res ?? []
  // 根据 ID 去重，避免下拉中出现重复标签
  const map = new Map<number, TagRecord>()
  list.forEach((t) => {
    if (!map.has(t.id)) {
      map.set(t.id, t)
    }
  })
  const unique = Array.from(map.values())
  tagOptions.value = unique.map((t) => ({ label: t.name, value: t.id }))
}

async function loadDetail(id: number) {
  const [articleRes, tagIdsRes]: any[] = await Promise.all([articleApi.getById(id), articleApi.getTagIds(id)])
  const a = articleRes?.data ?? articleRes
  if (a) {
    form.title = a.title
    form.summary = a.summary || ''
    form.content = a.content || ''
    form.status = a.status ?? 1
  }
  const ids = tagIdsRes?.data ?? tagIdsRes ?? []
  selectedTagIds.value = ids
}

function addNewTag() {
  const name = newTagName.value.trim()
  if (!name) return
  if (!newTags.value.includes(name)) {
    newTags.value.push(name)
  }
  newTagName.value = ''
}

async function submit() {
  if (!form.title.trim() || !form.content.trim()) {
    message.error('标题和内容不能为空')
    return
  }
  saving.value = true
  try {
    const payload = {
      title: form.title,
      summary: form.summary,
      content: form.content,
      status: form.status,
      tagIds: selectedTagIds.value,
      newTags: newTags.value,
    }
    const id = route.params.id ? Number(route.params.id) : undefined
    if (id) {
      await articleApi.update(id, payload)
      message.success('保存成功')
    } else {
      await articleApi.create(payload)
      message.success('创建成功')
    }
    router.push({ name: 'Article' })
  } catch (e: any) {
    message.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.back()
}
</script>

<style scoped>
</style>

