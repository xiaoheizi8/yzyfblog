import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: { '@': path.resolve(__dirname, 'src') },
  },
  server: {
    // 管理端开发端口：4000
    port: 4000,
    proxy: {
      '/api': { target: 'http://localhost:8081', changeOrigin: true },
    },
  },
})

