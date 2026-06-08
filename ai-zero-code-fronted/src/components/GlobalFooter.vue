<template>
  <a-layout-footer class="global-footer">
    <span>Copyright &copy; {{ year }} Kayson. All rights reserved.</span>
  </a-layout-footer>
</template>

<script setup lang="ts">
const year = new Date().getFullYear()
</script>

<style scoped>
.global-footer {
  text-align: center;
  background: linear-gradient(135deg, #e0f7fa 0%, #e8f5e9 50%, #f1f8e9 100%);
  font-size: 14px;
  padding: 24px 50px;
}
</style>
