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
          <template v-if="column.key === 'action'">
            <a-popconfirm title="确定删除该评论？" @confirm="onDelete(record.id)">
              <a class="danger">删除</a>
            </a-popconfirm>
          </template>
        </template>
      </a-table>
    </a-card>
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

