<template>
  <div>
    <a-card title="微信配置" style="margin-top: 16px">
      <a-spin :spinning="loading">
        <a-form layout="vertical" style="max-width: 500px">
          <a-form-item label="ID">
            <a-input v-model:value="config.id" disabled />
          </a-form-item>
          <a-form-item label="小程序控制人">
            <a-input v-model:value="config.winterfly" placeholder="请输入小程序控制人" />
          </a-form-item>
          <a-form-item label="密码">
            <a-input v-model:value="config.pwd" type="password" placeholder="请输入密码" />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="save" :loading="saving">保存配置</a-button>
            <a-button style="margin-left: 8px" @click="loadConfig">刷新</a-button>
          </a-form-item>
        </a-form>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { config as wxConfigApi, queryForConfig } from '@/api/wxConfig'

// 配置数据类型
interface WxConfig {
  id: number;
  winterfly: string;
  pwd: string;
}

// 配置数据
const config = reactive<WxConfig>({
  id: 0,
  winterfly: '',
  pwd: ''
})

const loading = ref(false)
const saving = ref(false)

// 加载配置
async function loadConfig() {
  loading.value = true
  try {
    const res = await queryForConfig()
    if (res.code === 200) {
      // 将返回的data中的配置数据赋值给config
      Object.assign(config, res.data)
      message.success('加载配置成功')
    } else {
      message.error(res.message || '加载配置失败')
    }
  } catch (e: any) {
    message.error(e?.message || '加载配置失败')
  } finally {
    loading.value = false
  }
}

// 保存配置
async function save() {
  if (!config.winterfly) {
    message.error('请输入小程序控制人')
    return
  }

  saving.value = true
  try {
    await wxConfigApi(config)
    message.success('保存成功')
  } catch (e: any) {
    message.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
</style>
