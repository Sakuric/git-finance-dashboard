import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  root: '.',
  publicDir: 'public',
  
  // 开发服务器配置
  server: {
    port: 5173,
    host: '0.0.0.0',
    open: true,
    cors: true,
    // 启用 SPA 路由回退
    historyApiFallback: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/api/, '/api')
      }
    }
  },
  
  // 构建配置
  build: {
    target: 'es2015',
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    },
    rollupOptions: {
      input: './index.html',
      output: {
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: '[ext]/[name]-[hash].[ext]',
        manualChunks: {
          'echarts': ['echarts'],
          'vue': ['vue', 'vue-router', 'pinia'],
          'utils': ['axios']
        }
      }
    },
    chunkSizeWarningLimit: 1000
  },
  
  // 路径解析配置
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      '@components': resolve(__dirname, 'src/components'),
      '@views': resolve(__dirname, 'src/views'),
      '@api': resolve(__dirname, 'src/api'),
      '@stores': resolve(__dirname, 'src/stores'),
      '@assets': resolve(__dirname, 'src/assets'),
      '@utils': resolve(__dirname, 'src/utils')
    },
    extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
  },
  
  // CSS配置
  css: {
    devSourcemap: true
  },
  
  // 优化配置
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'axios',
      'echarts'
    ],
    exclude: []
  },
  
  // 环境变量配置
  define: {
    __VUE_OPTIONS_API__: true,
    __VUE_PROD_DEVTOOLS__: false
  },
  
  // 预览配置
  preview: {
    port: 4173,
    host: '0.0.0.0'
  }
})