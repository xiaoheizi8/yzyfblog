<template>
  <div>
    <h2>用户管理</h2>
    <a-card style="margin-top: 16px">
      <a-form layout="inline" class="search-form">
        <a-form-item label="用户名">
          <a-input v-model:value="query.username" placeholder="用户名/昵称" allow-clear style="width: 160px" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" placeholder="全部" allow-clear style="width: 100px">
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="load">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button type="primary" @click="openEdit()">新增用户</a-button>
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
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '正常' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a @click="openEdit(record)">编辑</a>
              <a @click="openRoles(record)">角色</a>
              <a-popconfirm
                :title="record.status === 1 ? '确定禁用该用户？' : '确定启用该用户？'"
                @confirm="toggleStatus(record)"
              >
                <a>{{ record.status === 1 ? '禁用' : '启用' }}</a>
              </a-popconfirm>
              <a-popconfirm title="确定删除该用户？" @confirm="onDelete(record.id)">
                <a class="danger">删除</a>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="modalVisible"
      :title="editId ? '编辑用户' : '新增用户'"
      @ok="submitEdit"
      @cancel="closeEdit"
    >
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-form-item label="用户名" name="username" required>
          <a-input v-model:value="form.username" :disabled="!!editId" placeholder="登录用户名" />
        </a-form-item>
        <a-form-item v-if="!editId" label="密码" name="password" required>
          <a-input-password v-model:value="form.password" placeholder="密码" />
        </a-form-item>
        <a-form-item v-else label="新密码" name="password">
          <a-input-password v-model:value="form.password" placeholder="不填则不修改" />
        </a-form-item>
        <a-form-item label="昵称" name="nickname">
          <a-input v-model:value="form.nickname" placeholder="昵称" />
        </a-form-item>
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="form.email" placeholder="邮箱" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="form.status" placeholder="状态" style="width: 100%">
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="rolesModalVisible" title="分配角色" @ok="submitRoles" @cancel="rolesModalVisible = false">
      <a-form layout="vertical">
        <a-form-item label="用户">
          <span>{{ currentUser?.nickname || currentUser?.username }}</span>
        </a-form-item>
        <a-form-item label="角色">
          <a-checkbox-group v-model:value="selectedRoleIds" style="width: 100%">
            <a-row>
              <a-col v-for="r in roleOptions" :key="r.id" :span="12">
                <a-checkbox :value="r.id">{{ r.roleName }}</a-checkbox>
              </a-col>
            </a-row>
          </a-checkbox-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { userApi, type UserRecord } from '@/api/user'

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '用户名', dataIndex: 'username', width: 120 },
  { title: '昵称', dataIndex: 'nickname', width: 120 },
  { title: '邮箱', dataIndex: 'email', ellipsis: true },
  { title: '状态', key: 'status', width: 80 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 260 },
]

const list = ref<UserRecord[]>([])
const loading = ref(false)
const query = reactive({ username: '', status: undefined as number | undefined })
const pagination = reactive({ current: 1, pageSize: 10, total: 0 })

function onTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  load()
}

async function load() {
  loading.value = true
  try {
    const res: any = await userApi.page({
      current: pagination.current,
      size: pagination.pageSize,
      username: query.username || undefined,
      status: query.status,
    })
    const data = res?.data ?? res
    list.value = data?.records ?? []
    pagination.total = data?.total ?? 0
  } finally {
    loading.value = false
  }
}

function reset() {
  query.username = ''
  query.status = undefined
  pagination.current = 1
  load()
}

const modalVisible = ref(false)
const editId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive<UserRecord>({
  username: '',
  password: '',
  nickname: '',
  email: '',
  status: 1,
})
const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: false }],
}

function openEdit(record?: UserRecord) {
  editId.value = record?.id ?? null
  form.username = record?.username ?? ''
  form.password = ''
  form.nickname = record?.nickname ?? ''
  form.email = record?.email ?? ''
  form.status = record?.status ?? 1
  modalVisible.value = true
}

function closeEdit() {
  modalVisible.value = false
  editId.value = null
}

async function submitEdit() {
  try {
    await formRef.value?.validate().catch(() => {})
    if (!form.username?.trim()) {
      message.error('请输入用户名')
      return
    }
    if (!editId.value && !form.password?.trim()) {
      message.error('请输入密码')
      return
    }
    if (editId.value) {
      await userApi.update({ ...form, id: editId.value })
      message.success('修改成功')
    } else {
      await userApi.save(form)
      message.success('新增成功')
    }
    closeEdit()
    load()
  } catch (e: any) {
    if (e?.errorFields) return
    message.error(e?.message || '操作失败')
  }
}

const rolesModalVisible = ref(false)
const currentUser = ref<UserRecord | null>(null)
const roleOptions = ref<{ id: number; roleCode: string; roleName: string }[]>([])
const selectedRoleIds = ref<number[]>([])

async function openRoles(record: UserRecord) {
  currentUser.value = record
  const [rolesRes, idsRes]: any[] = await Promise.all([
    userApi.roleOptions(),
    userApi.getRoleIds(record.id!),
  ])
  roleOptions.value = (rolesRes?.data ?? rolesRes) ?? []
  selectedRoleIds.value = (idsRes?.data ?? idsRes) ?? []
  rolesModalVisible.value = true
}

async function submitRoles() {
  if (!currentUser.value?.id) return
  await userApi.assignRoles(currentUser.value.id, selectedRoleIds.value)
  message.success('角色已更新')
  rolesModalVisible.value = false
}

async function onDelete(id: number) {
  await userApi.delete(id)
  message.success('已删除')
  load()
}

async function toggleStatus(record: UserRecord) {
  if (!record.id) return
  if (record.status === 1) {
    await userApi.disable(record.id)
    message.success('已禁用')
  } else {
    await userApi.enable(record.id)
    message.success('已启用')
  }
  load()
}

onMounted(() => load())
</script>

<style scoped>
.search-form { margin-bottom: 16px; }
.danger { color: #ff4d4f; }
</style>
