import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // すべてのネットワークインタフェースで待ち受け（同一 LAN のスマホ等からアクセス可能に）
    host: true,
    port: 5173,
    proxy: {
      // 開発時は /api を Spring Boot バックエンドへプロキシ（CORS 回避）
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
