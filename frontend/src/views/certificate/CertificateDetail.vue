<template>
  <div class="certificate-detail">
    <h1>证书详情</h1>
    <p>证书ID: {{ $route.params.id }}</p>
  </div>
</template>

<script>
export default {
  name: 'CertificateDetail'
}
</script>

<style scoped>
.certificate-detail {
  padding: 20px;
}
</style>