<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { provide } from 'vue'

// 为整个应用提供全局配置
provide('appConfig', {
  name: import.meta.env.VITE_APP_NAME,
  version: import.meta.env.VITE_APP_VERSION,
  title: import.meta.env.VITE_APP_TITLE
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: #f5f5f5;
}

#app {
  min-height: 100vh;
}
</style>