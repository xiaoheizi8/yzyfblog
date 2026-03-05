<template>
  <div>
    <h2>标签管理</h2>
    <a-card style="margin-top: 16px">
      <div style="margin-bottom: 16px; display: flex; gap: 8px">
        <a-input
          v-model:value="newName"
          placeholder="输入标签名称"
          style="width: 240px"
          @pressEnter="createTag"
        />
        <a-button type="primary" @click="createTag">新增标签</a-button>
      </div>
      <a-table :columns="columns" :data-source="list" :loading="loading" row-key="id" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-popconfirm title="确定删除该标签？" @confirm="remove(record.id)">
              <a class="danger">删除</a>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { tagApi, type TagRecord } from '@/api/tag'

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '名称', dataIndex: 'name' },
  { title: '操作', key: 'action', width: 120 },
]

const list = ref<TagRecord[]>([])
const loading = ref(false)
const newName = ref('')

async function load() {
  loading.value = true
  try {
    const res: any = await tagApi.list()
    const data: TagRecord[] = res?.data ?? res ?? []
    // 按 ID 去重，避免出现重复记录
    const map = new Map<number, TagRecord>()
    data.forEach((t) => {
      if (!map.has(t.id)) {
        map.set(t.id, t)
      }
    })
    list.value = Array.from(map.values())
  } finally {
    loading.value = false
  }
}

async function createTag() {
  const name = newName.value.trim()
  if (!name) {
    message.warning('请输入标签名称')
    return
  }
  try {
    await tagApi.create(name)
    message.success('创建成功')
    newName.value = ''
    load()
  } catch (e: any) {
    message.error(e?.message || '创建失败')
  }
}

async function remove(id: number) {
  await tagApi.delete(id)
  message.success('已删除')
  load()
}

onMounted(() => {
  load()
})
</script>

<style scoped>
.danger {
  color: #ff4d4f;
}
</style>

