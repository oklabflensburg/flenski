import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite';
import {PrimeVueResolver} from '@primevue/auto-import-resolver';
import vueDevTools from 'vite-plugin-vue-devtools'
import tailwindcss from '@tailwindcss/postcss'


// https://vite.dev/config/
export default defineConfig({
  base : '/',
  plugins: [
    vue(),
    vueDevTools(),
    Components({
      resolvers: [
        PrimeVueResolver()
      ]
    })
  ],
  css: {
    postcss: {
      plugins: [tailwindcss],
    },
  },
  server: {
    host: '0.0.0.0', // This is crucial for Docker
    port: 5173,
    watch: {
      usePolling: true // Helps with file watching in Docker
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api/, '/api'),
      },
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  define: {
    global: 'globalThis',
  },
})
