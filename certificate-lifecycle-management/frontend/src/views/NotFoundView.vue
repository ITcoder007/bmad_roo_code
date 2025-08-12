<template>
  <div class="not-found-view">
    <h1>404</h1>
    <p>页面未找到</p>
    <router-link to="/">返回首页</router-link>
  </div>
</template>

<script>
export default {
  name: 'NotFoundView'
}
</script>

<style scoped>
.not-found-view {
  text-align: center;
  padding: 50px;
}
</style>