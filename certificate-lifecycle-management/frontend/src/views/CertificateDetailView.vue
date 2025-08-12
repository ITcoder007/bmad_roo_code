<template>
  <div class="certificate-detail-view">
    <h2>证书详情</h2>
    <p>证书ID: {{ $route.params.id }}</p>
  </div>
</template>

<script>
export default {
  name: 'CertificateDetailView'
}
</script>

<style scoped>
.certificate-detail-view {
  padding: 20px;
}
</style>