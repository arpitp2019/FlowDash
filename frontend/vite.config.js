import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

// https://vite.dev/config/
const __dirname = dirname(fileURLToPath(import.meta.url))
// eslint-disable-next-line no-undef
const env = typeof process !== 'undefined' ? process.env : {}
const isPagesBuild = env.GITHUB_PAGES === 'true'
const repoName = env.GITHUB_REPOSITORY ? env.GITHUB_REPOSITORY.split('/')[1] : ''

export default defineConfig({
  plugins: [react()],
  base: isPagesBuild && repoName ? `/${repoName}/` : '/',
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
      '/login': 'http://localhost:8080',
      '/oauth2': 'http://localhost:8080'
    }
  },
  build: {
    outDir: isPagesBuild ? resolve(__dirname, 'dist') : resolve(__dirname, '../backend/src/main/resources/static'),
    emptyOutDir: true
  }
})
