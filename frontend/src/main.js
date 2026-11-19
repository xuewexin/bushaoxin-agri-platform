import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)

// 注册 Element Plus 所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册水产专属鱼类现代矢量图标 (彻底解决水产缺少ico图标问题)
app.component('Fish', {
  name: 'Fish',
  template: `<svg viewBox="0 0 1024 1024" width="1em" height="1em" fill="currentColor"><path d="M893.3 540.8c-24.1-13.7-44.5-30.8-62.1-50.5-22.1-24.8-48.4-44.7-77.8-59.1-39.7-19.5-83.3-29.6-127.8-29.6-67.4 0-131.6 23.3-182.9 66.4l-118-99.3c-14.8-12.5-36.9-10.7-49.4 4.1l-10.4 12.3c-12.5 14.8-10.7 36.9 4.1 49.4l98.6 83c-23.7 29.5-39.8 63.8-46.7 100.8H184c-19.3 0-35 15.7-35 35v8c0 19.3 15.7 35 35 35h137c6.9 37 23 71.3 46.7 100.8l-98.6 83c-14.8 12.5-16.6 34.6-4.1 49.4l10.4 12.3c12.5 14.8 34.6 16.6 49.4 4.1l118-99.3c51.3 43.1 115.5 66.4 182.9 66.4 44.5 0 88.1-10.1 127.8-29.6 29.4-14.4 55.7-34.3 77.8-59.1 17.6-19.7 38-36.8 62.1-50.5 24.3-13.8 39.5-39.4 39.5-67.5s-15.2-53.7-39.5-67.5zM656 464c22.1 0 40 17.9 40 40s-17.9 40-40 40-40-17.9-40-40 17.9-40 40-40z"/></svg>`
})

app.use(ElementPlus)
app.mount('#app')
