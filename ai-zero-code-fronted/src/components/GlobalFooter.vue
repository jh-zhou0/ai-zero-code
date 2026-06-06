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
  background: #f0f2f5;
  color: rgba(0, 0, 0, 0.45);
  font-size: 14px;
  padding: 24px 50px;
}
</style>
