<template>
  <div>
    <h2>评论管理</h2>
    <a-card style="margin-top: 16px">
      <a-form layout="inline" class="search-form">
        <a-form-item label="文章ID">
          <a-input-number v-model:value="query.articleId" :min="1" style="width: 160px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="load">查询</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
      <a-table
        :columns="columns"
        :data-source="list"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="onTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'content'">
            <a @click="openDetail(record)">查看</a>
          </template>
          <template v-if="column.key === 'action'">
            <a-popconfirm title="确定删除该评论？" @confirm="onDelete(record.id)">
              <a class="danger">删除</a>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="detailVisible"
      title="评论详情"
      :footer="null"
      width="600px"
    >
      <a-descriptions v-if="current" bordered :column="1" size="small">
        <a-descriptions-item label="ID">{{ current.id }}</a-descriptions-item>
        <a-descriptions-item label="文章ID">{{ current.articleId }}</a-descriptions-item>
        <a-descriptions-item label="用户ID">{{ current.userId || '游客' }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ current.createTime }}</a-descriptions-item>
        <a-descriptions-item label="内容">
          <div style="white-space: pre-wrap">
            {{ current.content }}
          </div>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { commentApi, type CommentRecord } from '@/api/comment'

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '文章ID', dataIndex: 'articleId', width: 100 },
  { title: '用户ID', dataIndex: 'userId', width: 100 },
  { title: '内容', dataIndex: 'content', ellipsis: true },
  { title: '点赞数', dataIndex: 'likeCount', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 120 },
]

const list = ref<CommentRecord[]>([])
const loading = ref(false)
const detailVisible = ref(false)
const current = ref<CommentRecord | null>(null)
const query = reactive<{ articleId?: number }>({
  articleId: undefined,
})
const pagination = reactive<{ current: number; pageSize: number; total: number }>({
  current: 1,
  pageSize: 10,
  total: 0,
})

function onTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  load()
}

async function load() {
  loading.value = true
  try {
    const res: any = await commentApi.page({
      current: pagination.current,
      size: pagination.pageSize,
      articleId: query.articleId,
    })
    const data = res?.data ?? res
    list.value = data?.records ?? []
    pagination.total = data?.total ?? 0
  } catch (e: any) {
    message.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function reset() {
  query.articleId = undefined
  pagination.current = 1
  load()
}

async function onDelete(id: number) {
  await commentApi.delete(id)
  message.success('已删除')
  load()
}

function openDetail(record: CommentRecord) {
  current.value = record
  detailVisible.value = true
}

onMounted(() => load())
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
}
.danger {
  color: #ff4d4f;
}
</style>

