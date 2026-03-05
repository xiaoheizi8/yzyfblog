<template>
  <div>
    <h2>文章管理</h2>
    <a-card style="margin-top: 16px">
      <a-form layout="inline" class="search-form">
        <a-form-item label="标题">
          <a-input
            v-model:value="query.title"
            placeholder="文章标题"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="作者">
          <a-input
            v-model:value="query.author"
            placeholder="作者用户名/昵称"
            allow-clear
            style="width: 180px"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="query.status"
            placeholder="全部"
            allow-clear
            style="width: 120px"
          >
            <a-select-option :value="1">已发布</a-select-option>
            <a-select-option :value="0">草稿</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="load">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button type="primary" @click="openCreate">新建文章</a-button>
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
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'orange'">
              {{ record.status === 1 ? '已发布' : '草稿' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a @click="openEdit(record)">编辑</a>
              <a
                v-if="record.status === 1"
                @click="markIllegal(record)"
                class="danger"
              >
                标记违规
              </a>
              <a v-if="record.status === 0" @click="publish(record)">发布</a>
              <a v-if="record.status === 1" @click="unpublish(record)">下架</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { articleApi, type ArticleRecord } from '@/api/article'

const router = useRouter()

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '标题', dataIndex: 'title' },
  { title: '作者', dataIndex: 'authorName', width: 140 },
  { title: '浏览量', dataIndex: 'viewCount', width: 100 },
  { title: '点赞数', dataIndex: 'likeCount', width: 100 },
  { title: '评论数', dataIndex: 'commentCount', width: 100 },
  { title: '状态', key: 'status', width: 100 },
  { title: '发布时间', dataIndex: 'publishTime', width: 180 },
   { title: '操作', key: 'action', width: 220 },
]

const list = ref<ArticleRecord[]>([])
const loading = ref(false)
const query = reactive<{ title: string; author: string; status?: number }>({
  title: '',
  author: '',
  status: undefined,
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
    const res: any = await articleApi.page({
      current: pagination.current,
      size: pagination.pageSize,
      title: query.title || undefined,
      author: query.author || undefined,
      status: query.status,
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

async function publish(record: ArticleRecord) {
  if (!record.id) return
  await articleApi.updateStatus(record.id, 1)
  message.success('已发布')
  load()
}

async function unpublish(record: ArticleRecord) {
  if (!record.id) return
  await articleApi.updateStatus(record.id, 0)
  message.success('已下架')
  load()
}

async function markIllegal(record: ArticleRecord) {
  if (!record.id) return
  await articleApi.updateStatus(record.id, 2)
  message.success('已标记为违规')
  load()
}

function openCreate() {
  router.push({ name: 'ArticleEdit' })
}

function openEdit(record: ArticleRecord) {
  if (!record.id) return
  router.push({ name: 'ArticleEdit', params: { id: record.id } })
}

function reset() {
  query.title = ''
  query.author = ''
  query.status = undefined
  pagination.current = 1
  load()
}

onMounted(() => {
  load()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
}
.danger {
  color: #ff4d4f;
}
</style>
