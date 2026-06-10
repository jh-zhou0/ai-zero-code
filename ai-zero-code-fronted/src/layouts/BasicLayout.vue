<template>
  <a-layout class="basic-layout">
    <GlobalHeader />
    <a-layout-content class="basic-content">
      <router-view />
    </a-layout-content>
    <GlobalFooter />
  </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.basic-content {
  flex: 1;
  padding: 24px;
  background: linear-gradient(135deg, #e0f7fa 0%, #e8f5e9 50%, #f1f8e9 100%);
  min-height: 0;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .basic-content {
    padding: 12px;
  }
}
</style>
