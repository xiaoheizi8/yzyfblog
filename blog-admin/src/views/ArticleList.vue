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
              <a @click="openDetail(record)">查看</a>
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

    <a-drawer
      v-model:open="detailVisible"
      :title="detail?.title || '文章详情'"
      width="720"
      :footer="null"
    >
      <a-descriptions v-if="detail" bordered :column="1" size="small">
        <a-descriptions-item label="ID">{{ detail.id }}</a-descriptions-item>
        <a-descriptions-item label="标题">{{ detail.title }}</a-descriptions-item>
        <a-descriptions-item label="摘要">{{ detail.summary || '—' }}</a-descriptions-item>
        <a-descriptions-item label="内容">
          <div class="article-content" v-html="detailContentHtml || '暂无内容'"></div>
        </a-descriptions-item>
      </a-descriptions>
      <div v-else>正在加载...</div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { articleApi, type ArticleRecord } from '@/api/article'

const router = useRouter()

const columns = [
  { title: 'ID', dataIndex: 'id', width: 70 },
  {
    title: '封面',
    dataIndex: 'coverImage',
    width: 80,
    customRender: ({ record }: { record: ArticleRecord }) =>
      record.coverImage
        ? h('img', {
            src: record.coverImage,
            style: 'width:48px;height:32px;border-radius:4px;object-fit:cover;border:1px solid #e0dacf;',
          })
        : h(
            'div',
            {
              style:
                'width:48px;height:32px;border-radius:4px;border:1px dashed #d9d0c0;background:#f5f2ea;',
            },
            '',
          ),
  },
  {
    title: '标题 / 摘要',
    dataIndex: 'title',
    ellipsis: true,
    customRender: ({ record }: { record: ArticleRecord }) =>
      h('div', [
        h(
          'div',
          {
            style: 'font-weight:600;color:#262626;margin-bottom:2px;',
          },
          record.title,
        ),
        record.summary
          ? h(
              'div',
              {
                style:
                  'font-size:12px;color:#999;max-height:36px;overflow:hidden;text-overflow:ellipsis;',
              },
              record.summary,
            )
          : null,
      ]),
  },
  { title: '作者', dataIndex: 'authorName', width: 120 },
  { title: '浏览', dataIndex: 'viewCount', width: 80 },
  { title: '点赞', dataIndex: 'likeCount', width: 80 },
  { title: '评论', dataIndex: 'commentCount', width: 80 },
  { title: '状态', key: 'status', width: 90 },
  { title: '发布时间', dataIndex: 'publishTime', width: 180 },
  { title: '操作', key: 'action', width: 240 },
]

const list = ref<ArticleRecord[]>([])
const loading = ref(false)
const detailVisible = ref(false)
const detail = ref<ArticleRecord | null>(null)
const detailContentHtml = computed(() => {
  if (!detail.value?.content) return ''
  let html = detail.value.content as string
  // 将 Markdown 图片语法渲染为 img 标签
  const imgReg = /!\[[^\]]*]\((https?:\/\/[^\s)]+)\)/g
  html = html.replace(
    imgReg,
    '<img src="$1" style="max-width:100%;display:block;margin:8px 0;border-radius:4px;" />',
  )
  html = html.replace(/\n/g, '<br/>')
  return html
})
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

async function openDetail(record: ArticleRecord) {
  if (!record.id) return
  detailVisible.value = true
  detail.value = null
  try {
    const res: any = await articleApi.getById(record.id)
    detail.value = (res?.data ?? res) || null
  } catch (e: any) {
    message.error(e?.message || '加载文章详情失败')
  }
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
.article-content {
  max-height: 60vh;
  overflow: auto;
  font-size: 14px;
  line-height: 1.7;
}
.article-content img {
  max-width: 100%;
  border-radius: 4px;
  margin: 8px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
</style>
