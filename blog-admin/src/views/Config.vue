<template>
  <div>
    <h2>博客配置</h2>
    <a-card title="站点与外观" style="margin-top: 16px">
      <a-spin :spinning="loading">
        <a-form layout="vertical" style="max-width: 500px">
          <a-form-item label="站点名称">
            <a-input v-model:value="config.site_name" placeholder="站点名称" />
          </a-form-item>
          <a-form-item label="站点描述">
            <a-input v-model:value="config.site_description" placeholder="站点描述" />
          </a-form-item>
          <a-form-item label="背景图URL">
            <a-input v-model:value="config.background_image" placeholder="背景图地址" />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="save" :loading="saving">保存配置</a-button>
          </a-form-item>
        </a-form>
      </a-spin>
    </a-card>
    <a-card title="上传背景图" style="margin-top: 16px">
      <a-upload :before-upload="beforeUpload" :custom-request="uploadBg" :show-upload-list="false">
        <a-button :loading="uploading">选择背景图</a-button>
      </a-upload>
      <p v-if="config.background_image" class="tip">当前背景图：<a :href="config.background_image" target="_blank">{{ config.background_image }}</a></p>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { configApi } from '@/api/config'

const config = reactive<Record<string, string>>({
  site_name: '',
  site_description: '',
  background_image: '',
})
const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)

function beforeUpload() {
  return false
}

function uploadBg({ file, onSuccess, onError }: { file: any; onSuccess?: (res: any) => void; onError?: (err: any) => void }) {
  if (!file || !file.originFileObj) return
  uploading.value = true
  configApi.uploadBackground(file.originFileObj as File).then((res: any) => {
    const url = res?.data ?? res
    if (url) {
      config.background_image = url
      message.success('上传成功，请点击保存配置')
      onSuccess?.(res)
    } else {
      onError?.(new Error('未返回地址'))
    }
  }).catch((err) => {
    message.error('上传失败')
    onError?.(err)
  }).finally(() => { uploading.value = false })
}

async function load() {
  loading.value = true
  try {
    const res: any = await configApi.list()
    const list = res?.data ?? res ?? []
    list.forEach((item: { configKey: string; configValue?: string }) => {
      config[item.configKey] = item.configValue ?? ''
    })
  } finally {
    loading.value = false
  }
}

async function save() {
  saving.value = true
  try {
    await configApi.batchUpdate(config)
    message.success('保存成功')
  } catch (e: any) {
    message.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => load())
</script>

<style scoped>
.tip { margin-top: 8px; font-size: 12px; color: #888; }
</style>
