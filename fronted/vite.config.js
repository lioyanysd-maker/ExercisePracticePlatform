import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiTarget = env.VITE_API_TARGET || 'http://localhost:8001'
  const port = parseInt(env.VITE_PORT || '3000', 10)

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port,
      proxy: {
        '/api': {
          target: apiTarget,
          changeOrigin: true,
          timeout: 120000,
          proxyTimeout: 120000,
          secure: false,
          configure: (proxy) => {
            proxy.on('proxyReq', (proxyReq, req) => {
              if (req.headers?.authorization) {
                proxyReq.setHeader('Authorization', req.headers.authorization)
              }
            })
          }
        }
      }
    }
  }
})
