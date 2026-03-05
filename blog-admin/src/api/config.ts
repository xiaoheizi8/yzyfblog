import request from '@/utils/request'

export interface BlogConfigItem {
  id?: number
  configKey: string
  configValue?: string
  remark?: string
}

export const configApi = {
  list() {
    return request.get<BlogConfigItem[]>('/admin/config/list')
  },
  batchUpdate(configs: Record<string, string>) {
    return request.put<unknown>('/admin/config/batch', configs)
  },
  uploadBackground(file: File) {
    const form = new FormData()
    form.append('file', file)
    return request.post<string>('/admin/config/uploadBackground', form)
  },
}
